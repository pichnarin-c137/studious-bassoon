package com.gymapp.domain.usecase

import com.gymapp.data.repository.ProfileRepository
import com.gymapp.domain.state.ProfileData
import javax.inject.Inject

class GetProfileDataUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(): ProfileData = ProfileData(profileRepository.getMember())
}
