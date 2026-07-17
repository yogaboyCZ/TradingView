package cz.yogaboy.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.yogaboy.core.design.LocalDimens
import cz.yogaboy.core.design.AuroraBackground
import cz.yogaboy.core.design.FrostedSurface
import cz.yogaboy.core.design.R as DR
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    wideLayout: Boolean = false,
    supportingPane: Boolean = false,
    selectedTicker: String? = null,
    drawBackground: Boolean = true,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val gridState = rememberLazyStaggeredGridState()
    // On a wide single-pane screen the header has enough room and must stay fully
    // expanded. Once detail is open, the narrowed supporting pane can collapse it.
    val searchBarCollapseEnabled = !wideLayout || supportingPane
    // Header + recommendation card + roughly the first third of a product card.
    // Keeping this independent of the lazy-grid item index avoids a sudden snap.
    val collapseDistancePx = with(density) { 440.dp.toPx() }
    var accumulatedScrollPx by remember { mutableFloatStateOf(0f) }
    val collapseScrollConnection = remember(collapseDistancePx) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                accumulatedScrollPx = (accumulatedScrollPx - consumed.y)
                    .coerceIn(0f, collapseDistancePx)
                return Offset.Zero
            }
        }
    }
    val rawCollapseProgress = (accumulatedScrollPx / collapseDistancePx).coerceIn(0f, 1f)
    var manuallyExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(gridState.isScrollInProgress) {
        if (gridState.isScrollInProgress) manuallyExpanded = false
    }
    LaunchedEffect(searchBarCollapseEnabled) {
        if (!searchBarCollapseEnabled) {
            accumulatedScrollPx = 0f
            manuallyExpanded = false
        }
    }
    val collapseTarget = when {
        !searchBarCollapseEnabled -> 0f
        manuallyExpanded -> 0f
        else -> rawCollapseProgress
    }
    val collapseProgress by animateFloatAsState(
        targetValue = collapseTarget,
        animationSpec = tween(if (manuallyExpanded) 520 else 90),
        label = "scroll-linked-search-collapse",
    )
    var headerHeightPx by remember(density) {
        mutableIntStateOf(with(density) { 190.dp.roundToPx() })
    }
    val headerHeight = with(density) { headerHeightPx.toDp() }

    val content: @Composable () -> Unit = {
        Box(Modifier.fillMaxSize()) {
            SuggestedProducts(
                onProductClick = { onEvent(HomeEvent.ProductSelected(it.ticker)) },
                supportingPane = supportingPane,
                selectedTicker = selectedTicker,
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (searchBarCollapseEnabled) {
                            Modifier.nestedScroll(collapseScrollConnection)
                        } else {
                            Modifier
                        }
                    ),
                topContentPadding = headerHeight + LocalDimens.current.small,
                gridState = gridState,
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth(),
            ) {
                Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onSizeChanged {
                                    if (it.height > headerHeightPx) headerHeightPx = it.height
                                },
                        ) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .graphicsLayer { alpha = 1f - collapseProgress }
                                    .background(
                                    if (drawBackground) Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xF20A1742),
                                            Color(0xD90E2258),
                                            Color(0xA60D2B67),
                                            Color.Transparent,
                                        ),
                                    ) else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent)),
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
                                        bottom = 24.dp,
                                    ),
                            ) {
                                if (!supportingPane) {
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
                                                    color = Color.White,
                                                )
                                            }
                                        },
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = Color.Transparent,
                                            titleContentColor = Color.White,
                                        ),
                                    )
                                    Spacer(Modifier.height(LocalDimens.current.small))
                                } else {
                                    Text(
                                        text = stringResource(DR.string.home_title),
                                        modifier = Modifier.padding(
                                            start = LocalDimens.current.small,
                                            bottom = LocalDimens.current.small,
                                        ).graphicsLayer {
                                            alpha = 1f -
                                                (collapseProgress / 0.35f).coerceIn(0f, 1f)
                                        },
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                    )
                                }

                                TopSearchBar(
                                    value = state.query,
                                    onValueChange = { onEvent(HomeEvent.QueryChanged(it)) },
                                    onSearch = {
                                        if (state.query.isNotBlank()) onEvent(HomeEvent.Submit)
                                    },
                                    onClear = { onEvent(HomeEvent.Clear) },
                                    collapseProgress = collapseProgress,
                                    onExpand = {
                                        accumulatedScrollPx = 0f
                                        manuallyExpanded = true
                                    },
                                    modifier = Modifier.graphicsLayer {
                                        translationY = with(density) {
                                            -(if (supportingPane) 42.dp else 72.dp).toPx()
                                        } * collapseProgress
                                    },
                                )
                            }
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

private data class SuggestedProduct(
    val ticker: String,
    val companyName: String,
    val category: String,
    val trend: List<Float>,
    val price: String,
    val dailyChange: Float,
    val showTrendSummary: Boolean = false,
)

