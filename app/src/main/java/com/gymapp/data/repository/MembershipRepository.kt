package com.gymapp.data.repository

import com.gymapp.data.api.GymApi
import com.gymapp.data.model.BranchStatus
import com.gymapp.data.model.Membership
import com.gymapp.data.model.Payment
import com.gymapp.data.model.Plan
import com.gymapp.data.model.Referral
import javax.inject.Inject

interface MembershipRepository {
    suspend fun getMembership(): Membership
    suspend fun getBranch(): BranchStatus
    suspend fun getPlans(): List<Plan>
    suspend fun getPayments(): List<Payment>
    suspend fun getReferral(): Referral
}

class MembershipRepositoryImpl @Inject constructor(
    private val api: GymApi,
) : MembershipRepository {
    override suspend fun getMembership(): Membership = api.getMembership()
    override suspend fun getBranch(): BranchStatus = api.getBranch()
    override suspend fun getPlans(): List<Plan> = api.getPlans()
    override suspend fun getPayments(): List<Payment> = api.getPayments()
    override suspend fun getReferral(): Referral = api.getReferral()
}
