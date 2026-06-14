package com.gymapp.domain.usecase

import com.gymapp.data.model.SessionType
import com.gymapp.data.model.TimeRange
import com.gymapp.data.model.TrainingDay
import com.gymapp.data.model.TypeCount
import com.gymapp.data.repository.ProgressRepository
import com.gymapp.domain.state.ProgressData
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * Builds the Progress consistency dashboard for a window: fans out the per-day training history, the
 * kind streak (weekly target + run of met weeks), and the most recent session, then derives the time-invested totals
 * and the session-type mix from the honest quick-log facts. No fabricated volume.
 */
class GetProgressDataUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
) {
    suspend operator fun invoke(range: TimeRange): ProgressData = coroutineScope {
        val daysAsync = async { progressRepository.getTrainingDays(range.days) }
        val streakAsync = async { progressRepository.getStreakState() }
        val sessionAsync = async { progressRepository.getRecentSession() }
        val prsAsync = async { progressRepository.getPersonalRecords() }
        val volumeAsync = async { progressRepository.getVolumeTrend(range.days) }

        val days = daysAsync.await()
        val trained = days.filter { it.trained }
        val totalMinutes = trained.sumOf { it.durationMin }

        ProgressData(
            range = range,
            streak = streakAsync.await(),
            days = days,
            sessionCount = trained.size,
            totalMinutes = totalMinutes,
            avgMinutes = if (trained.isEmpty()) 0 else totalMinutes / trained.size,
            typeMix = typeMix(trained),
            lastSession = sessionAsync.await(),
            personalRecords = prsAsync.await(),
            volumeTrend = volumeAsync.await(),
        )
    }

    /** Counts trained sessions by type, in a stable [SessionType] order, dropping empty buckets. */
    private fun typeMix(trained: List<TrainingDay>): List<TypeCount> =
        SessionType.entries
            .map { type -> TypeCount(type, trained.count { it.type == type }) }
            .filter { it.count > 0 }
}
