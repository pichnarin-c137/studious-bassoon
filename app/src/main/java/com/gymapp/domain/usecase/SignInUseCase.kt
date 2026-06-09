package com.gymapp.domain.usecase

import com.gymapp.data.repository.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(memberId: String, password: String) =
        authRepository.signIn(memberId, password)
}
