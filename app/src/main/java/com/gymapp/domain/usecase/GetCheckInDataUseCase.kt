package com.gymapp.domain.usecase

import com.gymapp.data.model.Member
import com.gymapp.data.repository.CheckInRepository
import com.gymapp.data.repository.ProfileRepository
import com.gymapp.domain.state.CheckInData
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetCheckInDataUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val checkInRepository: CheckInRepository,
) {
    suspend operator fun invoke(): CheckInData = coroutineScope {
        val member = async { profileRepository.getMember() }
        val visits = async { checkInRepository.getCheckIns() }
        val m = member.await()
        CheckInData(
            member = m,
            qrPayload = qrPayload(m),
            recentVisits = visits.await(),
        )
    }

    /** Opaque payload the door scanner reads. Real impl would sign/expire this. */
    private fun qrPayload(member: Member): String = "GYMAPP|MEMBER|${member.memberCode}"
}
