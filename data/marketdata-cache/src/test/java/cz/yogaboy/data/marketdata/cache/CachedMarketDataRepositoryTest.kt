package cz.yogaboy.data.marketdata.cache

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import cz.yogaboy.domain.marketdata.MarketDataRepository
import cz.yogaboy.domain.marketdata.CompanyDetailsRepository
import cz.yogaboy.domain.marketdata.CompanyNews
import cz.yogaboy.domain.marketdata.CompanyProfile
import cz.yogaboy.domain.marketdata.Price
import cz.yogaboy.domain.marketdata.PricePoint
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

private class FakeCache(
    var stored: Any? = null,
    var ageMillis: Long = 0,
) : MarketDataCache {
    var writes = 0

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> read(
        provider: String,
        ticker: String,
        dataType: String,
        adapter: JsonAdapter<T>,
    ): CachedValue<T>? = stored?.let { CachedValue(it as T, ageMillis) }

    override suspend fun <T> write(
        provider: String,
        ticker: String,
        dataType: String,
        value: T,
        adapter: JsonAdapter<T>,
    ) {
        stored = value
        ageMillis = 0
        writes++
    }

    override suspend fun <T> withKeyLock(key: String, block: suspend () -> T): T = block()
}

private class FakeRemote(private val result: () -> Price?) : MarketDataRepository {
    var calls = 0
    override suspend fun getLatestPrice(ticker: String): Price? {
        calls++
        return result()
    }
}

private class FakeDetailsRemote(
    private val history: List<PricePoint>,
) : CompanyDetailsRepository {
    var historyCalls = 0

    override suspend fun getDailyHistory(ticker: String): List<PricePoint> {
        historyCalls++
        return history
    }

    override suspend fun getCompanyProfile(ticker: String): CompanyProfile = error("Not used")
    override suspend fun getCompanyNews(ticker: String): List<CompanyNews> = error("Not used")
}

class CachedMarketDataRepositoryTest {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    @Test
    fun `fresh cached price skips backend`() = runTest {
        val cachedPrice = Price("AAPL", 100.0)
        val cache = FakeCache(cachedPrice, CachePolicy.PRICE_FRESH - 1)
        val remote = FakeRemote { Price("AAPL", 200.0) }
        val repository = CachedMarketDataRepository("alpha", remote, cache, moshi)

        assertEquals(cachedPrice, repository.getLatestPrice("AAPL"))
        assertEquals(0, remote.calls)
    }

    @Test
    fun `expired price is refreshed and stored`() = runTest {
        val freshPrice = Price("MSFT", 401.1)
        val cache = FakeCache(Price("MSFT", 300.0), CachePolicy.PRICE_FRESH + 1)
        val remote = FakeRemote { freshPrice }
        val repository = CachedMarketDataRepository("twelve", remote, cache, moshi)

        assertEquals(freshPrice, repository.getLatestPrice("MSFT"))
        assertEquals(1, remote.calls)
        assertEquals(1, cache.writes)
    }

    @Test
    fun `backend failure falls back to allowed stale price`() = runTest {
        val stalePrice = Price("NVDA", 207.0)
        val cache = FakeCache(stalePrice, CachePolicy.PRICE_FRESH + 1)
        val remote = FakeRemote { null }
        val repository = CachedMarketDataRepository("alpha", remote, cache, moshi)

        assertEquals(stalePrice, repository.getLatestPrice("NVDA"))
        assertEquals(1, remote.calls)
    }

    @Test
    fun `too old price is not returned after backend failure`() = runTest {
        val cache = FakeCache(Price("NVDA", 207.0), CachePolicy.PRICE_MAX_STALE + 1)
        val remote = FakeRemote { null }
        val repository = CachedMarketDataRepository("alpha", remote, cache, moshi)

        assertNull(repository.getLatestPrice("NVDA"))
    }

    @Test
    fun `history loaded for preview is reused by detail`() = runTest {
        val history = listOf(
            PricePoint("2026-07-16", 100.0, 102.0, 99.0, 101.0, 1_000),
            PricePoint("2026-07-17", 101.0, 104.0, 100.0, 103.0, 1_200),
        )
        val cache = FakeCache()
        val remote = FakeDetailsRemote(history)
        val repository = CachedCompanyDetailsRepository("twelve", remote, cache, moshi)

        assertEquals(history, repository.getDailyHistory("AAPL"))
        assertEquals(history, repository.getDailyHistory("aapl"))
        assertEquals(1, remote.historyCalls)
        assertEquals(1, cache.writes)
    }
}
