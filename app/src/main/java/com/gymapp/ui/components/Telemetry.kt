package com.gymapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymapp.R
import com.gymapp.data.model.MembershipStatus
import com.gymapp.ui.theme.MonoNumbers
import com.gymapp.ui.theme.Spacing
import com.gymapp.ui.theme.accentInk

/**
 * Telemetry primitives: the building blocks of the data-led look — hairline rules instead of card
 * borders, oversized monospaced numbers as the hero, tiny tracked caps labels, and a lime status
 * dot in place of a filled pill.
 */

/** Full-bleed 1px section separator — the de-carded layout's only divider. */
@Composable
fun Hairline(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
}

/** A big monospaced number — the focal element on the data screens. */
@Composable
fun StatNumber(
    value: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 34.sp,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = value,
        modifier = modifier,
        fontFamily = MonoNumbers,
        fontWeight = FontWeight.Medium,
        fontSize = size,
        lineHeight = size,
        color = color,
    )
}

/**
 * Unboxed metric: a big mono number over a tiny tracked caps label. In a weighted row, set
 * [horizontalAlignment] per column (Start / CenterHorizontally / End) so the outer metrics hug the
 * screen edges and the row reads as an aligned grid instead of drifting left.
 */
@Composable
fun MetricBlock(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    numberColor: Color = MaterialTheme.colorScheme.onSurface,
    valueSize: TextUnit = 30.sp,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        StatNumber(value = value, size = valueSize, color = numberColor)
        OverlineLabel(label)
    }
}

/** Live/active status as a colored dot + caps label (lime ACTIVE), replacing the filled pill. */
@Composable
fun StatusDot(status: MembershipStatus, modifier: Modifier = Modifier) {
    val labelRes: Int
    val dot: Color
    when (status) {
        MembershipStatus.ACTIVE -> {
            labelRes = R.string.status_active
            dot = MaterialTheme.colorScheme.accentInk
        }
        MembershipStatus.EXPIRING -> {
            labelRes = R.string.status_expiring
            dot = MaterialTheme.colorScheme.tertiary
        }
        MembershipStatus.EXPIRED -> {
            labelRes = R.string.status_expired
            dot = MaterialTheme.colorScheme.error
        }
        MembershipStatus.FROZEN -> {
            labelRes = R.string.status_frozen
            dot = MaterialTheme.colorScheme.onSurfaceVariant
        }
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
        Text(
            text = stringResource(labelRes).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = if (isKhmerUi()) 0.sp else 1.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start,
        )
    }
}
