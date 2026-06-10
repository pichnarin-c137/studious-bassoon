package com.gymapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymapp.R
import com.gymapp.data.model.MembershipStatus
import com.gymapp.ui.theme.Spacing
import com.gymapp.ui.theme.accentInk
import com.gymapp.util.DateTimeUtil

private const val NEAR_EXPIRY_DAYS = 5

/**
 * De-carded Home hero: a big mono days-left number beside a thin draining bar (time elapsed),
 * with a lime status dot and the plan · branch. Near expiry it shifts to amber + a renewal nudge.
 * The whole block is tappable and opens the membership detail screen.
 */
@Composable
fun MembershipStatusBlock(
    planLabel: String,
    branchName: String,
    status: MembershipStatus,
    startedAt: Long,
    expiresAt: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val daysLeft = DateTimeUtil.daysFromNow(expiresAt)
    val expired = status == MembershipStatus.EXPIRED || daysLeft < 0
    val nearExpiry = !expired && daysLeft <= NEAR_EXPIRY_DAYS
    val accent = if (expired || nearExpiry) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.accentInk
    }

    val now = System.currentTimeMillis()
    val elapsed = ((now - startedAt).toFloat() / (expiresAt - startedAt).toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.xs),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OverlineLabel(stringResource(R.string.home_section_membership))
            StatusDot(status)
        }

        Text(
            text = stringResource(R.string.home_plan_branch, planLabel, branchName),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                StatNumber(
                    value = if (expired) "—" else daysLeft.toString(),
                    size = 46.sp,
                    color = if (expired || nearExpiry) accent else MaterialTheme.colorScheme.onSurface,
                )
                OverlineLabel(stringResource(R.string.home_days_left_label))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(elapsed)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(accent),
                    )
                }
                Text(
                    text = if (expired) {
                        stringResource(R.string.home_expired_label)
                    } else {
                        DateTimeUtil.formatExpiry(expiresAt)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (expired || nearExpiry) {
            Text(
                text = stringResource(R.string.home_renew_nudge),
                style = MaterialTheme.typography.bodySmall,
                color = accent,
            )
        }
    }
}
