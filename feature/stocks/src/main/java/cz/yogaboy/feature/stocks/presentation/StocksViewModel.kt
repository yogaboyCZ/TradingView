package cz.yogaboy.feature.stocks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.yogaboy.data.marketdata.Price
import cz.yogaboy.feature.stocks.domain.GetLatestPriceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StocksState(
    val loading: Boolean = false,
    val price: Price? = null,
    val error: String? = null
)

sealed interface StocksEvent {
    object Refresh : StocksEvent
}

class StocksViewModel(
    private val getLatestPrice: GetLatestPriceUseCase,
    private val ticker: String
) : ViewModel() {
    private val _state = MutableStateFlow(StocksState())
    val state: StateFlow<StocksState> = _state

    init { load() }

    fun handle(event: StocksEvent) {
        when (event) {
            StocksEvent.Refresh -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val r = getLatestPrice(ticker)
            _state.update { s ->
                r.fold(
                    onSuccess = { p -> s.copy(loading = false, price = p, error = null) },
                    onFailure = { e -> s.copy(loading = false, price = null, error = e.message) }
                )
            }
        }
    }
}
