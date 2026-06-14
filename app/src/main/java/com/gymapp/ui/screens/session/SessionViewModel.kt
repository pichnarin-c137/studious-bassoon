package com.gymapp.ui.screens.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.data.model.LoggedExercise
import com.gymapp.data.model.SessionType
import com.gymapp.data.model.SetEntry
import com.gymapp.data.model.StreakState
import com.gymapp.data.model.WorkoutPlan
import com.gymapp.domain.intent.SessionIntent
import com.gymapp.domain.usecase.GetWorkoutPlansUseCase
import com.gymapp.domain.usecase.LogDetailedSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One logged set, tagged with its exercise so the flat list folds into exercises on display/submit. */
data class DraftSet(val exercise: String, val reps: Int, val weightKg: Double?)

/**
 * Live-session state. The clock is driven from the UI ([SessionIntent.Tick]) rather than an internal
 * coroutine, which keeps this a pure reducer (trivially testable) and pauses the timer off-screen.
 * A non-null [result] flips the screen to its confirmation.
 */
data class SessionUiState(
    val type: SessionType = SessionType.GYM,
    val elapsedSec: Long = 0,
    val sets: List<DraftSet> = emptyList(),
    val restRemainingSec: Int = 0,
    val submitting: Boolean = false,
    val result: StreakState? = null,
    val availablePlans: List<WorkoutPlan> = emptyList(),
    val selectedPlan: WorkoutPlan? = null,
) {
    val resting: Boolean get() = restRemainingSec > 0

    /** Whole minutes the session has run, rounded up, at least 1 — the honest logged duration. */
    val durationMin: Int get() = ((elapsedSec + 59) / 60).toInt().coerceAtLeast(1)
}

/** Folds the flat draft sets into per-exercise groups, preserving first-seen order. */
fun List<DraftSet>.grouped(): List<LoggedExercise> {
    val order = LinkedHashMap<String, MutableList<SetEntry>>()
    forEach { order.getOrPut(it.exercise) { mutableListOf() }.add(SetEntry(it.reps, it.weightKg)) }
    return order.map { (name, sets) -> LoggedExercise(name, sets) }
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val logDetailedSession: LogDetailedSessionUseCase,
    private val getWorkoutPlans: GetWorkoutPlansUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SessionUiState())
    val state: StateFlow<SessionUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                _state.update { it.copy(availablePlans = getWorkoutPlans()) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Plans are optional guidance; a load failure just leaves a freeform session.
            }
        }
    }

    fun onIntent(intent: SessionIntent) {
        when (intent) {
            SessionIntent.Tick -> _state.update {
                if (it.result != null) {
                    it
                } else {
                    it.copy(
                        elapsedSec = it.elapsedSec + 1,
                        restRemainingSec = (it.restRemainingSec - 1).coerceAtLeast(0),
                    )
                }
            }
            is SessionIntent.SetType -> _state.update { it.copy(type = intent.type) }
            is SessionIntent.AddSet -> _state.update {
                it.copy(sets = it.sets + DraftSet(intent.exercise, intent.reps, intent.weightKg))
            }
            SessionIntent.RemoveLastSet -> _state.update { it.copy(sets = it.sets.dropLast(1)) }
            is SessionIntent.StartRest -> _state.update { it.copy(restRemainingSec = intent.seconds) }
            SessionIntent.StopRest -> _state.update { it.copy(restRemainingSec = 0) }
            is SessionIntent.SelectPlan -> _state.update { it.copy(selectedPlan = intent.plan) }
            is SessionIntent.Finish -> finish(intent.note)
        }
    }

    private fun finish(note: String) {
        val current = _state.value
        if (current.submitting || current.result != null) return
        _state.update { it.copy(submitting = true) }
        viewModelScope.launch {
            try {
                val streak = logDetailedSession(
                    current.type,
                    current.durationMin,
                    current.sets.grouped(),
                    note.trim().ifBlank { null },
                )
                _state.update { it.copy(submitting = false, result = streak) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Keep the live session intact on failure; the mock never fails.
                _state.update { it.copy(submitting = false) }
            }
        }
    }
}
