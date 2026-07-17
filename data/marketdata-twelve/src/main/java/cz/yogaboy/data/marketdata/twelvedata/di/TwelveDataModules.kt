package cz.yogaboy.data.marketdata.twelvedata.di

import com.squareup.moshi.Moshi
import cz.yogaboy.data.marketdata.cache.CachedCompanyDetailsRepository
import cz.yogaboy.data.marketdata.cache.CachedMarketDataRepository
import cz.yogaboy.data.marketdata.cache.MarketDataCache
import cz.yogaboy.data.marketdata.twelvedata.BuildConfig
import cz.yogaboy.data.marketdata.twelvedata.network.TwelveDataApi
import cz.yogaboy.data.marketdata.twelvedata.repository.TwelveCompanyDetailsRepository
import cz.yogaboy.data.marketdata.twelvedata.repository.TwelveMarketDataRepository
import cz.yogaboy.data.marketdata.twelvedata.repository.FallbackCompanyDetailsRepository
import cz.yogaboy.domain.marketdata.CompanyDetailsRepository
import cz.yogaboy.domain.marketdata.CompanyProfileRepository
import cz.yogaboy.domain.marketdata.MarketDataRepository
import okhttp3.OkHttpClient
import org.koin.core.annotation.Named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import retrofit2.Converter
import retrofit2.Retrofit

@Named("twelveApiKey")
private fun twelveApiKey(): String = BuildConfig.API_KEY

@Named("twelveRetrofit")
private fun twelveRetrofit(client: OkHttpClient, converter: Converter.Factory): Retrofit =
    Retrofit.Builder()
        .client(client)
        .baseUrl(BuildConfig.BASE_URI)
        .addConverterFactory(converter)
        .build()

private fun twelveApi(@Named("twelveRetrofit") retrofit: Retrofit): TwelveDataApi =
    retrofit.create(TwelveDataApi::class.java)

@Named("twelveRemote")
private fun twelveRemote(
    api: TwelveDataApi,
    @Named("twelveApiKey") apiKey: String,
): MarketDataRepository = TwelveMarketDataRepository(api, apiKey)

@Named("twelve")
private fun cachedTwelve(
    @Named("twelveRemote") remote: MarketDataRepository,
    cache: MarketDataCache,
    moshi: Moshi,
): MarketDataRepository = CachedMarketDataRepository("twelve", remote, cache, moshi)

@Named("twelveDetailsRemote")
private fun twelveDetailsRemote(
    api: TwelveDataApi,
    @Named("twelveApiKey") apiKey: String,
): CompanyDetailsRepository = TwelveCompanyDetailsRepository(api, apiKey)

@Named("twelveDetails")
private fun cachedDetails(
    @Named("twelveDetailsRemote") remote: CompanyDetailsRepository,
    cache: MarketDataCache,
    moshi: Moshi,
): CompanyDetailsRepository = CachedCompanyDetailsRepository("twelve", remote, cache, moshi)

private fun fallbackDetails(
    @Named("twelveDetails") primary: CompanyDetailsRepository,
    @Named("alphaProfile") profileFallback: CompanyProfileRepository,
): CompanyDetailsRepository = FallbackCompanyDetailsRepository(primary, profileFallback)

val twelveNetworkModule = module {
    single { create(::twelveApiKey) }
    single { create(::twelveRetrofit) }
    single { create(::twelveApi) }
}

val twelveModule = module {
    single { create(::twelveRemote) }
    single { create(::cachedTwelve) }
    single { create(::twelveDetailsRemote) }
    single { create(::cachedDetails) }
    single { create(::fallbackDetails) }
}
