package com.gymapp.domain.state

import com.gymapp.data.model.ActivityFeedItem
import com.gymapp.data.model.Announcement
import com.gymapp.data.model.BranchStatus
import com.gymapp.data.model.CheckIn
import com.gymapp.data.model.Member
import com.gymapp.data.model.Membership
import com.gymapp.data.model.Payment
import com.gymapp.data.model.Plan
import com.gymapp.data.model.Referral
import com.gymapp.data.model.SessionType
import com.gymapp.data.model.StreakState
import com.gymapp.data.model.TimeRange
import com.gymapp.data.model.TrainingDay
import com.gymapp.data.model.TypeCount
import com.gymapp.data.model.VisitStats
import com.gymapp.data.model.WeeklyActivity
import com.gymapp.data.model.WorkoutSession

/** Per-screen success payloads carried inside [UiState.Success]. */

data class HomeData(
    val member: Member,
    val membership: Membership,
    val branch: BranchStatus,
    val stats: VisitStats,
    val streak: StreakState,
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

/**
 * Progress as a daily consistency dashboard. Everything here is derivable from honest quick-log
 * facts (showing up, how long, what type) + the streak, so the screen moves with real activity
 * instead of fabricated volume. Strength (volume/PRs) is a deferred set-level pass.
 */
data class ProgressData(
    val range: TimeRange,
    val streak: StreakState,
    val days: List<TrainingDay>,
    val sessionCount: Int,
    val totalMinutes: Int,
    val avgMinutes: Int,
    val typeMix: List<TypeCount>,
    val lastSession: WorkoutSession,
)

data class ProfileData(
    val member: Member,
)

/**
 * Log fast-path state. The form selection (`selectedType`, `durationMin`) lives in state so Submit
 * can read it and the screen stays a pure render of state. After a successful log, `justLogged`
 * flips the screen to its confirmation and `streak` carries the moved-forward weekly streak.
 */
data class LogData(
    val types: List<SessionType>,
    val selectedType: SessionType?,
    val durationMin: Int,
    val lastSession: WorkoutSession?,
    val streak: StreakState,
    val justLogged: Boolean,
)

/** Activity feed payload: owner announcements pinned above the friends' workout feed. */
data class ActivityData(
    val announcements: List<Announcement>,
    val feed: List<ActivityFeedItem>,
)
