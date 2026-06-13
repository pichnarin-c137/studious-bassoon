package com.gymapp.data.api

import com.gymapp.data.model.Achievement
import com.gymapp.data.model.ActivityFeedItem
import com.gymapp.data.model.Announcement
import com.gymapp.data.model.BodyMetric
import com.gymapp.data.model.BranchStatus
import com.gymapp.data.model.BusyLevel
import com.gymapp.data.model.CheckIn
import com.gymapp.data.model.DaySession
import com.gymapp.data.model.FeedActor
import com.gymapp.data.model.FeedPr
import com.gymapp.data.model.GymBusyness
import com.gymapp.data.model.HourLoad
import com.gymapp.data.model.Member
import com.gymapp.data.model.Membership
import com.gymapp.data.model.MembershipStatus
import com.gymapp.data.model.Payment
import com.gymapp.data.model.PaymentMethod
import com.gymapp.data.model.PersonalRecord
import com.gymapp.data.model.Plan
import com.gymapp.data.model.PlanType
import com.gymapp.data.model.PtContact
import com.gymapp.data.model.Referral
import com.gymapp.data.model.SessionType
import com.gymapp.data.model.TrainingDay
import com.gymapp.data.model.StreakState
import com.gymapp.data.model.VisitStats
import com.gymapp.data.model.VolumePoint
import com.gymapp.data.model.WeeklyActivity
import com.gymapp.data.model.WorkoutLogEntry
import com.gymapp.data.model.WorkoutSession
import com.gymapp.util.DateTimeUtil

/** Sample data standing in for the backend. Timestamps are anchored to launch time. */
object MockData {
    private const val DAY = 86_400_000L
    private const val HOUR = 3_600_000L
    private val now = System.currentTimeMillis()

    val member = Member(
        id = "m_001",
        memberCode = "GX-100245",
        name = "Sokha Chan",
        khmerName = "ចាន់ សុខា",
        phone = "+855 12 345 678",
        photoUrl = null,
        memberSince = now - 220 * DAY,
        pt = PtContact(
            name = "Dara Kim",
            telegram = "https://t.me/coach_dara",
            messenger = "https://m.me/coach.dara",
        ),
    )

    val plans = listOf(
        Plan("p_day", PlanType.DAILY, 1.50),
        Plan("p_week", PlanType.WEEKLY, 7.00),
        Plan("p_month", PlanType.MONTHLY, 30.00),
        Plan("p_quarter", PlanType.QUARTERLY, 80.00),
        Plan("p_half", PlanType.HALF_YEAR, 150.00),
        Plan("p_year", PlanType.YEARLY, 280.00),
    )

    val membership = Membership(
        plan = plans[2], // Monthly
        branchName = "Phnom Penh Central",
        status = MembershipStatus.ACTIVE,
        startedAt = now - 18 * DAY,
        expiresAt = now + 12 * DAY,
        frozen = false,
        freezeDaysRemaining = 7,
    )

    val payments = listOf(
        Payment("pay_1", 30.00, PaymentMethod.ABA_KHQR, now - 18 * DAY, "Monthly renewal"),
        Payment("pay_2", 30.00, PaymentMethod.CASH, now - 48 * DAY, "Monthly renewal"),
        Payment("pay_3", 30.00, PaymentMethod.WING, now - 78 * DAY, "Monthly renewal"),
        Payment("pay_4", 7.00, PaymentMethod.ACLEDA, now - 92 * DAY, "Weekly pass"),
    )

    val referral = Referral(code = "SOKHA5", friendsReferred = 3, freeDaysEarned = 15)

    val checkIns = listOf(0, 1, 2, 4, 5, 7, 8, 11).map { d ->
        CheckIn(id = "ci_$d", timestamp = now - d * DAY - 5 * HOUR)
    }

    val visitStats = VisitStats(
        totalVisits = 86,
        visitsThisMonth = 12,
        lastVisit = checkIns.first().timestamp,
    )

    // The kind streak's default weekly target — shared with [weeklyActivity] so Home's week bars and
    // the streak goal always agree.
    const val WEEKLY_TARGET_DEFAULT = 5

    /**
     * The kind streak: an 8-week run of hitting the weekly target, with this week's progress derived
     * from the same Mon→Sun pattern as [weeklyActivity] (so it tracks the real weekday — e.g. "4 / 5,
     * 1 to your weekly goal"), and one freeze banked. A rest day never touches this.
     */
    fun streakState() = StreakState(
        weeklyTarget = WEEKLY_TARGET_DEFAULT,
        sessionsThisWeek = weeklyActivity().sessionsDone,
        weekStreak = 8,
        freezesAvailable = 1,
    )

