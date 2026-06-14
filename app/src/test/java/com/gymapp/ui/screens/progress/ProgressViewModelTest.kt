package com.gymapp.ui.screens.progress

import com.gymapp.data.model.Achievement
import com.gymapp.data.model.BodyMetric
import com.gymapp.data.model.LoggedExercise
import com.gymapp.data.model.PersonalRecord
import com.gymapp.data.model.SessionType
import com.gymapp.data.model.StreakState
import com.gymapp.data.model.TimeRange
import com.gymapp.data.model.TrainingDay
import com.gymapp.data.model.VisitStats
import com.gymapp.data.model.VolumePoint
import com.gymapp.data.model.WorkoutLogEntry
import com.gymapp.data.model.WorkoutPlan
import com.gymapp.data.model.WorkoutSession
import com.gymapp.data.repository.ProgressRepository
import com.gymapp.domain.intent.ProgressIntent
import com.gymapp.domain.state.ProgressData
import com.gymapp.domain.state.UiState
import com.gymapp.domain.usecase.GetProgressDataUseCase
import com.gymapp.domain.usecase.SetWeeklyTargetUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    // 8 days oldest→newest: 6 trained (GYM×3, CARDIO×2, BODYWEIGHT×1), durations sum to 245 min.
    private val allDays = listOf(
        TrainingDay(date = 0L, trained = true, type = SessionType.GYM, durationMin = 60),
        TrainingDay(date = 1L, trained = false),
        TrainingDay(date = 2L, trained = true, type = SessionType.CARDIO, durationMin = 30),
        TrainingDay(date = 3L, trained = true, type = SessionType.GYM, durationMin = 40),
        TrainingDay(date = 4L, trained = false),
        TrainingDay(date = 5L, trained = true, type = SessionType.BODYWEIGHT, durationMin = 50),
        TrainingDay(date = 6L, trained = true, type = SessionType.GYM, durationMin = 20),
        TrainingDay(date = 7L, trained = true, type = SessionType.CARDIO, durationMin = 45),
    )
    private val streak = StreakState(weeklyTarget = 4, sessionsThisWeek = 2, weekStreak = 8, freezesAvailable = 1)
    private val lastSession = WorkoutSession(
        id = "s_1", name = "Push day", performedAt = 7L, durationMin = 58,
        volumeKg = 0.0, totalSets = 0, kcal = 0, prCount = 2, type = SessionType.GYM,
    )

    private val repo = FakeProgressRepository(allDays, streak, lastSession)

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = ProgressViewModel(GetProgressDataUseCase(repo), SetWeeklyTargetUseCase(repo))
    private fun loaded(vm: ProgressViewModel): ProgressData = (vm.state.value as UiState.Success).data

    @Test
    fun `load derives consistency totals and the streak from the window`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        val data = loaded(vm)
        // Default range is 30 days → takeLast(30) returns all 8 seeded days.
        assertEquals(TimeRange.LAST_30_DAYS, data.range)
        assertEquals(8, data.streak.weekStreak)
        assertEquals(8, data.days.size)
        assertEquals(6, data.sessionCount)
        assertEquals(245, data.totalMinutes)
        assertEquals(40, data.avgMinutes) // 245 / 6 (integer)
        assertEquals("Push day", data.lastSession.name)
    }

    @Test
    fun `type mix counts trained sessions by type, gym-first, dropping empty buckets`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        val mix = loaded(vm).typeMix
        assertEquals(3, mix.size)
        assertEquals(SessionType.GYM, mix[0].type); assertEquals(3, mix[0].count)
        assertEquals(SessionType.CARDIO, mix[1].type); assertEquals(2, mix[1].count)
        assertEquals(SessionType.BODYWEIGHT, mix[2].type); assertEquals(1, mix[2].count)
    }

    @Test
    fun `changing the range reloads over the shorter window`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onIntent(ProgressIntent.SetRange(TimeRange.LAST_7_DAYS))
        advanceUntilIdle()
        val data = loaded(vm)
        assertEquals(TimeRange.LAST_7_DAYS, data.range)
        assertEquals(7, data.days.size)        // takeLast(7) drops the oldest day
        assertEquals(5, data.sessionCount)     // that dropped day was a trained GYM session
        assertEquals(185, data.totalMinutes)   // 245 - 60
    }

    @Test
    fun `setting the weekly target updates the streak in place`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(4, loaded(vm).streak.weeklyTarget)
        vm.onIntent(ProgressIntent.SetWeeklyTarget(6))
        advanceUntilIdle()
        assertEquals(6, loaded(vm).streak.weeklyTarget)
    }

    @Test
    fun `load surfaces personal records from the repository`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        val prs = loaded(vm).personalRecords
        assertEquals(1, prs.size)
        assertEquals("Bench press", prs[0].exercise)
    }
}

private class FakeProgressRepository(
    private val allDays: List<TrainingDay>,
    private val streak: StreakState,
    private val recent: WorkoutSession,
) : ProgressRepository {
    override suspend fun getWorkoutLogs(): List<WorkoutLogEntry> = emptyList()
    override suspend fun getWorkoutPlans(): List<WorkoutPlan> = emptyList()
    override suspend fun getRecentSession(): WorkoutSession = recent
    override suspend fun getPersonalRecords(): List<PersonalRecord> =
        listOf(PersonalRecord("Bench press", bestKg = 80.0, improvementKg = 5.0))
    override suspend fun getVolumeTrend(days: Int): List<VolumePoint> = emptyList()
    override suspend fun getTrainingDays(days: Int): List<TrainingDay> = allDays.takeLast(days)
    override suspend fun getVisitStats(): VisitStats = VisitStats(totalVisits = 80, visitsThisMonth = 11, lastVisit = 7L)
    override suspend fun getBodyMetrics(): List<BodyMetric> = emptyList()
    override suspend fun getAchievements(): List<Achievement> = emptyList()
    override suspend fun getStreakState(): StreakState = streak
    override suspend fun setWeeklyTarget(target: Int): StreakState = streak.copy(weeklyTarget = target)
    override suspend fun logSession(type: SessionType, durationMin: Int): StreakState =
        streak.copy(sessionsThisWeek = streak.sessionsThisWeek + 1)

    override suspend fun logDetailedSession(
        type: SessionType,
        durationMin: Int,
        exercises: List<LoggedExercise>,
        note: String?,
    ): StreakState = streak.copy(sessionsThisWeek = streak.sessionsThisWeek + 1)
}
