package com.gymapp.data.model

import kotlinx.serialization.Serializable

/** A single performed set within an exercise. [weightKg] is null for a bodyweight set. */
@Serializable
data class SetEntry(
    val reps: Int,
    val weightKg: Double? = null,
)

/** One exercise performed in a live session, with its sets in the order they were logged. */
@Serializable
data class LoggedExercise(
    val name: String,
    val sets: List<SetEntry>,
)

/** Request body for the detailed (live-session) log: type + measured duration + the work done. */
data class DetailedLogRequest(
    val type: SessionType,
    val durationMin: Int,
    val exercises: List<LoggedExercise>,
    val note: String?,
)

/**
 * A completed session as persisted by [com.gymapp.data.local.WorkoutStore] — it survives process
 * death. A quick-log persists with no [exercises]; a live session carries its per-set detail + [note].
 */
@Serializable
data class StoredSession(
    val id: String,
    val type: SessionType,
    val durationMin: Int,
    val performedAt: Long,
    val exercises: List<LoggedExercise> = emptyList(),
    val note: String? = null,
)

/**
 * Maps a persisted session to the UI [WorkoutSession] summary. Volume + set totals are computed from
 * the real entries (honest — zero for a quick-log), never fabricated; PRs/kcal land in a later pass.
 */
fun StoredSession.toWorkoutSession(): WorkoutSession {
    val volume = exercises.sumOf { ex -> ex.sets.sumOf { (it.weightKg ?: 0.0) * it.reps } }
    val totalSets = exercises.sumOf { it.sets.size }
    val title = exercises.firstOrNull()?.name ?: when (type) {
        SessionType.GYM -> "Gym session"
        SessionType.CARDIO -> "Cardio session"
        SessionType.BODYWEIGHT -> "Bodyweight session"
    }
    return WorkoutSession(
        id = id,
        name = title,
        performedAt = performedAt,
        durationMin = durationMin,
        volumeKg = volume,
        totalSets = totalSets,
        kcal = 0,
        prCount = 0,
        type = type,
    )
}
