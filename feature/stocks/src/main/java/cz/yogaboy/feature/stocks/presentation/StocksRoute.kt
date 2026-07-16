package cz.yogaboy.feature.stocks.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.core.parameter.parametersOf
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StocksRoute(
    ticker: String,
    onBackClick: () -> Unit,
    showBackNavigation: Boolean = true,
    drawBackground: Boolean = true,
    modifier: Modifier = Modifier
) {
    val vm: StocksViewModel = koinViewModel(
        key = "stocks-$ticker",
        parameters = { parametersOf(ticker) }
    )
    val state by vm.state.collectAsStateWithLifecycle()
    StocksScreen(
        ticker = ticker,
        state = state,
        onClick = vm::handle,
        onBackClick = onBackClick,
        showBackNavigation = showBackNavigation,
        drawBackground = drawBackground,
        modifier = modifier
    )
}
