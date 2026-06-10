package com.gymapp.domain.usecase

import com.gymapp.data.model.SessionType
import com.gymapp.data.repository.CheckInRepository
import com.gymapp.data.repository.ProgressRepository
import com.gymapp.domain.state.LogData
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/** Duration stepper bounds for the quick-log fast-path, shared by the use case and the ViewModel. */
object LogDefaults {
    const val DURATION_DEFAULT = 45
    const val DURATION_STEP = 15
    const val DURATION_MIN = 15
    const val DURATION_MAX = 180
}

/**
 * Seeds the Log fast-path: the selectable session types, a default duration, the most recent
 * session (so "repeat last" can prefill it), and the current streak (shown growing after a log).
 */
class GetLogDataUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val checkInRepository: CheckInRepository,
) {
    suspend operator fun invoke(): LogData = coroutineScope {
        val lastSession = async { progressRepository.getRecentSession() }
        val stats = async { checkInRepository.getVisitStats() }
        LogData(
            types = SessionType.entries,
            selectedType = null,
            durationMin = LogDefaults.DURATION_DEFAULT,
            lastSession = lastSession.await(),
            currentStreak = stats.await().currentStreak,
            justLogged = false,
        )
    }
}
