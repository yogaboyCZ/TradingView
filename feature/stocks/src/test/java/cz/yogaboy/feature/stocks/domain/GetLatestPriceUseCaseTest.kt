package cz.yogaboy.feature.stocks.domain

import cz.yogaboy.domain.marketdata.MarketDataRepository
import cz.yogaboy.domain.marketdata.Price
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

private class FakeRepo(private val block: (String) -> Price?) : MarketDataRepository {
    override suspend fun getLatestPrice(ticker: String): Price? = block(ticker)
}

class GetLatestPriceUseCaseTest {

    @Test
    fun success() = runTest {
        val expected = Price("AAPL", 227.76, 2.86, 1.2717, 224.9, "2025-08-22")
        val useCase = GetLatestPriceUseCase(FakeRepo { expected })

        val r = useCase("AAPL")

        assertTrue(r.isSuccess)
        assertEquals(expected, r.getOrNull())
    }

    @Test
    fun `failure when repo has no data`() = runTest {
        val useCase = GetLatestPriceUseCase(FakeRepo { null })

        val r = useCase("MSFT")

        assertTrue(r.isFailure)
        val ex = r.exceptionOrNull()
        assertTrue(ex is NoSuchElementException)
        assertEquals("No price for MSFT", ex?.message)
    }
}
