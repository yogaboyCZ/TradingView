package cz.yogaboy.data.marketdata.cache

import com.squareup.moshi.Moshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import cz.yogaboy.domain.marketdata.CompanyDetailsRepository
import cz.yogaboy.domain.marketdata.CompanyNews
import cz.yogaboy.domain.marketdata.CompanyProfile
import cz.yogaboy.domain.marketdata.CompanyProfileRepository
import cz.yogaboy.domain.marketdata.MarketDataRepository
import cz.yogaboy.domain.marketdata.Price
import cz.yogaboy.domain.marketdata.PricePoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CachedMarketDataRepository(
    private val provider: String,
    private val remote: MarketDataRepository,
    private val cache: MarketDataCache,
    moshi: Moshi,
) : MarketDataRepository {
    private val adapter = moshi.adapter(Price::class.java)

    override suspend fun getLatestPrice(ticker: String): Price? {
        val key = "$provider:${ticker.uppercase()}:price"
        return cache.withKeyLock(key) {
            val cached = cache.read(provider, ticker, "price", adapter)
            if (cached != null && cached.ageMillis <= CachePolicy.PRICE_FRESH) return@withKeyLock cached.value

            val fresh = runCatching { remote.getLatestPrice(ticker) }.getOrNull()
            if (fresh != null) {
                cache.write(provider, ticker, "price", fresh, adapter)
                fresh
            } else {
                cached?.takeIf { it.ageMillis <= CachePolicy.PRICE_MAX_STALE }?.value
            }
        }
    }
}

class CachedCompanyDetailsRepository(
    private val provider: String,
    private val remote: CompanyDetailsRepository,
    private val cache: MarketDataCache,
    moshi: Moshi,
) : CompanyDetailsRepository {
    private val historyAdapter = moshi.adapter<List<PricePoint>>(
        Types.newParameterizedType(List::class.java, PricePoint::class.java)
    )
    private val profileAdapter = moshi.adapter(CompanyProfile::class.java)
    private val newsAdapter = moshi.adapter<List<CompanyNews>>(
        Types.newParameterizedType(List::class.java, CompanyNews::class.java)
    )

    override suspend fun getDailyHistory(ticker: String): List<PricePoint> = cached(
        ticker = ticker,
        dataType = HISTORY_DATA_TYPE,
        freshFor = CachePolicy.HISTORY_FRESH,
        maxStale = CachePolicy.DETAILS_MAX_STALE,
        adapter = historyAdapter,
    ) { remote.getDailyHistory(ticker) }

    override fun observeDailyHistory(ticker: String): Flow<List<PricePoint>?> =
        cache.observe(provider, ticker, HISTORY_DATA_TYPE, historyAdapter).map { it?.value }

    override suspend fun getCompanyProfile(ticker: String): CompanyProfile = cached(
        ticker = ticker,
        dataType = "profile",
        freshFor = CachePolicy.PROFILE_FRESH,
        maxStale = CachePolicy.PROFILE_MAX_STALE,
        adapter = profileAdapter,
    ) { remote.getCompanyProfile(ticker) }

    override suspend fun getCompanyNews(ticker: String): List<CompanyNews> = cached(
        ticker = ticker,
        dataType = "news",
        freshFor = CachePolicy.NEWS_FRESH,
        maxStale = CachePolicy.DETAILS_MAX_STALE,
        adapter = newsAdapter,
    ) { remote.getCompanyNews(ticker) }

    private suspend fun <T> cached(
        ticker: String,
        dataType: String,
        freshFor: Long,
        maxStale: Long,
        adapter: JsonAdapter<T>,
        fetch: suspend () -> T,
    ): T = cache.withKeyLock("$provider:${ticker.uppercase()}:$dataType") {
        val cached = cache.read(provider, ticker, dataType, adapter)
        if (cached != null && cached.ageMillis <= freshFor) return@withKeyLock cached.value

        try {
            fetch().also { cache.write(provider, ticker, dataType, it, adapter) }
        } catch (error: Throwable) {
            cached?.takeIf { it.ageMillis <= maxStale }?.value ?: throw error
        }
    }

    private companion object {
        const val HISTORY_DATA_TYPE = "history-1day-365"
    }
}

class CachedCompanyProfileRepository(
    private val provider: String,
    private val remote: CompanyProfileRepository,
    private val cache: MarketDataCache,
    moshi: Moshi,
) : CompanyProfileRepository {
    private val adapter = moshi.adapter(CompanyProfile::class.java)

    override suspend fun getCompanyProfile(ticker: String): CompanyProfile =
        cache.withKeyLock("$provider:${ticker.uppercase()}:profile") {
            val cached = cache.read(provider, ticker, "profile", adapter)
            if (cached != null && cached.ageMillis <= CachePolicy.PROFILE_FRESH) {
                return@withKeyLock cached.value
            }

            try {
                remote.getCompanyProfile(ticker).also {
                    cache.write(provider, ticker, "profile", it, adapter)
                }
            } catch (error: Throwable) {
                cached?.takeIf { it.ageMillis <= CachePolicy.PROFILE_MAX_STALE }?.value
                    ?: throw error
            }
        }
}
