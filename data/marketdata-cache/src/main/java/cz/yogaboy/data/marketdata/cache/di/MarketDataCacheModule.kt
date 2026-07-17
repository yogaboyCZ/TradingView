package cz.yogaboy.data.marketdata.cache.di

import android.content.Context
import androidx.room3.Room
import cz.yogaboy.data.marketdata.cache.MarketDataCache
import cz.yogaboy.data.marketdata.cache.MarketDataCacheDao
import cz.yogaboy.data.marketdata.cache.MarketDataCacheDatabase
import cz.yogaboy.data.marketdata.cache.RoomMarketDataCache
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create

private fun createDatabase(context: Context): MarketDataCacheDatabase =
    Room.databaseBuilder(context, MarketDataCacheDatabase::class.java, "market-data-cache.db").build()

private fun createCacheDao(database: MarketDataCacheDatabase): MarketDataCacheDao = database.cacheDao()
private fun createMarketDataCache(dao: MarketDataCacheDao): RoomMarketDataCache = RoomMarketDataCache(dao)

val marketDataCacheModule = module {
    single { create(::createDatabase) }
    single { create(::createCacheDao) }
    single { create(::createMarketDataCache) } bind MarketDataCache::class
}
