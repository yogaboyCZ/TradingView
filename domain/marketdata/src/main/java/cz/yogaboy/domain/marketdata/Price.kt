package cz.yogaboy.domain.marketdata

data class Price(
    val ticker: String,
    val last: Double,
    val change: Double?,
    val changePercent: Double?,
    val previousClose: Double?,
    val asOf: String?,
)