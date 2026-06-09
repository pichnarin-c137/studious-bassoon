package com.gymapp.data.model

/** Plan lengths common in Cambodia — short-term options matter, not just monthly/yearly. */
enum class PlanType { DAILY, WEEKLY, MONTHLY, QUARTERLY, HALF_YEAR, YEARLY }

data class Plan(
    val id: String,
    val type: PlanType,
    val priceUsd: Double,
)

enum class MembershipStatus { ACTIVE, EXPIRING, EXPIRED, FROZEN }

data class Membership(
    val plan: Plan,
    val branchName: String,
    val status: MembershipStatus,
    val startedAt: Long,
    val expiresAt: Long,
    val frozen: Boolean,
    val freezeDaysRemaining: Int,
)
