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
        vm.effects.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToDetail -> selectedTicker = effect.ticker
            }
        }
    }

    HomeScreen(
        query = query,
        onQueryChange = {
            query = it
            vm.handle(HomeEvent.QueryChanged(it))
            if (it.isBlank()) selectedTicker = null
        },
        onSearch = { vm.handle(HomeEvent.Submit) },
        onClearSearch = {
            query = ""
            vm.handle(HomeEvent.QueryChanged(""))
            selectedTicker = null
        },
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