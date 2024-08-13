package com.example.detectmydevice.data.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import com.example.detectmydevice.domain.LocationRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService: Service() {
    private val TAG = "TrackingService"
    @Inject
    lateinit var locationRepository: LocationRepository

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        locationRepository.startLocationUpdates()
        locationRepository.lastLocation.observe(this) {

        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}