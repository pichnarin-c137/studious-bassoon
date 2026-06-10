package com.gymapp.ui.screens.home

import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymapp.R
import com.gymapp.data.model.BranchStatus
import com.gymapp.data.model.BusyLevel
import com.gymapp.data.model.PtContact
import com.gymapp.domain.intent.HomeIntent
import com.gymapp.domain.state.HomeData
import com.gymapp.ui.components.Avatar
import com.gymapp.ui.components.BusynessStrip
import com.gymapp.ui.components.Hairline
import com.gymapp.ui.components.MembershipStatusBlock
import com.gymapp.ui.components.MetricBlock
import com.gymapp.ui.components.OverlineLabel
import com.gymapp.ui.components.ScreenContainer
import com.gymapp.ui.components.UiStateContent
import com.gymapp.ui.components.WeekBarChart
import com.gymapp.ui.screens.membership.labelRes
import com.gymapp.ui.theme.MonoNumbers
import com.gymapp.ui.theme.Spacing
import com.gymapp.ui.theme.accentInk
import com.gymapp.util.DateTimeUtil

private fun pad2(n: Int): String = if (n in 0..9) "0$n" else n.toString()

@Composable
fun HomeScreen(
    onOpenMembership: () -> Unit,
    onShowQr: () -> Unit,
    onStartWorkout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    UiStateContent(state, onRetry = { viewModel.onIntent(HomeIntent.Retry) }) { data ->
        HomeContent(
            data = data,
            onOpenMembership = onOpenMembership,
            onShowQr = onShowQr,
            onStartWorkout = onStartWorkout,
        )
    }
}

@Composable
private fun HomeContent(
    data: HomeData,
    onOpenMembership: () -> Unit,
    onShowQr: () -> Unit,
    onStartWorkout: () -> Unit,
) {
    val context = LocalContext.current
    ScreenContainer {
        // Greeting
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                OverlineLabel(stringResource(R.string.home_welcome))
                Text(
                    text = data.member.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                )
            }
            Avatar(name = data.member.name, photoUrl = data.member.photoUrl, size = 44.dp)
        }

        Hairline()

        // Membership (top priority)
        MembershipStatusBlock(
            planLabel = stringResource(data.membership.plan.type.labelRes()),
            branchName = data.membership.branchName,
            status = data.membership.status,
            startedAt = data.membership.startedAt,
            expiresAt = data.membership.expiresAt,
            onClick = onOpenMembership,
        )

        Hairline()

        // Entry QR — the daily door check-in
        EntryQrRow(memberCode = data.member.memberCode, onClick = onShowQr)

        Hairline()

        // Mono metric row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            MetricBlock(
                value = pad2(data.stats.currentStreak),
                label = stringResource(R.string.home_metric_streak),
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
            )
            MetricBlock(
                value = data.stats.totalVisits.toString(),
                label = stringResource(R.string.home_metric_visits),
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            )
            MetricBlock(
                value = pad2(data.stats.visitsThisMonth),
                label = stringResource(R.string.home_metric_month),
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
            )
        }

        Hairline()

        // This week
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OverlineLabel(stringResource(R.string.home_section_week))
                Text(
                    text = stringResource(
                        R.string.home_week_summary,
                        data.weeklyActivity.sessionsDone,
                        data.weeklyActivity.sessionsTarget,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            WeekBarChart(data.weeklyActivity.days)
        }

        Hairline()

        // At the gym: open-now + live busy-ness
        AtTheGym(data.branch)

        Spacer(Modifier.height(Spacing.sm))
        Button(
            onClick = onStartWorkout,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(Spacing.sm))
            Text(stringResource(R.string.home_start_workout), fontWeight = FontWeight.Medium)
        }

        // Coach quick-contact (deep links)
        data.member.pt?.let { pt ->
            Hairline()
            CoachRow(pt, onOpen = { url ->
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
            })
        }
    }
}

@Composable
private fun EntryQrRow(memberCode: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            OverlineLabel(stringResource(R.string.home_entry_qr))
            Text(
                text = memberCode,
                fontFamily = MonoNumbers,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(R.string.home_entry_qr_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_qr),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.accentInk,
            modifier = Modifier.size(36.dp),
        )
    }
}

@Composable
private fun AtTheGym(branch: BranchStatus) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                OverlineLabel(stringResource(R.string.home_gym_section))
                Text(branch.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Dot(if (branch.openNow) MaterialTheme.colorScheme.accentInk else MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = stringResource(if (branch.openNow) R.string.branch_open else R.string.branch_closed),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
                if (branch.openNow) {
                    Text(
                        text = stringResource(R.string.home_closes_at, DateTimeUtil.formatTime(branch.closesAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OverlineLabel(stringResource(R.string.home_right_now))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Dot(busyColor(branch.busyness.level))
                Text(
                    text = stringResource(busyLabelRes(branch.busyness.level)),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "· " + stringResource(busyCaptionRes(branch.busyness.level)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        BusynessStrip(branch.busyness.hourly)
    }
}

@Composable
private fun CoachRow(pt: PtContact, onOpen: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        OverlineLabel(stringResource(R.string.home_coach))
        Text(pt.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            pt.telegram?.let { url ->
                OutlinedButton(onClick = { onOpen(url) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.sm))
                    Text(stringResource(R.string.profile_message_telegram))
                }
            }
            pt.messenger?.let { url ->
                OutlinedButton(onClick = { onOpen(url) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.sm))
                    Text(stringResource(R.string.profile_message_messenger))
                }
            }
        }
    }
}

@Composable
private fun Dot(color: Color) {
    Box(Modifier.size(8.dp).clip(CircleShape).background(color))
}

@Composable
private fun busyColor(level: BusyLevel): Color = when (level) {
    BusyLevel.QUIET -> MaterialTheme.colorScheme.accentInk
    BusyLevel.MODERATE -> MaterialTheme.colorScheme.tertiary
    BusyLevel.BUSY -> MaterialTheme.colorScheme.error
}

@StringRes
private fun busyLabelRes(level: BusyLevel): Int = when (level) {
    BusyLevel.QUIET -> R.string.busy_quiet
    BusyLevel.MODERATE -> R.string.busy_moderate
    BusyLevel.BUSY -> R.string.busy_busy
}

@StringRes
private fun busyCaptionRes(level: BusyLevel): Int = when (level) {
    BusyLevel.QUIET -> R.string.busy_caption_quiet
    BusyLevel.MODERATE -> R.string.busy_caption_moderate
    BusyLevel.BUSY -> R.string.busy_caption_busy
}
