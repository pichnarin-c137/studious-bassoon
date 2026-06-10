package com.gymapp.ui.screens.activity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymapp.R
import com.gymapp.data.model.ActivityFeedItem
import com.gymapp.data.model.Announcement
import com.gymapp.data.model.FeedPr
import com.gymapp.domain.intent.ActivityIntent
import com.gymapp.domain.state.ActivityData
import com.gymapp.ui.components.Avatar
import com.gymapp.ui.components.Hairline
import com.gymapp.ui.components.OverlineLabel
import com.gymapp.ui.components.ScreenContainer
import com.gymapp.ui.components.UiStateContent
import com.gymapp.ui.screens.log.labelRes
import com.gymapp.ui.theme.MonoNumbers
import com.gymapp.ui.theme.Spacing
import com.gymapp.ui.theme.accentInk
import com.gymapp.util.DateTimeUtil
import java.util.Locale

@Composable
fun ActivityScreen(viewModel: ActivityViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    UiStateContent(state, onRetry = { viewModel.onIntent(ActivityIntent.Retry) }) { data ->
        ActivityContent(data, onIntent = viewModel::onIntent)
    }
}

@Composable
private fun ActivityContent(data: ActivityData, onIntent: (ActivityIntent) -> Unit) {
    ScreenContainer {
        if (data.announcements.isNotEmpty()) {
            OverlineLabel(stringResource(R.string.activity_from_gym))
            data.announcements.forEachIndexed { index, announcement ->
                AnnouncementRow(announcement)
                if (index < data.announcements.lastIndex) Hairline()
            }
            Hairline()
        }

        OverlineLabel(stringResource(R.string.activity_feed_section))
        if (data.feed.isEmpty()) {
            Text(
                text = stringResource(R.string.activity_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            data.feed.forEachIndexed { index, item ->
                FeedRow(item, onKudos = { onIntent(ActivityIntent.ToggleKudos(item.id)) })
                if (index < data.feed.lastIndex) Hairline()
            }
        }
    }
}

@Composable
private fun AnnouncementRow(announcement: Announcement) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = announcement.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = DateTimeUtil.relative(announcement.postedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = announcement.body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FeedRow(item: ActivityFeedItem, onKudos: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(name = item.actor.name, photoUrl = item.actor.photoUrl, size = 40.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(
                text = if (item.isYou) stringResource(R.string.activity_you) else item.actor.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(
                    R.string.activity_feed_meta,
                    stringResource(item.sessionType.labelRes()),
                    item.durationMin,
                    DateTimeUtil.relative(item.performedAt),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            item.pr?.let { PrBadge(it) }
        }
        KudosControl(
            count = item.kudosCount,
            given = item.youGaveKudos,
            interactive = !item.isYou,
            onClick = onKudos,
        )
    }
}

/** 🏆 milestone, lime ink — same treatment as the Progress screen's PR marker. */
@Composable
private fun PrBadge(pr: FeedPr) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_trophy),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.accentInk,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = stringResource(R.string.activity_pr, pr.exercise, kgLabel(pr.valueKg)),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.accentInk,
        )
    }
}

/** One-tap kudos: filled lime heart when given. Read-only (no heart fill, no tap) on your own rows. */
@Composable
private fun KudosControl(count: Int, given: Boolean, interactive: Boolean, onClick: () -> Unit) {
    val base = Modifier.clip(RoundedCornerShape(50))
    val modifier = if (interactive) base.clickable(onClick = onClick) else base
    Row(
        modifier = modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Icon(
            imageVector = if (given) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = stringResource(R.string.activity_kudos_cd),
            tint = if (given) MaterialTheme.colorScheme.accentInk else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontFamily = MonoNumbers,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Whole kg shown without a trailing .0; fractional plates keep one decimal. */
private fun kgLabel(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.US, "%.1f", value)
