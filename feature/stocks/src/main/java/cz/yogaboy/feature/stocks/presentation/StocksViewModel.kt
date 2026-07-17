package cz.yogaboy.feature.stocks.presentation

import androidx.lifecycle.ViewModel
import cz.yogaboy.core.common.flow.stateInWhileSubscribed
import cz.yogaboy.feature.stocks.domain.GetLatestPriceUseCase
import cz.yogaboy.feature.stocks.presentation.model.DisplayPrice
import cz.yogaboy.feature.stocks.presentation.model.toDisplayPrice
import cz.yogaboy.domain.marketdata.CompanyDetailsRepository
import cz.yogaboy.domain.marketdata.CompanyNews
import cz.yogaboy.domain.marketdata.CompanyProfile
import cz.yogaboy.domain.marketdata.LivePriceRepository
import cz.yogaboy.domain.marketdata.LivePriceTick
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Named

sealed interface StocksUiState<out T> {
    data object Deferred : StocksUiState<Nothing>
    data object Loading : StocksUiState<Nothing>
    data class Data<T>(val value: T) : StocksUiState<T>
    data class Error(
        val message: String,
        val technicalMessage: String? = null,
    ) : StocksUiState<Nothing>
}

data class StocksState(
    val alpha: StocksUiState<DisplayPrice> = StocksUiState.Loading,
    val twelve: StocksUiState<DisplayPrice> = StocksUiState.Loading,
    val history: StocksUiState<List<PricePoint>> = StocksUiState.Loading,
    val profile: StocksUiState<CompanyProfile> = StocksUiState.Loading,
    val news: StocksUiState<List<CompanyNews>> = StocksUiState.Loading,
    val livePrice: LivePriceTick? = null,
)

sealed interface StocksEvent {
    data object Refresh : StocksEvent
    data object RequestAlphaComparison : StocksEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
class StocksViewModel(
    @Named("alphaUC") private val getAlpha: GetLatestPriceUseCase,
    @Named("twelveUC") private val getTwelve: GetLatestPriceUseCase,
    @InjectedParam private val ticker: String,
    private val companyDetails: CompanyDetailsRepository? = null,
    private val livePrices: LivePriceRepository,
) : ViewModel() {

    // Refresh is an event, not state: do not replay an old refresh when the UI subscribes again.
    // Each provider performs its initial load through onStart below.
    private val refreshEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val alphaRequested = MutableStateFlow(false)

    private val alphaState: StateFlow<StocksUiState<DisplayPrice>> =
        with(this) {
            combine(alphaRequested, refreshEvents.onStart { emit(Unit) }) { requested, _ -> requested }
                .flatMapLatest {
                    if (!it) return@flatMapLatest flow { emit(StocksUiState.Deferred) }
                    flow {
                        emit(StocksUiState.Loading)
                        val result = getAlpha(ticker)
                        emit(
                            result.fold(
                                onSuccess = { StocksUiState.Data(it.toDisplayPrice()) },
                                onFailure = {
                                    StocksUiState.Error(
                                        message = "Srovnávací data teď nejsou dostupná.",
                                        technicalMessage = it.message,
                                    )
                                }
                            )
                        )
                    }.catch { exception ->
                        if (exception is CancellationException) throw exception
                        emit(
                            StocksUiState.Error(
                                message = "Srovnávací data teď nejsou dostupná.",
                                technicalMessage = exception.message,
                            )
                        )
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
                                onFailure = {
                                    StocksUiState.Error(
                                        message = "Aktuální cena teď není dostupná.",
                                        technicalMessage = it.message,
                                    )
                                }
                            )
                        )
                    }.catch { exception ->
                        if (exception is CancellationException) throw exception
                        emit(
                            StocksUiState.Error(
                                message = "Aktuální cena teď není dostupná.",
                                technicalMessage = exception.message,
                            )
                        )
                    }
                }
                .stateInWhileSubscribed(StocksUiState.Loading)
        }

    private val historyState = detailsState { getDailyHistory(ticker) }
    private val profileState = detailsState { getCompanyProfile(ticker) }
    private val newsState = detailsState { getCompanyNews(ticker) }

    private val livePriceState: StateFlow<LivePriceTick?> =
        with(this) {
            twelveState
                .flatMapLatest { quoteState ->
                    val quote = (quoteState as? StocksUiState.Data)?.value
                        ?: return@flatMapLatest flowOf(null)
                    livePrices.observePrices(ticker, quote.last)
                        .map<LivePriceTick, LivePriceTick?> { it }
                        .onStart { emit(null) }
                }
                .stateInWhileSubscribed(null)
        }

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
            }.combine(livePriceState) { current, livePrice ->
                current.copy(livePrice = livePrice)
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
                    emit(
                        StocksUiState.Error(
                            message = "Data teď nejsou dostupná.",
                            technicalMessage = exception.message,
                        )
                    )
                }
            }
            .stateInWhileSubscribed(StocksUiState.Loading)
    }

    fun handle(event: StocksEvent) {
        when (event) {
            StocksEvent.Refresh -> refreshEvents.tryEmit(Unit)
            StocksEvent.RequestAlphaComparison -> alphaRequested.value = true
        }
    }
}
