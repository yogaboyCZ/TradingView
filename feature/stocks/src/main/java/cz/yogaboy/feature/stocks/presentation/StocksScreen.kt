package cz.yogaboy.feature.stocks.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.yogaboy.core.design.LocalDimens
import cz.yogaboy.core.design.AuroraBackground
import cz.yogaboy.core.design.FrostedSurface
import cz.yogaboy.core.design.formatCurrencyAmount
import cz.yogaboy.core.design.theme.tradingColors
import cz.yogaboy.feature.stocks.presentation.model.DisplayPrice
import cz.yogaboy.domain.marketdata.CompanyNews
import cz.yogaboy.domain.marketdata.CompanyProfile
import cz.yogaboy.domain.marketdata.LivePriceTick
import cz.yogaboy.domain.marketdata.PricePoint
import cz.yogaboy.feature.stocks.presentation.preview.StocksSuccessProvider
import cz.yogaboy.core.design.R as DR
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.blurEffect
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StocksScreen(
    ticker: String,
    state: StocksState,
    onClick: (StocksEvent) -> Unit,
    onBackClick: () -> Unit,
    showBackNavigation: Boolean = true,
    drawBackground: Boolean = true,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val appBarHazeState = rememberHazeState()
    val appBarHazeStyle = remember {
        HazeBlurStyle(
            colorEffects = emptyList(),
            blurRadius = 24.dp,
            noiseFactor = 0.015f,
        )
    }
    val density = LocalDensity.current
    val frostRevealDistancePx = with(density) { 72.dp.toPx() }
    val appBarFrostProgress by remember(listState, frostRevealDistancePx) {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                1f
            } else {
                (listState.firstVisibleItemScrollOffset / frostRevealDistancePx)
                    .coerceIn(0f, 1f)
            }
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val navigationBarPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val primaryContentSettled =
        state.twelve is StocksUiState.Error ||
            (state.twelve !is StocksUiState.Loading &&
                state.history !is StocksUiState.Loading)
    val content: @Composable () -> Unit = {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer { alpha = appBarFrostProgress }
                            .hazeEffect(state = appBarHazeState) {
                                blurEffect {
                                    style = appBarHazeStyle
                                }
                            },
                    )
                    TopAppBar(
                        title = {
                            Text(
                                text = ticker.uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                            )
                        },
                        navigationIcon = {
                            if (showBackNavigation) {
                                IconButton(onClick = onBackClick) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Zpět",
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            navigationIconContentColor = MaterialTheme.tradingColors.onBackdrop,
                            titleContentColor = MaterialTheme.tradingColors.onBackdrop,
                        ),
                    )
                }
            },
        ) { contentPadding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = appBarHazeState)
                    .padding(
                        horizontal = LocalDimens.current.default,
                    ),
                verticalArrangement = Arrangement.spacedBy(LocalDimens.current.medium),
                contentPadding = PaddingValues(
                    top = contentPadding.calculateTopPadding() + LocalDimens.current.medium,
                    bottom = LocalDimens.current.default + navigationBarPadding,
                ),
            ) {
                item(key = "price-summary-$ticker") {
                    PriceSummaryCard(
                        ticker = ticker,
                        alphaState = state.alpha,
                        twelveState = state.twelve,
                        historyState = state.history,
                        livePrice = state.livePrice,
                        onRequestAlphaComparison = {
                            onClick(StocksEvent.RequestAlphaComparison)
                        },
                    )
                }
                item(key = "company-profile-$ticker") {
                    AnimatedVisibility(
                        visible = primaryContentSettled,
                        enter = fadeIn(
                            animationSpec = tween(durationMillis = 320, delayMillis = 70),
                        ) + slideInVertically(
                            animationSpec = tween(
                                durationMillis = 420,
                                delayMillis = 70,
                                easing = FastOutSlowInEasing,
                            ),
                            initialOffsetY = { height -> height / 6 },
                        ),
                        exit = fadeOut(tween(120)),
                    ) {
                        CompanyProfileSection(state.profile)
                    }
                }
                item(key = "company-news-$ticker") {
                    AnimatedVisibility(
                        visible = primaryContentSettled,
                        enter = fadeIn(
                            animationSpec = tween(durationMillis = 320, delayMillis = 150),
                        ) + slideInVertically(
                            animationSpec = tween(
                                durationMillis = 420,
                                delayMillis = 150,
                                easing = FastOutSlowInEasing,
                            ),
                            initialOffsetY = { height -> height / 6 },
                        ),
                        exit = fadeOut(tween(120)),
                    ) {
                        CompanyNewsSection(
                            state = state.news,
                            onNewsOpened = {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(NEWS_SECTION_INDEX)
                                }
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

@Composable
private fun PriceHistoryContent(
    state: StocksUiState<List<PricePoint>>,
    onInspectedPointChange: (PricePoint?) -> Unit,
) {
    var selectedPeriod by rememberSaveable { mutableStateOf(ChartPeriod.ONE_YEAR) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    when (state) {
            StocksUiState.Deferred -> SectionUnavailable("Data se načtou na vyžádání.")
            StocksUiState.Loading -> SectionLoading()
            is StocksUiState.Error -> SectionUnavailable(state.message)
            is StocksUiState.Data -> {
                val points = selectedPeriod.selectFrom(state.value)
                Text(
                    text = "Dlouhým stiskem a tažením zobrazíte cenu v čase",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f),
                )
                PriceHistoryChart(
                    points = points,
                    selectedIndex = selectedIndex,
                    onSelectedIndexChange = {
                        selectedIndex = it ?: -1
                        onInspectedPointChange(it?.let(points::getOrNull))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(LocalDimens.current.tiny),
                ) {
                    ChartPeriod.entries.forEach { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = {
                                selectedPeriod = period
                                selectedIndex = -1
                                onInspectedPointChange(null)
                            },
                            label = { Text(period.label) },
                        )
                    }
                }

                val first = points.firstOrNull()?.close
                val last = points.lastOrNull()?.close
                if (first != null && last != null) {
                    val change = (last / first - 1.0) * 100.0
                    Text(
                        text = "${selectedPeriod.label} · ${points.size} obchodních dnů: " +
                            "${if (change >= 0) "+" else ""}${"%.2f".format(change)} %",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (change >= 0) MaterialTheme.tradingColors.positive else MaterialTheme.tradingColors.negative,
                    )
                }
            }
        }
}

@Composable
private fun PriceHistoryChart(
    points: List<PricePoint>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (points.size < 2) {
        SectionUnavailable("Pro graf není dostatek bodů.")
        return
    }
    val values = points.map(PricePoint::close)
    val positive = values.last() >= values.first()
    val lineColor = if (positive) MaterialTheme.tradingColors.positive else MaterialTheme.tradingColors.negative
    val fillColor = lineColor.copy(alpha = 0.18f)
    val guideColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    val selectionLineColor = MaterialTheme.tradingColors.chartCrosshair
    val selectionCenterColor = MaterialTheme.colorScheme.surface
    val selectionLabelBackground = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f)
    val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()
    val axisTextStyle = TextStyle(color = axisTextColor, fontSize = 10.sp)
    val axisEndPaddingPx = with(LocalDensity.current) { 4.dp.toPx() }
    val axisGapPx = with(LocalDensity.current) { 4.dp.toPx() }
    val axisLabelWidthPx = remember(values, textMeasurer, axisTextStyle) {
        val min = values.min()
        val max = values.max()
        listOf(max, (max + min) / 2.0, min).maxOf { value ->
            textMeasurer.measure(
                text = "%.0f".format(value),
                style = axisTextStyle,
            ).size.width
        }.toFloat()
    }
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current
    val chartHaptics = remember(context, hapticFeedback) {
        ChartHaptics(context.applicationContext, hapticFeedback)
    }
    val drawProgress = remember(points) { Animatable(0f) }

    LaunchedEffect(points) {
        drawProgress.snapTo(0f)
        drawProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 950, easing = FastOutSlowInEasing),
        )
    }

    Canvas(
        modifier.pointerInput(points, axisEndPaddingPx, axisLabelWidthPx, axisGapPx) {
            var lastHapticIndex: Int? = null

            fun selectPoint(x: Float, performTick: Boolean) {
                val chartWidth = (size.width - axisEndPaddingPx - axisLabelWidthPx - axisGapPx)
                    .coerceAtLeast(1f)
                val index = ((x.coerceIn(0f, chartWidth) / chartWidth) * points.lastIndex)
                    .toInt()
                    .coerceIn(0, points.lastIndex)
                if (index != lastHapticIndex) {
                    if (performTick) {
                        chartHaptics.tick()
                    }
                    lastHapticIndex = index
                    onSelectedIndexChange(index)
                }
            }

            detectDragGesturesAfterLongPress(
                onDragStart = {
                    chartHaptics.longPress()
                    lastHapticIndex = null
                    selectPoint(it.x, performTick = false)
                },
                onDrag = { change, _ ->
                    change.consume()
                    selectPoint(change.position.x, performTick = true)
                },
                onDragEnd = {
                    lastHapticIndex = null
                    onSelectedIndexChange(null)
                },
                onDragCancel = {
                    lastHapticIndex = null
                    onSelectedIndexChange(null)
                },
            )
        }
    ) {
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0.0 } ?: 1.0
        val chartWidth = (size.width - axisEndPaddingPx - axisLabelWidthPx - axisGapPx)
            .coerceAtLeast(1f)
        val step = chartWidth / values.lastIndex
        val verticalPadding = 12.dp.toPx()
        val dateAxisHeight = 18.dp.toPx()
        val chartBottom = size.height - dateAxisHeight
        val usableHeight = chartBottom - verticalPadding * 2
        val linePath = androidx.compose.ui.graphics.Path()
        val fillPath = androidx.compose.ui.graphics.Path()

        values.forEachIndexed { index, value ->
            val x = index * step
            val y = verticalPadding + usableHeight * (1f - ((value - min) / range).toFloat())
            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, chartBottom)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(chartWidth, chartBottom)
        fillPath.close()

        repeat(3) { index ->
            val y = chartBottom * (index + 1) / 4f
            drawLine(guideColor, androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(chartWidth, y))
        }
        val revealedWidth = chartWidth * drawProgress.value
        clipRect(right = revealedWidth) {
            drawPath(fillPath, brush = Brush.verticalGradient(listOf(fillColor, Color.Transparent)))
            drawPath(
                linePath,
                color = lineColor.copy(alpha = 0.10f),
                style = Stroke(width = 11.dp.toPx(), cap = StrokeCap.Round),
            )
            drawPath(
                linePath,
                color = lineColor.copy(alpha = 0.24f),
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
            )
            drawPath(
                linePath,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
        }

        if (drawProgress.value in 0.001f..0.999f) {
            val position = drawProgress.value * values.lastIndex
            val leftIndex = position.toInt().coerceAtMost(values.lastIndex - 1)
            val fraction = position - leftIndex
            val animatedValue = values[leftIndex] +
                (values[leftIndex + 1] - values[leftIndex]) * fraction
            val headY = verticalPadding + usableHeight *
                (1f - ((animatedValue - min) / range).toFloat())
            val head = androidx.compose.ui.geometry.Offset(revealedWidth, headY)
            drawCircle(lineColor.copy(alpha = 0.10f), 13.dp.toPx(), head)
            drawCircle(lineColor.copy(alpha = 0.24f), 8.dp.toPx(), head)
            drawCircle(lineColor, 3.5.dp.toPx(), head)
        }

        if (selectedIndex in points.indices) {
            val selectedX = selectedIndex * step
            val selectedY = verticalPadding + usableHeight *
                (1f - ((values[selectedIndex] - min) / range).toFloat())
            val beamBrush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    selectionLineColor.copy(alpha = 0.92f),
                    selectionLineColor,
                    selectionLineColor.copy(alpha = 0.92f),
                    Color.Transparent,
                ),
                startY = 0f,
                endY = chartBottom,
            )
            drawLine(
                brush = beamBrush,
                start = androidx.compose.ui.geometry.Offset(selectedX, 0f),
                end = androidx.compose.ui.geometry.Offset(selectedX, chartBottom),
                strokeWidth = 16.dp.toPx(),
                alpha = 0.08f,
                cap = StrokeCap.Round,
            )
            drawLine(
                brush = beamBrush,
                start = androidx.compose.ui.geometry.Offset(selectedX, 0f),
                end = androidx.compose.ui.geometry.Offset(selectedX, chartBottom),
                strokeWidth = 8.dp.toPx(),
                alpha = 0.20f,
                cap = StrokeCap.Round,
            )
            drawLine(
                brush = beamBrush,
                start = androidx.compose.ui.geometry.Offset(selectedX, 0f),
                end = androidx.compose.ui.geometry.Offset(selectedX, chartBottom),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawCircle(
                color = selectionLineColor.copy(alpha = 0.10f),
                radius = 15.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(selectedX, selectedY),
            )
            drawCircle(
                color = selectionLineColor.copy(alpha = 0.30f),
                radius = 9.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(selectedX, selectedY),
            )
            drawCircle(
                color = selectionLineColor,
                radius = 6.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(selectedX, selectedY),
            )
            drawCircle(
                color = selectionCenterColor,
                radius = 2.5.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(selectedX, selectedY),
            )

            val dateLabel = textMeasurer.measure(
                text = points[selectedIndex].date.toChartDate(),
                style = axisTextStyle,
            )
            val labelPadding = 4.dp.toPx()
            val labelGap = 8.dp.toPx()
            val labelWidth = dateLabel.size.width.toFloat()
            val labelHeight = dateLabel.size.height.toFloat()
            val labelX = if (selectedX + labelGap + labelWidth + labelPadding * 2 <= chartWidth) {
                selectedX + labelGap + labelPadding
            } else {
                selectedX - labelGap - labelWidth - labelPadding
            }
            val labelY = (selectedY - labelHeight / 2f)
                .coerceIn(labelPadding, chartBottom - labelHeight - labelPadding)
            drawRoundRect(
                color = selectionLabelBackground,
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = labelX - labelPadding,
                    y = labelY - labelPadding,
                ),
                size = androidx.compose.ui.geometry.Size(
                    width = labelWidth + labelPadding * 2,
                    height = labelHeight + labelPadding * 2,
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(labelPadding),
            )
            drawText(
                textLayoutResult = dateLabel,
                topLeft = androidx.compose.ui.geometry.Offset(labelX, labelY),
            )
        }

        listOf(max, (max + min) / 2.0, min).forEachIndexed { index, value ->
            val label = textMeasurer.measure(
                text = "%.0f".format(value),
                style = axisTextStyle,
            )
            val centerY = when (index) {
                0 -> verticalPadding
                1 -> chartBottom / 2f
                else -> chartBottom - verticalPadding
            }
            drawText(
                textLayoutResult = label,
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = size.width - axisEndPaddingPx - label.size.width,
                    y = (centerY - label.size.height / 2f)
                        .coerceIn(0f, chartBottom - label.size.height),
                ),
            )
        }

        listOf(points.first().date to 0f, points.last().date to chartWidth).forEachIndexed {
                index, (date, anchorX) ->
            val label = textMeasurer.measure(
                text = date.toChartDate(),
                style = axisTextStyle,
            )
            drawText(
                textLayoutResult = label,
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = if (index == 0) anchorX else anchorX - label.size.width,
                    y = chartBottom + 2.dp.toPx(),
                ),
            )
        }
    }
}

