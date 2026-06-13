package com.gymapp.domain.usecase

import com.gymapp.data.repository.CheckInRepository
import com.gymapp.data.repository.MembershipRepository
import com.gymapp.data.repository.ProfileRepository
import com.gymapp.domain.state.HomeData
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetHomeDataUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val membershipRepository: MembershipRepository,
    private val checkInRepository: CheckInRepository,
) {
    suspend operator fun invoke(): HomeData = coroutineScope {
        val member = async { profileRepository.getMember() }
        val membership = async { membershipRepository.getMembership() }
        val branch = async { membershipRepository.getBranch() }
        val stats = async { checkInRepository.getVisitStats() }
        val streak = async { checkInRepository.getStreakState() }
        val weekly = async { checkInRepository.getWeeklyActivity() }
        HomeData(member.await(), membership.await(), branch.await(), stats.await(), streak.await(), weekly.await())
    }
}
