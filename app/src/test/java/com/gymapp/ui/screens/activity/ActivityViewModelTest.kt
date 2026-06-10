package com.gymapp.ui.screens.activity

import com.gymapp.data.model.ActivityFeedItem
import com.gymapp.data.model.Announcement
import com.gymapp.data.model.FeedActor
import com.gymapp.data.model.SessionType
import com.gymapp.data.repository.ActivityRepository
import com.gymapp.domain.intent.ActivityIntent
import com.gymapp.domain.state.ActivityData
import com.gymapp.domain.state.UiState
import com.gymapp.domain.usecase.GetActivityDataUseCase
import com.gymapp.domain.usecase.ToggleKudosUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val item = ActivityFeedItem(
        id = "af_1", actor = FeedActor("f_1", "Vibol Sok", null),
        sessionType = SessionType.GYM, durationMin = 50, performedAt = 0L,
        kudosCount = 3, youGaveKudos = false,
    )
    private val announcement = Announcement("an_1", "Hours", "Open 6am.", 0L)

    private val repo = FakeActivityRepository(listOf(announcement), listOf(item))

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = ActivityViewModel(
        getActivityData = GetActivityDataUseCase(repo),
        toggleKudosUseCase = ToggleKudosUseCase(repo),
    )

    private fun loaded(vm: ActivityViewModel): ActivityData = (vm.state.value as UiState.Success).data

    @Test
    fun `load seeds announcements and feed`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        val data = loaded(vm)
        assertEquals(1, data.announcements.size)
        assertEquals(1, data.feed.size)
        assertFalse(data.feed.first().youGaveKudos)
        assertEquals(3, data.feed.first().kudosCount)
    }

    @Test
    fun `giving kudos flips the heart and bumps the count`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onIntent(ActivityIntent.ToggleKudos("af_1"))
        advanceUntilIdle()
        val feedItem = loaded(vm).feed.first()
        assertTrue(feedItem.youGaveKudos)
        assertEquals(4, feedItem.kudosCount)
    }

    @Test
    fun `toggling kudos twice returns to the baseline`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onIntent(ActivityIntent.ToggleKudos("af_1"))
        vm.onIntent(ActivityIntent.ToggleKudos("af_1"))
        advanceUntilIdle()
        val feedItem = loaded(vm).feed.first()
        assertFalse(feedItem.youGaveKudos)
        assertEquals(3, feedItem.kudosCount)
    }
}

private class FakeActivityRepository(
    private val announcements: List<Announcement>,
    private val feed: List<ActivityFeedItem>,
) : ActivityRepository {
    override suspend fun getAnnouncements(): List<Announcement> = announcements
    override suspend fun getFeed(): List<ActivityFeedItem> = feed
    override suspend fun toggleKudos(itemId: String, give: Boolean) = Unit
}
