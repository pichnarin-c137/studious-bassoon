package com.gymapp.domain.usecase

import com.gymapp.data.model.StreakState
import com.gymapp.data.repository.ProgressRepository
import javax.inject.Inject

/** Changes the member's weekly session target and returns the recomputed kind streak. */
class SetWeeklyTargetUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
) {
    suspend operator fun invoke(target: Int): StreakState =
        progressRepository.setWeeklyTarget(target)
}
