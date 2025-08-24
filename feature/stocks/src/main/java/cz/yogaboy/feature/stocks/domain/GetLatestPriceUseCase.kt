package cz.yogaboy.feature.stocks.domain

import cz.yogaboy.domain.marketdata.MarketDataRepository
import cz.yogaboy.domain.marketdata.Price


class GetLatestPriceUseCase(
    private val repo: MarketDataRepository
) {
    suspend operator fun invoke(ticker: String): Result<Price> {
        val price = repo.getLatestPrice(ticker)
        return if (price != null) {
            Result.success(price)
        } else {
            Result.failure(NoSuchElementException("No price for $ticker"))
        }
    }
}
