package com.entrymyslot.app.core.components

import android.Manifest
import android.content.Context
import android.content.Intent
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
import com.entrymyslot.app.core.utils.LocationHelper
import kotlinx.coroutines.launch

// Shared Theme Colors

sealed class LocationFetchState {
    object Idle : LocationFetchState()
    object Loading : LocationFetchState()
    data class Success(val cityName: String) : LocationFetchState()
    object PermissionDenied : LocationFetchState()
    object GpsDisabled : LocationFetchState()
    data class Error(val message: String) : LocationFetchState()
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
                val city = LocationHelper.fetchCurrentCityName(context)
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
            if (LocationHelper.isGpsEnabled(context)) {
                runFetch()
            } else {
                state = LocationFetchState.GpsDisabled
                showGpsDialog = true
            }
        } else {
            state = LocationFetchState.PermissionDenied
        }
    }

    fun start() {
        when {
            LocationHelper.hasLocationPermission(context) -> {
                if (LocationHelper.isGpsEnabled(context)) {
                    runFetch()
                } else {
                    state = LocationFetchState.GpsDisabled
                    showGpsDialog = true
                }
            }
            else -> {
                // Skip rationale dialog as requested and request permission directly
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
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

@Composable
fun GpsDisabledDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFFF8A00)) },
        title = { Text("Turn on location", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Text("Your device location is off. Turn it on to detect your city automatically.", color = Color.White.copy(alpha = 0.7f))
        },
        containerColor = Color(0xFF0A1D4D),
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A00))
            ) { Text("Turn on", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White.copy(alpha = 0.5f)) }
        }
    )
}
