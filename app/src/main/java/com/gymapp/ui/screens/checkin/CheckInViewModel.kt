package com.gymapp.ui.screens.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.domain.intent.CheckInIntent
import com.gymapp.domain.state.CheckInData
import com.gymapp.domain.state.UiState
import com.gymapp.domain.usecase.GetCheckInDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val getCheckInData: GetCheckInDataUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<CheckInData>>(UiState.Loading)
    val state: StateFlow<UiState<CheckInData>> = _state.asStateFlow()

    init {
        onIntent(CheckInIntent.Load)
    }

    fun onIntent(intent: CheckInIntent) {
        when (intent) {
            CheckInIntent.Load, CheckInIntent.Retry -> load()
            CheckInIntent.Scan -> Unit // Camera scanning lands in a later pass.
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                _state.value = UiState.Success(getCheckInData())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message)
            }
        }
    }
}
