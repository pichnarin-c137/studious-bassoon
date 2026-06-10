package com.gymapp.ui.screens.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.data.model.ActivityFeedItem
import com.gymapp.domain.intent.ActivityIntent
import com.gymapp.domain.state.ActivityData
import com.gymapp.domain.state.UiState
import com.gymapp.domain.usecase.GetActivityDataUseCase
import com.gymapp.domain.usecase.ToggleKudosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val getActivityData: GetActivityDataUseCase,
    private val toggleKudosUseCase: ToggleKudosUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<ActivityData>>(UiState.Loading)
    val state: StateFlow<UiState<ActivityData>> = _state.asStateFlow()

    init {
        onIntent(ActivityIntent.Load)
    }

    fun onIntent(intent: ActivityIntent) {
        when (intent) {
            ActivityIntent.Load, ActivityIntent.Retry -> load()
            is ActivityIntent.ToggleKudos -> toggleKudos(intent.itemId)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                _state.value = UiState.Success(getActivityData())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message)
            }
        }
    }

    private fun toggleKudos(itemId: String) {
        val data = (_state.value as? UiState.Success)?.data ?: return
        val item = data.feed.firstOrNull { it.id == itemId } ?: return
        if (item.isYou) return // you can't kudos your own session
        val give = !item.youGaveKudos
        setKudos(itemId, give) // optimistic
        viewModelScope.launch {
            try {
                toggleKudosUseCase(itemId, give)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setKudos(itemId, !give) // revert on failure
            }
        }
    }

    /** Sets one item's kudos state, keeping the count in step with [give]. */
    private fun setKudos(itemId: String, give: Boolean) {
        val current = _state.value
        if (current !is UiState.Success) return
        val feed = current.data.feed.map { item ->
            if (item.id != itemId || item.youGaveKudos == give) {
                item
            } else {
                item.copy(youGaveKudos = give, kudosCount = item.kudosCount + if (give) 1 else -1)
            }
        }
        _state.value = UiState.Success(current.data.copy(feed = feed))
    }
}
