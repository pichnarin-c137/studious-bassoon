package com.gymapp.ui.screens.checkin

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymapp.R
import com.gymapp.domain.intent.CheckInIntent
import com.gymapp.domain.state.CheckInData
import com.gymapp.ui.components.Hairline
import com.gymapp.ui.components.InfoRow
import com.gymapp.ui.components.OverlineLabel
import com.gymapp.ui.components.QrCodeImage
import com.gymapp.ui.components.ScreenContainer
import com.gymapp.ui.components.UiStateContent
import com.gymapp.ui.theme.Dimension
import com.gymapp.ui.theme.MonoNumbers
import com.gymapp.ui.theme.Spacing
import com.gymapp.ui.theme.accentInk
import com.gymapp.util.DateTimeUtil
import com.gymapp.util.LocalWindowSize

@Composable
fun CheckInScreen(viewModel: CheckInViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    UiStateContent(state, onRetry = { viewModel.onIntent(CheckInIntent.Retry) }) { data ->
        CheckInContent(data, onScan = { viewModel.onIntent(CheckInIntent.Scan) })
    }
}

@Composable
private fun CheckInContent(data: CheckInData, onScan: () -> Unit) {
    val window = LocalWindowSize.current
    var scanRequested by remember { mutableStateOf(false) }

    ScreenContainer {
        // Member identity (the "card")
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            OverlineLabel(stringResource(R.string.checkin_your_qr))
            Text(
                text = data.member.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
            )
            data.member.khmerName?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Lime-framed QR (a scan target)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .border(2.dp, MaterialTheme.colorScheme.accentInk, RoundedCornerShape(20.dp))
                    .padding(Spacing.sm),
            ) {
                QrCodeImage(content = data.qrPayload, size = Dimension.qrSize(window))
            }
        }

        // Member code — the hero, in mono
        Text(
            text = data.member.memberCode,
            fontFamily = MonoNumbers,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.checkin_instruction),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        OutlinedButton(
            onClick = { scanRequested = true; onScan() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.checkin_scan_button))
        }
        if (scanRequested) {
            Text(
                text = stringResource(R.string.checkin_scan_unavailable),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }

        Hairline()

        // Visit history
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OverlineLabel(stringResource(R.string.checkin_visit_history))
            data.recentVisits.firstOrNull()?.let { last ->
                Text(
                    text = DateTimeUtil.relative(last.timestamp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        data.recentVisits.forEachIndexed { index, visit ->
            InfoRow(
                label = "#${data.recentVisits.size - index}",
                value = DateTimeUtil.relative(visit.timestamp),
            )
        }
    }
}
