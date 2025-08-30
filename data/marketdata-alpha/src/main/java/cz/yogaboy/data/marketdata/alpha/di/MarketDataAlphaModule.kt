// data/marketdata-alpha/src/main/java/.../di/AlphaNetworkModule.kt
package cz.yogaboy.data.marketdata.alpha.di

import com.squareup.moshi.Moshi
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import cz.yogaboy.data.marketdata.alpha.BuildConfig
import cz.yogaboy.data.marketdata.alpha.network.AlphaVantageApi
import cz.yogaboy.domain.marketdata.MarketDataRepository
import cz.yogaboy.data.marketdata.alpha.repository.AlphaMarketDataRepository

val marketDataAlphaNetworkModule = module {
    single(named("alphaApiKey")) { BuildConfig.API_KEY }

    // Reuse Moshi z core:
    single(named("alphaRetrofit")) {
        val moshi: Moshi = get()
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URI)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
}

val marketDataAlphaModule = module {
    single { get<Retrofit>(named("alphaRetrofit")).create(AlphaVantageApi::class.java) }
    single<MarketDataRepository> { AlphaMarketDataRepository(get(), get(named("alphaApiKey"))) }
}