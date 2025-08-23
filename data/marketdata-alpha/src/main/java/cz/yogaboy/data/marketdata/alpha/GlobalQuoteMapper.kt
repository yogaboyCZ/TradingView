package cz.yogaboy.data.marketdata.alpha

import cz.yogaboy.data.marketdata.Price

fun GlobalQuoteEnvelope.toPrice(ticker: String): Price? {
    val q = globalQuote
    val last = q["05. price"]?.toDoubleOrNull() ?: return null
    val change = q["09. change"]?.toDoubleOrNull()
    val changePercent = q["10. change percent"]?.removeSuffix("%")?.toDoubleOrNull()
    val previousClose = q["08. previous close"]?.toDoubleOrNull()
    val asOf = q["07. latest trading day"]
    return Price(
        ticker = ticker,
        last = last,
        change = change,
        changePercent = changePercent,
        previousClose = previousClose,
        asOf = asOf
    )
}
