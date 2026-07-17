package cz.yogaboy.data.marketdata.cache

import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import java.util.concurrent.ConcurrentHashMap

data class CachedValue<T>(
    val value: T,
    val ageMillis: Long,
)

interface MarketDataCache {
    fun <T> observe(
        provider: String,
        ticker: String,
        dataType: String,
        adapter: JsonAdapter<T>,
    ): Flow<CachedValue<T>?> = flowOf(null)

    suspend fun <T> read(
        provider: String,
        ticker: String,
        dataType: String,
        adapter: JsonAdapter<T>,
    ): CachedValue<T>?

    suspend fun <T> write(
        provider: String,
        ticker: String,
        dataType: String,
        value: T,
        adapter: JsonAdapter<T>,
    )

    suspend fun <T> withKeyLock(key: String, block: suspend () -> T): T
}

class RoomMarketDataCache(
    private val dao: MarketDataCacheDao,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) : MarketDataCache {
    private val locks = ConcurrentHashMap<String, Mutex>()

    override fun <T> observe(
        provider: String,
        ticker: String,
        dataType: String,
        adapter: JsonAdapter<T>,
    ): Flow<CachedValue<T>?> {
        val normalizedTicker = ticker.trim().uppercase()
        return dao.observe(provider, normalizedTicker, dataType).map { entry ->
            entry ?: return@map null
            adapter.fromJson(entry.payload)?.let { value ->
                CachedValue(value, (nowMillis() - entry.fetchedAt).coerceAtLeast(0L))
            }
        }
    }

    override suspend fun <T> read(
        provider: String,
        ticker: String,
        dataType: String,
        adapter: JsonAdapter<T>,
    ): CachedValue<T>? {
        val normalizedTicker = ticker.trim().uppercase()
        val entry = dao.get(provider, normalizedTicker, dataType) ?: return null
        val value = runCatching { adapter.fromJson(entry.payload) }.getOrNull()
        if (value == null) {
            dao.delete(provider, normalizedTicker, dataType)
            return null
        }
        return CachedValue(value, (nowMillis() - entry.fetchedAt).coerceAtLeast(0L))
    }

    override suspend fun <T> write(
        provider: String,
        ticker: String,
        dataType: String,
        value: T,
        adapter: JsonAdapter<T>,
    ) {
        dao.upsert(
            CacheEntry(
                provider = provider,
                ticker = ticker.trim().uppercase(),
                dataType = dataType,
                payload = adapter.toJson(value),
                fetchedAt = nowMillis(),
            )
        )
    }

    override suspend fun <T> withKeyLock(key: String, block: suspend () -> T): T =
        locks.getOrPut(key) { Mutex() }.withLock { block() }
}
