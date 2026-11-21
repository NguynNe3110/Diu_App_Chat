package com.uzuu.diuchat.feature.location.data.adapter

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.uzuu.diuchat.feature.location.domain.model.LocationSnapshot
import java.time.Instant
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DefaultLocationProviderAdapter(
    private val context: Context? = null,
) : LocationProviderAdapter {

    override suspend fun requestSingleFix(): Result<LocationSnapshot> {
        val appContext = context?.applicationContext
        if (appContext != null && hasLocationPermission(appContext)) {
            val realLocation = queryLastKnownLocation(appContext)
            if (realLocation != null) {
                val address = resolveAddress(appContext, realLocation.latitude, realLocation.longitude)
                return Result.success(
                    LocationSnapshot(
                        latitude = realLocation.latitude,
                        longitude = realLocation.longitude,
                        accuracyMeters = if (realLocation.hasAccuracy()) realLocation.accuracy else DEFAULT_ACCURACY,
                        address = address,
                        capturedAt = Instant.now(),
                    ),
                )
            }
        }
        return Result.success(fallbackSnapshot())
    }

    private fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun queryLastKnownLocation(context: Context): Location? {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )
        return providers
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }
    }

    @Suppress("DEPRECATION", "TooGenericExceptionCaught")
    private suspend fun resolveAddress(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): String = withContext(Dispatchers.IO) {
        val fallbackCoords = "%.4f, %.4f".format(Locale.US, latitude, longitude)
        if (!Geocoder.isPresent()) {
            fallbackCoords
        } else {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val first = geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
                if (first == null) {
                    fallbackCoords
                } else {
                    listOfNotNull(
                        first.thoroughfare,
                        first.subLocality ?: first.locality,
                        first.adminArea,
                    ).filter { it.isNotBlank() }
                        .distinct()
                        .joinToString(", ")
                        .ifBlank { fallbackCoords }
                }
            } catch (_: Exception) {
                fallbackCoords
            }
        }
    }

    private fun fallbackSnapshot(): LocationSnapshot = LocationSnapshot(
        latitude = DEFAULT_LATITUDE,
        longitude = DEFAULT_LONGITUDE,
        accuracyMeters = DEFAULT_ACCURACY,
        address = DEFAULT_ADDRESS,
        capturedAt = Instant.now(),
    )

    override fun getAdaptiveSamplingIntervalMs(
        speedMps: Float,
        accuracyMeters: Float,
    ): Long {
        if (accuracyMeters > ACCURACY_POOR_THRESHOLD_METERS) {
            return INTERVAL_POOR_ACCURACY_MS
        }
        return when {
            speedMps < SPEED_STATIONARY_THRESHOLD_MPS -> INTERVAL_STATIONARY_MS
            speedMps <= SPEED_WALKING_THRESHOLD_MPS -> INTERVAL_WALKING_MS
            else -> INTERVAL_FAST_TRANSIT_MS
        }
    }

    companion object {
        const val SPEED_STATIONARY_THRESHOLD_MPS = 0.5f
        const val SPEED_WALKING_THRESHOLD_MPS = 4.0f
        const val ACCURACY_POOR_THRESHOLD_METERS = 50.0f

        const val INTERVAL_STATIONARY_MS = 30_000L
        const val INTERVAL_WALKING_MS = 10_000L
        const val INTERVAL_FAST_TRANSIT_MS = 3_000L
        const val INTERVAL_POOR_ACCURACY_MS = 15_000L

        const val DEFAULT_LATITUDE = 21.0285
        const val DEFAULT_LONGITUDE = 105.8542
        const val DEFAULT_ACCURACY = 12.0f
        const val DEFAULT_ADDRESS = "Hồ Hoàn Kiếm, Hoàn Kiếm, Hà Nội"
    }
}
