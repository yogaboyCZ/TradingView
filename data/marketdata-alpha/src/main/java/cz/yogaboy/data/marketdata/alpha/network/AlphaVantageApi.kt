package cz.yogaboy.data.marketdata.alpha.network

import cz.yogaboy.data.marketdata.alpha.dto.GlobalQuoteEnvelope
import retrofit2.http.GET
import retrofit2.http.Query

interface AlphaVantageApi {

    @GET("query")
    suspend fun getGlobalQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String,
    ): GlobalQuoteEnvelope

}
