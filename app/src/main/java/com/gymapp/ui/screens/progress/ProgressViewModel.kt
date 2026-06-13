package com.gymapp.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.data.model.TimeRange
import com.gymapp.domain.intent.ProgressIntent
import com.gymapp.domain.state.ProgressData
import com.gymapp.domain.state.UiState
import com.gymapp.domain.usecase.GetProgressDataUseCase
import com.gymapp.domain.usecase.SetWeeklyTargetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getProgressData: GetProgressDataUseCase,
    private val setWeeklyTarget: SetWeeklyTargetUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<ProgressData>>(UiState.Loading)
    val state: StateFlow<UiState<ProgressData>> = _state.asStateFlow()

    private var range = TimeRange.LAST_30_DAYS

    init {
        onIntent(ProgressIntent.Load)
    }

    fun onIntent(intent: ProgressIntent) {
        when (intent) {
            ProgressIntent.Load, ProgressIntent.Retry -> load()
            is ProgressIntent.SetRange -> {
                range = intent.range
                load()
            }
            is ProgressIntent.SetWeeklyTarget -> changeTarget(intent.target)
        }
    }

    /** Updates only the streak in place so changing the goal doesn't flash the whole screen. */
    private fun changeTarget(target: Int) {
        viewModelScope.launch {
            try {
                val streak = setWeeklyTarget(target)
                val current = _state.value
                if (current is UiState.Success) {
                    _state.value = UiState.Success(current.data.copy(streak = streak))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Keep the current goal on failure; the mock never fails.
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                _state.value = UiState.Success(getProgressData(range))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message)
            }
        }
    }
}
