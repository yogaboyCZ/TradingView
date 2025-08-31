package cz.yogaboy.data.marketdata.alpha.di

import cz.yogaboy.data.marketdata.alpha.BuildConfig
import cz.yogaboy.data.marketdata.alpha.network.AlphaVantageApi
import cz.yogaboy.data.marketdata.alpha.repository.AlphaMarketDataRepository
import cz.yogaboy.domain.marketdata.MarketDataRepository
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Converter
import retrofit2.Retrofit

val marketDataAlphaNetworkModule = module {
    single(named("alphaApiKey")) { BuildConfig.API_KEY }

    single(named("alphaRetrofit")) {
        Retrofit.Builder()
            .client(get<OkHttpClient>())
            .baseUrl(BuildConfig.BASE_URI)
            .addConverterFactory(get<Converter.Factory>())
            .build()
    }

    single {
        get<Retrofit>(named("alphaRetrofit")).create(AlphaVantageApi::class.java)
    }
}

val marketDataAlphaModule = module {
    single<MarketDataRepository>(named("alpha")) {
        AlphaMarketDataRepository(
            api = get(),
            apiKey = get(named("alphaApiKey"))
        )
    }
    single<MarketDataRepository> { get(named("alpha")) }
}