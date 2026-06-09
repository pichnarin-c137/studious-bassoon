package com.gymapp.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.data.model.TimeRange
import com.gymapp.domain.intent.ProgressIntent
import com.gymapp.domain.state.ProgressData
import com.gymapp.domain.state.UiState
import com.gymapp.domain.usecase.GetProgressDataUseCase
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