    val workouts = listOf(
        WorkoutLogEntry("w_1", "Bench press", 4, 8, 60.0, now - 5 * HOUR),
        WorkoutLogEntry("w_2", "Squat", 5, 5, 80.0, now - 5 * HOUR),
        WorkoutLogEntry("w_3", "Deadlift", 3, 5, 100.0, now - 1 * DAY),
        WorkoutLogEntry("w_4", "Pull-ups", 3, 10, null, now - 1 * DAY),
        WorkoutLogEntry("w_5", "Overhead press", 4, 6, 40.0, now - 2 * DAY),
    )

    // Strava-style session summaries (most recent first).
    val workoutSessions = listOf(
        WorkoutSession("s_1", "Push day", now - 5 * HOUR, durationMin = 58, volumeKg = 4820.0, totalSets = 18, kcal = 420, prCount = 2, type = SessionType.GYM),
        WorkoutSession("s_2", "Pull day", now - 1 * DAY - 5 * HOUR, durationMin = 52, volumeKg = 5210.0, totalSets = 17, kcal = 395, prCount = 1, type = SessionType.GYM),
        WorkoutSession("s_3", "Legs", now - 3 * DAY - 4 * HOUR, durationMin = 64, volumeKg = 6740.0, totalSets = 20, kcal = 510, prCount = 0, type = SessionType.GYM),
    )

    val personalRecords = listOf(
        PersonalRecord("Bench press", bestKg = 72.5, improvementKg = 2.5),
        PersonalRecord("Squat", bestKg = 110.0, improvementKg = 5.0),
        PersonalRecord("Deadlift", bestKg = 140.0, improvementKg = 5.0),
        PersonalRecord("Overhead press", bestKg = 47.5, improvementKg = 2.5),
    )

    // 90 days of total volume lifted, trending upward. Range queries return the tail of this.
    val volumeTrend: List<VolumePoint> = (89 downTo 0).map { d ->
        val dayIndex = 90 - d // 1..90, ascending toward today
        val base = 2600.0 + dayIndex * 26.0
        val wobble = ((dayIndex * 37) % 11 - 5) * 55.0
        VolumePoint(date = now - d * DAY, volumeKg = (base + wobble).coerceAtLeast(1800.0))
    }

    // Gym-dominant rotation (Cambodian iron-gym culture), repeated across the history.
    private val sessionCycle = listOf(
        SessionType.GYM, SessionType.GYM, SessionType.CARDIO,
        SessionType.GYM, SessionType.BODYWEIGHT, SessionType.GYM, SessionType.CARDIO,
    )

    // 90 days of training history: ~3 sessions/week early, ramping to ~5/week recently, with the
    // last four days trained (so the recent weeks clear the streakState weekly target). Honest
    // quick-log facts only — type + duration, no set/volume. Range queries return the tail.
    val trainingDays: List<TrainingDay> = (89 downTo 0).map { d ->
        val i = 89 - d          // 0 = oldest, 89 = today
        val slot = i % 7
        val week = i / 7        // 0..12, ascending toward today
        val trained = when {
            d <= 3 -> true                              // recent unbroken streak
            slot == 0 || slot == 2 || slot == 4 -> true // base 3×/week
            slot == 5 && week >= 4 -> true              // 4×/week from mid-history
            slot == 6 && week >= 9 -> true              // 5×/week in the last few weeks
            else -> false
        }
        if (trained) {
            TrainingDay(
                date = now - d * DAY,
                trained = true,
                type = sessionCycle[i % sessionCycle.size],
                durationMin = 35 + (i * 5) % 36, // 35..70 min, deterministic
            )
        } else {
            TrainingDay(date = now - d * DAY, trained = false)
        }
    }

    // Current calendar week, Mon→Sun. `true` = a session was logged; days after today are still
    // ahead, so they never count as done. Computed per fetch so it tracks the real weekday.
    private val weekPattern = listOf(true, true, false, true, false, true, false) // Mon..Sun
    fun weeklyActivity(): WeeklyActivity {
        val monday = DateTimeUtil.startOfWeekMillis()
        val todayIndex = DateTimeUtil.currentWeekdayIndex()
        val days = (0..6).map { i ->
            DaySession(date = monday + i * DAY, done = weekPattern[i] && i <= todayIndex)
        }
        return WeeklyActivity(
            days = days,
            sessionsDone = days.count { it.done },
            sessionsTarget = WEEKLY_TARGET_DEFAULT,
        )
    }

