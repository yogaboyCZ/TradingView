package cz.yogaboy.core.design

import java.util.Locale

/** Formats a market price with a compact symbol while preserving unknown ISO currencies. */
fun formatCurrencyAmount(
    amount: Double,
    currencyCode: String? = "USD",
): String {
    val code = currencyCode?.trim()?.uppercase(Locale.ROOT).orEmpty()
    val value = String.format(Locale.US, "%.2f", amount)
    return when (code) {
        "USD" -> "$$value"
        "EUR" -> "€$value"
        "GBP" -> "£$value"
        "GBX", "GBp" -> "${value}p"
        "JPY", "CNY" -> "¥$value"
        "KRW" -> "₩$value"
        "INR" -> "₹$value"
        "RUB" -> "₽$value"
        "TRY" -> "₺$value"
        "PLN" -> "${value} zł"
        "CZK" -> "${value} Kč"
        "CHF" -> "CHF $value"
        "SEK", "NOK", "DKK" -> "$value kr"
        "" -> value
        else -> "$value $code"
    }
}
