package cz.yogaboy.data.marketdata.simulated.di

import cz.yogaboy.data.marketdata.simulated.DemoSuggestedProductsRepository
import cz.yogaboy.data.marketdata.simulated.SimulatedLivePriceRepository
import cz.yogaboy.domain.marketdata.LivePriceRepository
import cz.yogaboy.domain.marketdata.SuggestedProductsRepository
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create

private fun simulatedLivePrices(): LivePriceRepository = SimulatedLivePriceRepository()
private fun suggestedProducts(): SuggestedProductsRepository = DemoSuggestedProductsRepository()

val simulatedMarketDataModule = module {
    single { create(::simulatedLivePrices) } bind LivePriceRepository::class
    single { create(::suggestedProducts) } bind SuggestedProductsRepository::class
}
