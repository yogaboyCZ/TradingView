package cz.yogaboy.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeState(
    val query: String = "",
)

sealed interface HomeEvent {
    data class QueryChanged(val value: String) : HomeEvent
    data class ProductSelected(val ticker: String) : HomeEvent
    data object Submit : HomeEvent
    data object Clear : HomeEvent
}

sealed interface HomeEffect {
    data class NavigateToDetail(val ticker: String) : HomeEffect
}

class HomeViewModel : ViewModel() {
    val state: StateFlow<HomeState>
        field: MutableStateFlow<HomeState> = MutableStateFlow(HomeState())

    val effects: SharedFlow<HomeEffect>
        field: MutableSharedFlow<HomeEffect> =
            MutableSharedFlow(extraBufferCapacity = 1)

    fun handle(event: HomeEvent) {
        when (event) {
            is HomeEvent.QueryChanged -> state.update { it.copy(query = event.value) }
            HomeEvent.Clear -> state.value = HomeState()
            is HomeEvent.ProductSelected -> navigateToDetail(event.ticker)
            HomeEvent.Submit -> {
                val q = state.value.query.trim()
                if (q.isNotEmpty()) {
                    navigateToDetail(q)
                }
            }
        }
    }

    private fun navigateToDetail(ticker: String) {
        viewModelScope.launch {
            effects.emit(HomeEffect.NavigateToDetail(ticker.trim().uppercase()))
        }
    }
}
