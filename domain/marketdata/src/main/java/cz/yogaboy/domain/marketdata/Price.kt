package cz.yogaboy.domain.marketdata

data class Price(
    val ticker: String,
    val last: Double,
    val change: Double? = null,
    val changePercent: Double? = null,
    val previousClose: Double? = null,
    val asOf: String? = null,
)