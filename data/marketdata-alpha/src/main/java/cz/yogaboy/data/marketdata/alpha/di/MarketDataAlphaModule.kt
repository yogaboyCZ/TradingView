package cz.yogaboy.data.marketdata.alpha.di

import com.squareup.moshi.Moshi
import cz.yogaboy.data.marketdata.alpha.BuildConfig
import cz.yogaboy.data.marketdata.alpha.network.AlphaVantageApi
import cz.yogaboy.data.marketdata.alpha.repository.AlphaMarketDataRepository
import cz.yogaboy.data.marketdata.alpha.repository.AlphaCompanyProfileRepository
import cz.yogaboy.data.marketdata.cache.CachedCompanyProfileRepository
import cz.yogaboy.data.marketdata.cache.CachedMarketDataRepository
import cz.yogaboy.data.marketdata.cache.MarketDataCache
import cz.yogaboy.domain.marketdata.MarketDataRepository
import cz.yogaboy.domain.marketdata.CompanyProfileRepository
import okhttp3.OkHttpClient
import org.koin.core.annotation.Named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import retrofit2.Converter
import retrofit2.Retrofit

@Named("alphaApiKey")
private fun alphaApiKey(): String = BuildConfig.API_KEY

@Named("alphaRetrofit")
private fun alphaRetrofit(client: OkHttpClient, converter: Converter.Factory): Retrofit =
    Retrofit.Builder()
        .client(client)
        .baseUrl(BuildConfig.BASE_URI)
        .addConverterFactory(converter)
        .build()

private fun alphaApi(@Named("alphaRetrofit") retrofit: Retrofit): AlphaVantageApi =
    retrofit.create(AlphaVantageApi::class.java)

@Named("alphaRemote")
private fun alphaRemote(
    api: AlphaVantageApi,
    @Named("alphaApiKey") apiKey: String,
): MarketDataRepository = AlphaMarketDataRepository(api, apiKey)

@Named("alpha")
private fun cachedAlpha(
    @Named("alphaRemote") remote: MarketDataRepository,
    cache: MarketDataCache,
    moshi: Moshi,
): MarketDataRepository = CachedMarketDataRepository("alpha", remote, cache, moshi)

@Named("alphaProfileRemote")
private fun alphaProfileRemote(
    api: AlphaVantageApi,
    @Named("alphaApiKey") apiKey: String,
): CompanyProfileRepository = AlphaCompanyProfileRepository(api, apiKey)

@Named("alphaProfile")
private fun cachedAlphaProfile(
    @Named("alphaProfileRemote") remote: CompanyProfileRepository,
    cache: MarketDataCache,
    moshi: Moshi,
): CompanyProfileRepository = CachedCompanyProfileRepository("alpha", remote, cache, moshi)

val marketDataAlphaNetworkModule = module {
    single { create(::alphaApiKey) }
    single { create(::alphaRetrofit) }
    single { create(::alphaApi) }
}

val marketDataAlphaModule = module {
    single { create(::alphaRemote) }
    single { create(::cachedAlpha) } bind MarketDataRepository::class
    single { create(::alphaProfileRemote) }
    single { create(::cachedAlphaProfile) }
}
