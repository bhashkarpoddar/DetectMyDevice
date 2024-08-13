package com.example.detectmydevice.domain

import android.location.Location

interface LocationRepository {
    fun startLocationUpdates()
    fun stopLocationUpdates()
}