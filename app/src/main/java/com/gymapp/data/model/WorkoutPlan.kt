package com.gymapp.data.model

/** One exercise in a [WorkoutPlan] template, with its target volume (sets × reps). */
data class PlannedExercise(
    val name: String,
    val targetSets: Int,
    val targetReps: Int,
)

/**
 * An owner-provisioned workout template a member can pull into a live session as an agenda. Guidance
 * only — the member logs their own real sets against it; the plan itself captures no performed data.
 */
data class WorkoutPlan(
    val id: String,
    val name: String,
    val exercises: List<PlannedExercise>,
)
