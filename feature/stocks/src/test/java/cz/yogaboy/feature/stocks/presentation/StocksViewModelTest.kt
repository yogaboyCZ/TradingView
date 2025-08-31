package cz.yogaboy.feature.stocks.presentation

import android.util.Log
import app.cash.turbine.test
import cz.yogaboy.domain.marketdata.MarketDataRepository
import cz.yogaboy.domain.marketdata.Price
import cz.yogaboy.feature.stocks.domain.GetLatestPriceUseCase
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.extension.RegisterExtension

private class FakeRepo(private val block: (String) -> Price?) : MarketDataRepository {
    override suspend fun getLatestPrice(ticker: String): Price? = block(ticker)
}

@OptIn(ExperimentalCoroutinesApi::class)
class StocksViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    @BeforeEach
    fun stubAndroidLog() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any(), any()) } returns 0
    }

    @AfterEach
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `emits loading then both prices on double success`() = runTest(mainDispatcher.dispatcher()) {
        val alphaP = Price("AAPL", 227.76, 2.86, 1.2717, 224.9, "2025-08-22")
        val twelveP = Price("AAPL", 227.70)

        val getAlpha = GetLatestPriceUseCase(FakeRepo { alphaP })
        val getTwelve = GetLatestPriceUseCase(FakeRepo { twelveP })

        val vm = StocksViewModel(getAlpha = getAlpha, getTwelve = getTwelve, ticker = "AAPL")

        vm.state.test {
            assertEquals(StocksState(), awaitItem())

            advanceUntilIdle()
            val loading = awaitItem()
            assertTrue(loading.loading)
            assertNull(loading.error)
            assertNull(loading.alphaPrice)
            assertNull(loading.twelvePrice)

            advanceUntilIdle()
            val done = awaitItem()
            assertFalse(done.loading)
            assertEquals(alphaP, done.alphaPrice)
            assertEquals(twelveP, done.twelvePrice)
            assertNull(done.error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits loading then single price when one provider fails`() = runTest(mainDispatcher.dispatcher()) {
        val twelveP = Price("MSFT", 506.69)

        val getAlpha = GetLatestPriceUseCase(FakeRepo { null })     // fail
        val getTwelve = GetLatestPriceUseCase(FakeRepo { twelveP }) // success

        val vm = StocksViewModel(getAlpha = getAlpha, getTwelve = getTwelve, ticker = "MSFT")

        vm.state.test {
            assertEquals(StocksState(), awaitItem())

            advanceUntilIdle()
            val loading = awaitItem()
            assertTrue(loading.loading)

            advanceUntilIdle()
            val done = awaitItem()
            assertFalse(done.loading)
            assertNull(done.alphaPrice)
            assertEquals(twelveP, done.twelvePrice)
            assertNull(done.error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits loading then error when both providers fail`() = runTest(mainDispatcher.dispatcher()) {
        val getAlpha = GetLatestPriceUseCase(FakeRepo { null })
        val getTwelve = GetLatestPriceUseCase(FakeRepo { null })

        val vm = StocksViewModel(getAlpha = getAlpha, getTwelve = getTwelve, ticker = "NVDA")

        vm.state.test {
            assertEquals(StocksState(), awaitItem())

            advanceUntilIdle()
            val loading = awaitItem()
            assertTrue(loading.loading)

            advanceUntilIdle()
            val done = awaitItem()
            assertFalse(done.loading)
            assertNull(done.alphaPrice)
            assertNull(done.twelvePrice)
            assertEquals("No price for NVDA", done.error)

            cancelAndIgnoreRemainingEvents()
        }
    }
}