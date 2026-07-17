package cz.yogaboy.data.marketdata.twelvedata.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TwelveQuote(
    @param:Json(name = "code") val errorCode: Int?,
    @param:Json(name = "message") val errorMessage: String?,
    @param:Json(name = "status") val status: String?,
    @param:Json(name = "symbol") val symbol: String?,
    @param:Json(name = "name") val name: String?,
    @param:Json(name = "close") val close: String?,
    @param:Json(name = "open") val open: String?,
    @param:Json(name = "high") val high: String?,
    @param:Json(name = "low") val low: String?,
    @param:Json(name = "volume") val volume: String?,
    @param:Json(name = "previous_close") val previousClose: String?,
    @param:Json(name = "change") val change: String?,
    @param:Json(name = "percent_change") val percentChange: String?,
    @param:Json(name = "datetime") val datetime: String?,
    @param:Json(name = "currency") val currency: String?,
    @param:Json(name = "exchange") val exchange: String?,
    @param:Json(name = "mic_code") val micCode: String?,
    @param:Json(name = "type") val type: String?,
)
