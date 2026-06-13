package com.gymapp.ui.screens.log

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.gymapp.data.model.WorkoutSession
import com.gymapp.domain.intent.LogIntent
import com.gymapp.domain.state.LogData
import com.gymapp.domain.usecase.LogDefaults
import com.gymapp.ui.components.Hairline
import com.gymapp.ui.components.OverlineLabel
import com.gymapp.ui.components.ScreenContainer
import com.gymapp.ui.components.StatNumber
import com.gymapp.ui.components.UiStateContent
import com.gymapp.ui.theme.Spacing
import com.gymapp.ui.theme.accentInk

/** Quick-pick durations beside the stepper — the common session lengths, one tap each. */
private val DURATION_PRESETS = listOf(30, 45, 60, 90)

@Composable
fun LogWorkoutScreen(viewModel: LogViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    UiStateContent(state, onRetry = { viewModel.onIntent(LogIntent.Retry) }) { data ->
        if (data.justLogged) {
            LogConfirmation(data, onIntent = viewModel::onIntent)
        } else {
            LogForm(data, onIntent = viewModel::onIntent)
        }
    }
}

/** The sub-10-second daily log: pick a type, nudge the duration, tap Log. */
@Composable
private fun LogForm(data: LogData, onIntent: (LogIntent) -> Unit) {
    ScreenContainer {
        // Header: the motivating week-progress is given weight — lime pips, not muted text.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OverlineLabel(stringResource(R.string.log_title))
            WeekPips(done = data.streak.sessionsThisWeek, target = data.streak.weeklyTarget)
        }
        Text(
            text = if (data.streak.goalMet) {
                stringResource(R.string.streak_goal_met)
            } else {
                stringResource(
                    R.string.log_week_progress,
                    data.streak.sessionsThisWeek,
                    data.streak.weeklyTarget,
                )
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (data.streak.goalMet) FontWeight.Medium else FontWeight.Normal,
            color = if (data.streak.goalMet) {
                MaterialTheme.colorScheme.accentInk
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )

        Hairline()

        // The primary choice — icon tiles, not text pills, so it reads as the hero decision.
        OverlineLabel(stringResource(R.string.log_question))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            data.types.forEach { type ->
                TypeTile(
                    icon = type.iconRes(),
                    label = stringResource(type.labelRes()),
                    selected = type == data.selectedType,
                    onClick = { onIntent(LogIntent.SelectType(type)) },
                )
            }
        }

        // Duration as the mono hero — the big number anchors the screen — plus one-tap presets.
        OverlineLabel(stringResource(R.string.log_duration))
        DurationHero(
            durationMin = data.durationMin,
            onLess = { onIntent(LogIntent.DecrementDuration) },
            onMore = { onIntent(LogIntent.IncrementDuration) },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            DURATION_PRESETS.forEach { preset ->
                PresetChip(
                    value = preset,
                    selected = preset == data.durationMin,
                    onClick = { onIntent(LogIntent.SetDuration(preset)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        data.lastSession?.let { last ->
            Hairline()
            RepeatLastRow(last, onClick = { onIntent(LogIntent.RepeatLast) })
        }

        Spacer(Modifier.height(Spacing.sm))

        Button(
            onClick = { onIntent(LogIntent.Submit) },
            enabled = data.selectedType != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.log_submit))
        }
    }
}

/** Flat confirmation: the streak is the hero, in lime mono; back arrow leaves, "Log another" resets. */
@Composable
private fun LogConfirmation(data: LogData, onIntent: (LogIntent) -> Unit) {
    ScreenContainer {
        Spacer(Modifier.height(Spacing.xl))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            StatNumber(
                value = data.streak.weekStreak.toString(),
                size = 72.sp,
                color = MaterialTheme.colorScheme.accentInk,
            )
            OverlineLabel(stringResource(R.string.log_week_streak))
            data.selectedType?.let { type ->
                Text(
                    text = stringResource(
                        R.string.log_logged_summary,
                        stringResource(type.labelRes()),
                        data.durationMin,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            // Kind celebration: once the week's target is cleared, say so in lime — never guilt.
            if (data.streak.goalMet) {
                Text(
                    text = stringResource(R.string.streak_goal_met),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.accentInk,
                )
            }
        }
        Spacer(Modifier.height(Spacing.sm))
        Hairline()
        OutlinedButton(onClick = { onIntent(LogIntent.LogAnother) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.log_another))
        }
    }
}

/** This week's progress as pips: one filled lime dot per logged session, hollow for the rest of the goal. */
@Composable
private fun WeekPips(done: Int, target: Int) {
    val accent = MaterialTheme.colorScheme.accentInk
    val miss = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.30f)
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(target.coerceAtLeast(1)) { index ->
            Box(Modifier.size(9.dp).clip(CircleShape).background(if (index < done) accent else miss))
        }
    }
}

/** A type as a tappable tile: tinted glyph over its label, lime ink + lime border when active (never a fill). */
@Composable
private fun RowScope.TypeTile(
    @DrawableRes icon: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.accentInk
    val border = if (selected) accent else MaterialTheme.colorScheme.outline
    val fg = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = fg, modifier = Modifier.size(26.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, color = fg)
    }
}

/** The big mono minutes count flanked by the −/+ steppers — the focal element of the form. */
@Composable
private fun DurationHero(durationMin: Int, onLess: () -> Unit, onMore: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xl, Alignment.CenterHorizontally),
    ) {
        StepButton("−", enabled = durationMin > LogDefaults.DURATION_MIN, onClick = onLess)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            StatNumber(durationMin.toString(), size = 56.sp)
            OverlineLabel(stringResource(R.string.log_min_label))
        }
        StepButton("+", enabled = durationMin < LogDefaults.DURATION_MAX, onClick = onMore)
    }
}

@Composable
private fun StepButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    val color = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, style = MaterialTheme.typography.titleLarge, color = color)
    }
}

/** One-tap preset minutes — lime ink + border on the current value (active selection), matching the type tiles. */
@Composable
private fun PresetChip(value: Int, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val accent = MaterialTheme.colorScheme.accentInk
    val border = if (selected) accent else MaterialTheme.colorScheme.outline
    val fg = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, border, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Text(value.toString(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, color = fg)
    }
}

/** Secondary shortcut — kept off the lime budget (only the chip selection + CTA carry lime). */
@Composable
private fun RepeatLastRow(last: WorkoutSession, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Text(
            text = stringResource(R.string.log_repeat_last),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = stringResource(
                R.string.log_session_summary,
                last.type?.let { stringResource(it.labelRes()) } ?: last.name,
                last.durationMin,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
