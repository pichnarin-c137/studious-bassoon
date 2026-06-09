package com.gymapp.data.model

/** How busy the gym is right now — drives the Home "right now" indicator (walk-in timing). */
enum class BusyLevel { QUIET, MODERATE, BUSY }

/** One open hour's relative load (0–100); [current] marks the present hour. */
data class HourLoad(
    val hour: Int,
    val load: Int,
    val current: Boolean,
)

data class GymBusyness(
    val level: BusyLevel,
    val hourly: List<HourLoad>,
)

/** Branch status shown on Home: is it open, when does it close, and how busy is it now. */
data class BranchStatus(
    val name: String,
    val openNow: Boolean,
    val closesAt: Long,
    val busyness: GymBusyness,
)
