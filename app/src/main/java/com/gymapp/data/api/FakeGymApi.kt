package com.gymapp.data.api

import com.gymapp.data.local.WorkoutAnalytics
import com.gymapp.data.local.WorkoutStore
import com.gymapp.data.model.Achievement
import com.gymapp.data.model.ActivityFeedItem
import com.gymapp.data.model.Announcement
import com.gymapp.data.model.AuthSession
import com.gymapp.data.model.BodyMetric
import com.gymapp.data.model.BranchStatus
import com.gymapp.data.model.CheckIn
import com.gymapp.data.model.DetailedLogRequest
import com.gymapp.data.model.KudosRequest
import com.gymapp.data.model.LoginRequest
import com.gymapp.data.model.Member
import com.gymapp.data.model.Membership
import com.gymapp.data.model.Payment
import com.gymapp.data.model.PersonalRecord
import com.gymapp.data.model.LogSessionRequest
import com.gymapp.data.model.Plan
import com.gymapp.data.model.Referral
import com.gymapp.data.model.StoredSession
import com.gymapp.data.model.StreakState
import com.gymapp.data.model.TrainingDay
import com.gymapp.data.model.VisitStats
import com.gymapp.data.model.VolumePoint
import com.gymapp.data.model.WeeklyTargetRequest
import com.gymapp.data.model.WeeklyActivity
import com.gymapp.data.model.WorkoutLogEntry
import com.gymapp.data.model.WorkoutPlan
import com.gymapp.data.model.WorkoutSession
import com.gymapp.data.model.toWorkoutSession
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/** In-memory [GymApi] with a small simulated network delay so loading states are exercised. */
@Singleton
class FakeGymApi @Inject constructor(
    private val store: WorkoutStore,
) : GymApi {

    private suspend fun <T> respond(value: T): T {
        delay(NETWORK_DELAY_MS)
        return value
    }

    /**
     * Quick-logs in this run move the weekly streak + visit counters forward, so Home, Progress and
     * the log confirmation reflect "you trained" without a real backend behind them. State is
     * per-run (resets when the process dies); a real backend persists it across weeks.
     */
    private var sessionsLoggedThisRun = 0
    private var weeklyTarget = MockData.streakState().weeklyTarget
    private var weekStreak = MockData.streakState().weekStreak
    private var freezesAvailable = MockData.streakState().freezesAvailable

    private fun currentVisitStats(): VisitStats =
        if (sessionsLoggedThisRun > 0) {
            MockData.visitStats.copy(
                totalVisits = MockData.visitStats.totalVisits + sessionsLoggedThisRun,
                visitsThisMonth = MockData.visitStats.visitsThisMonth + sessionsLoggedThisRun,
                lastVisit = System.currentTimeMillis(),
            )
        } else {
            MockData.visitStats
        }

    private fun currentStreakState(): StreakState = StreakState(
        weeklyTarget = weeklyTarget,
        sessionsThisWeek = MockData.streakState().sessionsThisWeek + sessionsLoggedThisRun,
        weekStreak = weekStreak,
        freezesAvailable = freezesAvailable,
    )

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
    override suspend fun getStreakState(): StreakState = respond(currentStreakState())
    override suspend fun setWeeklyTarget(request: WeeklyTargetRequest): StreakState {
        weeklyTarget = request.target.coerceIn(WEEKLY_TARGET_MIN, WEEKLY_TARGET_MAX)
        return respond(currentStreakState())
    }
    override suspend fun getWorkoutLogs(): List<WorkoutLogEntry> = respond(MockData.workouts)
    override suspend fun getWorkoutPlans(): List<WorkoutPlan> = respond(MockData.workoutPlans)
    override suspend fun getRecentSession(): WorkoutSession =
        respond(store.mostRecent()?.toWorkoutSession() ?: MockData.workoutSessions.first())

    override suspend fun logSession(request: LogSessionRequest): StreakState {
        registerLoggedSession()
        store.append(
            StoredSession(
                id = "log_${System.currentTimeMillis()}",
                type = request.type,
                durationMin = request.durationMin,
                performedAt = System.currentTimeMillis(),
            ),
        )
        return respond(currentStreakState())
    }

    override suspend fun logDetailedSession(request: DetailedLogRequest): StreakState {
        registerLoggedSession()
        val prCount = WorkoutAnalytics.prCount(request.exercises, WorkoutAnalytics.bestPerExercise(store.all()))
        store.append(
            StoredSession(
                id = "sess_${System.currentTimeMillis()}",
                type = request.type,
                durationMin = request.durationMin,
                performedAt = System.currentTimeMillis(),
                exercises = request.exercises,
                note = request.note,
                prCount = prCount,
            ),
        )
        return respond(currentStreakState())
    }

    /**
     * Moves the in-run weekly streak forward for one logged session. Crossing the weekly target
     * secures the week: bump the streak once and bank a freeze every fourth secured week (capped);
     * logging more sessions the same week doesn't re-bump.
     */
    private fun registerLoggedSession() {
        val before = MockData.streakState().sessionsThisWeek + sessionsLoggedThisRun
        sessionsLoggedThisRun++
        val after = before + 1
        if (before < weeklyTarget && after >= weeklyTarget) {
            weekStreak++
            if (weekStreak % FREEZE_EARN_EVERY == 0 && freezesAvailable < FREEZE_CAP) {
                freezesAvailable++
            }
        }
    }
    override suspend fun getPersonalRecords(): List<PersonalRecord> =
        respond(WorkoutAnalytics.personalRecords(store.all()))
    override suspend fun getVolumeTrend(days: Int): List<VolumePoint> =
        respond(WorkoutAnalytics.volumeTrend(store.all()).takeLast(days))
    override suspend fun getTrainingDays(days: Int): List<TrainingDay> =
        respond(MockData.trainingDays.takeLast(days))
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
        const val FREEZE_EARN_EVERY = 4   // bank a freeze every 4th secured week
        const val FREEZE_CAP = 2          // never hold more than this many
        const val WEEKLY_TARGET_MIN = 3
        const val WEEKLY_TARGET_MAX = 6
    }
}
