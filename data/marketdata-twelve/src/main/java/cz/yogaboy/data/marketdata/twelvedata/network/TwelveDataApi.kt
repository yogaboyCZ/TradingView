package cz.yogaboy.data.marketdata.twelvedata.network

import cz.yogaboy.data.marketdata.twelvedata.dto.TwelveQuote
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface TwelveDataApi {
    @GET("price")
    suspend fun getPriceRaw(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): ResponseBody

    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): TwelveQuote
}