package com.entrymyslot.app.screens.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.entrymyslot.app.core.components.LocationFetchState
import com.entrymyslot.app.core.components.PremiumLoadingState
import com.entrymyslot.app.core.components.rememberLocationFetcher
import com.entrymyslot.app.core.utils.LocationHelper
import com.entrymyslot.app.screens.home.GlowBackground

@Composable
fun PermissionsScreen(
    onPermissionsComplete: (String?) -> Unit,
    onDeclineLocation: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var permissionsStarted by remember { mutableStateOf(false) }
    var flowFinished by remember { mutableStateOf(false) }

    fun continueWithDetectedCity(city: String?) {
        if (flowFinished) return
        flowFinished = true
        onPermissionsComplete(city?.takeIf { it.isNotBlank() && it != "Unknown" })
    }

    fun continueWithManualSelection() {
        if (flowFinished) return
        flowFinished = true
        onDeclineLocation()
    }
    
    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.POST_NOTIFICATIONS
    } else null

    val locationFetcher = rememberLocationFetcher(
        onCityResolved = { city ->
            continueWithDetectedCity(city)
        }
    )

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            locationFetcher.onStart()
        } else {
            continueWithManualSelection()
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        locationLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(Unit) {
        if (!permissionsStarted) {
            permissionsStarted = true
            if (notificationPermission != null && ContextCompat.checkSelfPermission(context, notificationPermission) != PackageManager.PERMISSION_GRANTED) {
                notificationLauncher.launch(notificationPermission)
            } else {
                locationLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    LaunchedEffect(locationFetcher.state) {
        when (locationFetcher.state) {
            LocationFetchState.PermissionDenied,
            is LocationFetchState.Error -> continueWithManualSelection()
            else -> Unit
        }
    }

    DisposableEffect(lifecycleOwner, locationFetcher.state) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME &&
                locationFetcher.state is LocationFetchState.GpsDisabled &&
                !flowFinished
            ) {
                if (LocationHelper.isGpsEnabled(context)) {
                    locationFetcher.onStart()
                } else {
                    continueWithManualSelection()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GlowBackground()
        PremiumLoadingState(
            modifier = Modifier.align(Alignment.Center),
            message = when (locationFetcher.state) {
                LocationFetchState.Loading -> "Detecting your city..."
                LocationFetchState.GpsDisabled -> "Waiting for device location..."
                else -> "Setting up permissions..."
            }
        )
    }
}
