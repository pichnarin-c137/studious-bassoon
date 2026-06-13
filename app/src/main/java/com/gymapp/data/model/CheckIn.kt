package com.gymapp.data.model

data class CheckIn(
    val id: String,
    val timestamp: Long,
)

data class VisitStats(
    val totalVisits: Int,
    val visitsThisMonth: Int,
    val lastVisit: Long?,
)
