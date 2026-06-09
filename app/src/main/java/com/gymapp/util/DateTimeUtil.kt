package com.gymapp.util

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * All timestamps are stored as UTC epoch milliseconds and rendered in Cambodia local time.
 * Date format: `dd MMM yyyy`, time format: `h:mm a` (spec).
 *
 * Relative strings are intentionally English-only for now; localizing them belongs with the
 * real backend pass (they'd move to plurals in strings.xml).
 */
object DateTimeUtil {
    private val zone = TimeZone.of("Asia/Phnom_Penh")
    private val months = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
    )
    private val weekdayInitials = arrayOf("M", "T", "W", "T", "F", "S", "S")

    private fun localDateTime(epochMillis: Long): LocalDateTime =
        Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(zone)

    /** e.g. `08 Jun 2025`. */
    fun formatDate(epochMillis: Long): String {
        val d = localDateTime(epochMillis)
        return "%02d %s %d".format(d.dayOfMonth, months[d.monthNumber - 1], d.year)
    }

    /** e.g. `7:30 AM`. */
    fun formatTime(epochMillis: Long): String {
        val d = localDateTime(epochMillis)
        val hour12 = if (d.hour % 12 == 0) 12 else d.hour % 12
        val marker = if (d.hour < 12) "AM" else "PM"
        return "%d:%02d %s".format(hour12, d.minute, marker)
    }

    /** e.g. `08 Jun 2025, 10:00 AM`. */
    fun formatDateTime(epochMillis: Long): String =
        "${formatDate(epochMillis)}, ${formatTime(epochMillis)}"

    /** e.g. `Expires 30 Jun 2025`. */
    fun formatExpiry(epochMillis: Long): String = "Expires ${formatDate(epochMillis)}"

    /** `Today, 7:30 AM` / `Yesterday` / `3 days ago`, falling back to a full date. */
    fun relative(epochMillis: Long): String {
        val today = Clock.System.now().toLocalDateTime(zone).date
        val date = localDateTime(epochMillis).date
        return when (val daysAgo = date.daysUntil(today)) {
            0 -> "Today, ${formatTime(epochMillis)}"
            1 -> "Yesterday"
            in 2..6 -> "$daysAgo days ago"
            else -> formatDate(epochMillis)
        }
    }

    /** Whole days from now until [epochMillis] in Cambodia time (negative if already past). */
    fun daysFromNow(epochMillis: Long): Int {
        val today = Clock.System.now().toLocalDateTime(zone).date
        val target = localDateTime(epochMillis).date
        return today.daysUntil(target)
    }

    /** Single-letter weekday initial (Mon–Sun). English-only for now, like [relative]. */
    fun weekdayInitial(epochMillis: Long): String =
        weekdayInitials[localDateTime(epochMillis).dayOfWeek.ordinal]

    /** Current hour-of-day (0–23) in Cambodia time. */
    fun currentHour(): Int = Clock.System.now().toLocalDateTime(zone).hour

    /** Epoch millis for today at [hour]:[minute] Cambodia local time. */
    fun todayAt(hour: Int, minute: Int = 0): Long {
        val today = Clock.System.now().toLocalDateTime(zone).date
        val dt = LocalDateTime(today.year, today.monthNumber, today.dayOfMonth, hour, minute)
        return dt.toInstant(zone).toEpochMilliseconds()
    }

    /** Today's weekday index, 0 = Monday … 6 = Sunday (Cambodia time). */
    fun currentWeekdayIndex(): Int =
        Clock.System.now().toLocalDateTime(zone).date.dayOfWeek.ordinal

    /** Epoch millis for Monday 00:00 of the current week (Cambodia time). */
    fun startOfWeekMillis(): Long {
        val today = Clock.System.now().toLocalDateTime(zone).date
        val monday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
        val dt = LocalDateTime(monday.year, monday.monthNumber, monday.dayOfMonth, 0, 0)
        return dt.toInstant(zone).toEpochMilliseconds()
    }
}
