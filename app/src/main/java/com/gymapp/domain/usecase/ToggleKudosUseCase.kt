package com.gymapp.domain.usecase

import com.gymapp.data.repository.ActivityRepository
import javax.inject.Inject

/** Gives or takes back a kudos on a feed item. The ViewModel updates state optimistically. */
class ToggleKudosUseCase @Inject constructor(
    private val activityRepository: ActivityRepository,
) {
    suspend operator fun invoke(itemId: String, give: Boolean) =
        activityRepository.toggleKudos(itemId, give)
}
