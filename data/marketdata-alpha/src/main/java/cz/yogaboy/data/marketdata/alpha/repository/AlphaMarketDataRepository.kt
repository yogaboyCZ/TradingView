package cz.yogaboy.data.marketdata.alpha.repository

import android.util.Log
import cz.yogaboy.domain.marketdata.MarketDataRepository
import cz.yogaboy.data.marketdata.alpha.network.AlphaVantageApi
import cz.yogaboy.domain.marketdata.Price
import org.json.JSONObject

class AlphaMarketDataRepository(
    private val api: AlphaVantageApi,
    private val apiKey: String
) : MarketDataRepository {
    override suspend fun getLatestPrice(ticker: String): Price? {
//        val result = api.getGlobalQuote(symbol = ticker, apiKey = apiKey).toDomain(fallbackTicker = ticker)
//        Log.d("LSY___","LSY stock price for $ticker is $result")
//        val raw = api.getGlobalQuoteRaw(symbol = ticker, apiKey = apiKey).string()
        val body = api.getPriceRaw(symbol = ticker, apiKey = apiKey).string()
        val last = JSONObject(body).getString("price").toDouble()
        Log.d("LSY___$ticker", "RAW=$body")
        return Price(ticker = ticker, last = last)
    }
}
