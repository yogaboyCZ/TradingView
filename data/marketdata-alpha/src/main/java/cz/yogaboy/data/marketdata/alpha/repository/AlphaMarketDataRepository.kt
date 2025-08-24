package cz.yogaboy.data.marketdata.alpha.repository

import cz.yogaboy.domain.marketdata.MarketDataRepository
import cz.yogaboy.data.marketdata.alpha.mapper.toDomain
import cz.yogaboy.data.marketdata.alpha.network.AlphaVantageApi
import cz.yogaboy.domain.marketdata.Price

class AlphaMarketDataRepository(
    private val api: AlphaVantageApi,
    private val apiKey: String
) : MarketDataRepository {
    override suspend fun getLatestPrice(ticker: String): Price? =
        api.getGlobalQuote(symbol = ticker, apiKey = apiKey).toDomain(fallbackTicker = ticker)
}
