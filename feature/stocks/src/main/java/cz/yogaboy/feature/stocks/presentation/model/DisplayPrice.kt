package cz.yogaboy.feature.stocks.presentation.model

data class DisplayPrice(
    val ticker: String,
    val last: Double,
    val change: Double?,
    val changePercent: Double?,
    val previousClose: Double?,
    val asOf: String?,
    val name: String?,
)