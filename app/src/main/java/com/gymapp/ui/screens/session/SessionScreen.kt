package com.gymapp.ui.screens.session

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymapp.R
import com.gymapp.data.model.PlannedExercise
import com.gymapp.data.model.SessionType
import com.gymapp.data.model.SetEntry
import com.gymapp.domain.intent.SessionIntent
import com.gymapp.ui.components.Hairline
import com.gymapp.ui.components.OverlineLabel
import com.gymapp.ui.components.ScreenContainer
import com.gymapp.ui.components.StatNumber
import com.gymapp.ui.screens.log.iconRes
import com.gymapp.ui.screens.log.labelRes
import com.gymapp.ui.theme.Spacing
import com.gymapp.ui.theme.accentInk
import kotlinx.coroutines.delay

/** Common rest lengths between sets, in seconds. */
private val REST_PRESETS = listOf(60, 90, 120)

/** mm:ss for a duration in seconds. */
private fun clock(totalSec: Long): String = "%d:%02d".format(totalSec / 60, totalSec % 60)

/** Trims a trailing ".0" so a whole-kg weight reads "60", not "60.0". */
private fun fmtKg(kg: Double): String = if (kg % 1.0 == 0.0) kg.toInt().toString() else kg.toString()

@Composable
fun SessionScreen(onExit: () -> Unit, viewModel: SessionViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Drive the clock while the session is live; the loop tears down once it's finished.
    LaunchedEffect(state.result == null) {
        if (state.result == null) {
            while (true) {
                delay(1000)
                viewModel.onIntent(SessionIntent.Tick)
            }
        }
    }

    if (state.result != null) {
        SessionDone(state, onExit)
    } else {
        SessionLive(state, viewModel::onIntent)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SessionLive(state: SessionUiState, onIntent: (SessionIntent) -> Unit) {
    var exercise by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    ScreenContainer {
        // Elapsed mono hero + live dot.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                OverlineLabel(stringResource(R.string.session_elapsed))
                StatNumber(clock(state.elapsedSec), size = 44.sp, color = MaterialTheme.colorScheme.accentInk)
            }
            LiveDot()
        }

        Hairline()

        OverlineLabel(stringResource(R.string.session_type))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            SessionType.entries.forEach { type ->
                TypePill(
                    icon = type.iconRes(),
                    label = stringResource(type.labelRes()),
                    selected = type == state.type,
                    onClick = { onIntent(SessionIntent.SetType(type)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Hairline()

        // Pull an owner plan in as a tappable agenda; tapping an exercise pre-fills the composer.
        if (state.availablePlans.isNotEmpty()) {
            OverlineLabel(stringResource(R.string.session_from_plan))
            val plan = state.selectedPlan
            if (plan == null) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    state.availablePlans.forEach { p ->
                        PlanChip(p.name, selected = false, onClick = { onIntent(SessionIntent.SelectPlan(p)) })
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlanChip(plan.name, selected = true, onClick = {})
                    Text(
                        text = stringResource(R.string.session_plan_freeform),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onIntent(SessionIntent.SelectPlan(null)) },
                    )
                }
                plan.exercises.forEach { pe ->
                    PlanAgendaRow(pe, onClick = { exercise = pe.name; reps = pe.targetReps.toString() })
                }
            }
            Hairline()
        }

        OverlineLabel(stringResource(R.string.session_exercises))
        val groups = state.sets.grouped()
        if (groups.isEmpty()) {
            Text(
                text = stringResource(R.string.session_no_sets),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            groups.forEach { ex -> ExerciseRow(ex.name, ex.sets) }
        }
        SetComposer(
            exercise = exercise, onExercise = { exercise = it },
            reps = reps, onReps = { reps = it },
            weight = weight, onWeight = { weight = it },
            onAdd = {
                val r = reps.toIntOrNull()
                if (exercise.isNotBlank() && r != null && r > 0) {
                    onIntent(SessionIntent.AddSet(exercise.trim(), r, weight.toDoubleOrNull()))
                    reps = "" // keep exercise + weight for fast same-exercise logging
                }
            },
        )

        Hairline()

        OverlineLabel(stringResource(R.string.session_rest))
        if (state.resting) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatNumber(
                    clock(state.restRemainingSec.toLong()),
                    size = 34.sp,
                    color = MaterialTheme.colorScheme.accentInk,
                )
                OutlinedButton(onClick = { onIntent(SessionIntent.StopRest) }) {
                    Text(stringResource(R.string.session_rest_stop))
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                REST_PRESETS.forEach { sec ->
                    RestChip(
                        label = stringResource(R.string.session_rest_seconds, sec),
                        onClick = { onIntent(SessionIntent.StartRest(sec)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Hairline()

        OverlineLabel(stringResource(R.string.session_note))
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.session_note_hint)) },
            minLines = 2,
        )

        Spacer(Modifier.height(Spacing.sm))

        Button(
            onClick = { onIntent(SessionIntent.Finish(note)) },
            enabled = !state.submitting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.session_finish))
        }
    }
}

/** Flat confirmation mirroring the quick-log: the streak is the lime mono hero; "Done" leaves. */
@Composable
private fun SessionDone(state: SessionUiState, onExit: () -> Unit) {
    val streak = state.result ?: return
    ScreenContainer {
        Spacer(Modifier.height(Spacing.xl))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            StatNumber(streak.weekStreak.toString(), size = 72.sp, color = MaterialTheme.colorScheme.accentInk)
            OverlineLabel(stringResource(R.string.log_week_streak))
            Text(
                text = stringResource(
                    R.string.log_logged_summary,
                    stringResource(state.type.labelRes()),
                    state.durationMin,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (streak.goalMet) {
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
        OutlinedButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.session_done))
        }
    }
}

@Composable
private fun LiveDot() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.accentInk))
        OverlineLabel(stringResource(R.string.session_live))
    }
}

