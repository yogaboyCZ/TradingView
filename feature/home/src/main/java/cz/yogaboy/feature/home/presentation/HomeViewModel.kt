package cz.yogaboy.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeState(
    val query: String = "",
    val showPlaceholder: Boolean = true
)

sealed interface HomeEvent {
    data class QueryChanged(val value: String) : HomeEvent
    data object Submit : HomeEvent
    data object Clear : HomeEvent
}

sealed interface HomeEffect {
    data class NavigateToDetail(val ticker: String) : HomeEffect
}

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<HomeEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<HomeEffect> = _effects.asSharedFlow()

    fun handle(event: HomeEvent) {
        when (event) {
            is HomeEvent.QueryChanged -> _state.update { it.copy(query = event.value) }
            HomeEvent.Clear -> _state.value = HomeState()
            HomeEvent.Submit -> {
                val q = _state.value.query.trim()
                if (q.isNotEmpty()) {
                    _state.update { it.copy(showPlaceholder = false) }
                    viewModelScope.launch { _effects.emit(HomeEffect.NavigateToDetail(q)) }
                }
            }
        }
    }
}