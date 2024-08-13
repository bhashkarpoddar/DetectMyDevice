package com.example.detectmydevice.data.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.detectmydevice.BuildConfig
import com.example.detectmydevice.R
import com.example.detectmydevice.data.repository.LocationRepositoryImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService: Service() {
    private val TAG = "TrackingService"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Inject
    lateinit var locationRepositoryImpl: LocationRepositoryImpl

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        // This action comes from our ongoing notification. The user requested to stop updates.
        when(intent?.action){
            ACTION_STOP_UPDATES -> {
                locationRepositoryImpl.stopLocationUpdates()
                stopForeground(true)
                stopSelf()
            }
            ACTION_START_UPDATES -> {
                // Startup tasks only happen once.
                locationRepositoryImpl.startLocationUpdates()
                // Update any foreground notification when we receive location updates.
                serviceScope.launch {
                    locationRepositoryImpl.lastLocation.collect(::showNotification)
                }
            }
            else -> {

            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showNotification(location: Location?) {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(location))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }
    }

    private fun buildNotification(location: Location?) : Notification {
        // Tapping the notification opens the app.
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(this.packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Include an action to stop location updates without going through the app UI.
        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, this::class.java).setAction(ACTION_STOP_UPDATES),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val contentText = if (location != null) {
            getString(R.string.location_lat_lng, location.latitude, location.longitude)
        } else {
            getString(R.string.waiting_for_location)
        }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_location_24)
            .addAction(R.drawable.ic_stop, getString(R.string.stop), stopIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()
    }

    companion object {
        const val UNBIND_DELAY_MILLIS = 2000.toLong() // 2 seconds
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "LocationUpdates"
        const val ACTION_STOP_UPDATES = BuildConfig.APPLICATION_ID + ".ACTION_STOP_UPDATES"
        const val ACTION_START_UPDATES = BuildConfig.APPLICATION_ID + ".ACTION_START_UPDATES"
    }
}