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
    /** Set by the quick-log fast-path; null for legacy/seed sessions. Powers "repeat last session". */
    val type: SessionType? = null,
)

/** The quick-log categories on the Log fast-path. Cambodian iron-gym focused: no class/combat. */
enum class SessionType { GYM, CARDIO, BODYWEIGHT }

/** Body of the quick-log request — a session type plus how long it ran. */
data class LogSessionRequest(val type: SessionType, val durationMin: Int)

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

/**
 * One day in the consistency history. [trained] drives the per-day mark on the Progress strip;
 * [type] and [durationMin] (zero on rest days) feed the training-mix and time-invested totals. Built
 * from honest quick-log facts only — no set/volume data.
 */
data class TrainingDay(
    val date: Long,
    val trained: Boolean,
    val type: SessionType? = null,
    val durationMin: Int = 0,
)

/** How many sessions of one [type] fell in the active Progress window — for the training-mix bar. */
data class TypeCount(
    val type: SessionType,
    val count: Int,
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
