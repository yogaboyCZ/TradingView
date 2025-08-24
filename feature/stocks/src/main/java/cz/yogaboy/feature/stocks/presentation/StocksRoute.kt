package cz.yogaboy.feature.stocks.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun StocksRoute(
    ticker: String,
    onBackClick: () -> Unit,
) {
    val vm: StocksViewModel = koinViewModel(parameters = { parametersOf(ticker) })
    val state by vm.state.collectAsStateWithLifecycle()

    StocksScreen(
        state = state,
        onClick = vm::handle,
        onBackClick = onBackClick
    )
}
