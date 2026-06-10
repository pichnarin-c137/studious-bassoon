package com.gymapp.di

import com.gymapp.data.repository.ActivityRepository
import com.gymapp.data.repository.ActivityRepositoryImpl
import com.gymapp.data.repository.AuthRepository
import com.gymapp.data.repository.AuthRepositoryImpl
import com.gymapp.data.repository.CheckInRepository
import com.gymapp.data.repository.CheckInRepositoryImpl
import com.gymapp.data.repository.MembershipRepository
import com.gymapp.data.repository.MembershipRepositoryImpl
import com.gymapp.data.repository.ProfileRepository
import com.gymapp.data.repository.ProfileRepositoryImpl
import com.gymapp.data.repository.ProgressRepository
import com.gymapp.data.repository.ProgressRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindMembershipRepository(impl: MembershipRepositoryImpl): MembershipRepository

    @Binds
    @Singleton
    abstract fun bindCheckInRepository(impl: CheckInRepositoryImpl): CheckInRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository

    @Binds
    @Singleton
    abstract fun bindActivityRepository(impl: ActivityRepositoryImpl): ActivityRepository
}
