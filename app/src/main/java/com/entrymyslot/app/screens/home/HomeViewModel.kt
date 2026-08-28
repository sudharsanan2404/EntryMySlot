package com.entrymyslot.app.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.core.network.RetrofitClient
import com.entrymyslot.app.data.home.ApiResponse
import com.entrymyslot.app.data.home.EventDto
import com.entrymyslot.app.data.home.HomeApi
import com.entrymyslot.app.data.home.MovieDto
import com.entrymyslot.app.data.home.SportsVenueDto
import com.entrymyslot.app.data.model.HomeContent
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.Response
import java.io.IOException
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


data class HomeUiState(
    val events: List<HomeContent> = emptyList(),
    val movies: List<HomeContent> = emptyList(),
    val sports: List<HomeContent> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(private val api: HomeApi = RetrofitClient.homeApi) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        runCatching {
            coroutineScope {
                val events = async { api.featuredEvents().contentOrThrow().map { it.toHomeContent() } }
                val movies = async { api.featuredMovies().contentOrThrow().map { it.toHomeContent() } }
                val sports = async { api.nearbySports().contentOrThrow().map { it.toHomeContent() } }
                Triple(events.await(), movies.await(), sports.await())
            }
        }.onSuccess { (events, movies, sports) ->
            _uiState.value = HomeUiState(events, movies, sports, isLoading = false)
        }.onFailure { error ->
            _uiState.value = _uiState.value.copy(isLoading = false, error = error.message ?: "Could not load home content")
        }
    }
}

private fun EventDto.toHomeContent() = HomeContent(
    id = id.toString(),
    title = title,
    date = eventDate ?: startAt ?: "Date to be announced",
    location = listOfNotNull(venue, city).filter(String::isNotBlank).joinToString(", "),
    price = if (isFree) "Free" else price.asPrice(),
    imageUrl = thumbnailUrl ?: bannerUrl
)

private fun MovieDto.toHomeContent() = HomeContent(
    id = id.toString(),
    title = title,
    date = status?.replace('_', ' ') ?: "Coming soon",
    location = language ?: "In cinemas",
    price = "Book tickets",
    imageUrl = posterUrl ?: backdropUrl
)

private fun SportsVenueDto.toHomeContent() = HomeContent(
    id = id.toString(),
    title = venueName?.takeIf(String::isNotBlank) ?: name ?: "Sports venue",
    date = category ?: "Available today",
    location = listOfNotNull(address, city).filter(String::isNotBlank).joinToString(", "),
    price = basePrice.asPrice() + " / hour"
)

private fun JsonElement?.asPrice(): String {
    val amount = (this as? JsonPrimitive)?.content?.toDoubleOrNull() ?: return "Free"
    return if (amount <= 0) "Free" else "From ₹" + if (amount % 1.0 == 0.0) amount.toInt() else amount
}

private fun <T> Response<ApiResponse<T>>.contentOrThrow(): T {
    if (!isSuccessful) throw IOException("The server returned HTTP ${code()}")
    val payload = body() ?: throw IOException("The server returned an empty response")
    if (!payload.success) throw IOException(payload.message ?: "Unable to load home content")
    return payload.data ?: throw IOException("The server returned no data")
}

// ---------------------------------------------------------------------------
// 1. STATE MODEL
// ---------------------------------------------------------------------------

sealed class LocationFetchState {
    object Idle : LocationFetchState()
    object Loading : LocationFetchState()
    data class Success(val cityName: String) : LocationFetchState()
    object PermissionDenied : LocationFetchState()
    object PermissionPermanentlyDenied : LocationFetchState() // user checked "Don't ask again"
    object GpsDisabled : LocationFetchState()
    data class Error(val message: String) : LocationFetchState()
}

// ---------------------------------------------------------------------------
// 2. HELPER FUNCTIONS (permission check, GPS check, reverse geocode)
// ---------------------------------------------------------------------------

fun hasLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

fun isGpsEnabled(context: Context): Boolean {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

/**
 * Suspends until a fresh location fix is available (or throws).
 * Requires ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION to already be granted.
 */
@Suppress("MissingPermission")
suspend fun getCurrentLocation(context: Context): Location =
    suspendCancellableCoroutine { cont ->
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        fusedClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(location)
                } else {
                    // No cached location — request a fresh one
                    val cts = com.google.android.gms.tasks.CancellationTokenSource()
                    fusedClient.getCurrentLocation(
                        com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        cts.token
                    ).addOnSuccessListener { freshLocation ->
                        if (freshLocation != null) {
                            cont.resume(freshLocation)
                        } else {
                            cont.resumeWithException(Exception("Unable to get current location"))
                        }
                    }.addOnFailureListener { e -> cont.resumeWithException(e) }
                    cont.invokeOnCancellation { cts.cancel() }
                }
            }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }

