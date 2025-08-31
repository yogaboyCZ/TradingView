package cz.yogaboy.data.marketdata.twelvedata.repository

import android.util.Log
import cz.yogaboy.data.marketdata.twelvedata.mapper.toDomain
import cz.yogaboy.data.marketdata.twelvedata.network.TwelveDataApi
import cz.yogaboy.domain.marketdata.MarketDataRepository
import cz.yogaboy.domain.marketdata.Price
import org.json.JSONObject

class TwelveMarketDataRepository(
    private val api: TwelveDataApi,
    private val apiKey: String
) : MarketDataRepository {

    override suspend fun getLatestPrice(ticker: String): Price? = try {
        api.getQuote(symbol = ticker, apiKey = apiKey).toDomain(fallbackTicker = ticker)
            ?: run {
                // Fallback on price when quote is not available
                val body = api.getPriceRaw(symbol = ticker, apiKey = apiKey).string()
                Log.d("TWELVE_$ticker", "RAW=$body")
                val last = JSONObject(body).optString("price", "error").toDoubleOrNull()
                last?.let { Price(ticker = ticker, last = it) }
            }
    } catch (t: Throwable) {
        Log.w("TwelveMarketDataRepo", "Failed to fetch price for $ticker", t)
        null
    }
}