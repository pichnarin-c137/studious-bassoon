package com.gymapp.ui.screens.membership

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.domain.intent.MembershipIntent
import com.gymapp.domain.state.MembershipData
import com.gymapp.domain.state.UiState
import com.gymapp.domain.usecase.GetMembershipDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MembershipViewModel @Inject constructor(
    private val getMembershipData: GetMembershipDataUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<MembershipData>>(UiState.Loading)
    val state: StateFlow<UiState<MembershipData>> = _state.asStateFlow()

    init {
        onIntent(MembershipIntent.Load)
    }

    fun onIntent(intent: MembershipIntent) {
        when (intent) {
            MembershipIntent.Load, MembershipIntent.Retry -> load()
            MembershipIntent.ToggleFreeze -> Unit // Freeze flow lands in a later pass.
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                _state.value = UiState.Success(getMembershipData())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message)
            }
        }
    }
}
