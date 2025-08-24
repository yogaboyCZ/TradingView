package cz.yogaboy.tv.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class StocksDest(val ticker: String) : NavKey
