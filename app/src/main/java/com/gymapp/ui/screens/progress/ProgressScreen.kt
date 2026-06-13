package com.gymapp.ui.screens.progress

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymapp.R
import com.gymapp.data.model.TimeRange
import com.gymapp.data.model.WorkoutSession
import com.gymapp.domain.intent.ProgressIntent
import com.gymapp.domain.state.ProgressData
import com.gymapp.ui.components.ConsistencyStrip
import com.gymapp.ui.components.Hairline
import com.gymapp.ui.components.MetricBlock
import com.gymapp.ui.components.OverlineLabel
import com.gymapp.ui.components.ScreenContainer
import com.gymapp.ui.components.StatNumber
import com.gymapp.ui.components.TypeMixBar
import com.gymapp.ui.components.UiStateContent
import com.gymapp.ui.screens.log.labelRes
import com.gymapp.ui.theme.Spacing
import com.gymapp.ui.theme.accentInk
import com.gymapp.util.DateTimeUtil
import java.util.Locale

private fun hours(minutes: Int): String = String.format(Locale.US, "%.1f", minutes / 60.0)

/** Selectable weekly session targets for the inline goal setter (matches the API's accepted range). */
private val WEEKLY_TARGET_OPTIONS = listOf(3, 4, 5, 6)

@StringRes
private fun TimeRange.labelRes(): Int = when (this) {
    TimeRange.LAST_7_DAYS -> R.string.progress_range_7
    TimeRange.LAST_30_DAYS -> R.string.progress_range_30
    TimeRange.LAST_90_DAYS -> R.string.progress_range_90
}

@Composable
fun ProgressScreen(viewModel: ProgressViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    UiStateContent(state, onRetry = { viewModel.onIntent(ProgressIntent.Retry) }) { data ->
        ProgressContent(
            data,
            onSetRange = { viewModel.onIntent(ProgressIntent.SetRange(it)) },
            onSetTarget = { viewModel.onIntent(ProgressIntent.SetWeeklyTarget(it)) },
        )
    }
}

@Composable
private fun ProgressContent(
    data: ProgressData,
    onSetRange: (TimeRange) -> Unit,
    onSetTarget: (Int) -> Unit,
) {
    ScreenContainer {
        OverlineLabel(stringResource(R.string.progress_title))

        // Active range + 7 / 30 / 90 toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(data.range.labelRes()).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                TimeRange.entries.forEach { range ->
                    RangeToggle(
                        label = range.days.toString(),
                        selected = range == data.range,
                        onClick = { onSetRange(range) },
                    )
                }
            }
        }

        Hairline()

        // Consistency hero — the kind streak: weeks of hitting the weekly goal (a rest day never
        // breaks it), this week's progress, banked freezes, and an inline goal setter, over the
        // per-day strip for the window.
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    OverlineLabel(stringResource(R.string.progress_week_streak))
                    StatNumber(data.streak.weekStreak.toString(), size = 44.sp)
                }
                if (data.streak.freezesAvailable > 0) FreezeBadge(data.streak.freezesAvailable)
            }
            Text(
                text = if (data.streak.goalMet) {
                    stringResource(R.string.streak_goal_met)
                } else {
                    stringResource(
                        R.string.progress_weekly_goal,
                        data.streak.sessionsThisWeek,
                        data.streak.weeklyTarget,
                    )
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = if (data.streak.goalMet) {
                    MaterialTheme.colorScheme.accentInk
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            ConsistencyStrip(data.days)
            WeeklyGoalSetter(current = data.streak.weeklyTarget, onSet = onSetTarget)
        }

        Hairline()

        // Time invested over the window — all from honest quick-log facts (showing up + duration).
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            MetricBlock(
                value = data.sessionCount.toString(),
                label = stringResource(R.string.progress_sessions),
                modifier = Modifier.weight(1f),
                valueSize = 24.sp,
                horizontalAlignment = Alignment.Start,
            )
            MetricBlock(
                value = hours(data.totalMinutes),
                label = stringResource(R.string.progress_hours),
                modifier = Modifier.weight(1f),
                valueSize = 24.sp,
                horizontalAlignment = Alignment.CenterHorizontally,
            )
            MetricBlock(
                value = data.avgMinutes.toString(),
                label = stringResource(R.string.progress_avg),
                modifier = Modifier.weight(1f),
                valueSize = 24.sp,
                horizontalAlignment = Alignment.End,
            )
        }

        Hairline()

        // Training mix — neutral proportion bar, off the lime budget.
        if (data.typeMix.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OverlineLabel(stringResource(R.string.progress_training_mix))
                TypeMixBar(segments = data.typeMix.map { stringResource(it.type.labelRes()) to it.count })
            }
            Hairline()
        }

        // Last session — honest summary: type · duration · when (no fabricated volume/sets).
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            OverlineLabel(stringResource(R.string.progress_last_session))
            LastSession(data.lastSession)
        }

        Hairline()

        // Strength — set-level stub; lights up when detailed logging lands.
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            OverlineLabel(stringResource(R.string.progress_strength))
            Text(
                text = stringResource(R.string.progress_strength_stub),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Inline weekly-target setter: tap a number to change how many sessions a week clear the streak. */
@Composable
private fun WeeklyGoalSetter(current: Int, onSet: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OverlineLabel(stringResource(R.string.progress_goal_label))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            WEEKLY_TARGET_OPTIONS.forEach { target ->
                RangeToggle(
                    label = target.toString(),
                    selected = target == current,
                    onClick = { onSet(target) },
                )
            }
        }
    }
}

/** Banked streak freezes — neutral (a safety net, not the next action), snowflake + count. */
@Composable
private fun FreezeBadge(count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_freeze),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OverlineLabel(stringResource(R.string.progress_freeze_label))
    }
}

@Composable
private fun RangeToggle(label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.accentInk else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            Modifier
                .padding(top = 4.dp)
                .height(2.dp)
                .width(if (selected) 18.dp else 0.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.accentInk),
        )
    }
}

@Composable
private fun LastSession(session: WorkoutSession) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(session.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            val relative = DateTimeUtil.relative(session.performedAt)
            val typeLabel = session.type?.let { stringResource(it.labelRes()) }
            Text(
                text = if (typeLabel != null) {
                    stringResource(R.string.progress_session_meta, typeLabel, session.durationMin, relative)
                } else {
                    stringResource(R.string.progress_session_meta_notype, session.durationMin, relative)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (session.prCount > 0) PrInline(session.prCount)
    }
}

@Composable
private fun PrInline(count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Icon(
            painter = painterResource(R.drawable.ic_trophy),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.accentInk,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = stringResource(R.string.progress_prs, count),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.accentInk,
        )
    }
}
