package cz.yogaboy.data.marketdata.twelvedata.di

import cz.yogaboy.data.marketdata.twelvedata.BuildConfig
import cz.yogaboy.data.marketdata.twelvedata.network.TwelveDataApi
import cz.yogaboy.data.marketdata.twelvedata.repository.TwelveMarketDataRepository
import cz.yogaboy.domain.marketdata.MarketDataRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.Converter

val twelveModule = module {
    single { get<Retrofit>(named("twelveRetrofit")).create(TwelveDataApi::class.java) }
    single<MarketDataRepository>(named("twelve")) { TwelveMarketDataRepository(get(), get(named("twelveApiKey"))) }
}

val twelveNetworkModule = module {
    single(named("twelveApiKey")) { BuildConfig.API_KEY }
    single(named("twelveRetrofit")) {
        Retrofit.Builder()
            .client(get())
            .baseUrl(BuildConfig.BASE_URI)
            .addConverterFactory(get<Converter.Factory>())
            .build()
    }
}

