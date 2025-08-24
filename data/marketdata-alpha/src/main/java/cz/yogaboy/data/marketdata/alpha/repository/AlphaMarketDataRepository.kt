package cz.yogaboy.data.marketdata.alpha.repository

import cz.yogaboy.data.marketdata.MarketDataRepository
import cz.yogaboy.data.marketdata.Price
import cz.yogaboy.data.marketdata.alpha.mapper.toDomain
import cz.yogaboy.data.marketdata.alpha.network.AlphaVantageApi

class AlphaMarketDataRepository(
    private val api: AlphaVantageApi,
    private val apiKey: String
) : MarketDataRepository {
    override suspend fun getLatestPrice(ticker: String): Price? =
        api.getGlobalQuote(symbol = ticker, apiKey = apiKey).toDomain(fallbackTicker = ticker)
}
