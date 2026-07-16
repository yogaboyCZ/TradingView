package cz.yogaboy.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import cz.yogaboy.tv.nav.HomeDest
import cz.yogaboy.tv.nav.HomeEntryScreen
import cz.yogaboy.tv.nav.StocksDest
import cz.yogaboy.feature.stocks.presentation.StocksRoute

@Composable
fun RootNavGraph(initialTicker: String? = null) {
    val backStack = rememberNavBackStack(HomeDest)
    var selectedTicker by rememberSaveable { mutableStateOf(initialTicker) }

    LaunchedEffect(initialTicker) {
        if (initialTicker != null && backStack.none { it is StocksDest }) {
            selectedTicker = initialTicker
            backStack.add(StocksDest(initialTicker))
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isListDetailLayout = maxWidth >= 840.dp

        LaunchedEffect(isListDetailLayout) {
            if (!isListDetailLayout && selectedTicker != null) {
                val currentDetail = backStack.lastOrNull() as? StocksDest
                if (currentDetail?.ticker != selectedTicker) {
                    if (currentDetail != null) backStack.removeLastOrNull()
                    backStack.add(StocksDest(selectedTicker!!))
                }
            }
        }

        if (isListDetailLayout) {
            BackHandler(enabled = selectedTicker != null) {
                selectedTicker = null
                if (backStack.lastOrNull() is StocksDest) backStack.removeLastOrNull()
            }
            ListDetailLayout(
                selectedTicker = selectedTicker,
                onTickerSelected = { selectedTicker = it },
            )
        } else {
            PhoneNavigation(
                backStack = backStack,
                onTickerSelected = { ticker ->
                    selectedTicker = ticker
                    backStack.add(StocksDest(ticker))
                },
                onBack = {
                    backStack.removeLastOrNull()
                    selectedTicker = null
                },
            )
        }
    }
}

@Composable
private fun ListDetailLayout(
    selectedTicker: String?,
    onTickerSelected: (String) -> Unit,
) {
    Row(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(0.42f)
                .fillMaxSize(),
        ) {
            HomeEntryScreen(
                onNavigateToDetail = onTickerSelected,
                supportingPane = true,
            )
        }

        Box(
            modifier = Modifier
                .weight(0.58f)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(start = 1.dp),
        ) {
            if (selectedTicker != null) {
                StocksRoute(
                    ticker = selectedTicker,
                    onBackClick = {},
                    showBackNavigation = false,
                )
            } else {
                EmptyDetailPane()
            }
        }
    }
}

@Composable
private fun EmptyDetailPane() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        androidx.compose.material3.Text(
            text = "Vyberte produkt ze seznamu",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onTertiary,
        )
    }
}

@Composable
private fun PhoneNavigation(
    backStack: MutableList<androidx.navigation3.runtime.NavKey>,
    onTickerSelected: (String) -> Unit,
    onBack: () -> Unit,
) {
    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(350),
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tween(350),
            )
        },
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tween(350),
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(350),
            )
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 4 },
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
            )
        },
        entryProvider = entryProvider {
            entry<HomeDest> {
                HomeEntryScreen(
                    onNavigateToDetail = onTickerSelected,
                )
            }
            entry<StocksDest> { destination ->
                StocksRoute(
                    ticker = destination.ticker,
                    onBackClick = onBack,
                )
            }
        },
    )
}
