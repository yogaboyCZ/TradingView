package cz.yogaboy.tv

import android.app.Application
import cz.yogaboy.core.network.di.networkModule
import cz.yogaboy.data.marketdata.alpha.di.marketDataAlphaModule
import cz.yogaboy.data.marketdata.alpha.di.marketDataAlphaNetworkModule
import cz.yogaboy.data.marketdata.twelvedata.di.twelveModule
import cz.yogaboy.data.marketdata.twelvedata.di.twelveNetworkModule
import cz.yogaboy.feature.stocks.di.stocksFeatureModule
import cz.yogaboy.feature.stocks.di.stocksPresentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import cz.yogaboy.feature.home.di.homeModule


class TvApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TvApp)
            modules(
                networkModule,
                marketDataAlphaNetworkModule,
                marketDataAlphaModule,
                twelveNetworkModule,
                twelveModule,
                stocksFeatureModule,
                stocksPresentationModule,
                homeModule,
            )
        }
    }
}
