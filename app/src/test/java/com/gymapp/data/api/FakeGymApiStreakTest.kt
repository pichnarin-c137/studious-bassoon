package com.gymapp.data.api

import com.gymapp.data.model.LogSessionRequest
import com.gymapp.data.model.SessionType
import com.gymapp.data.model.WeeklyTargetRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The kind-streak mechanic in the in-memory backend. Assertions are relative (deltas), so they hold
 * regardless of the real weekday that seeds this week's progress.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FakeGymApiStreakTest {

    @Test
    fun `crossing the weekly target bumps the week streak exactly once`() = runTest {
        val api = FakeGymApi()
        val start = api.getStreakState()
        val toCross = start.weeklyTarget - start.sessionsThisWeek
        assertTrue("seed should start below the weekly target", toCross >= 1)
        assertFalse(start.goalMet)

        var last = start
        repeat(toCross) { last = api.logSession(LogSessionRequest(SessionType.GYM, 45)) }
        assertTrue(last.goalMet)
        assertEquals(start.weekStreak + 1, last.weekStreak)

        // Logging more sessions the same week clears nothing new — the streak doesn't double-count.
        val after = api.logSession(LogSessionRequest(SessionType.GYM, 45))
        assertEquals(start.weekStreak + 1, after.weekStreak)
    }

    @Test
    fun `a rest day never reduces the week streak`() = runTest {
        val api = FakeGymApi()
        val first = api.getStreakState()
        val second = api.getStreakState() // no session logged in between
        assertEquals(first.weekStreak, second.weekStreak)
        assertTrue(second.weekStreak >= first.weekStreak)
    }

    @Test
    fun `setting the weekly target changes the goal without touching the streak`() = runTest {
        val api = FakeGymApi()
        val before = api.getStreakState()
        val after = api.setWeeklyTarget(WeeklyTargetRequest(before.weeklyTarget + 1))
        assertEquals(before.weeklyTarget + 1, after.weeklyTarget)
        assertEquals(before.weekStreak, after.weekStreak)
    }
}