private val suggestedProducts = listOf(
    SuggestedProduct("AAPL", "Apple", "Technologie", listOf(42f, 44f, 43f, 48f, 47f, 51f, 55f), "334.20 USD", 2.04f, true),
    SuggestedProduct("MSFT", "Microsoft", "Technologie", listOf(38f, 40f, 44f, 43f, 47f, 49f, 52f), "403.44 USD", 1.97f),
    SuggestedProduct("NVDA", "NVIDIA", "Polovodiče", listOf(29f, 34f, 32f, 41f, 46f, 45f, 58f), "206.97 USD", -2.60f, true),
    SuggestedProduct("GOOGL", "Alphabet", "Internet", listOf(48f, 46f, 49f, 52f, 51f, 55f, 57f), "355.55 USD", -4.14f),
    SuggestedProduct("AMZN", "Amazon", "E-commerce", listOf(35f, 39f, 37f, 42f, 46f, 44f, 49f), "252.49 USD", -0.97f),
    SuggestedProduct("KO", "Coca-Cola", "Spotřební zboží", listOf(51f, 50f, 52f, 51f, 53f, 54f, 55f), "69.18 USD", 0.42f, true),
    SuggestedProduct("BRK.B", "Berkshire Hathaway", "Finance", listOf(40f, 42f, 45f, 44f, 48f, 50f, 53f), "503.71 USD", 0.81f),
    SuggestedProduct("BA", "Boeing", "Letecký průmysl", listOf(55f, 52f, 54f, 49f, 47f, 45f, 43f), "224.86 USD", -1.35f, true),
    SuggestedProduct("TSLA", "Tesla", "Automobily", listOf(45f, 53f, 47f, 56f, 50f, 58f, 54f), "390.13 USD", -1.10f),
    SuggestedProduct("JPM", "JPMorgan Chase", "Bankovnictví", listOf(41f, 43f, 42f, 46f, 49f, 48f, 52f), "289.42 USD", 0.64f, true),
)

@Composable
private fun SuggestedProducts(
    onProductClick: (SuggestedProduct) -> Unit,
    supportingPane: Boolean,
    selectedTicker: String?,
    modifier: Modifier = Modifier,
    topContentPadding: androidx.compose.ui.unit.Dp = LocalDimens.current.default,
    gridState: LazyStaggeredGridState,
) {
    val density = LocalDensity.current
    val textColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    LazyVerticalStaggeredGrid(
        columns = if (supportingPane) {
            StaggeredGridCells.Fixed(2)
        } else {
            StaggeredGridCells.Adaptive(minSize = 160.dp)
        },
        state = gridState,
        modifier = modifier,
        contentPadding = PaddingValues(
            start = LocalDimens.current.default,
            top = topContentPadding,
            end = LocalDimens.current.default,
            bottom = LocalDimens.current.default + 32.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(LocalDimens.current.small),
        verticalItemSpacing = LocalDimens.current.small,
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            FrostedSurface(
                shape = RoundedCornerShape(LocalDimens.current.radiusLarge),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(LocalDimens.current.default)) {
                    Text(
                        text = "Doporučené produkty",
                        style = MaterialTheme.typography.headlineSmall,
                        color = textColor,
                    )
                    Spacer(Modifier.height(LocalDimens.current.tiny))
                    Text(
                        text = "Vyberte akcii a zobrazte její aktuální cenu.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                    )
                }
            }
        }

        items(suggestedProducts, key = SuggestedProduct::ticker) { product ->
            AnimatedVisibility(
                visible = product.ticker != selectedTicker,
                enter = fadeIn(tween(420)),
                exit = slideOutHorizontally(
                    animationSpec = tween(520),
                    targetOffsetX = { it * 2 },
                ) + fadeOut(tween(420)) + shrinkVertically(tween(520)),
            ) {
                val rotationY by transition.animateFloat(
                    transitionSpec = { tween(520) },
                    label = "product-card-flip-${product.ticker}",
                ) { visibility ->
                    if (visibility == EnterExitState.Visible) 0f else 88f
                }
                SuggestedProductCard(
                    product = product,
                    onClick = { onProductClick(product) },
                    modifier = Modifier.graphicsLayer {
                        this.rotationY = rotationY
                        cameraDistance = 18f * density.density
                    },
                )
            }
        }
    }
}

@Composable
private fun SuggestedProductCard(
    product: SuggestedProduct,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val shape = RoundedCornerShape(LocalDimens.current.radiusLarge)

    FrostedSurface(
        shape = shape,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(LocalDimens.current.default),
            verticalArrangement = Arrangement.spacedBy(LocalDimens.current.small),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(LocalDimens.current.tiny),
            ) {
                Text(
                    text = product.companyName,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                )
                Surface(
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(LocalDimens.current.radiusMedium),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = product.ticker,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            SparklineChart(
                values = product.trend,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (product.showTrendSummary) 58.dp else 40.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = product.price,
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor,
                )
                Text(
                    text = "${if (product.dailyChange >= 0) "+" else ""}${"%.2f".format(product.dailyChange)} %",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (product.dailyChange >= 0) Color(0xFF008F73) else Color(0xFFD32F2F),
                )
            }

            if (product.showTrendSummary) {
                val change = ((product.trend.last() / product.trend.first()) - 1f) * 100f
                Text(
                    text = "Vývoj období: ${if (change >= 0f) "+" else ""}${"%.1f".format(change)} %",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (change >= 0f) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                )
            }

            Text(
                text = product.category,
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
            )
        }
    }
}