/**
 * Reverse-geocodes a Location into a city name only.
 * Falls back through locality -> subAdminArea -> adminArea if locality is null.
 */
suspend fun getCityNameFromLocation(context: Context, location: Location): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    return suspendCancellableCoroutine { cont ->
        try {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            val address = addresses?.firstOrNull()
            val city = address?.locality
                ?: address?.subAdminArea
                ?: address?.adminArea
                ?: "Unknown"
            cont.resume(city)
        } catch (e: Exception) {
            cont.resumeWithException(e)
        }
    }
}

/**
 * The single entry point: checks permission -> checks GPS -> fetches location
 * -> reverse geocodes -> returns city name string.
 * Call this ONLY after you've confirmed permission is granted and GPS is on;
 * the composable below orchestrates those checks and calls this last.
 */
suspend fun fetchCurrentCityName(context: Context): String {
    if (!hasLocationPermission(context)) {
        throw SecurityException("Location permission not granted")
    }
    if (!isGpsEnabled(context)) {
        throw IllegalStateException("GPS/Location services disabled")
    }
    val location = getCurrentLocation(context)
    return getCityNameFromLocation(context, location)
}

// ---------------------------------------------------------------------------
// 3. COMPOSABLE STATE HOLDER — orchestrates permission + GPS + fetch
// ---------------------------------------------------------------------------

@Composable
fun rememberLocationFetcher(
    onCityResolved: (String) -> Unit
): LocationFetcherController {
    val context = LocalContext.current
    var state by remember { mutableStateOf<LocationFetchState>(LocationFetchState.Idle) }
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showGpsDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun runFetch() {
        scope.launch {
            state = LocationFetchState.Loading
            try {
                val city = fetchCurrentCityName(context)
                state = LocationFetchState.Success(city)
                onCityResolved(city)
            } catch (e: SecurityException) {
                state = LocationFetchState.PermissionDenied
            } catch (e: IllegalStateException) {
                state = LocationFetchState.GpsDisabled
                showGpsDialog = true
            } catch (e: Exception) {
                state = LocationFetchState.Error(e.message ?: "Failed to fetch location")
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            if (isGpsEnabled(context)) {
                runFetch()
            } else {
                state = LocationFetchState.GpsDisabled
                showGpsDialog = true
            }
        } else {
            // Denied — could be permanent (don't ask again) or temporary
            state = LocationFetchState.PermissionDenied
        }
    }

    fun start() {
        when {
            hasLocationPermission(context) -> {
                if (isGpsEnabled(context)) {
                    runFetch()
                } else {
                    state = LocationFetchState.GpsDisabled
                    showGpsDialog = true
                }
            }
            else -> {
                showRationaleDialog = true
            }
        }
    }

    return remember(state, showRationaleDialog, showGpsDialog) {
        LocationFetcherController(
            state = state,
            showRationaleDialog = showRationaleDialog,
            showGpsDialog = showGpsDialog,
            onStart = ::start,
            onConfirmRationale = {
                showRationaleDialog = false
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            onDismissRationale = {
                showRationaleDialog = false
                state = LocationFetchState.PermissionDenied
            },
            onDismissGpsDialog = { showGpsDialog = false },
            onOpenLocationSettings = {
                showGpsDialog = false
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            },
            onOpenAppSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        )
    }
}

data class LocationFetcherController(
    val state: LocationFetchState,
    val showRationaleDialog: Boolean,
    val showGpsDialog: Boolean,
    val onStart: () -> Unit,
    val onConfirmRationale: () -> Unit,
    val onDismissRationale: () -> Unit,
    val onDismissGpsDialog: () -> Unit,
    val onOpenLocationSettings: () -> Unit,
    val onOpenAppSettings: () -> Unit
)

// ---------------------------------------------------------------------------
// 4. DIALOGS
// ---------------------------------------------------------------------------

@Composable
fun LocationPermissionRationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
        title = { Text("Allow location access") },
        text = {
            Text("We use your location to show venues, events, and movies near you. You can change this anytime in Settings.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Allow") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Not now") }
        }
    )
}

@Composable
fun GpsDisabledDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
        title = { Text("Turn on location") },
        text = {
            Text("Your device location is off. Turn it on to detect your city automatically.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Turn on") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ---------------------------------------------------------------------------
// 5. INLINE UI BANNER FOR DENIED STATE (shows clearly in the picker UI)
// ---------------------------------------------------------------------------

@Composable
fun PermissionDeniedBanner(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF3A1414), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF6B6B))
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Location permission denied",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Enable it in Settings to auto-detect your city.",
                color = Color(0xFFD4A0A0),
                fontSize = 11.sp
            )
        }
        TextButton(onClick = onOpenSettings) {
            Text("Open Settings", fontSize = 12.sp)
        }
    }
}