package cz.yogaboy.data.marketdata.twelvedata.mapper

import cz.yogaboy.data.marketdata.twelvedata.dto.TwelveQuote
import cz.yogaboy.domain.marketdata.Price

fun TwelveQuote.toDomain(fallbackTicker: String): Price? {
    val lastStr = close
    val last = lastStr?.toDoubleOrNull() ?: return null
    val ch = change?.toDoubleOrNull()
    val pct = percentChange?.removeSuffix("%")?.toDoubleOrNull()
    val prev = previousClose?.toDoubleOrNull()
    val tkr = symbol ?: fallbackTicker
    val name = name
    return Price(
        ticker = tkr,
        last = last,
        open = open?.toDoubleOrNull(),
        high = high?.toDoubleOrNull(),
        low = low?.toDoubleOrNull(),
        volume = volume?.toDoubleOrNull()?.toLong(),
        change = ch,
        changePercent = pct,
        previousClose = prev,
        asOf = datetime,
        name = name,
        currency = currency,
        exchange = exchange,
        micCode = micCode,
        instrumentType = type,
    )
}
