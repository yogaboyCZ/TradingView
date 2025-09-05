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
import org.junit.jupiter.api.Assertions.*
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
    fun `emits Loading then Data for both providers on success`() = runTest(mainDispatcher.dispatcher()) {
        val alphaP = Price("AAPL", 227.76, 2.86, 1.2717, 224.9, "2025-08-22", name = "Apple Inc.")
        val twelveP = Price("AAPL", 227.70)

        val getAlpha = GetLatestPriceUseCase(FakeRepo { alphaP })
        val getTwelve = GetLatestPriceUseCase(FakeRepo { twelveP })

        val vm = StocksViewModel(getAlpha = getAlpha, getTwelve = getTwelve, ticker = "AAPL")

        vm.state.test {
            // initial state (both Loading by default)
            val initial = awaitItem()
            assertTrue(initial.alpha is StocksUiState.Loading)
            assertTrue(initial.twelve is StocksUiState.Loading)

            // wait for load()
            advanceUntilIdle()

            val done = awaitItem()
            val alpha = done.alpha as StocksUiState.Data
            val twelve = done.twelve as StocksUiState.Data

            assertEquals("AAPL", alpha.value.ticker)
            assertEquals(227.76, alpha.value.last, 0.0001)
            assertEquals("AAPL", twelve.value.ticker)
            assertEquals(227.70, twelve.value.last, 0.0001)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Loading then Data for one provider and Error for the other`() = runTest(mainDispatcher.dispatcher()) {
        val twelveP = Price("MSFT", 506.69)

        val getAlpha = GetLatestPriceUseCase(FakeRepo { null })      // fail
        val getTwelve = GetLatestPriceUseCase(FakeRepo { twelveP })  // success

        val vm = StocksViewModel(getAlpha = getAlpha, getTwelve = getTwelve, ticker = "MSFT")

        vm.state.test {
            val initial = awaitItem()
            assertTrue(initial.alpha is StocksUiState.Loading)
            assertTrue(initial.twelve is StocksUiState.Loading)

            advanceUntilIdle()

            val done = awaitItem()
            assertTrue(done.alpha is StocksUiState.Error)
            val twelve = done.twelve as StocksUiState.Data
            assertEquals("MSFT", twelve.value.ticker)
            assertEquals(506.69, twelve.value.last, 0.0001)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Loading then Error for both providers on double failure`() = runTest(mainDispatcher.dispatcher()) {
        val getAlpha = GetLatestPriceUseCase(FakeRepo { null })
        val getTwelve = GetLatestPriceUseCase(FakeRepo { null })

        val vm = StocksViewModel(getAlpha = getAlpha, getTwelve = getTwelve, ticker = "NVDA")

        vm.state.test {
            val initial = awaitItem()
            assertTrue(initial.alpha is StocksUiState.Loading)
            assertTrue(initial.twelve is StocksUiState.Loading)

            advanceUntilIdle()

            val done = awaitItem()
            assertTrue(done.alpha is StocksUiState.Error)
            assertTrue(done.twelve is StocksUiState.Error)

            val alphaErr = done.alpha as StocksUiState.Error
            val twelveErr = done.twelve as StocksUiState.Error
            assertTrue(alphaErr.message.isNotBlank())
            assertTrue(twelveErr.message.isNotBlank())

            cancelAndIgnoreRemainingEvents()
        }
    }
}