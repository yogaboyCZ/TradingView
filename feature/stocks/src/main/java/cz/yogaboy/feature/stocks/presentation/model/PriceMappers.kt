package cz.yogaboy.feature.stocks.presentation.model

import cz.yogaboy.domain.marketdata.Price
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun Price.toDisplayPrice(name: String? = null): DisplayPrice =
    DisplayPrice(
        ticker = ticker,
        last = last,
        change = change,
        changePercent = changePercent,
        previousClose = previousClose,
        asOf = asOf?.let(::formatAsCzDate),
        name = name
    )

private fun formatAsCzDate(iso: String): String = try {
    LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
} catch (_: Throwable) { iso }