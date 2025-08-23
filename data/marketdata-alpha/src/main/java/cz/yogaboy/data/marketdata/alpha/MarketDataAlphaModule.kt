package cz.yogaboy.data.marketdata.alpha

import cz.yogaboy.core.network.BuildConfig
import cz.yogaboy.data.marketdata.MarketDataRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val marketDataAlphaModule = module {
    single { get<Retrofit>(named("alphaRetrofit")).create(AlphaVantageApi::class.java) }
    single<MarketDataRepository> { AlphaMarketDataRepository(get(), get(named("alphaApiKey"))) }
}

val marketDataAlphaNetworkModule = module {
    single(named("alphaApiKey")) { BuildConfig.API_KEY }
    single(named("alphaRetrofit")) {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URI)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }
}