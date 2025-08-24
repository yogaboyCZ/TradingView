package cz.yogaboy.tv.nav

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import cz.yogaboy.feature.home.presentation.*
import cz.yogaboy.feature.stocks.presentation.StocksRoute
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeEntryScreen() {
    val vm: HomeViewModel = koinViewModel()
    var query by rememberSaveable { mutableStateOf("") }
    var selectedTicker by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        vm.effects.collect { e ->
            when (e) {
                is HomeEffect.NavigateToDetail -> selectedTicker = e.symbol
            }
        }
    }

    HomeScreen(
        query = query,
        onQueryChange = {
            query = it
            vm.handle(HomeEvent.QueryChanged(it))
        },
        onSearch = { vm.handle(HomeEvent.Submit) },
        showPlaceholder = selectedTicker == null,
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
