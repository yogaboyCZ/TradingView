package cz.yogaboy.data.marketdata.alpha.mapper

import cz.yogaboy.domain.marketdata.Price
import cz.yogaboy.data.marketdata.alpha.dto.GlobalQuoteEnvelope

/**
 * Maps AlphaVantage envelope -> domain Price.
 * fallbackTicker just in case API "01. symbol" is null
 */
fun GlobalQuoteEnvelope.toDomain(fallbackTicker: String? = null): Price? {
    val q = quote ?: return null
    val ticker = q.symbol ?: fallbackTicker ?: return null
    val last = q.price?.toDoubleOrNull() ?: return null
    val change = q.change?.toDoubleOrNull()
    val changePct = q.changePercent?.removeSuffix("%")?.toDoubleOrNull()
    val prevClose = q.previousClose?.toDoubleOrNull()
    val asOf = q.latestTradingDay
    return Price(ticker, last, change, changePct, prevClose, asOf)
}
