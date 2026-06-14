package com.gymapp.domain.usecase

import com.gymapp.data.model.WorkoutPlan
import com.gymapp.data.repository.ProgressRepository
import javax.inject.Inject

/** Owner-provisioned workout templates a member can pull into a live session. */
class GetWorkoutPlansUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
) {
    suspend operator fun invoke(): List<WorkoutPlan> = progressRepository.getWorkoutPlans()
}
