package com.gymapp.data.model

/** Owner-provisioned login: the member ID + password handed out at the branch front desk. */
data class LoginRequest(
    val memberId: String,
    val password: String,
)

/** Session returned on a successful sign-in. */
data class AuthSession(
    val token: String,
    val memberId: String,
)
