package com.example.detectmydevice.data.repository

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.example.detectmydevice.domain.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val locationRequest: LocationRequest,
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationRepository {

    private val _isReceivingUpdates = MutableStateFlow(false)
    val isReceivingLocationUpdates = _isReceivingUpdates.asStateFlow()

    private val _lastLocation = MutableStateFlow<Location?>(null)
    val lastLocation = _lastLocation.asStateFlow()

    private var locationCallback: LocationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult.lastLocation?.let {
                _lastLocation.value = it
            }
        }

        override fun onLocationAvailability(locationAvailability: LocationAvailability) {
            super.onLocationAvailability(locationAvailability)
        }
    }

    @SuppressLint("MissingPermission")
    override fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        _isReceivingUpdates.value = true
    }

    override fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        _isReceivingUpdates.value = false
        _lastLocation.value = null
    }
}