package cz.yogaboy.data.marketdata.cache

import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.Query
import androidx.room3.RoomDatabase
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "market_data_cache",
    primaryKeys = ["provider", "ticker", "dataType"],
)
data class CacheEntry(
    val provider: String,
    val ticker: String,
    val dataType: String,
    val payload: String,
    val fetchedAt: Long,
)

@Dao
interface MarketDataCacheDao {
    @Query(
        "SELECT * FROM market_data_cache " +
            "WHERE provider = :provider AND ticker = :ticker AND dataType = :dataType"
    )
    suspend fun get(provider: String, ticker: String, dataType: String): CacheEntry?

    @Query(
        "SELECT * FROM market_data_cache " +
            "WHERE provider = :provider AND ticker = :ticker AND dataType = :dataType"
    )
    fun observe(provider: String, ticker: String, dataType: String): Flow<CacheEntry?>

    @Upsert
    suspend fun upsert(entry: CacheEntry)

    @Query(
        "DELETE FROM market_data_cache " +
            "WHERE provider = :provider AND ticker = :ticker AND dataType = :dataType"
    )
    suspend fun delete(provider: String, ticker: String, dataType: String)

    @Query("DELETE FROM market_data_cache WHERE fetchedAt < :oldestAllowed")
    suspend fun deleteOlderThan(oldestAllowed: Long)
}

@Database(entities = [CacheEntry::class], version = 1, exportSchema = false)
abstract class MarketDataCacheDatabase : RoomDatabase() {
    abstract fun cacheDao(): MarketDataCacheDao
}
