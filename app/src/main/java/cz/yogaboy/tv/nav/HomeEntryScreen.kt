package cz.yogaboy.tv.nav

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import org.koin.compose.viewmodel.koinViewModel
import cz.yogaboy.feature.home.presentation.*
import cz.yogaboy.feature.stocks.presentation.StocksRoute

@Composable
fun HomeEntryScreen() {
    val vm: HomeViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    var selectedTicker by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        vm.effects.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToDetail -> selectedTicker = effect.ticker
            }
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