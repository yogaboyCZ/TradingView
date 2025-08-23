package cz.yogaboy.data.marketdata.alpha

import retrofit2.http.GET
import retrofit2.http.Query

interface AlphaVantageApi {
    companion object { const val BASE_URL = "https://www.alphavantage.co/" }

    @GET("query")
    suspend fun getGlobalQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): GlobalQuoteEnvelope
}
