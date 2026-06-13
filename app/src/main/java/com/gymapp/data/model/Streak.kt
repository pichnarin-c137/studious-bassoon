package com.gymapp.data.model

/**
 * The kind streak. The unit is the **week**, not the day: a week counts once you hit your
 * [weeklyTarget], and [weekStreak] is the run of consecutive met weeks — so a rest day never
 * breaks anything. [freezesAvailable] protects the run when a week is missed (banked, earned on
 * milestone weeks; the missed-week consume is a backend stub in v1, see `FakeGymApi`).
 */
data class StreakState(
    val weeklyTarget: Int,       // sessions/week the member set (default 4)
    val sessionsThisWeek: Int,   // logged in the current Mon–Sun week (Asia/Phnom_Penh)
    val weekStreak: Int,         // consecutive weeks the target was met — the kind streak
    val freezesAvailable: Int,   // banked freezes that protect a missed week
) {
    val goalMet: Boolean get() = sessionsThisWeek >= weeklyTarget
    val sessionsRemaining: Int get() = (weeklyTarget - sessionsThisWeek).coerceAtLeast(0)
}

/** Body for changing the member's weekly session target. */
data class WeeklyTargetRequest(val target: Int)
