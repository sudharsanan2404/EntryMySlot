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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
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
        withTimeout(20_000) { suspendCancellableCoroutine { cont ->
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation
                .addOnSuccessListener cached@{ location ->
                    if (!cont.isActive) return@cached
                    if (location != null) {
                        cont.resume(location)
                    } else {
                        // No cached location — request a fresh one
                        val cts = com.google.android.gms.tasks.CancellationTokenSource()
                        fusedClient.getCurrentLocation(
                            com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                            cts.token
                        ).addOnSuccessListener fresh@{ freshLocation ->
                            if (!cont.isActive) return@fresh
                            if (freshLocation != null) {
                                cont.resume(freshLocation)
                            } else {
                                cont.resumeWithException(Exception("Unable to get current location"))
                            }
                        }.addOnFailureListener { e -> if (cont.isActive) cont.resumeWithException(e) }
                        cont.invokeOnCancellation { cts.cancel() }
                    }
                }
                .addOnFailureListener { e -> if (cont.isActive) cont.resumeWithException(e) }
        } }

    /**
     * Reverse-geocodes a Location into a city name only.
     * Falls back through locality -> subAdminArea -> adminArea if locality is null.
     */
    suspend fun getCityNameFromLocation(context: Context, location: Location): String = withContext(Dispatchers.IO) {
        val geocoder = Geocoder(context, Locale.getDefault())
        // The legacy geocoder is a blocking network call, even inside a suspend function.
        @Suppress("DEPRECATION")
        val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
        address?.locality?.takeIf(String::isNotBlank)
            ?: address?.subAdminArea?.takeIf(String::isNotBlank)
            ?: address?.adminArea?.takeIf(String::isNotBlank)
            ?: throw Exception("Unable to identify your city")
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
