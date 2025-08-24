package cz.yogaboy.data.marketdata.alpha.di

import cz.yogaboy.domain.marketdata.MarketDataRepository
import cz.yogaboy.data.marketdata.alpha.BuildConfig
import cz.yogaboy.data.marketdata.alpha.network.AlphaVantageApi
import cz.yogaboy.data.marketdata.alpha.repository.AlphaMarketDataRepository
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
//    single(named("alphaMoshi")) {
//        Moshi.Builder()
//            .add(KotlinJsonAdapterFactory())
//            .build()
//    }
    single(named("alphaRetrofit")) {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URI)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }
}