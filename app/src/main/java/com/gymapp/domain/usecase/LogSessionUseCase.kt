package com.gymapp.domain.usecase

import com.gymapp.data.model.SessionType
import com.gymapp.data.model.StreakState
import com.gymapp.data.repository.ProgressRepository
import javax.inject.Inject

/** Records a quick-logged session and returns the updated streak (the weekly goal moves forward). */
class LogSessionUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
) {
    suspend operator fun invoke(type: SessionType, durationMin: Int): StreakState =
        progressRepository.logSession(type, durationMin)
}
