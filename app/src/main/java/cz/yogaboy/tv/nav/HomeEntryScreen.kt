package cz.yogaboy.tv.nav

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import org.koin.compose.viewmodel.koinViewModel
import cz.yogaboy.feature.home.presentation.*
import cz.yogaboy.feature.stocks.presentation.StocksRoute

@Composable
fun HomeEntryScreen(initialTicker: String? = null) {
    val vm: HomeViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    var selectedTicker by rememberSaveable { mutableStateOf(initialTicker) }

    LaunchedEffect(Unit) {
        vm.effects.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToDetail -> selectedTicker = effect.ticker
            }
        }
    }

    LaunchedEffect(initialTicker) {
        initialTicker?.let { ticker ->
            // Make a deep-linked ticker look exactly like a ticker submitted
            // through the Home search field.
            vm.handle(HomeEvent.QueryChanged(ticker))
            vm.handle(HomeEvent.Submit)
        }
    }

    HomeScreen(
        state = state,
        onEvent = { vm.handle(it) },
        content = { modifier ->
            selectedTicker?.let { ticker ->
                StocksRoute(
                    ticker = ticker,
                    onBackClick = { selectedTicker = null },
                    modifier = modifier
                )
            }
        }
    )
}
