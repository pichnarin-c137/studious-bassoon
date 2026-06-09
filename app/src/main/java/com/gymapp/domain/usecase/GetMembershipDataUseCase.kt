package com.gymapp.domain.usecase

import com.gymapp.data.repository.MembershipRepository
import com.gymapp.domain.state.MembershipData
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetMembershipDataUseCase @Inject constructor(
    private val membershipRepository: MembershipRepository,
) {
    suspend operator fun invoke(): MembershipData = coroutineScope {
        val membership = async { membershipRepository.getMembership() }
        val plans = async { membershipRepository.getPlans() }
        val payments = async { membershipRepository.getPayments() }
        val referral = async { membershipRepository.getReferral() }
        MembershipData(membership.await(), plans.await(), payments.await(), referral.await())
    }
}
