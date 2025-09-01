package cz.yogaboy.feature.stocks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.yogaboy.core.common.state.NetworkViewState
import cz.yogaboy.domain.marketdata.Price
import cz.yogaboy.feature.stocks.domain.GetLatestPriceUseCase
import cz.yogaboy.feature.stocks.presentation.model.DisplayPrice
import cz.yogaboy.feature.stocks.presentation.model.toDisplayPrice
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

data class StocksState(
    val alpha: NetworkViewState<DisplayPrice> = NetworkViewState.Loading(),
    val twelve: NetworkViewState<DisplayPrice> = NetworkViewState.Loading()
)

sealed interface StocksEvent {
    object Refresh : StocksEvent
    object Clear : StocksEvent
}

class StocksViewModel(
    private val getAlpha: GetLatestPriceUseCase,
    private val getTwelve: GetLatestPriceUseCase,
    private val ticker: String
) : ViewModel() {

    private val _state = MutableStateFlow(StocksState())
    val state: StateFlow<StocksState> = _state

    init { load() }

    fun handle(event: StocksEvent) {
        when (event) {
            StocksEvent.Refresh -> load()
            StocksEvent.Clear -> _state.value = StocksState()
        }
    }

    private fun <T, R> Result<T>.toViewState(map: (T) -> R): NetworkViewState<R> =
        fold(
            onSuccess = { NetworkViewState.Success(map(it)) },
            onFailure = { NetworkViewState.Error(it, it.message) }
        )

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(alpha = NetworkViewState.Loading(), twelve = NetworkViewState.Loading()) }

            val alphaDeferred = async { getAlpha(ticker) }
            val twelveDeferred = async { getTwelve(ticker) }

            val alphaRes = alphaDeferred.await()
            val twelveRes = twelveDeferred.await()

            _state.update { state ->
                state.copy(
                    alpha = alphaRes.toViewState { it.toDisplayPrice() },
                    twelve = twelveRes.toViewState { it.toDisplayPrice() },
                )
            }
        }
    }
}

private val IN_FMT  = DateTimeFormatter.ISO_LOCAL_DATE
private val OUT_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale("cs", "CZ"))

fun Price.toDisplay(): DisplayPrice =
    DisplayPrice(
        ticker = ticker,
        last = last,
        change = change,
        changePercent = changePercent,
        previousClose = previousClose,
        name = name,
        asOf = asOf?.let { raw ->
            try { LocalDate.parse(raw, IN_FMT).format(OUT_FMT) } catch (_: DateTimeParseException) { raw }

        }
    )