package com.gymapp.data.repository

import com.gymapp.data.api.GymApi
import com.gymapp.data.model.LoginRequest
import com.gymapp.util.SessionManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface AuthRepository {
    val signedIn: Flow<Boolean>
    suspend fun signIn(memberId: String, password: String)
    suspend fun signOut()
}

class AuthRepositoryImpl @Inject constructor(
    private val api: GymApi,
    private val sessionManager: SessionManager,
) : AuthRepository {
    override val signedIn: Flow<Boolean> = sessionManager.signedIn

    override suspend fun signIn(memberId: String, password: String) {
        val session = api.signIn(LoginRequest(memberId.trim(), password))
        sessionManager.signIn(session.token, session.memberId)
    }

    override suspend fun signOut() = sessionManager.clear()
}
