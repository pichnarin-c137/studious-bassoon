package com.gymapp.data.local

import com.gymapp.data.model.LoggedExercise
import com.gymapp.data.model.SessionType
import com.gymapp.data.model.SetEntry
import com.gymapp.data.model.StoredSession
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutAnalyticsTest {

    private val DAY = 86_400_000L

    private fun session(performedAt: Long, vararg exercises: LoggedExercise) =
        StoredSession(id = "s_$performedAt", type = SessionType.GYM, durationMin = 60, performedAt = performedAt, exercises = exercises.toList())

    private fun ex(name: String, vararg sets: Pair<Int, Double?>) =
        LoggedExercise(name, sets.map { SetEntry(reps = it.first, weightKg = it.second) })

    @Test
    fun `personal records track the running best and the size of the last beat`() {
        val sessions = listOf(
            session(3 * DAY, ex("Bench press", 8 to 60.0)),   // newest in store order...
            session(1 * DAY, ex("Bench press", 8 to 50.0)),   // ...but processed oldest-first
        )
        val prs = WorkoutAnalytics.personalRecords(sessions)
        assertEquals(1, prs.size)
        assertEquals("Bench press", prs[0].exercise)
        assertEquals(60.0, prs[0].bestKg, 0.0)
        assertEquals(10.0, prs[0].improvementKg, 0.0) // 60 beat the earlier 50
    }

    @Test
    fun `first ever lift records a personal record with zero improvement`() {
        val prs = WorkoutAnalytics.personalRecords(listOf(session(DAY, ex("Squat", 5 to 100.0))))
        assertEquals(100.0, prs[0].bestKg, 0.0)
        assertEquals(0.0, prs[0].improvementKg, 0.0)
    }

    @Test
    fun `volume trend buckets weight times reps per calendar day`() {
        val sessions = listOf(
            session(DAY + 3_600_000, ex("Bench press", 10 to 50.0)),  // 500, same day as below
            session(DAY + 7_200_000, ex("Squat", 5 to 100.0)),         // 500
            session(2 * DAY, ex("Deadlift", 5 to 120.0)),              // 600, next day
        )
        val trend = WorkoutAnalytics.volumeTrend(sessions)
        assertEquals(2, trend.size)
        assertEquals(1000.0, trend[0].volumeKg, 0.0) // both day-1 sessions summed
        assertEquals(600.0, trend[1].volumeKg, 0.0)
    }

    @Test
    fun `prCount counts only exercises that beat the prior best`() {
        val priorBest = mapOf("Bench press" to 60.0, "Squat" to 100.0)
        val today = listOf(
            ex("Bench press", 8 to 62.5), // new PR
            ex("Squat", 5 to 100.0),      // ties, not a PR
            ex("Pull-ups", 10 to null),   // bodyweight, never a weight PR
        )
        assertEquals(1, WorkoutAnalytics.prCount(today, priorBest))
    }
}
