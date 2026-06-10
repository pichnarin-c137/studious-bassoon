package com.gymapp.data.model

/**
 * A friend in the feed. Deliberately slim — name + photo only, never phone/memberCode — so the
 * social graph can't be used to harvest other members' contact details.
 */
data class FeedActor(
    val id: String,
    val name: String,
    val photoUrl: String?,
)

/** A personal record surfaced as a feed milestone. Filled by the set-level logger (slice 3). */
data class FeedPr(
    val exercise: String,
    val valueKg: Double,
)

/**
 * One entry in the friends' activity feed — a workout that happened (post-hoc, friends-only, opt-in).
 * Intentionally carries only type + duration + an optional PR: no exact clock time (rendered
 * relative), no location, no volume/sets. `isYou` marks your own logged sessions (slice 2), which
 * show no self-kudos affordance.
 */
data class ActivityFeedItem(
    val id: String,
    val actor: FeedActor,
    val sessionType: SessionType,
    val durationMin: Int,
    val performedAt: Long,
    val pr: FeedPr? = null,
    val kudosCount: Int,
    val youGaveKudos: Boolean,
    val isYou: Boolean = false,
)

/** Owner-authored notice pinned above the feed (hours, classes, promos). No user-privacy surface. */
data class Announcement(
    val id: String,
    val title: String,
    val body: String,
    val postedAt: Long,
)

/** Body of a kudos toggle — give or take back a kudos on a feed item. */
data class KudosRequest(
    val itemId: String,
    val give: Boolean,
)
