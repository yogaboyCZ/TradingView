package cz.yogaboy.data.marketdata.twelvedata.mapper

import cz.yogaboy.data.marketdata.twelvedata.dto.TwelveQuote
import cz.yogaboy.domain.marketdata.Price

fun TwelveQuote.toDomain(fallbackTicker: String): Price? {
    val lastStr = price ?: close
    val last = lastStr?.toDoubleOrNull() ?: return null
    val ch = change?.toDoubleOrNull()
    val pct = percentChange?.removeSuffix("%")?.toDoubleOrNull()
    val prev = previousClose?.toDoubleOrNull()
    val tkr = symbol ?: fallbackTicker
    return Price(
        ticker = tkr,
        last = last,
        change = ch,
        changePercent = pct,
        previousClose = prev,
        asOf = datetime
    )
}