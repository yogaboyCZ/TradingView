package cz.yogaboy.feature.stocks.presentation

import app.cash.turbine.test
import cz.yogaboy.data.marketdata.MarketDataRepository
import cz.yogaboy.data.marketdata.Price
import cz.yogaboy.feature.stocks.domain.GetLatestPriceUseCase
import cz.yogaboy.feature.stocks.testutil.MainDispatcherExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

private class FakeRepo(private val block: (String) -> Price?) : MarketDataRepository {
    override suspend fun getLatestPrice(ticker: String): Price? = block(ticker)
}

@OptIn(ExperimentalCoroutinesApi::class)
class StocksViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    @Test
    fun `emits loading then data on success`() = runTest(mainDispatcher.dispatcher()) {
        // given
        val expected = Price("AAPL", 227.76, 2.86, 1.2717, 224.9, "2025-08-22")
        val repo = FakeRepo { expected }
        val useCase = GetLatestPriceUseCase(repo)
        val vm = StocksViewModel(getLatestPrice = useCase, ticker = "AAPL")

        vm.state.test {
            // initial
            assertEquals(StocksState(), awaitItem())

            advanceUntilIdle()

            val loading = awaitItem()
            assertTrue(loading.loading)
            assertNull(loading.error)
            assertNull(loading.price)

            val done = awaitItem()
            assertFalse(done.loading)
            assertEquals(expected, done.price)
            assertNull(done.error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits loading then error on failure`() = runTest(mainDispatcher.dispatcher()) {
        // given
        val repo = FakeRepo { null }
        val useCase = GetLatestPriceUseCase(repo)
        val vm = StocksViewModel(getLatestPrice = useCase, ticker = "MSFT")

        vm.state.test {
            // initial
            assertEquals(StocksState(), awaitItem())

            advanceUntilIdle()

            val loading = awaitItem()
            assertTrue(loading.loading)

            val error = awaitItem()
            assertFalse(error.loading)
            assertNull(error.price)
            assertEquals("No price for MSFT", error.error)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
