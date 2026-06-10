package com.gymapp.data.api

import com.gymapp.data.model.Achievement
import com.gymapp.data.model.ActivityFeedItem
import com.gymapp.data.model.Announcement
import com.gymapp.data.model.AuthSession
import com.gymapp.data.model.BodyMetric
import com.gymapp.data.model.BranchStatus
import com.gymapp.data.model.CheckIn
import com.gymapp.data.model.KudosRequest
import com.gymapp.data.model.LoginRequest
import com.gymapp.data.model.Member
import com.gymapp.data.model.Membership
import com.gymapp.data.model.Payment
import com.gymapp.data.model.PersonalRecord
import com.gymapp.data.model.LogSessionRequest
import com.gymapp.data.model.Plan
import com.gymapp.data.model.Referral
import com.gymapp.data.model.VisitStats
import com.gymapp.data.model.VolumePoint
import com.gymapp.data.model.WeeklyActivity
import com.gymapp.data.model.WorkoutLogEntry
import com.gymapp.data.model.WorkoutSession
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/** In-memory [GymApi] with a small simulated network delay so loading states are exercised. */
@Singleton
class FakeGymApi @Inject constructor() : GymApi {

    private suspend fun <T> respond(value: T): T {
        delay(NETWORK_DELAY_MS)
        return value
    }

    /**
     * A quick-log in this run bumps the streak + visit counters once, so Home and the log
     * confirmation reflect "you trained today" without a real backend behind them.
     */
    private var loggedToday = false

    private fun currentVisitStats(): VisitStats =
        if (loggedToday) {
            MockData.visitStats.copy(
                currentStreak = MockData.visitStats.currentStreak + 1,
                totalVisits = MockData.visitStats.totalVisits + 1,
                visitsThisMonth = MockData.visitStats.visitsThisMonth + 1,
                lastVisit = System.currentTimeMillis(),
            )
        } else {
            MockData.visitStats
        }

    /**
     * Owner-provisioned login: v1 accepts any non-blank member ID + password (the branch front
     * desk issues real credentials). A real backend verifies them and returns a signed token.
     */
    override suspend fun signIn(request: LoginRequest): AuthSession {
        delay(NETWORK_DELAY_MS)
        require(request.memberId.isNotBlank() && request.password.isNotBlank())
        return AuthSession(token = "mock-token-${request.memberId}", memberId = request.memberId)
    }

    override suspend fun getMember(): Member = respond(MockData.member)
    override suspend fun getMembership(): Membership = respond(MockData.membership)
    override suspend fun getPlans(): List<Plan> = respond(MockData.plans)
    override suspend fun getBranch(): BranchStatus = respond(MockData.branchStatus())
    override suspend fun getPayments(): List<Payment> = respond(MockData.payments)
    override suspend fun getReferral(): Referral = respond(MockData.referral)
    override suspend fun getCheckIns(): List<CheckIn> = respond(MockData.checkIns)
    override suspend fun getVisitStats(): VisitStats = respond(currentVisitStats())
    override suspend fun getWeeklyActivity(): WeeklyActivity = respond(MockData.weeklyActivity())
    override suspend fun getWorkoutLogs(): List<WorkoutLogEntry> = respond(MockData.workouts)
    override suspend fun getRecentSession(): WorkoutSession = respond(MockData.workoutSessions.first())
    override suspend fun logSession(request: LogSessionRequest): VisitStats {
        loggedToday = true
        return respond(currentVisitStats())
    }
    override suspend fun getPersonalRecords(): List<PersonalRecord> = respond(MockData.personalRecords)
    override suspend fun getVolumeTrend(days: Int): List<VolumePoint> =
        respond(MockData.volumeTrend.takeLast(days))
    override suspend fun getBodyMetrics(): List<BodyMetric> = respond(MockData.bodyMetrics)
    override suspend fun getAchievements(): List<Achievement> = respond(MockData.achievements)
    override suspend fun getAnnouncements(): List<Announcement> = respond(MockData.announcements)
    override suspend fun getActivityFeed(): List<ActivityFeedItem> = respond(MockData.activityFeed)
    override suspend fun toggleKudos(request: KudosRequest) {
        // The ViewModel updates kudos optimistically; the mock just simulates the round-trip.
        delay(NETWORK_DELAY_MS)
    }

    private companion object {
        const val NETWORK_DELAY_MS = 500L
    }
}
