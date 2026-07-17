package cz.yogaboy.data.marketdata.simulated

import cz.yogaboy.domain.marketdata.LivePriceRepository
import cz.yogaboy.domain.marketdata.LivePriceTick
import kotlin.random.Random
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

class SimulatedLivePriceRepository : LivePriceRepository {
    override fun observePrices(ticker: String, initialPrice: Double): Flow<LivePriceTick> = flow {
        var currentPrice = initialPrice
        while (currentCoroutineContext().isActive) {
            delay(Random.nextLong(from = 900L, until = 1_900L))
            // Small bounded market-like movement: at most ±0.12 % per update.
            val change = currentPrice * Random.nextDouble(from = -0.0012, until = 0.0012)
            currentPrice = (currentPrice + change).coerceAtLeast(0.01)
            emit(
                LivePriceTick(
                    price = currentPrice,
                    change = change,
                    timestampMillis = System.currentTimeMillis(),
                    simulated = true,
                )
            )
        }
    }
}
