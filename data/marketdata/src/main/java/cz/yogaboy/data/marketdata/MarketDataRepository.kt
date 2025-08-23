package cz.yogaboy.data.marketdata

interface MarketDataRepository {
    suspend fun getLatestPrice(ticker: String): Price?
}
