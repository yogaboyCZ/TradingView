package cz.yogaboy.feature.stocks.di

import cz.yogaboy.domain.marketdata.MarketDataRepository
import cz.yogaboy.feature.stocks.domain.GetLatestPriceUseCase
import cz.yogaboy.feature.stocks.presentation.StocksViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val stocksFeatureModule = module {
    factory(named("alphaUC")) { GetLatestPriceUseCase(get<MarketDataRepository>(named("alpha"))) }
    factory(named("twelveUC")) { GetLatestPriceUseCase(get<MarketDataRepository>(named("twelve"))) }
}

val stocksPresentationModule = module {
    viewModel { params ->
        StocksViewModel(
            getAlpha = get(named("alphaUC")),
            getTwelve = get(named("twelveUC")),
            ticker = params.get()
        )
    }
}