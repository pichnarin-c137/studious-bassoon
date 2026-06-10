package com.gymapp.ui.screens.log

import com.gymapp.data.model.Achievement
import com.gymapp.data.model.BodyMetric
import com.gymapp.data.model.CheckIn
import com.gymapp.data.model.PersonalRecord
import com.gymapp.data.model.SessionType
import com.gymapp.data.model.VisitStats
import com.gymapp.data.model.VolumePoint
import com.gymapp.data.model.WeeklyActivity
import com.gymapp.data.model.WorkoutLogEntry
import com.gymapp.data.model.WorkoutSession
import com.gymapp.data.repository.CheckInRepository
import com.gymapp.data.repository.ProgressRepository
import com.gymapp.domain.intent.LogIntent
import com.gymapp.domain.state.LogData
import com.gymapp.domain.state.UiState
import com.gymapp.domain.usecase.GetLogDataUseCase
import com.gymapp.domain.usecase.LogDefaults
import com.gymapp.domain.usecase.LogSessionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LogViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val lastSession = WorkoutSession(
        id = "s_1", name = "Push day", performedAt = 0L, durationMin = 60,
        volumeKg = 1000.0, totalSets = 10, kcal = 300, prCount = 0, type = SessionType.GYM,
    )

    // getVisitStats returns streak 4; logSession returns the ticked-up 5.
    private val progress = FakeProgressRepository(recent = lastSession, afterLog = stats(streak = 5))
    private val checkIn = FakeCheckInRepository(stats(streak = 4))

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = LogViewModel(
        getLogData = GetLogDataUseCase(progress, checkIn),
        logSession = LogSessionUseCase(progress),
    )

    private fun loaded(vm: LogViewModel): LogData = (vm.state.value as UiState.Success).data

    @Test
    fun `load seeds the three types, default duration and current streak`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        val data = loaded(vm)
        assertEquals(3, data.types.size)
        assertTrue(data.types.contains(SessionType.GYM))
        assertNull(data.selectedType)
        assertEquals(LogDefaults.DURATION_DEFAULT, data.durationMin)
        assertEquals(4, data.currentStreak)
        assertFalse(data.justLogged)
    }

    @Test
    fun `incrementing duration clamps at the max`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        repeat(100) { vm.onIntent(LogIntent.IncrementDuration) }
        assertEquals(LogDefaults.DURATION_MAX, loaded(vm).durationMin)
    }

    @Test
    fun `submitting a selected type ticks the streak and shows the confirmation`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onIntent(LogIntent.SelectType(SessionType.CARDIO))
        vm.onIntent(LogIntent.Submit)
        advanceUntilIdle()
        val data = loaded(vm)
        assertTrue(data.justLogged)
        assertEquals(5, data.currentStreak)
        assertEquals(SessionType.CARDIO, data.selectedType)
    }

    @Test
    fun `log another resets the form back to defaults`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onIntent(LogIntent.SelectType(SessionType.GYM))
        vm.onIntent(LogIntent.Submit)
        advanceUntilIdle()
        vm.onIntent(LogIntent.LogAnother)
        val data = loaded(vm)
        assertFalse(data.justLogged)
        assertNull(data.selectedType)
        assertEquals(LogDefaults.DURATION_DEFAULT, data.durationMin)
    }

    @Test
    fun `repeat last prefills the type and duration from the recent session`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onIntent(LogIntent.RepeatLast)
        val data = loaded(vm)
        assertEquals(SessionType.GYM, data.selectedType)
        assertEquals(60, data.durationMin)
    }

    private companion object {
        fun stats(streak: Int) = VisitStats(currentStreak = streak, totalVisits = 80, visitsThisMonth = 10, lastVisit = null)
    }
}

private class FakeProgressRepository(
    private val recent: WorkoutSession,
    private val afterLog: VisitStats,
) : ProgressRepository {
    override suspend fun getWorkoutLogs(): List<WorkoutLogEntry> = emptyList()
    override suspend fun getRecentSession(): WorkoutSession = recent
    override suspend fun getPersonalRecords(): List<PersonalRecord> = emptyList()
    override suspend fun getVolumeTrend(days: Int): List<VolumePoint> = emptyList()
    override suspend fun getBodyMetrics(): List<BodyMetric> = emptyList()
    override suspend fun getAchievements(): List<Achievement> = emptyList()
    override suspend fun logSession(type: SessionType, durationMin: Int): VisitStats = afterLog
}

private class FakeCheckInRepository(private val stats: VisitStats) : CheckInRepository {
    override suspend fun getCheckIns(): List<CheckIn> = emptyList()
    override suspend fun getVisitStats(): VisitStats = stats
    override suspend fun getWeeklyActivity(): WeeklyActivity = WeeklyActivity(emptyList(), 0, 0)
}
