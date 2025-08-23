package cz.yogaboy.tv

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import cz.yogaboy.core.network.networkModule
import cz.yogaboy.data.marketdata.alpha.marketDataAlphaModule
import cz.yogaboy.data.marketdata.alpha.marketDataAlphaNetworkModule

class TvApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TvApp)
            modules(
                networkModule,
                marketDataAlphaNetworkModule,
                marketDataAlphaModule
            )
        }
    }
}
