package cz.yogaboy.data.marketdata.twelvedata.network

import cz.yogaboy.data.marketdata.twelvedata.dto.TwelveQuote
import cz.yogaboy.data.marketdata.twelvedata.dto.TwelveCompanyProfile
import cz.yogaboy.data.marketdata.twelvedata.dto.TwelvePressReleases
import cz.yogaboy.data.marketdata.twelvedata.dto.TwelveTimeSeries
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

    @GET("time_series")
    suspend fun getTimeSeries(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "1day",
        @Query("outputsize") outputSize: Int = 30,
        @Query("order") order: String = "asc",
        @Query("apikey") apiKey: String,
    ): TwelveTimeSeries

    @GET("profile")
    suspend fun getProfile(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String,
    ): TwelveCompanyProfile

    @GET("press_releases")
    suspend fun getPressReleases(
        @Query("symbol") symbol: String,
        @Query("outputsize") outputSize: Int = 5,
        @Query("apikey") apiKey: String,
    ): TwelvePressReleases
}
