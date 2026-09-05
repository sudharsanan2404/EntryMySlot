package com.entrymyslot.app.core.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object LocationHelper {

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
                if (city.isNullOrBlank()) {
                    cont.resumeWithException(Exception("Unable to identify your city"))
                } else {
                    cont.resume(city)
                }
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }
    }

    /**
     * The single entry point: checks permission -> checks GPS -> fetches location
     * -> reverse geocodes -> returns city name string.
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
}
