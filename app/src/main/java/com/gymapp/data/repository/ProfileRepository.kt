package com.gymapp.data.repository

import com.gymapp.data.api.GymApi
import com.gymapp.data.model.Member
import javax.inject.Inject

interface ProfileRepository {
    suspend fun getMember(): Member
}

class ProfileRepositoryImpl @Inject constructor(
    private val api: GymApi,
) : ProfileRepository {
    override suspend fun getMember(): Member = api.getMember()
}
