package cz.yogaboy.feature.stocks.presentation

import androidx.lifecycle.ViewModel
import cz.yogaboy.core.common.flow.stateInWhileSubscribed
import cz.yogaboy.feature.stocks.domain.GetLatestPriceUseCase
import cz.yogaboy.feature.stocks.presentation.model.DisplayPrice
import cz.yogaboy.feature.stocks.presentation.model.toDisplayPrice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.yield

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

@OptIn(ExperimentalCoroutinesApi::class)
class StocksViewModel(
    private val getAlpha: GetLatestPriceUseCase,
    private val getTwelve: GetLatestPriceUseCase,
    private val ticker: String
) : ViewModel() {

    private val loadEvents = MutableSharedFlow<Unit>(replay = 1)

    init {
        loadEvents.tryEmit(Unit)
    }

    private val alphaState: StateFlow<StocksUiState<DisplayPrice>> =
        with(this) {
            loadEvents.flatMapLatest {
                flow {
                    emit(StocksUiState.Loading)
                    yield()
                    val res = getAlpha(ticker)
                    emit(
                        res.fold(
                            onSuccess = { StocksUiState.Data(it.toDisplayPrice()) },
                            onFailure = { StocksUiState.Error(it.message ?: "Unknown error") }
                        )
                    )
                }.flowOn(Dispatchers.IO)
            }.stateInWhileSubscribed(StocksUiState.Loading)
        }

    private val twelveState: StateFlow<StocksUiState<DisplayPrice>> =
        with(this) {
            loadEvents.flatMapLatest {
                flow {
                    emit(StocksUiState.Loading)
                    yield()
                    val res = getTwelve(ticker)
                    emit(
                        res.fold(
                            onSuccess = { StocksUiState.Data(it.toDisplayPrice()) },
                            onFailure = { StocksUiState.Error(it.message ?: "Unknown error") }
                        )
                    )
                }.flowOn(Dispatchers.IO)
            }.stateInWhileSubscribed(StocksUiState.Loading)
        }

    val state: StateFlow<StocksState> =
        with(this) {
            combine(alphaState, twelveState) { a, t ->
                StocksState(alpha = a, twelve = t)
            }.stateInWhileSubscribed(StocksState())
        }

    fun handle(event: StocksEvent) {
        when (event) {
            StocksEvent.Refresh -> loadEvents.tryEmit(Unit)
        }
    }
}