package cz.yogaboy.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import cz.yogaboy.tv.nav.HomeDest
import cz.yogaboy.tv.nav.HomeEntryScreen
import cz.yogaboy.tv.nav.StocksDest
import cz.yogaboy.feature.stocks.presentation.StocksRoute
import cz.yogaboy.core.design.AuroraBackground

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
                onDetailClosed = {
                    selectedTicker = null
                    if (backStack.lastOrNull() is StocksDest) backStack.removeLastOrNull()
                },
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
    onDetailClosed: () -> Unit,
) {
    val listFraction by animateFloatAsState(
        targetValue = if (selectedTicker == null) 1f else 0.42f,
        animationSpec = tween(450),
        label = "list-pane-width",
    )
    val detailReveal by animateFloatAsState(
        targetValue = if (selectedTicker == null) 0f else 1f,
        animationSpec = tween(450),
        label = "detail-pane-reveal",
    )

    AuroraBackground(Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val listWidth = maxWidth * listFraction
            // Measure detail at its final width from the first frame. Animating its
            // measured width makes every card reflow vertically during the transition.
            val detailWidth = maxWidth * 0.58f

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(listWidth)
                    .fillMaxHeight()
                    .clipToBounds(),
            ) {
                HomeEntryScreen(
                    onNavigateToDetail = onTickerSelected,
                    wideLayout = true,
                    supportingPane = selectedTicker != null,
                    selectedTicker = selectedTicker,
                    drawBackground = false,
                )
            }

            if (selectedTicker != null) {
                Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(detailWidth)
                    .fillMaxHeight()
                    .clipToBounds()
                    .graphicsLayer {
                        translationX = size.width * (1f - detailReveal)
                        alpha = detailReveal
                    },
                ) {
                    StocksRoute(
                        ticker = selectedTicker,
                        onBackClick = onDetailClosed,
                        showBackNavigation = true,
                        drawBackground = false,
                    )
                }
            }
        }
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
