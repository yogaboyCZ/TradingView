package cz.yogaboy.feature.stocks.presentation.model

data class DisplayPrice(
    val ticker: String,
    val last: Double,
    val change: Double?,
    val changePercent: Double?,
    val previousClose: Double?,
    val asOf: String?,
    val name: String?,
    val open: Double? = null,
    val high: Double? = null,
    val low: Double? = null,
    val volume: Long? = null,
    val currency: String? = null,
    val exchange: String? = null,
    val micCode: String? = null,
    val instrumentType: String? = null,
)
