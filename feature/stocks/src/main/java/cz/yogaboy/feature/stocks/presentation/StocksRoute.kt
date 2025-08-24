package cz.yogaboy.feature.stocks.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.core.parameter.parametersOf
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StocksRoute(
    ticker: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vm: StocksViewModel = koinViewModel(
        key = "stocks-$ticker",
        parameters = { parametersOf(ticker) }
    )
    val state by vm.state.collectAsState()
    StocksScreen(
        state = state,
        onClick = vm::handle,
        onBackClick = onBackClick,
        modifier = modifier
    )
}