@Composable
private fun SparklineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
) {
    if (values.size < 2) return

    val isPositive = values.last() >= values.first()
    val lineColor = if (isPositive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
    val guideColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)

    Canvas(modifier = modifier) {
        val minValue = values.min()
        val maxValue = values.max()
        val valueRange = (maxValue - minValue).takeIf { it > 0f } ?: 1f
        val horizontalStep = size.width / values.lastIndex
        val verticalPadding = 4.dp.toPx()
        val chartHeight = size.height - verticalPadding * 2

        drawLine(
            color = guideColor,
            start = androidx.compose.ui.geometry.Offset(0f, size.height),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
            strokeWidth = 1.dp.toPx(),
        )

        val path = Path()
        values.forEachIndexed { index, value ->
            val x = index * horizontalStep
            val normalizedValue = (value - minValue) / valueRange
            val y = verticalPadding + chartHeight * (1f - normalizedValue)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )

        val lastValue = values.last()
        val lastY = verticalPadding + chartHeight * (1f - (lastValue - minValue) / valueRange)
        drawCircle(
            color = lineColor,
            radius = 3.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(size.width, lastY),
        )
    }
}

@Composable
private fun TopSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    collapseProgress: Float,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val focusRequester = remember { FocusRequester() }
    var focusAfterExpand by remember { mutableStateOf(false) }
    var wasKeyboardVisible by remember { mutableStateOf(false) }

    LaunchedEffect(imeBottom) {
        if (imeBottom > 0) {
            wasKeyboardVisible = true
        } else if (wasKeyboardVisible) {
            focusManager.clearFocus(force = true)
            wasKeyboardVisible = false
        }
    }

    LaunchedEffect(focusAfterExpand) {
        if (focusAfterExpand) {
            focusRequester.requestFocus()
            delay(16)
            keyboardController?.show()
            focusAfterExpand = false
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {
        val mergeProgress = ((collapseProgress - 0.08f) / 0.27f).coerceIn(0f, 1f)
        val buttonCollapse = ((collapseProgress - 0.68f) / 0.32f).coerceIn(0f, 1f)
        val buttonWidth = 104.dp * (1f - buttonCollapse)
        val gapWidth = LocalDimens.current.medium * (1f - mergeProgress)
        val searchWidth = 56.dp +
            (maxWidth - 56.dp - buttonWidth - gapWidth) *
            (1f - collapseProgress)
        val searchShape = RoundedCornerShape(28.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
            modifier = Modifier
                .width(searchWidth)
                .height(56.dp)
                .clip(searchShape)
                .background(Color(0x8F52658F))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.48f),
                            Color.White.copy(alpha = 0.10f),
                        ),
                    ),
                    shape = searchShape,
                )
            ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .graphicsLayer {
                        alpha = 1f - ((collapseProgress - 0.82f) / 0.18f).coerceIn(0f, 1f)
                    },
                singleLine = true,
                shape = searchShape,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (value.isNotBlank()) {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            onSearch()
                        }
                    }
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = Color.White,
                    )
                },
                trailingIcon = {
                    if (value.isNotEmpty()) {
                        IconButton(onClick = {
                            onClear()
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                tint = Color.White,
                            )
                        }
                    }
                },
                placeholder = {
                    Text(
                        text = stringResource(DR.string.home_search_placeholder),
                        color = Color.White.copy(alpha = 0.72f),
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                ),
            )
            if (collapseProgress > 0.90f) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Rozbalit hledání",
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            if (collapseProgress > 0.01f) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable {
                            onExpand()
                            focusAfterExpand = true
                        },
                )
            }
            }
            Spacer(Modifier.width(gapWidth))
            Button(
            onClick = {
                if (value.isNotBlank()) {
                    focusManager.clearFocus(force = true)
                    onSearch()
                }
            },
            enabled = value.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xB92D5B91),
                contentColor = Color.White,
                disabledContainerColor = Color(0x8F52658F),
                disabledContentColor = Color.White.copy(alpha = 0.72f),
            ),
            modifier = Modifier
                .width(buttonWidth)
                .height(56.dp)
                .graphicsLayer {
                    alpha = 1f - mergeProgress
                    translationX = -size.width * 0.82f * mergeProgress
                },
            shape = RoundedCornerShape(28.dp),
            ) {
                if (buttonWidth > 1.dp) {
                    Text(
                        text = stringResource(DR.string.home_search_button),
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        state = HomeState(query = ""),
        onEvent = {},
    )
}