    val bodyMetrics = listOf(
        BodyMetric(now - 84 * DAY, 72.0, 18.0),
        BodyMetric(now - 56 * DAY, 71.2, 17.4),
        BodyMetric(now - 28 * DAY, 70.6, 16.9),
        BodyMetric(now - 2 * DAY, 70.1, 16.5),
    )

    val achievements = listOf(
        Achievement("a_1", "First check-in", "Showed up for day one.", unlocked = true),
        Achievement("a_2", "7-day streak", "Trained 7 days in a row.", unlocked = true),
        Achievement("a_3", "50 visits", "Reached 50 gym visits.", unlocked = true),
        Achievement("a_4", "Early bird", "10 check-ins before 7 AM.", unlocked = true),
        Achievement("a_5", "100 visits", "Reach 100 gym visits.", unlocked = false),
        Achievement("a_6", "Century lift", "Deadlift 100 kg for reps.", unlocked = false),
    )

    // Friends in the activity feed — slim actors (no phone/memberCode exposed across the social graph).
    private val friendVibol = FeedActor("f_1", "Vibol Sok", null)
    private val friendSreypov = FeedActor("f_2", "Sreypov Chea", null)
    private val friendRithy = FeedActor("f_3", "Rithy Pen", null)
    private val friendBopha = FeedActor("f_4", "Bopha Nan", null)

    // Friends' workouts (most recent first). Type + duration + optional PR only — no exact times.
    val activityFeed = listOf(
        ActivityFeedItem("af_1", friendVibol, SessionType.GYM, 52, now - 2 * HOUR, pr = FeedPr("Bench press", 75.0), kudosCount = 12, youGaveKudos = false),
        ActivityFeedItem("af_2", friendSreypov, SessionType.CARDIO, 35, now - 5 * HOUR, kudosCount = 4, youGaveKudos = true),
        ActivityFeedItem("af_3", friendRithy, SessionType.BODYWEIGHT, 40, now - 1 * DAY, kudosCount = 7, youGaveKudos = false),
        ActivityFeedItem("af_4", friendBopha, SessionType.GYM, 60, now - 1 * DAY - 3 * HOUR, kudosCount = 9, youGaveKudos = false),
        ActivityFeedItem("af_5", friendVibol, SessionType.CARDIO, 28, now - 2 * DAY, kudosCount = 3, youGaveKudos = false),
        ActivityFeedItem("af_6", friendSreypov, SessionType.GYM, 48, now - 3 * DAY, pr = FeedPr("Squat", 95.0), kudosCount = 15, youGaveKudos = true),
    )

    // Owner-authored notices pinned above the feed (most recent first).
    val announcements = listOf(
        Announcement("an_1", "Pchum Ben hours", "Shorter hours over the Pchum Ben holiday. We're back to the full schedule right after — train safe!", now - 6 * HOUR),
        Announcement("an_2", "New class: Zumba", "Zumba every Tue & Thu at 6 PM — free for all members. Bring a friend.", now - 2 * DAY),
    )

    // Branch hours + live busy-ness. Open 06:00–22:00; load curve peaks in the evening. Computed
    // fresh per fetch so the "current hour" highlight and open/closed state track real time.
    private const val OPEN_HOUR = 6
    private const val CLOSE_HOUR = 22
    private val hourlyLoad = listOf(30, 55, 70, 45, 30, 35, 50, 40, 30, 35, 55, 80, 95, 85, 60, 40)

    fun branchStatus(): BranchStatus {
        val hour = DateTimeUtil.currentHour()
        val open = hour in OPEN_HOUR until CLOSE_HOUR
        val hourly = hourlyLoad.mapIndexed { i, load ->
            val h = OPEN_HOUR + i
            HourLoad(hour = h, load = load, current = open && h == hour)
        }
        val currentLoad = hourly.firstOrNull { it.current }?.load ?: 0
        val level = when {
            !open -> BusyLevel.QUIET
            currentLoad >= 70 -> BusyLevel.BUSY
            currentLoad >= 40 -> BusyLevel.MODERATE
            else -> BusyLevel.QUIET
        }
        return BranchStatus(
            name = membership.branchName,
            openNow = open,
            closesAt = DateTimeUtil.todayAt(CLOSE_HOUR),
            busyness = GymBusyness(level = level, hourly = hourly),
        )
    }
}
