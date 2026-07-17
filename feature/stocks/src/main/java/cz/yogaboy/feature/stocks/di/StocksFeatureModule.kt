package cz.yogaboy.feature.stocks.di

import cz.yogaboy.domain.marketdata.MarketDataRepository
import cz.yogaboy.feature.stocks.domain.GetLatestPriceUseCase
import cz.yogaboy.feature.stocks.presentation.StocksViewModel
import org.koin.core.annotation.Named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.viewModel

@Named("alphaUC")
private fun alphaUseCase(@Named("alpha") repository: MarketDataRepository) =
    GetLatestPriceUseCase(repository)

@Named("twelveUC")
private fun twelveUseCase(@Named("twelve") repository: MarketDataRepository) =
    GetLatestPriceUseCase(repository)

val stocksFeatureModule = module {
    factory { create(::alphaUseCase) }
    factory { create(::twelveUseCase) }
}

val stocksPresentationModule = module {
    viewModel<StocksViewModel>()
}
