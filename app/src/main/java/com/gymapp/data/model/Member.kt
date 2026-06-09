package com.gymapp.data.model

/** A gym member. [memberCode] is what's shown as the Member ID and encoded in the QR. */
data class Member(
    val id: String,
    val memberCode: String,
    val name: String,
    val khmerName: String?,
    val phone: String,
    val photoUrl: String?,
    val memberSince: Long,
    val pt: PtContact?,
)

/** Personal trainer contact. Communication is via deep-link, not in-app chat (spec). */
data class PtContact(
    val name: String,
    val telegram: String?,
    val messenger: String?,
)
