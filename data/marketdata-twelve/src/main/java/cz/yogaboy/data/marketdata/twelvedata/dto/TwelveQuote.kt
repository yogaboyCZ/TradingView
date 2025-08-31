package cz.yogaboy.data.marketdata.twelvedata.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TwelveQuote(
    @param:Json(name = "symbol") val symbol: String?,
    @param:Json(name = "price") val price: String?,
    @param:Json(name = "close") val close: String?,
    @param:Json(name = "open") val open: String?,
    @param:Json(name = "high") val high: String?,
    @param:Json(name = "low") val low: String?,
    @param:Json(name = "previous_close") val previousClose: String?,
    @param:Json(name = "change") val change: String?,
    @param:Json(name = "percent_change") val percentChange: String?,
    @param:Json(name = "datetime") val datetime: String?
)