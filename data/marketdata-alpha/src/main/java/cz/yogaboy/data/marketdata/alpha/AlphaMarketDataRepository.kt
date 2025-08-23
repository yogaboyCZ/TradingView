package cz.yogaboy.data.marketdata.alpha

import cz.yogaboy.data.marketdata.MarketDataRepository
import cz.yogaboy.data.marketdata.Price

class AlphaMarketDataRepository(
    private val api: AlphaVantageApi,
    private val apiKey: String
) : MarketDataRepository {
    override suspend fun getLatestPrice(ticker: String): Price? =
        runCatching { api.getGlobalQuote(symbol = ticker, apiKey = apiKey).toPrice(ticker) }.getOrNull()
}
