package com.gymapp.data.model

/** Cambodia-first payment rails plus cash logging (spec). */
enum class PaymentMethod { ABA_KHQR, ACLEDA, WING, CASH }

data class Payment(
    val id: String,
    val amountUsd: Double,
    val method: PaymentMethod,
    val paidAt: Long,
    val description: String,
)

data class Referral(
    val code: String,
    val friendsReferred: Int,
    val freeDaysEarned: Int,
)
