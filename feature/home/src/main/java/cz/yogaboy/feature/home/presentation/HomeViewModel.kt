package cz.yogaboy.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.yogaboy.domain.marketdata.CompanyDetailsRepository
import cz.yogaboy.domain.marketdata.PricePoint
import cz.yogaboy.domain.marketdata.SuggestedProduct
import cz.yogaboy.domain.marketdata.SuggestedProductsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeState(
    val query: String = "",
    val suggestedProducts: List<SuggestedProduct> = emptyList(),
    val histories: Map<String, List<PricePoint>> = emptyMap(),
    val failedHistories: Set<String> = emptySet(),
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

class HomeViewModel(
    private val companyDetails: CompanyDetailsRepository,
    suggestedProductsRepository: SuggestedProductsRepository,
) : ViewModel() {
    private val suggestedProducts = suggestedProductsRepository.getSuggestedProducts()

    val state: StateFlow<HomeState>
        field: MutableStateFlow<HomeState> = MutableStateFlow(
            HomeState(suggestedProducts = suggestedProducts),
        )

    val effects: SharedFlow<HomeEffect>
        field: MutableSharedFlow<HomeEffect> =
            MutableSharedFlow(extraBufferCapacity = 1)

    fun handle(event: HomeEvent) {
        when (event) {
            is HomeEvent.QueryChanged -> state.update { it.copy(query = event.value) }
            HomeEvent.Clear -> state.update { it.copy(query = "") }
            is HomeEvent.ProductSelected -> navigateToDetail(event.ticker)
            HomeEvent.Submit -> {
                val q = state.value.query.trim()
                if (q.isNotEmpty()) {
                    navigateToDetail(q)
                }
            }
        }
    }

    init {
        suggestedProducts.forEach { product ->
            viewModelScope.launch {
                val ticker = product.ticker
                companyDetails.observeDailyHistory(ticker).collect { history ->
                    if (!history.isNullOrEmpty()) {
                        state.update { current ->
                            current.copy(
                                histories = current.histories + (ticker to history),
                                failedHistories = current.failedHistories - ticker,
                            )
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            suggestedProducts.forEach { product ->
                val ticker = product.ticker
                runCatching { companyDetails.getDailyHistory(ticker) }
                    .onFailure {
                        state.update { current ->
                            if (ticker in current.histories) current else current.copy(
                                failedHistories = current.failedHistories + ticker,
                            )
                        }
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
