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

data class StocksState(
    val loading: Boolean = false,
    val alphaPrice: DisplayPrice? = null,
    val twelvePrice: DisplayPrice? = null,
    val error: String? = null
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

    init { load() }

    fun handle(event: StocksEvent) {
        when (event) {
            StocksEvent.Refresh -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }

            val alphaDeferred = async { getAlpha(ticker) }
            val twelveDeferred = async { getTwelve(ticker) }

            val alpha = alphaDeferred.await()
            val twelve = twelveDeferred.await()

            _state.update { s ->
                s.copy(
                    loading = false,
                    alphaPrice = alpha.getOrNull()?.toDisplay(),
                    twelvePrice = twelve.getOrNull()?.toDisplay(),
                    error = when {
                        alpha.isFailure && twelve.isFailure ->
                            alpha.exceptionOrNull()?.message ?: twelve.exceptionOrNull()?.message
                        else -> null
                    }
                )
            }
            //TODO use Timber for logging
            Log.d("STOCKS", "LSY alpha=${alpha.getOrNull()}  twelve=${twelve.getOrNull()}")
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