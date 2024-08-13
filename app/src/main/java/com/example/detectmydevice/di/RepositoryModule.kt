package com.example.detectmydevice.di

import com.example.detectmydevice.data.repository.LocationRepositoryImpl
import com.example.detectmydevice.domain.LocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindLocationRepositoryImpl(impl: LocationRepositoryImpl): LocationRepository

}