package com.gymapp.data.local

import com.gymapp.data.model.LoggedExercise
import com.gymapp.data.model.PersonalRecord
import com.gymapp.data.model.StoredSession
import com.gymapp.data.model.VolumePoint

/**
 * Derives Strength facts (PRs + volume) from the honest persisted session history. Pure functions over
 * [StoredSession]s — no Android deps, so they unit-test directly. Only weighted sets set a weight PR;
 * bodyweight sets contribute reps to volume at zero load.
 */
object WorkoutAnalytics {

    private const val DAY = 86_400_000L

    /** Heaviest weight lifted per exercise across all sessions (weighted sets only). */
    fun bestPerExercise(sessions: List<StoredSession>): Map<String, Double> {
        val best = LinkedHashMap<String, Double>()
        sessions.forEach { s -> sessionBests(s).forEach { (name, w) -> best[name] = maxOf(best[name] ?: 0.0, w) } }
        return best
    }

    /**
     * Current best per exercise plus the size of the most recent beat. Processes sessions oldest→newest
     * so [PersonalRecord.improvementKg] reflects the last time the record moved (0.0 if it's the first),
     * sorted heaviest first.
     */
    fun personalRecords(sessions: List<StoredSession>): List<PersonalRecord> {
        val best = LinkedHashMap<String, Double>()
        val improvement = HashMap<String, Double>()
        sessions.sortedBy { it.performedAt }.forEach { s ->
            sessionBests(s).forEach { (name, w) ->
                val prev = best[name]
                if (prev == null || w > prev) {
                    improvement[name] = if (prev == null) 0.0 else w - prev
                    best[name] = w
                }
            }
        }
        return best.map { (name, kg) -> PersonalRecord(name, kg, improvement[name] ?: 0.0) }
            .sortedByDescending { it.bestKg }
    }

    /** Per-day total volume (Σ weight·reps), bucketed by calendar day, oldest→newest. */
    fun volumeTrend(sessions: List<StoredSession>): List<VolumePoint> =
        sessions
            .groupBy { (it.performedAt / DAY) * DAY }
            .map { (day, daySessions) -> VolumePoint(date = day, volumeKg = daySessions.sumOf { volume(it) }) }
            .sortedBy { it.date }

    /** Count of [exercises] whose heaviest set beats the prior best — for tagging a freshly logged session. */
    fun prCount(exercises: List<LoggedExercise>, priorBest: Map<String, Double>): Int =
        exercises.count { ex ->
            val w = ex.sets.mapNotNull { it.weightKg }.maxOrNull()
            w != null && w > (priorBest[ex.name] ?: 0.0)
        }

    private fun sessionBests(s: StoredSession): Map<String, Double> {
        val best = HashMap<String, Double>()
        s.exercises.forEach { ex ->
            val w = ex.sets.mapNotNull { it.weightKg }.maxOrNull() ?: return@forEach
            best[ex.name] = maxOf(best[ex.name] ?: 0.0, w)
        }
        return best
    }

    private fun volume(s: StoredSession): Double =
        s.exercises.sumOf { ex -> ex.sets.sumOf { (it.weightKg ?: 0.0) * it.reps } }
}
