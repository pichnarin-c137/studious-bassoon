package com.gymapp.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymapp.R
import com.gymapp.domain.intent.LoginIntent
import com.gymapp.ui.components.Hairline
import com.gymapp.ui.theme.Spacing

/** Owner-provisioned login: member ID + password issued at the gym branch front desk. */
@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) {
    val form by viewModel.form.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg, vertical = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            // Brand mark
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_barbell),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp),
                )
            }
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )

            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = stringResource(R.string.login_welcome),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(R.string.login_subtext),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(Spacing.sm))
            OutlinedTextField(
                value = form.memberId,
                onValueChange = { viewModel.onIntent(LoginIntent.UpdateMemberId(it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = form.error,
                label = { Text(stringResource(R.string.login_member_id)) },
                placeholder = { Text(stringResource(R.string.login_member_id_hint)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Next,
                ),
            )
            OutlinedTextField(
                value = form.password,
                onValueChange = { viewModel.onIntent(LoginIntent.UpdatePassword(it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = form.error,
                label = { Text(stringResource(R.string.login_password)) },
                visualTransformation = if (form.passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { viewModel.onIntent(LoginIntent.Submit) }),
                trailingIcon = {
                    val visible = form.passwordVisible
                    IconButton(onClick = { viewModel.onIntent(LoginIntent.TogglePasswordVisibility) }) {
                        Icon(
                            painter = painterResource(if (visible) R.drawable.ic_eye_off else R.drawable.ic_eye),
                            contentDescription = stringResource(
                                if (visible) R.string.login_hide_password else R.string.login_show_password,
                            ),
                        )
                    }
                },
            )

            if (form.error) {
                Text(
                    text = stringResource(R.string.login_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Button(
                onClick = { viewModel.onIntent(LoginIntent.Submit) },
                enabled = form.canSubmit,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                if (form.submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(stringResource(R.string.login_sign_in), fontWeight = FontWeight.Medium)
                }
            }

            Text(
                text = stringResource(R.string.login_forgot),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xs),
            )

            Spacer(Modifier.height(Spacing.md))
            Hairline()
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text(
                    text = stringResource(R.string.login_new_here_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(R.string.login_new_here_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
