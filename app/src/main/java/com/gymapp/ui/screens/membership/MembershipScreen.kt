package com.gymapp.ui.screens.membership

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymapp.R
import com.gymapp.data.model.Payment
import com.gymapp.domain.intent.MembershipIntent
import com.gymapp.domain.state.MembershipData
import com.gymapp.ui.components.Hairline
import com.gymapp.ui.components.InfoRow
import com.gymapp.ui.components.MetricBlock
import com.gymapp.ui.components.OverlineLabel
import com.gymapp.ui.components.ScreenContainer
import com.gymapp.ui.components.StatusDot
import com.gymapp.ui.components.UiStateContent
import com.gymapp.ui.theme.MonoNumbers
import com.gymapp.ui.theme.Spacing
import com.gymapp.util.DateTimeUtil

@Composable
fun MembershipScreen(
    onShowQr: () -> Unit,
    viewModel: MembershipViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    UiStateContent(state, onRetry = { viewModel.onIntent(MembershipIntent.Retry) }) { data ->
        MembershipContent(
            data = data,
            onFreeze = { viewModel.onIntent(MembershipIntent.ToggleFreeze) },
            onShowQr = onShowQr,
        )
    }
}

@Composable
private fun MembershipContent(data: MembershipData, onFreeze: () -> Unit, onShowQr: () -> Unit) {
    val membership = data.membership
    ScreenContainer {
        // Current plan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(membership.plan.type.labelRes()),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
            )
            StatusDot(membership.status)
        }
        InfoRow(stringResource(R.string.membership_current_plan), money(membership.plan.priceUsd))
        Text(
            text = stringResource(R.string.membership_renews, DateTimeUtil.formatDate(membership.expiresAt)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (membership.frozen) {
            Text(
                text = stringResource(R.string.membership_frozen_notice, membership.freezeDaysRemaining),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        OutlinedButton(onClick = onShowQr, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_show_qr))
        }

        Hairline()

        // Plans
        OverlineLabel(stringResource(R.string.membership_plans))
        data.plans.forEach { plan ->
            InfoRow(stringResource(plan.type.labelRes()), money(plan.priceUsd))
        }

        Hairline()

        // Freeze
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            OverlineLabel(stringResource(R.string.membership_freeze))
            Text(
                text = stringResource(R.string.membership_freeze_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = onFreeze, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.membership_freeze_action))
            }
        }

        Hairline()

        // Referral
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            OverlineLabel(stringResource(R.string.membership_referral))
            Text(
                text = stringResource(R.string.membership_referral_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.membership_referral_code),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = data.referral.code,
                    fontFamily = MonoNumbers,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                MetricBlock(
                    value = data.referral.friendsReferred.toString(),
                    label = stringResource(R.string.membership_friends),
                    modifier = Modifier.weight(1f),
                    valueSize = 24.sp,
                    horizontalAlignment = Alignment.Start,
                )
                MetricBlock(
                    value = data.referral.freeDaysEarned.toString(),
                    label = stringResource(R.string.membership_free_days),
                    modifier = Modifier.weight(1f),
                    valueSize = 24.sp,
                    horizontalAlignment = Alignment.End,
                )
            }
            OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.membership_invite))
            }
        }

        Hairline()

        // Billing history
        OverlineLabel(stringResource(R.string.membership_billing))
        data.payments.forEachIndexed { index, payment ->
            PaymentRow(payment)
            if (index < data.payments.lastIndex) Hairline()
        }
    }
}

@Composable
private fun PaymentRow(payment: Payment) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(payment.description, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                text = money(payment.amountUsd),
                fontFamily = MonoNumbers,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = stringResource(payment.method.labelRes()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = DateTimeUtil.formatDate(payment.paidAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
