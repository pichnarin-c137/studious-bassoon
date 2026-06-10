package com.gymapp.data.repository

import com.gymapp.data.api.GymApi
import com.gymapp.data.model.ActivityFeedItem
import com.gymapp.data.model.Announcement
import com.gymapp.data.model.KudosRequest
import javax.inject.Inject

interface ActivityRepository {
    suspend fun getAnnouncements(): List<Announcement>
    suspend fun getFeed(): List<ActivityFeedItem>
    suspend fun toggleKudos(itemId: String, give: Boolean)
}

class ActivityRepositoryImpl @Inject constructor(
    private val api: GymApi,
) : ActivityRepository {
    override suspend fun getAnnouncements(): List<Announcement> = api.getAnnouncements()
    override suspend fun getFeed(): List<ActivityFeedItem> = api.getActivityFeed()
    override suspend fun toggleKudos(itemId: String, give: Boolean) =
        api.toggleKudos(KudosRequest(itemId, give))
}
