package com.gymapp.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.domain.intent.ProfileIntent
import com.gymapp.domain.model.AppLanguage
import com.gymapp.domain.model.ThemeMode
import com.gymapp.data.repository.AuthRepository
import com.gymapp.domain.state.ProfileData
import com.gymapp.domain.state.UiState
import com.gymapp.domain.usecase.GetProfileDataUseCase
import com.gymapp.util.LanguageManager
import com.gymapp.util.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileData: GetProfileDataUseCase,
    private val themeManager: ThemeManager,
    private val languageManager: LanguageManager,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<ProfileData>>(UiState.Loading)
    val state: StateFlow<UiState<ProfileData>> = _state.asStateFlow()

    val themeMode: StateFlow<ThemeMode> = themeManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    val language: StateFlow<AppLanguage> = languageManager.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppLanguage.ENGLISH)

    init {
        onIntent(ProfileIntent.Load)
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.Load, ProfileIntent.Retry -> load()
            is ProfileIntent.SetTheme -> viewModelScope.launch { themeManager.setThemeMode(intent.mode) }
            is ProfileIntent.SetLanguage -> viewModelScope.launch { languageManager.setLanguage(intent.language) }
            ProfileIntent.SignOut -> viewModelScope.launch { authRepository.signOut() }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                _state.value = UiState.Success(getProfileData())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message)
            }
        }
    }
}
