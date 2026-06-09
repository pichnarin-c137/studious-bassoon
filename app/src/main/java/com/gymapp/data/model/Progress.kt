package com.gymapp.data.model

data class WorkoutLogEntry(
    val id: String,
    val exercise: String,
    val sets: Int,
    val reps: Int,
    val weightKg: Double?,
    val performedAt: Long,
)

data class BodyMetric(
    val recordedAt: Long,
    val weightKg: Double,
    val bodyFatPct: Double?,
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val unlocked: Boolean,
)
