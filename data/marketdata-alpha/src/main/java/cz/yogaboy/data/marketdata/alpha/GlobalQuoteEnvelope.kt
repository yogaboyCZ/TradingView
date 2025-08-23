package cz.yogaboy.data.marketdata.alpha

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GlobalQuoteEnvelope(
    @Json(name = "Global Quote")
    val globalQuote: Map<String, String>
)
