package com.gymapp.domain.state

import com.gymapp.data.model.BranchStatus
import com.gymapp.data.model.CheckIn
import com.gymapp.data.model.Member
import com.gymapp.data.model.Membership
import com.gymapp.data.model.Payment
import com.gymapp.data.model.PersonalRecord
import com.gymapp.data.model.Plan
import com.gymapp.data.model.Referral
import com.gymapp.data.model.TimeRange
import com.gymapp.data.model.VisitStats
import com.gymapp.data.model.VolumePoint
import com.gymapp.data.model.WeeklyActivity
import com.gymapp.data.model.WorkoutSession

/** Per-screen success payloads carried inside [UiState.Success]. */

data class HomeData(
    val member: Member,
    val membership: Membership,
    val branch: BranchStatus,
    val stats: VisitStats,
    val weeklyActivity: WeeklyActivity,
)

data class CheckInData(
    val member: Member,
    val qrPayload: String,
    val recentVisits: List<CheckIn>,
)

data class MembershipData(
    val membership: Membership,
    val plans: List<Plan>,
    val payments: List<Payment>,
    val referral: Referral,
)

data class ProgressData(
    val range: TimeRange,
    val volumeTrend: List<VolumePoint>,
    val volumeDeltaPct: Double,
    val recentSession: WorkoutSession,
    val personalRecords: List<PersonalRecord>,
)

data class ProfileData(
    val member: Member,
)
