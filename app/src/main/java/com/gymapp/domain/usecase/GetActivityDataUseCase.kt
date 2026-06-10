package com.gymapp.domain.usecase

import com.gymapp.data.repository.ActivityRepository
import com.gymapp.domain.state.ActivityData
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/** Loads the Activity screen: owner announcements (pinned) + the friends' workout feed, in parallel. */
class GetActivityDataUseCase @Inject constructor(
    private val activityRepository: ActivityRepository,
) {
    suspend operator fun invoke(): ActivityData = coroutineScope {
        val announcements = async { activityRepository.getAnnouncements() }
        val feed = async { activityRepository.getFeed() }
        ActivityData(
            announcements = announcements.await(),
            feed = feed.await(),
        )
    }
}