private enum class ChartPeriod(
    val label: String,
    private val tradingDays: Int?,
) {
    ONE_WEEK("1T", 5),
    ONE_MONTH("1M", 22),
    THREE_MONTHS("3M", 66),
    SIX_MONTHS("6M", 132),
    ONE_YEAR("1R", 264),
    MAX("MAX", null);

    fun selectFrom(points: List<PricePoint>): List<PricePoint> =
        tradingDays?.let(points::takeLast) ?: points
}

private val chartDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val newsDateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH.mm")

private fun String.toChartDate(): String =
    runCatching { LocalDate.parse(this).format(chartDateFormatter) }.getOrDefault(this)

private fun String.toNewsDateTime(): String =
    runCatching {
        OffsetDateTime.parse(this)
            .atZoneSameInstant(ZoneId.systemDefault())
            .format(newsDateTimeFormatter)
    }.recoverCatching {
        LocalDateTime.parse(this).format(newsDateTimeFormatter)
    }.getOrDefault(this)

@Composable
private fun CompanyProfileSection(state: StocksUiState<CompanyProfile>) {
    val badge = (state as? StocksUiState.Data)?.value?.source?.badge ?: "…"
    DetailSection(title = "O společnosti", badge = badge) {
        when (state) {
            StocksUiState.Deferred -> SectionUnavailable("Data se načtou na vyžádání.")
            StocksUiState.Loading -> SectionLoading()
            is StocksUiState.Error -> SectionUnavailable(
                "Profil společnosti teď není dostupný."
            )
            is StocksUiState.Data -> {
                Text(
                    text = state.value.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                state.value.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ProfileValue("CEO", state.value.chiefExecutiveOfficer)
                ProfileValue("Burza", listOfNotNull(state.value.exchange, state.value.micCode).joinToString(" · "))
                ProfileValue("Sektor", state.value.sector)
                ProfileValue("Odvětví", state.value.industry)
                ProfileValue("Zaměstnanci", state.value.employees?.let { "%,d".format(it) })
                ProfileValue("Web", state.value.website)
                ProfileValue(
                    "Sídlo",
                    listOfNotNull(
                        state.value.address,
                        state.value.city,
                        state.value.state,
                        state.value.country,
                    ).joinToString(", "),
                )
                ProfileValue("Telefon", state.value.phone)
            }
        }
    }
}

@Composable
private fun CompanyNewsSection(
    state: StocksUiState<List<CompanyNews>>,
    onNewsOpened: () -> Unit,
) {
    when (state) {
        is StocksUiState.Data -> {
            if (state.value.isEmpty()) {
                DetailSection(title = "Zprávy společnosti", badge = "TD") {
                    SectionUnavailable("Pro tuto společnost zatím nejsou tiskové zprávy.")
                }
            } else {
                CompanyNewsFlipCard(
                    news = state.value,
                    onNewsOpened = onNewsOpened,
                )
            }
        }
        else -> DetailSection(title = "Zprávy společnosti", badge = "TD") {
            when (state) {
            StocksUiState.Deferred -> SectionUnavailable("Data se načtou na vyžádání.")
            StocksUiState.Loading -> SectionLoading()
            is StocksUiState.Error -> SectionUnavailable(
                "Zprávy společnosti teď nejsou dostupné."
            )
            is StocksUiState.Data -> Unit
            }
        }
    }
}

@Composable
private fun CompanyNewsFlipCard(
    news: List<CompanyNews>,
    onNewsOpened: () -> Unit,
) {
    var selectedNewsId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedNews = news.firstOrNull { it.id == selectedNewsId }
    LaunchedEffect(selectedNewsId, selectedNews) {
        if (selectedNewsId != null && selectedNews == null) selectedNewsId = null
    }
    val rotation by animateFloatAsState(
        targetValue = if (selectedNews == null) 0f else 180f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "company-news-card-flip",
    )
    val density = LocalDensity.current.density

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 18f * density
            },
    ) {
        DetailSection(
            title = "Zprávy společnosti",
            badge = "TD",
            modifier = Modifier
                .graphicsLayer { alpha = if (rotation <= 90f) 1f else 0f }
                .then(if (rotation <= 90f) Modifier else Modifier.clearAndSetSemantics { }),
        ) {
            news.forEachIndexed { index, item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(LocalDimens.current.radiusMedium))
                        .clickable(enabled = rotation <= 90f) {
                            selectedNewsId = item.id
                            onNewsOpened()
                        }
                        .padding(vertical = LocalDimens.current.small),
                    verticalArrangement = Arrangement.spacedBy(LocalDimens.current.small),
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    item.date?.let {
                        Text(
                            text = it.toNewsDateTime(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                    item.summary?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (index != news.lastIndex) HorizontalDivider()
            }
        }

        DetailSection(
            title = null,
            badge = "TD",
            headerLeading = {
                IconButton(onClick = { selectedNewsId = null }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Zavřít detail zprávy",
                    )
                }
            },
            modifier = Modifier
                .graphicsLayer {
                    rotationY = 180f
                    alpha = if (rotation > 90f) 1f else 0f
                }
                .then(if (rotation > 90f) Modifier else Modifier.clearAndSetSemantics { }),
        ) {
            selectedNews?.let { item ->
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                item.date?.let {
                    Text(
                        text = it.toNewsDateTime(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
                Text(
                    text = item.summary?.takeIf(String::isNotBlank)
                        ?: "Ke zprávě není dostupný podrobnější text.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private const val NEWS_SECTION_INDEX = 2

@Composable
private fun DetailSection(
    title: String?,
    badge: String,
    modifier: Modifier = Modifier,
    headerLeading: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    FrostedSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(LocalDimens.current.radiusLarge),
    ) {
        Column(
            modifier = Modifier.padding(LocalDimens.current.default),
            verticalArrangement = Arrangement.spacedBy(LocalDimens.current.small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                headerLeading?.invoke()
                if (title != null) {
                    Text(
                        text = title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            content()
        }
    }
}

@Composable
private fun ProfileValue(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.weight(0.35f),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.65f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionLoading() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
        Spacer(Modifier.width(LocalDimens.current.small))
        Text("Načítám data…")
    }
}

@Composable
private fun SectionUnavailable(message: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalDimens.current.small),
    ) {
        MarketOrbitEmptyIcon(Modifier.size(56.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun PriceSummaryCard(
    ticker: String,
    alphaState: StocksUiState<DisplayPrice>,
    twelveState: StocksUiState<DisplayPrice>,
    historyState: StocksUiState<List<PricePoint>> = StocksUiState.Deferred,
    livePrice: LivePriceTick? = null,
    onRequestAlphaComparison: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAlphaVantage by rememberSaveable(ticker) { mutableStateOf(false) }
    var entryAnimationPlayed by rememberSaveable(ticker) { mutableStateOf(false) }
    val entryRotation = remember(ticker) {
        Animatable(if (entryAnimationPlayed) 0f else -88f)
    }
    LaunchedEffect(ticker) {
        if (!entryAnimationPlayed) {
            entryAnimationPlayed = true
            entryRotation.snapTo(-88f)
            entryRotation.animateTo(
                targetValue = 0f,
                animationSpec = tween(680),
            )
        } else {
            entryRotation.snapTo(0f)
        }
    }
    val rotation by animateFloatAsState(
        targetValue = if (showAlphaVantage) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "provider-card-flip",
    )
    val shape = RoundedCornerShape(LocalDimens.current.radiusLarge)
    val cardInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                transformOrigin = TransformOrigin(0f, 0.5f)
                rotationY = entryRotation.value
                alpha = 1f - (kotlin.math.abs(entryRotation.value) / 88f) * 0.45f
                cameraDistance = 18f * density
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    transformOrigin = TransformOrigin.Center
                    rotationY = rotation
                    cameraDistance = 18f * density
                }
                .clip(shape)
                .clickable(
                    interactionSource = cardInteractionSource,
                    indication = null,
                ) {
                    if (!showAlphaVantage) onRequestAlphaComparison()
                    showAlphaVantage = !showAlphaVantage
                },
        ) {
            FrostedSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = shape,
            ) {
                if (rotation <= 90f) {
                    ProviderCardFace(
                        badge = "TD",
                        provider = stringResource(DR.string.twelve_data_provider),
                        state = twelveState,
                        livePrice = livePrice,
                        simulationEnabled = true,
                        historyState = historyState,
                    )
                } else {
                    Box(Modifier.graphicsLayer { rotationY = 180f }) {
                        ProviderCardFace(
                            badge = "AV",
                            provider = stringResource(DR.string.alpha_vantage_provider),
                            state = alphaState,
                            livePrice = null,
                            simulationEnabled = false,
                            historyState = null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderCardFace(
    badge: String,
    provider: String,
    state: StocksUiState<DisplayPrice>,
    livePrice: LivePriceTick?,
    simulationEnabled: Boolean,
    historyState: StocksUiState<List<PricePoint>>?,
) {
    val expandedContentIsLoading = historyState != null &&
        state !is StocksUiState.Error &&
        (state is StocksUiState.Loading ||
            state is StocksUiState.Deferred ||
            historyState is StocksUiState.Loading)
    Column(
        // Reserve the space occupied by a regular quote before its asynchronous
        // data arrives. Otherwise the sections below jump up during the entry flip
        // and back down as soon as the quote replaces the shorter loading row.
        modifier = Modifier
            .fillMaxWidth()
            // Reserve chart geometry only while a chart can still arrive. Error
            // and unsupported-symbol states must remain compact.
            .heightIn(min = if (expandedContentIsLoading) 720.dp else 320.dp)
            .padding(LocalDimens.current.default),
        verticalArrangement = Arrangement.spacedBy(LocalDimens.current.medium),
    ) {
        when (state) {
            StocksUiState.Deferred -> {
                ProviderBadge(badge)
                ProviderLoadingRow(provider)
            }
            is StocksUiState.Loading -> {
                ProviderBadge(badge)
                ProviderLoadingRow(provider)
            }
            is StocksUiState.Error -> {
                ProviderBadge(badge)
                ProviderErrorRow(provider, message = state.message)
            }
            is StocksUiState.Data -> QuoteDetails(
                price = state.value,
                badge = badge,
                livePrice = livePrice,
                simulationEnabled = simulationEnabled,
                historyState = historyState,
            )
        }

        Text(
            text = "Klepnutím kartu otočíte",
            modifier = Modifier.align(Alignment.End),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun ProviderBadge(badge: String, modifier: Modifier = Modifier) {
    val isAlphaVantage = badge == "AV"
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(LocalDimens.current.radiusMedium),
        color = if (isAlphaVantage) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
    ) {
        Text(
            text = badge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (isAlphaVantage) {
                MaterialTheme.colorScheme.onTertiaryContainer
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuoteDetails(
    price: DisplayPrice,
    badge: String,
    livePrice: LivePriceTick?,
    simulationEnabled: Boolean,
    historyState: StocksUiState<List<PricePoint>>?,
) {
    var showSimulationInfo by rememberSaveable { mutableStateOf(false) }
    var inspectedPoint by remember { mutableStateOf<PricePoint?>(null) }
    val displayedPrice = inspectedPoint?.close ?: livePrice?.price ?: price.last
    val priceColor by animateColorAsState(
        targetValue = when {
            livePrice == null -> MaterialTheme.colorScheme.tertiary
            livePrice.change >= 0.0 -> MaterialTheme.tradingColors.positive
            else -> MaterialTheme.tradingColors.negative
        },
        animationSpec = tween(260),
        label = "live-price-color",
    )
    val stableTickerStyle = MaterialTheme.typography.headlineSmall
    val stablePriceStyle = MaterialTheme.typography.headlineSmall.copy(
        fontFamily = FontFamily.Monospace,
        fontFeatureSettings = "tnum",
    )

    Column(verticalArrangement = Arrangement.spacedBy(LocalDimens.current.small)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = price.name ?: price.ticker.uppercase(),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ProviderBadge(badge)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = price.ticker.substringBefore(':').uppercase(),
                        style = stableTickerStyle,
                        color = priceColor,
                        maxLines = 1,
                    )
                    Spacer(Modifier.width(LocalDimens.current.small))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            text = formatCurrencyAmount(displayedPrice, price.currency),
                            style = stablePriceStyle,
                            color = priceColor,
                            maxLines = 1,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    livePrice?.let { tick ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (tick.change >= 0.0) {
                                Icons.Filled.ArrowUpward
                            } else {
                                Icons.Filled.ArrowDownward
                            },
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = priceColor,
                        )
                        Text(
                            text = formatCurrencyAmount(abs(tick.change), price.currency),
                            style = MaterialTheme.typography.labelMedium,
                            color = priceColor,
                        )
                    }
                    }
                }
            }
            if (simulationEnabled) {
                IconButton(onClick = { showSimulationInfo = true }) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Informace o simulované ceně",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        historyState?.let {
            PriceHistoryContent(
                state = it,
                onInspectedPointChange = { point -> inspectedPoint = point },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LocalDimens.current.small),
        ) {
            QuoteValue("Open", price.open, Modifier.weight(1f))
            QuoteValue("High", price.high, Modifier.weight(1f))
            QuoteValue("Low", price.low, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LocalDimens.current.small),
        ) {
            QuoteValue("Previous", price.previousClose, Modifier.weight(1f))
            QuoteValue("Change", price.change, Modifier.weight(1f))
            QuoteValue("Change %", price.changePercent, Modifier.weight(1f))
        }

        price.volume?.let { QuoteText("Volume", "%,d".format(it)) }
        price.currency?.let { QuoteText("Currency", it) }
        price.exchange?.let { QuoteText("Exchange", it) }
        price.micCode?.let { QuoteText("MIC", it) }
        price.instrumentType?.let { QuoteText("Type", it) }
        price.asOf?.let { QuoteText("As of", it) }
    }

    if (showSimulationInfo) {
        ModalBottomSheet(onDismissRequest = { showSimulationInfo = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(LocalDimens.current.small),
            ) {
                Text(
                    text = "Simulovaný živý kurz",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "Drobné přírůstky a úbytky ceny jsou pouze simulace pro ukázku živého trhu a animací. Nejde o skutečná tržní data ani investiční informaci.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun QuoteValue(
    label: String,
    value: Double?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(LocalDimens.current.radiusMedium))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f))
            .padding(LocalDimens.current.small),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value?.toString() ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun QuoteText(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProviderLoadingRow(provider: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LocalDimens.current.radiusMedium))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .padding(
                horizontal = LocalDimens.current.default,
                vertical = LocalDimens.current.small
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = provider,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun ProviderErrorRow(provider: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LocalDimens.current.radiusMedium))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
                    )
                )
            )
            .padding(
                horizontal = LocalDimens.current.default,
                vertical = LocalDimens.current.default,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalDimens.current.small),
    ) {
        MarketOrbitEmptyIcon()
        Text(
            text = "Data právě nejsou dostupná",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "$provider · $message",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f),
        )
    }
}

@Composable
private fun MarketOrbitEmptyIcon(
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "market-orbit")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200),
            repeatMode = RepeatMode.Restart,
        ),
        label = "orbit-rotation",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "orbit-pulse",
    )
    val primary = MaterialTheme.colorScheme.tertiary
    val secondary = MaterialTheme.colorScheme.primary
    val glow = MaterialTheme.colorScheme.onPrimary

    Canvas(
        modifier = modifier
            .size(76.dp)
            .graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            },
    ) {
        val center = center
        val orbitRadius = size.minDimension * 0.34f
        val angle = Math.toRadians(rotation.toDouble())

        drawCircle(
            brush = Brush.radialGradient(
                0f to glow.copy(alpha = 0.42f),
                0.55f to primary.copy(alpha = 0.18f),
                1f to Color.Transparent,
                center = center,
                radius = size.minDimension * 0.5f,
            ),
            radius = size.minDimension * 0.5f,
        )
        drawCircle(
            color = glow.copy(alpha = 0.55f),
            radius = orbitRadius,
            style = Stroke(width = 1.3.dp.toPx()),
        )
        drawArc(
            brush = Brush.sweepGradient(listOf(primary, secondary, Color.Transparent, primary)),
            startAngle = rotation,
            sweepAngle = 220f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(
                center.x - orbitRadius,
                center.y - orbitRadius,
            ),
            size = androidx.compose.ui.geometry.Size(orbitRadius * 2, orbitRadius * 2),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(glow, primary.copy(alpha = 0.7f)),
                center = center,
                radius = size.minDimension * 0.17f,
            ),
            radius = size.minDimension * 0.16f,
        )
        drawCircle(
            color = glow,
            radius = 4.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(
                x = center.x + kotlin.math.cos(angle).toFloat() * orbitRadius,
                y = center.y + kotlin.math.sin(angle).toFloat() * orbitRadius,
            ),
        )
    }
}

@Composable
private fun ProviderRow(provider: String, price: DisplayPrice) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LocalDimens.current.radiusMedium))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = LocalDimens.current.default, vertical = LocalDimens.current.small)
    ) {
        Text(
            text = provider,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Cena ${price.ticker.uppercase()}: ${formatCurrencyAmount(price.last, price.currency)}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun TwelveRow(
    price: DisplayPrice,
    modifier: Modifier = Modifier
) {
    ProviderRow(provider = stringResource(DR.string.twelve_data_provider), price = price)

    Column(
        modifier
            .fillMaxWidth()
            .padding(start = LocalDimens.current.default)
    ) {
        price.name?.let {
            Spacer(Modifier.height(LocalDimens.current.tiny))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(LocalDimens.current.tiny))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LocalDimens.current.medium)
        ) {
            price.previousClose?.let {
                Text(
                    "Previous close: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            price.change?.let { changeValue ->
                val isUp = changeValue >= 0.0
                val changeColor = if (isUp) MaterialTheme.tradingColors.positive else MaterialTheme.tradingColors.negative
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isUp) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                        contentDescription = null,
                        tint = changeColor
                    )
                    Text(
                        text = "Change: $changeValue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = changeColor
                    )
                }
            }
        }

        price.asOf?.let {
            Spacer(Modifier.height(LocalDimens.current.tiny))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StocksScreenPreview(
    @PreviewParameter(StocksSuccessProvider::class)
    state: StocksState
) {
    StocksScreen(
        ticker = "AAPL",
        state = state,
        onClick = {},
        onBackClick = {},
    )
}
