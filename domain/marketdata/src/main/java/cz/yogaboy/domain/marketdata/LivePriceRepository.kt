package cz.yogaboy.domain.marketdata

import kotlinx.coroutines.flow.Flow

data class LivePriceTick(
    val price: Double,
    val change: Double,
    val timestampMillis: Long,
    val simulated: Boolean,
)

/**
 * Provider-neutral stream boundary. A WebSocket/SSE implementation can replace the simulator
 * without changing presentation or domain consumers.
 */
interface LivePriceRepository {
    fun observePrices(ticker: String, initialPrice: Double): Flow<LivePriceTick>
}
