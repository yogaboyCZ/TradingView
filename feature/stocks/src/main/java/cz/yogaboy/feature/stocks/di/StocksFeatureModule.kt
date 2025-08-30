package cz.yogaboy.feature.stocks.di

import cz.yogaboy.domain.marketdata.MarketDataRepository
import cz.yogaboy.feature.stocks.domain.GetLatestPriceUseCase
import cz.yogaboy.feature.stocks.presentation.StocksViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val stocksPresentationModule = module {
    viewModel { (ticker: String) -> StocksViewModel(get(), ticker) }
}

val stocksFeatureModule = module {
    factory { GetLatestPriceUseCase(get<MarketDataRepository>()) }
}
