package com.gymapp.di

import com.gymapp.data.api.FakeGymApi
import com.gymapp.data.api.GymApi
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the API contract to the in-memory fake for v1. To go live, replace this binding with
 * a Retrofit-provided implementation (add a @Provides for OkHttp/Retrofit + a converter).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ApiModule {

    @Binds
    @Singleton
    abstract fun bindGymApi(impl: FakeGymApi): GymApi
}
