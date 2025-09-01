package cz.yogaboy.feature.stocks.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.yogaboy.domain.marketdata.Price
import cz.yogaboy.feature.stocks.domain.GetLatestPriceUseCase
import cz.yogaboy.feature.stocks.presentation.model.DisplayPrice
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

sealed interface StocksUiState<out T> {
    data object Loading : StocksUiState<Nothing>
    data class Data<T>(val value: T) : StocksUiState<T>
    data class Error(val message: String) : StocksUiState<Nothing>
}

data class StocksState(
    val alpha: StocksUiState<DisplayPrice> = StocksUiState.Loading,
    val twelve: StocksUiState<DisplayPrice> = StocksUiState.Loading
)

sealed interface StocksEvent {
    object Refresh : StocksEvent
}

class StocksViewModel(
    private val getAlpha: GetLatestPriceUseCase,
    private val getTwelve: GetLatestPriceUseCase,
    private val ticker: String
) : ViewModel() {

    private val _state = MutableStateFlow(StocksState())
    val state: StateFlow<StocksState> = _state

    init {
        load()
    }

    fun handle(event: StocksEvent) {
        when (event) {
            StocksEvent.Refresh -> load()
        }
    }

    private fun <T, R> Result<T>.toUi(map: (T) -> R): StocksUiState<R> =
        fold(
            onSuccess = { StocksUiState.Data(map(it)) },
            onFailure = { StocksUiState.Error(it.message ?: "Unknown error") }
        )

    private fun load() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    alpha = StocksUiState.Loading,
                    twelve = StocksUiState.Loading
                )
            }

            val alphaDeferred = async { getAlpha(ticker) }
            val twelveDeferred = async { getTwelve(ticker) }

            val alphaRes = alphaDeferred.await()
            val twelveRes = twelveDeferred.await()

            _state.update { s ->
                s.copy(
                    alpha = alphaRes.toUi { it.toDisplay() },
                    twelve = twelveRes.toUi { it.toDisplay() },
                )
            }

            Log.d("STOCKS", "alpha=${alphaRes.getOrNull()}  twelve=${twelveRes.getOrNull()}")
        }
    }
}

private val IN_FMT = DateTimeFormatter.ISO_LOCAL_DATE
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
            try {
                LocalDate.parse(raw, IN_FMT).format(OUT_FMT)
            } catch (_: DateTimeParseException) {
                raw
            }
        }
    )