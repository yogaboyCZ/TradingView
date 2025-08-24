package cz.yogaboy.data.marketdata.alpha.dto

import com.squareup.moshi.Json

data class GlobalQuoteEnvelope(
    @field:Json(name = "Global Quote")
    val quote: GlobalQuote?
)

data class GlobalQuote(
    @field:Json(name = "01. symbol") val symbol: String?,
    @field:Json(name = "02. open")   val open: String?,
    @field:Json(name = "03. high")   val high: String?,
    @field:Json(name = "04. low")    val low: String?,
    @field:Json(name = "05. price")  val price: String?,
    @field:Json(name = "06. volume") val volume: String?,
    @field:Json(name = "07. latest trading day") val latestTradingDay: String?,
    @field:Json(name = "08. previous close")    val previousClose: String?,
    @field:Json(name = "09. change")            val change: String?,
    @field:Json(name = "10. change percent")    val changePercent: String?,
)
