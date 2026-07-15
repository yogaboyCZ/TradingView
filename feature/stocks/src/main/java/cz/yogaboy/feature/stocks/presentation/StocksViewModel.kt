package cz.yogaboy.feature.stocks.presentation

import androidx.lifecycle.ViewModel
import cz.yogaboy.core.common.flow.stateInWhileSubscribed
import cz.yogaboy.feature.stocks.domain.GetLatestPriceUseCase
import cz.yogaboy.feature.stocks.presentation.model.DisplayPrice
import cz.yogaboy.feature.stocks.presentation.model.toDisplayPrice
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

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
    data object Refresh : StocksEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
class StocksViewModel(
    private val getAlpha: GetLatestPriceUseCase,
    private val getTwelve: GetLatestPriceUseCase,
    private val ticker: String
) : ViewModel() {

    // Refresh is an event, not state: do not replay an old refresh when the UI subscribes again.
    // Each provider performs its initial load through onStart below.
    private val refreshEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val alphaState: StateFlow<StocksUiState<DisplayPrice>> =
        with(this) {
            refreshEvents
                .onStart { emit(Unit) }
                .flatMapLatest {
                    flow {
                        emit(StocksUiState.Loading)
                        val result = getAlpha(ticker)
                        emit(
                            result.fold(
                                onSuccess = { StocksUiState.Data(it.toDisplayPrice()) },
                                onFailure = { StocksUiState.Error(it.message ?: "Unknown error") }
                            )
                        )
                    }.catch { exception ->
                        if (exception is CancellationException) throw exception
                        emit(StocksUiState.Error(exception.message ?: "Unknown error"))
                    }
                }
                .stateInWhileSubscribed(StocksUiState.Loading)
        }

    private val twelveState: StateFlow<StocksUiState<DisplayPrice>> =
        with(this) {
            refreshEvents
                .onStart { emit(Unit) }
                .flatMapLatest {
                    flow {
                        emit(StocksUiState.Loading)
                        val result = getTwelve(ticker)
                        emit(
                            result.fold(
                                onSuccess = { StocksUiState.Data(it.toDisplayPrice()) },
                                onFailure = { StocksUiState.Error(it.message ?: "Unknown error") }
                            )
                        )
                    }.catch { exception ->
                        if (exception is CancellationException) throw exception
                        emit(StocksUiState.Error(exception.message ?: "Unknown error"))
                    }
                }
                .stateInWhileSubscribed(StocksUiState.Loading)
        }

    val state: StateFlow<StocksState> =
        with(this) {
            combine(alphaState, twelveState) { a, t ->
                StocksState(alpha = a, twelve = t)
            }.stateInWhileSubscribed(StocksState())
        }

    fun handle(event: StocksEvent) {
        when (event) {
            StocksEvent.Refresh -> refreshEvents.tryEmit(Unit)
        }
    }
}
