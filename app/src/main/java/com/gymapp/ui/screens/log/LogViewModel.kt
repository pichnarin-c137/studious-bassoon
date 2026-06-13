package com.gymapp.ui.screens.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.domain.intent.LogIntent
import com.gymapp.domain.state.LogData
import com.gymapp.domain.state.UiState
import com.gymapp.domain.usecase.GetLogDataUseCase
import com.gymapp.domain.usecase.LogDefaults
import com.gymapp.domain.usecase.LogSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    private val getLogData: GetLogDataUseCase,
    private val logSession: LogSessionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<LogData>>(UiState.Loading)
    val state: StateFlow<UiState<LogData>> = _state.asStateFlow()

    init {
        onIntent(LogIntent.Load)
    }

    fun onIntent(intent: LogIntent) {
        when (intent) {
            LogIntent.Load, LogIntent.Retry -> load()
            is LogIntent.SelectType -> update { it.copy(selectedType = intent.type) }
            LogIntent.IncrementDuration -> update {
                it.copy(durationMin = (it.durationMin + LogDefaults.DURATION_STEP).coerceAtMost(LogDefaults.DURATION_MAX))
            }
            LogIntent.DecrementDuration -> update {
                it.copy(durationMin = (it.durationMin - LogDefaults.DURATION_STEP).coerceAtLeast(LogDefaults.DURATION_MIN))
            }
            is LogIntent.SetDuration -> update {
                it.copy(durationMin = intent.durationMin.coerceIn(LogDefaults.DURATION_MIN, LogDefaults.DURATION_MAX))
            }
            LogIntent.RepeatLast -> update { d ->
                d.copy(
                    selectedType = d.lastSession?.type ?: d.selectedType,
                    durationMin = d.lastSession?.durationMin
                        ?.coerceIn(LogDefaults.DURATION_MIN, LogDefaults.DURATION_MAX) ?: d.durationMin,
                )
            }
            LogIntent.Submit -> submit()
            LogIntent.LogAnother -> update {
                it.copy(justLogged = false, selectedType = null, durationMin = LogDefaults.DURATION_DEFAULT)
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                _state.value = UiState.Success(getLogData())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message)
            }
        }
    }

    private fun submit() {
        val data = (_state.value as? UiState.Success)?.data ?: return
        val type = data.selectedType ?: return // CTA is disabled until a type is picked.
        viewModelScope.launch {
            try {
                val streak = logSession(type, data.durationMin)
                update { it.copy(justLogged = true, streak = streak) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Keep the form intact on failure; the mock never fails. A later pass can surface this.
            }
        }
    }

    /** Applies [transform] only when data is loaded — form edits never clobber Loading/Error. */
    private fun update(transform: (LogData) -> LogData) {
        val current = _state.value
        if (current is UiState.Success) _state.value = UiState.Success(transform(current.data))
    }
}
