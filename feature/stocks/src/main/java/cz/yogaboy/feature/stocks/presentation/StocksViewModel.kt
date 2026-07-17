package cz.yogaboy.feature.stocks.presentation

import androidx.lifecycle.ViewModel
import cz.yogaboy.core.common.flow.stateInWhileSubscribed
import cz.yogaboy.feature.stocks.domain.GetLatestPriceUseCase
import cz.yogaboy.feature.stocks.presentation.model.DisplayPrice
import cz.yogaboy.feature.stocks.presentation.model.toDisplayPrice
import cz.yogaboy.domain.marketdata.CompanyDetailsRepository
import cz.yogaboy.domain.marketdata.CompanyNews
import cz.yogaboy.domain.marketdata.CompanyProfile
import cz.yogaboy.domain.marketdata.PricePoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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
    val twelve: StocksUiState<DisplayPrice> = StocksUiState.Loading,
    val history: StocksUiState<List<PricePoint>> = StocksUiState.Loading,
    val profile: StocksUiState<CompanyProfile> = StocksUiState.Loading,
    val news: StocksUiState<List<CompanyNews>> = StocksUiState.Loading,
)

sealed interface StocksEvent {
    data object Refresh : StocksEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
class StocksViewModel(
    private val getAlpha: GetLatestPriceUseCase,
    private val getTwelve: GetLatestPriceUseCase,
    private val ticker: String,
    private val companyDetails: CompanyDetailsRepository? = null,
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

    private val historyState = detailsState { getDailyHistory(ticker) }
    // These endpoints require a paid Twelve Data tier. Avoid spending two API
    // credits on every opened ticker when they cannot succeed on the free plan.
    private val profileState = MutableStateFlow<StocksUiState<CompanyProfile>>(
        StocksUiState.Error("Firemní profil vyžaduje placený Twelve Data tarif.")
    )
    private val newsState = MutableStateFlow<StocksUiState<List<CompanyNews>>>(
        StocksUiState.Error("Zprávy společnosti vyžadují placený Twelve Data tarif.")
    )

    val state: StateFlow<StocksState> =
        with(this) {
            combine(alphaState, twelveState, historyState, profileState, newsState) {
                    alpha, twelve, history, profile, news ->
                StocksState(
                    alpha = alpha,
                    twelve = twelve,
                    history = history,
                    profile = profile,
                    news = news,
                )
            }.stateInWhileSubscribed(StocksState())
        }

    private fun <T> detailsState(
        load: suspend CompanyDetailsRepository.() -> T,
    ): StateFlow<StocksUiState<T>> = with(this) {
        refreshEvents
            .onStart { emit(Unit) }
            .flatMapLatest {
                flow {
                    emit(StocksUiState.Loading)
                    val repository = companyDetails
                    if (repository == null) {
                        emit(StocksUiState.Error("Zdroj detailních dat není dostupný."))
                    } else {
                        emit(StocksUiState.Data(repository.load()))
                    }
                }.catch { exception ->
                    if (exception is CancellationException) throw exception
                    emit(StocksUiState.Error(exception.message ?: "Data nejsou dostupná."))
                }
            }
            .stateInWhileSubscribed(StocksUiState.Loading)
    }

    fun handle(event: StocksEvent) {
        when (event) {
            StocksEvent.Refresh -> refreshEvents.tryEmit(Unit)
        }
    }
}
