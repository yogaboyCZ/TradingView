package cz.yogaboy.app

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import cz.yogaboy.feature.stocks.presentation.StocksRoute
import cz.yogaboy.tv.nav.StocksDest

@Composable
fun RootNavGraph() {
    val backStack = rememberNavBackStack(StocksDest("AAPL"))
    NavDisplay(backStack, entryProvider = entryProvider {
        entry<StocksDest> { key ->
            StocksRoute(
                ticker = key.ticker,
                onBackClick = { backStack.removeLastOrNull() }
            )
        }
    })
}
