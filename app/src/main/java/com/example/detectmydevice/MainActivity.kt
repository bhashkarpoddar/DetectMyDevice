package com.example.detectmydevice

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.detectmydevice.data.services.TrackingService
import com.example.detectmydevice.data.services.TrackingService.Companion.ACTION_START_UPDATES
import com.example.detectmydevice.databinding.ActivityMainBinding
import com.example.detectmydevice.utils.goToAppDetailsSettings
import com.example.detectmydevice.utils.requestMultiplePermissions
import com.example.detectmydevice.utils.showOkayAlertFunction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.startLocationUpdatesBtn.setOnClickListener {

        }

        initObservers()
    }

    private fun initObservers() {
        lifecycleScope.launch {
            mainViewModel.locationUpdates.collectLatest {location->
                val contentText = if (location != null) {
                    getString(R.string.location_lat_lng, location.latitude, location.longitude)
                } else {
                    getString(R.string.waiting_for_location)
                }
                binding.locationDetails.text = contentText
            }
        }
    }

    override fun onStart() {
        super.onStart()
        requestMandatoryPermissions()
    }

    private fun requestMandatoryPermissions() {
        var permissionList = arrayOf<String>(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionList += Manifest.permission.POST_NOTIFICATIONS
        }
        requestMultiplePermissions(
            permissionList,
            allGranted = {
                /*Do your task here*/
                Intent(this, TrackingService::class.java).apply {
                    action = ACTION_START_UPDATES
                    startService(this)
                }
            },
            denied = {
                // Permission application failed and it is unchecked, do not ask again, you can continue to apply next time
                showOkayAlertFunction(
                    getString(R.string.permission_denied_title),
                    getString(R.string.permission_denied_alert),
                    positiveBtnClick = { goToAppDetailsSettings() })
            },
            explained = {
                // Permission application failed and it has been checked Do not ask again, you need to explain the reason to the user and guide the user to open the permission
                showOkayAlertFunction(
                    getString(R.string.permission_denied_title),
                    getString(R.string.permission_denied_alert),
                    positiveBtnClick = { goToAppDetailsSettings() })
            })
    }
}