@Composable
private fun ExerciseRow(name: String, sets: List<SetEntry>) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        SetLines(sets)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SetLines(sets: List<SetEntry>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        sets.forEach { s ->
            val line = if (s.weightKg != null) {
                stringResource(R.string.session_set_weighted, fmtKg(s.weightKg), s.reps)
            } else {
                stringResource(R.string.session_set_reps, s.reps)
            }
            Text(line, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Inline composer: exercise name, then reps + kg + Add. Exercise & weight persist for the next set. */
@Composable
private fun SetComposer(
    exercise: String, onExercise: (String) -> Unit,
    reps: String, onReps: (String) -> Unit,
    weight: String, onWeight: (String) -> Unit,
    onAdd: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        OutlinedTextField(
            value = exercise,
            onValueChange = onExercise,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(stringResource(R.string.session_exercise_hint)) },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = reps,
                onValueChange = onReps,
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.session_reps_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                value = weight,
                onValueChange = onWeight,
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.session_weight_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            OutlinedButton(onClick = onAdd) {
                Text(stringResource(R.string.session_add_set))
            }
        }
    }
}

/** Compact type chip: tinted glyph + label, lime ink + border when active (never a fill). */
@Composable
private fun TypePill(
    @DrawableRes icon: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.accentInk
    val border = if (selected) accent else MaterialTheme.colorScheme.outline
    val fg = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, border, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = fg)
    }
}

@Composable
private fun RestChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PlanChip(name: String, selected: Boolean, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.accentInk
    val border = if (selected) accent else MaterialTheme.colorScheme.outline
    val fg = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, border, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
    ) {
        Text(name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, color = fg)
    }
}

/** A plan's exercise as a tappable agenda row — tapping pre-fills the composer with name + target reps. */
@Composable
private fun PlanAgendaRow(exercise: PlannedExercise, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(exercise.name, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = stringResource(R.string.plan_target, exercise.targetSets, exercise.targetReps),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
