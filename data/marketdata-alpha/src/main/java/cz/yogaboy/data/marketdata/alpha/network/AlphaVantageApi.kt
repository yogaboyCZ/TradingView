package cz.yogaboy.data.marketdata.alpha.network

import cz.yogaboy.data.marketdata.alpha.dto.GlobalQuoteEnvelope
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface AlphaVantageApi {

    @GET("query")
    suspend fun getGlobalQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String,
    ): GlobalQuoteEnvelope

    @GET("query")
    suspend fun getGlobalQuoteRaw(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): ResponseBody

    @GET("price")
    suspend fun getPriceRaw(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): ResponseBody
}

interface TwelveDataApi {
    @GET("quote")
    suspend fun getQuoteRaw(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): ResponseBody
}
