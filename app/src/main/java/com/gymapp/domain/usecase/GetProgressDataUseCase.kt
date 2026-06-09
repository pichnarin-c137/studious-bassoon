package com.gymapp.domain.usecase

import com.gymapp.data.model.TimeRange
import com.gymapp.data.model.VolumePoint
import com.gymapp.data.repository.ProgressRepository
import com.gymapp.domain.state.ProgressData
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetProgressDataUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
) {
    suspend operator fun invoke(range: TimeRange): ProgressData = coroutineScope {
        val trend = async { progressRepository.getVolumeTrend(range.days) }
        val session = async { progressRepository.getRecentSession() }
        val records = async { progressRepository.getPersonalRecords() }
        val volumeTrend = trend.await()
        ProgressData(
            range = range,
            volumeTrend = volumeTrend,
            volumeDeltaPct = deltaPct(volumeTrend),
            recentSession = session.await(),
            personalRecords = records.await(),
        )
    }

    /** Percent change from the first to the last point of the trend window. */
    private fun deltaPct(trend: List<VolumePoint>): Double {
        val first = trend.firstOrNull()?.volumeKg ?: return 0.0
        val last = trend.lastOrNull()?.volumeKg ?: return 0.0
        if (first <= 0.0) return 0.0
        return (last - first) / first * 100.0
    }
}
