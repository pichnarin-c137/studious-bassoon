package com.gymapp.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.domain.intent.LoginIntent
import com.gymapp.domain.usecase.SignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Login form state. On a successful sign-in the session flips and the gate swaps to the shell. */
data class LoginFormState(
    val memberId: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val submitting: Boolean = false,
    val error: Boolean = false,
) {
    val canSubmit: Boolean get() = memberId.isNotBlank() && password.isNotBlank() && !submitting
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signIn: SignInUseCase,
) : ViewModel() {

    private val _form = MutableStateFlow(LoginFormState())
    val form: StateFlow<LoginFormState> = _form.asStateFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.UpdateMemberId -> _form.update { it.copy(memberId = intent.value, error = false) }
            is LoginIntent.UpdatePassword -> _form.update { it.copy(password = intent.value, error = false) }
            LoginIntent.TogglePasswordVisibility -> _form.update { it.copy(passwordVisible = !it.passwordVisible) }
            LoginIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val current = _form.value
        if (!current.canSubmit) {
            _form.update { it.copy(error = true) }
            return
        }
        viewModelScope.launch {
            _form.update { it.copy(submitting = true, error = false) }
            try {
                signIn(current.memberId, current.password)
                // Session now stored; the gate in MainActivity recomposes into the app shell.
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _form.update { it.copy(submitting = false, error = true) }
            }
        }
    }
}
