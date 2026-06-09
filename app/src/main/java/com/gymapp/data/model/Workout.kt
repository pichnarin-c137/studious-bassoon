package com.gymapp.data.model

/** A completed training session — the Strava-style summary shown on Progress (and later Activity). */
data class WorkoutSession(
    val id: String,
    val name: String,
    val performedAt: Long,
    val durationMin: Int,
    val volumeKg: Double,
    val totalSets: Int,
    val kcal: Int,
    val prCount: Int,
)

/** A personal record for one exercise, with the improvement over the previous best (kg). */
data class PersonalRecord(
    val exercise: String,
    val bestKg: Double,
    val improvementKg: Double,
)

/** One point on the volume-lifted trend: total kg moved on [date]. */
data class VolumePoint(
    val date: Long,
    val volumeKg: Double,
)

/** A single day in the current-week training chart. The weekday label is derived from [date]. */
data class DaySession(
    val date: Long,
    val done: Boolean,
)

/** This week's sessions vs. a personal target, for the Home week chart. */
data class WeeklyActivity(
    val days: List<DaySession>,
    val sessionsDone: Int,
    val sessionsTarget: Int,
)

/** Selectable ranges for the Progress volume trend. */
enum class TimeRange(val days: Int) {
    LAST_7_DAYS(7),
    LAST_30_DAYS(30),
    LAST_90_DAYS(90),
}
