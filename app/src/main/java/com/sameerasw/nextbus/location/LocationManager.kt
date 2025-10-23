package com.sameerasw.nextbus.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Looper
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class LocationData(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null
)

class LocationManager(
    private val context: Context,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) {
    private val _locationState = mutableStateOf(LocationData())
    val locationState: State<LocationData> = _locationState

    private var locationCallback: LocationCallback? = null
    private val geocoder = Geocoder(context, Locale.getDefault())

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L // 5 seconds
        ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
                    _locationState.value = _locationState.value.copy(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )

        // Also get last known location immediately
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                _locationState.value = _locationState.value.copy(
                    latitude = it.latitude,
                    longitude = it.longitude
                )
            }
        }
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationProviderClient.removeLocationUpdates(it)
        }
    }

    suspend fun reverseGeocode(latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    buildAddressString(address)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun buildAddressString(address: Address): String {
        return listOfNotNull(
            address.thoroughfare,
            address.locality,
            address.adminArea,
            address.countryName
        ).joinToString(", ")
    }

    suspend fun updateAddressFromLocation() {
        val lat = _locationState.value.latitude
        val lng = _locationState.value.longitude
        if (lat != null && lng != null) {
            val address = reverseGeocode(lat, lng)
            _locationState.value = _locationState.value.copy(address = address)
        }
    }

    fun getCurrentLocation(): LocationData = _locationState.value
}

