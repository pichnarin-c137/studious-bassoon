package com.gymapp.domain.usecase

import com.gymapp.data.model.SessionType
import com.gymapp.data.model.VisitStats
import com.gymapp.data.repository.ProgressRepository
import javax.inject.Inject

/** Records a quick-logged session and returns the updated visit stats (the streak ticks once/day). */
class LogSessionUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
) {
    suspend operator fun invoke(type: SessionType, durationMin: Int): VisitStats =
        progressRepository.logSession(type, durationMin)
}
