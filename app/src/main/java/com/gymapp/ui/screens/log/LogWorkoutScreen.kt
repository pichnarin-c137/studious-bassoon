package com.gymapp.ui.screens.log

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LogForm(data: LogData, onIntent: (LogIntent) -> Unit) {
    ScreenContainer {
        OverlineLabel(stringResource(R.string.log_title))
        Text(
            text = stringResource(R.string.log_streak_line, data.currentStreak),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Hairline()

        OverlineLabel(stringResource(R.string.log_question))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            data.types.forEach { type ->
                TypeChip(
                    label = stringResource(type.labelRes()),
                    selected = type == data.selectedType,
                    onClick = { onIntent(LogIntent.SelectType(type)) },
                )
            }
        }

        OverlineLabel(stringResource(R.string.log_duration))
        DurationStepper(
            durationMin = data.durationMin,
            onLess = { onIntent(LogIntent.DecrementDuration) },
            onMore = { onIntent(LogIntent.IncrementDuration) },
        )

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
                value = data.currentStreak.toString(),
                size = 72.sp,
                color = MaterialTheme.colorScheme.accentInk,
            )
            OverlineLabel(stringResource(R.string.log_day_streak))
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
        }
        Spacer(Modifier.height(Spacing.sm))
        Hairline()
        OutlinedButton(onClick = { onIntent(LogIntent.LogAnother) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.log_another))
        }
    }
}

/** Pill chip — lime border + ink when active (selection ink, never a lime fill). */
@Composable
private fun TypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.accentInk
    val border = if (selected) accent else MaterialTheme.colorScheme.outline
    val fg = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, border, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = 10.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, color = fg)
    }
}

@Composable
private fun DurationStepper(durationMin: Int, onLess: () -> Unit, onMore: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        StepButton("−", enabled = durationMin > LogDefaults.DURATION_MIN, onClick = onLess)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            StatNumber(durationMin.toString(), size = 40.sp)
            Text(
                text = stringResource(R.string.log_min_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp),
            )
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
