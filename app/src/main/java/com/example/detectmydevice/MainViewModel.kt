package com.example.detectmydevice

import androidx.lifecycle.ViewModel
import com.example.detectmydevice.data.repository.LocationRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val locationRepositoryImpl: LocationRepositoryImpl) : ViewModel() {

    val locationUpdates = locationRepositoryImpl.lastLocation

}