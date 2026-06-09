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
import com.gymapp.data.model.PersonalRecord
import com.gymapp.data.model.TimeRange
import com.gymapp.data.model.WorkoutSession
import com.gymapp.domain.intent.ProgressIntent
import com.gymapp.domain.state.ProgressData
import com.gymapp.ui.components.Hairline
import com.gymapp.ui.components.MetricBlock
import com.gymapp.ui.components.OverlineLabel
import com.gymapp.ui.components.ScreenContainer
import com.gymapp.ui.components.StatNumber
import com.gymapp.ui.components.TrendLineChart
import com.gymapp.ui.components.UiStateContent
import com.gymapp.ui.theme.Spacing
import com.gymapp.util.DateTimeUtil
import java.util.Locale
import kotlin.math.roundToInt

private fun oneDecimal(value: Double): String = String.format(Locale.US, "%.1f", value)
private fun grouped(value: Double): String = String.format(Locale.US, "%,d", value.roundToInt())

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
        ProgressContent(data, onSetRange = { viewModel.onIntent(ProgressIntent.SetRange(it)) })
    }
}

@Composable
private fun ProgressContent(data: ProgressData, onSetRange: (TimeRange) -> Unit) {
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

        // Volume lifted
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OverlineLabel(stringResource(R.string.progress_volume_trend))
                DeltaBadge(data.volumeDeltaPct)
            }
            val latest = data.volumeTrend.lastOrNull()?.volumeKg ?: 0.0
            StatNumber(stringResource(R.string.progress_unit_kg, grouped(latest)), size = 40.sp)
            TrendLineChart(values = data.volumeTrend.map { it.volumeKg })
        }

        Hairline()

        // Recent session
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            OverlineLabel(stringResource(R.string.progress_recent_session))
            RecentSession(data.recentSession)
        }

        Hairline()

        // Personal records
        OverlineLabel(stringResource(R.string.progress_personal_records))
        data.personalRecords.forEachIndexed { index, record ->
            PersonalRecordRow(record)
            if (index < data.personalRecords.lastIndex) Hairline()
        }
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
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            Modifier
                .padding(top = 4.dp)
                .height(2.dp)
                .width(if (selected) 18.dp else 0.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}

@Composable
private fun DeltaBadge(deltaPct: Double) {
    val rounded = deltaPct.roundToInt()
    val positive = rounded >= 0
    val signed = (if (positive) "+" else "") + rounded.toString()
    Text(
        text = stringResource(R.string.progress_unit_pct, signed),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium,
        color = if (positive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
    )
}

@Composable
private fun RecentSession(session: WorkoutSession) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(session.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Text(
                text = "${DateTimeUtil.relative(session.performedAt)}  ·  ${
                    stringResource(R.string.progress_unit_min, session.durationMin)
                }",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (session.prCount > 0) PrInline(session.prCount)
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        MetricBlock(
            value = grouped(session.volumeKg),
            label = stringResource(R.string.progress_stat_volume),
            modifier = Modifier.weight(1f),
            valueSize = 24.sp,
            horizontalAlignment = Alignment.Start,
        )
        MetricBlock(
            value = session.totalSets.toString(),
            label = stringResource(R.string.progress_stat_sets),
            modifier = Modifier.weight(1f),
            valueSize = 24.sp,
            horizontalAlignment = Alignment.CenterHorizontally,
        )
        MetricBlock(
            value = session.kcal.toString(),
            label = stringResource(R.string.progress_stat_kcal),
            modifier = Modifier.weight(1f),
            valueSize = 24.sp,
            horizontalAlignment = Alignment.End,
        )
    }
}

@Composable
private fun PrInline(count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Icon(
            painter = painterResource(R.drawable.ic_trophy),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = stringResource(R.string.progress_prs, count),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun PersonalRecordRow(record: PersonalRecord) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(record.exercise, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            StatNumber(stringResource(R.string.progress_unit_kg, oneDecimal(record.bestKg)), size = 18.sp)
            Text(
                text = "▲ " + stringResource(R.string.progress_pr_delta, oneDecimal(record.improvementKg)),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
