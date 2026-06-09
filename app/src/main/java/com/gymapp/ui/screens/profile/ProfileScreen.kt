package com.gymapp.ui.screens.profile

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymapp.R
import com.gymapp.data.model.Member
import com.gymapp.domain.intent.ProfileIntent
import com.gymapp.domain.model.AppLanguage
import com.gymapp.domain.model.ThemeMode
import com.gymapp.domain.state.ProfileData
import com.gymapp.ui.components.Avatar
import com.gymapp.ui.components.Hairline
import com.gymapp.ui.components.InfoRow
import com.gymapp.ui.components.OverlineLabel
import com.gymapp.ui.components.ScreenContainer
import com.gymapp.ui.components.UiStateContent
import com.gymapp.ui.theme.MonoNumbers
import com.gymapp.ui.theme.Spacing
import com.gymapp.util.DateTimeUtil

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()

    UiStateContent(state, onRetry = { viewModel.onIntent(ProfileIntent.Retry) }) { data ->
        ProfileContent(
            data = data,
            themeMode = themeMode,
            language = language,
            onSetTheme = { viewModel.onIntent(ProfileIntent.SetTheme(it)) },
            onSetLanguage = { viewModel.onIntent(ProfileIntent.SetLanguage(it)) },
            onSignOut = { viewModel.onIntent(ProfileIntent.SignOut) },
        )
    }
}

@Composable
private fun ProfileContent(
    data: ProfileData,
    themeMode: ThemeMode,
    language: AppLanguage,
    onSetTheme: (ThemeMode) -> Unit,
    onSetLanguage: (AppLanguage) -> Unit,
    onSignOut: () -> Unit,
) {
    ScreenContainer {
        ProfileHeader(data.member)

        Hairline()
        OverlineLabel(stringResource(R.string.profile_account))
        InfoRow(stringResource(R.string.profile_phone), data.member.phone)
        InfoRow(stringResource(R.string.profile_member_since), DateTimeUtil.formatDate(data.member.memberSince))

        data.member.pt?.let { pt ->
            Hairline()
            CoachSection(name = pt.name, telegram = pt.telegram, messenger = pt.messenger)
        }

        Hairline()
        SettingsSection(
            themeMode = themeMode,
            language = language,
            onSetTheme = onSetTheme,
            onSetLanguage = onSetLanguage,
        )

        Hairline()
        OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.profile_sign_out))
        }
    }
}

@Composable
private fun ProfileHeader(member: Member) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Avatar(name = member.name, photoUrl = member.photoUrl, size = 64.dp)
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(member.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
            if (!member.khmerName.isNullOrBlank()) {
                Text(
                    text = member.khmerName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = member.memberCode,
                fontFamily = MonoNumbers,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CoachSection(name: String, telegram: String?, messenger: String?) {
    val context = LocalContext.current
    fun open(url: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        OverlineLabel(stringResource(R.string.profile_pt_title))
        Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Text(
            text = stringResource(R.string.profile_pt_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            telegram?.let { url ->
                OutlinedButton(onClick = { open(url) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.sm))
                    Text(stringResource(R.string.profile_message_telegram))
                }
            }
            messenger?.let { url ->
                OutlinedButton(onClick = { open(url) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.sm))
                    Text(stringResource(R.string.profile_message_messenger))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSection(
    themeMode: ThemeMode,
    language: AppLanguage,
    onSetTheme: (ThemeMode) -> Unit,
    onSetLanguage: (AppLanguage) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        OverlineLabel(stringResource(R.string.profile_settings))

        Text(
            text = stringResource(R.string.profile_theme),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val themeOptions = listOf(
            ThemeMode.LIGHT to stringResource(R.string.theme_light),
            ThemeMode.DARK to stringResource(R.string.theme_dark),
            ThemeMode.SYSTEM to stringResource(R.string.theme_system),
        )
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            themeOptions.forEachIndexed { index, (mode, label) ->
                SegmentedButton(
                    selected = mode == themeMode,
                    onClick = { onSetTheme(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index, themeOptions.size),
                ) { Text(label) }
            }
        }

        Text(
            text = stringResource(R.string.profile_language),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val languageOptions = AppLanguage.entries
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            languageOptions.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == language,
                    onClick = { onSetLanguage(option) },
                    shape = SegmentedButtonDefaults.itemShape(index, languageOptions.size),
                ) { Text(option.displayName) }
            }
        }
    }
}
