package com.gymapp.ui.screens.session

import com.gymapp.data.model.Achievement
import com.gymapp.data.model.BodyMetric
import com.gymapp.data.model.LoggedExercise
import com.gymapp.data.model.PersonalRecord
import com.gymapp.data.model.SessionType
import com.gymapp.data.model.StreakState
import com.gymapp.data.model.TrainingDay
import com.gymapp.data.model.VisitStats
import com.gymapp.data.model.VolumePoint
import com.gymapp.data.model.WorkoutLogEntry
import com.gymapp.data.model.WorkoutSession
import com.gymapp.data.repository.ProgressRepository
import com.gymapp.domain.intent.SessionIntent
import com.gymapp.domain.usecase.LogDetailedSessionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val streak = StreakState(weeklyTarget = 4, sessionsThisWeek = 4, weekStreak = 6, freezesAvailable = 1)

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = SessionViewModel(LogDetailedSessionUseCase(FakeRepo(streak)))

    @Test
    fun `adding sets groups them by exercise in first-seen order`() {
        val vm = viewModel()
        vm.onIntent(SessionIntent.AddSet("Bench press", 8, 60.0))
        vm.onIntent(SessionIntent.AddSet("Bench press", 8, 60.0))
        vm.onIntent(SessionIntent.AddSet("Squat", 5, 80.0))
        val groups = vm.state.value.sets.grouped()
        assertEquals(2, groups.size)
        assertEquals("Bench press", groups[0].name)
        assertEquals(2, groups[0].sets.size)
        assertEquals("Squat", groups[1].name)
        assertEquals(1, groups[1].sets.size)
    }

    @Test
    fun `ticks advance elapsed and count a started rest down to zero`() {
        val vm = viewModel()
        repeat(3) { vm.onIntent(SessionIntent.Tick) }
        assertEquals(3L, vm.state.value.elapsedSec)
        vm.onIntent(SessionIntent.StartRest(2))
        assertTrue(vm.state.value.resting)
        vm.onIntent(SessionIntent.Tick)
        vm.onIntent(SessionIntent.Tick)
        assertEquals(0, vm.state.value.restRemainingSec)
        assertEquals(5L, vm.state.value.elapsedSec)
    }

    @Test
    fun `duration rounds elapsed seconds up to whole minutes`() {
        val vm = viewModel()
        repeat(61) { vm.onIntent(SessionIntent.Tick) }
        assertEquals(2, vm.state.value.durationMin)
    }

    @Test
    fun `finishing logs the session and flips to the confirmation`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onIntent(SessionIntent.AddSet("Deadlift", 5, 100.0))
        assertNull(vm.state.value.result)
        vm.onIntent(SessionIntent.Finish("felt strong"))
        runCurrent()
        val result = vm.state.value.result
        assertNotNull(result)
        assertEquals(6, result!!.weekStreak)
    }
}

/** Minimal repository double — only [logDetailedSession] is exercised; the rest are unused. */
private class FakeRepo(private val streak: StreakState) : ProgressRepository {
    override suspend fun getWorkoutLogs(): List<WorkoutLogEntry> = emptyList()
    override suspend fun getRecentSession(): WorkoutSession = error("unused")
    override suspend fun getPersonalRecords(): List<PersonalRecord> = emptyList()
    override suspend fun getVolumeTrend(days: Int): List<VolumePoint> = emptyList()
    override suspend fun getTrainingDays(days: Int): List<TrainingDay> = emptyList()
    override suspend fun getVisitStats(): VisitStats = VisitStats(0, 0, null)
    override suspend fun getBodyMetrics(): List<BodyMetric> = emptyList()
    override suspend fun getAchievements(): List<Achievement> = emptyList()
    override suspend fun getStreakState(): StreakState = streak
    override suspend fun setWeeklyTarget(target: Int): StreakState = streak
    override suspend fun logSession(type: SessionType, durationMin: Int): StreakState = streak
    override suspend fun logDetailedSession(
        type: SessionType,
        durationMin: Int,
        exercises: List<LoggedExercise>,
        note: String?,
    ): StreakState = streak
}
