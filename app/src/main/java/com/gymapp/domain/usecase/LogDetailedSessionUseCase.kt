package com.gymapp.domain.usecase

import com.gymapp.data.model.LoggedExercise
import com.gymapp.data.model.SessionType
import com.gymapp.data.model.StreakState
import com.gymapp.data.repository.ProgressRepository
import javax.inject.Inject

/** Logs a detailed live session (per-set work + note) and returns the moved-forward weekly streak. */
class LogDetailedSessionUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
) {
    suspend operator fun invoke(
        type: SessionType,
        durationMin: Int,
        exercises: List<LoggedExercise>,
        note: String?,
    ): StreakState = progressRepository.logDetailedSession(type, durationMin, exercises, note)
}
