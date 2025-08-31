package cz.yogaboy.data.marketdata.alpha.dto

import com.squareup.moshi.Json

data class GlobalQuoteEnvelope(
    @param:Json(name = "Global Quote")
    val quote: GlobalQuote?
)

data class GlobalQuote(
    @param:Json(name = "01. symbol") val symbol: String?,
    @param:Json(name = "02. open")   val open: String?,
    @param:Json(name = "03. high")   val high: String?,
    @param:Json(name = "04. low")    val low: String?,
    @param:Json(name = "05. price")  val price: String?,
    @param:Json(name = "06. volume") val volume: String?,
    @param:Json(name = "07. latest trading day") val latestTradingDay: String?,
    @param:Json(name = "08. previous close")    val previousClose: String?,
    @param:Json(name = "09. change")            val change: String?,
    @param:Json(name = "10. change percent")    val changePercent: String?,
)