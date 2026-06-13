package com.gymapp.di

import com.gymapp.data.local.DataStoreWorkoutStore
import com.gymapp.data.local.WorkoutStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Binds the workout persistence interface to its DataStore-backed implementation. */
@Module
@InstallIn(SingletonComponent::class)
abstract class StoreModule {

    @Binds
    @Singleton
    abstract fun bindWorkoutStore(impl: DataStoreWorkoutStore): WorkoutStore
}
