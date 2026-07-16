package cz.yogaboy.domain.marketdata

data class Price(
    val ticker: String,
    val last: Double,
    val change: Double? = null,
    val changePercent: Double? = null,
    val previousClose: Double? = null,
    val asOf: String? = null,
    val name: String? = null,
    val open: Double? = null,
    val high: Double? = null,
    val low: Double? = null,
    val volume: Long? = null,
    val currency: String? = null,
    val exchange: String? = null,
    val micCode: String? = null,
    val instrumentType: String? = null,
)
