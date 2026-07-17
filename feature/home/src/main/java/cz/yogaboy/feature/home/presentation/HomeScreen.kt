package cz.yogaboy.feature.home.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.yogaboy.core.design.AuroraBackground
import cz.yogaboy.core.design.LocalDimens
import cz.yogaboy.core.design.theme.tradingColors
import cz.yogaboy.core.design.R as DR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    wideLayout: Boolean = false,
    supportingPane: Boolean = false,
    selectedTicker: String? = null,
    drawBackground: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val windowHeight = with(density) { LocalWindowInfo.current.containerSize.height.toDp() }
    val compactHeight = windowHeight < 600.dp
    val gridState = rememberLazyStaggeredGridState()
    val searchBarCollapseEnabled = compactHeight || !wideLayout || supportingPane
    val collapseDistancePx = with(density) {
        (if (compactHeight) 160.dp else 440.dp).toPx()
    }
    var accumulatedScrollPx by remember { mutableFloatStateOf(0f) }
    var manuallyExpanded by remember { mutableStateOf(false) }
    val collapseScrollConnection = remember(collapseDistancePx) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (consumed.y != 0f) manuallyExpanded = false
                accumulatedScrollPx = (accumulatedScrollPx - consumed.y)
                    .coerceIn(0f, collapseDistancePx)
                return Offset.Zero
            }
        }
    }
    val rawCollapseProgress = (accumulatedScrollPx / collapseDistancePx).coerceIn(0f, 1f)
    val collapseProgress by animateFloatAsState(
        targetValue = when {
            !searchBarCollapseEnabled -> 0f
            manuallyExpanded -> 0f
            else -> rawCollapseProgress
        },
        animationSpec = tween(if (manuallyExpanded) 520 else 90),
        label = "scroll-linked-search-collapse",
    )
    var headerHeightPx by remember(density, compactHeight) {
        mutableIntStateOf(with(density) {
            (if (compactHeight) 80.dp else 190.dp).roundToPx()
        })
    }
    val headerHeight = with(density) { headerHeightPx.toDp() }

    val content: @Composable () -> Unit = {
        Box(Modifier.fillMaxSize()) {
            SuggestedProducts(
                products = state.suggestedProducts,
                onProductClick = { onEvent(HomeEvent.ProductSelected(it.ticker)) },
                histories = state.histories,
                failedHistories = state.failedHistories,
                supportingPane = supportingPane,
                selectedTicker = selectedTicker,
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (searchBarCollapseEnabled) {
                            Modifier.nestedScroll(collapseScrollConnection)
                        } else {
                            Modifier
                        },
                    ),
                topContentPadding = headerHeight + LocalDimens.current.small,
                gridState = gridState,
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .onSizeChanged {
                        if (it.height != headerHeightPx) headerHeightPx = it.height
                    },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = 1f - collapseProgress }
                        .background(
                            if (drawBackground) {
                                Brush.verticalGradient(MaterialTheme.tradingColors.headerScrim)
                            } else {
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Transparent),
                                )
                            },
                        ),
                )
                Column(
                    Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(
                            start = LocalDimens.current.medium,
                            end = LocalDimens.current.medium,
                            top = LocalDimens.current.tiny,
                            bottom = if (compactHeight) LocalDimens.current.tiny else 24.dp,
                        ),
                ) {
                    if (!compactHeight && !supportingPane) {
                        TopAppBar(
                            title = {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            alpha = 1f -
                                                (collapseProgress / 0.35f).coerceIn(0f, 1f)
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(DR.string.home_title),
                                        color = MaterialTheme.tradingColors.onBackdrop,
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                titleContentColor = MaterialTheme.tradingColors.onBackdrop,
                            ),
                        )
                        Spacer(Modifier.height(LocalDimens.current.small))
                    } else if (!compactHeight) {
                        Text(
                            text = stringResource(DR.string.home_title),
                            modifier = Modifier
                                .padding(
                                    start = LocalDimens.current.small,
                                    bottom = LocalDimens.current.small,
                                )
                                .graphicsLayer {
                                    alpha = 1f -
                                        (collapseProgress / 0.35f).coerceIn(0f, 1f)
                                },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.tradingColors.onBackdrop,
                        )
                    }

                    TopSearchBar(
                        value = state.query,
                        onValueChange = { onEvent(HomeEvent.QueryChanged(it)) },
                        onSearch = { if (state.query.isNotBlank()) onEvent(HomeEvent.Submit) },
                        onClear = { onEvent(HomeEvent.Clear) },
                        collapseProgress = collapseProgress,
                        onExpand = {
                            accumulatedScrollPx = 0f
                            manuallyExpanded = true
                        },
                        modifier = Modifier.graphicsLayer {
                            translationY = if (compactHeight) {
                                0f
                            } else {
                                with(density) {
                                    -(if (supportingPane) 42.dp else 72.dp).toPx()
                                } * collapseProgress
                            }
                        },
                    )
                }
            }
        }
    }

    if (drawBackground) {
        AuroraBackground(modifier = modifier.fillMaxSize()) { content() }
    } else {
        Box(modifier = modifier.fillMaxSize()) { content() }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(state = HomeState(), onEvent = {})
}
