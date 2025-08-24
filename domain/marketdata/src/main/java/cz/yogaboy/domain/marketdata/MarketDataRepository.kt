package cz.yogaboy.domain.marketdata

interface MarketDataRepository {
    suspend fun getLatestPrice(ticker: String): Price?
}
