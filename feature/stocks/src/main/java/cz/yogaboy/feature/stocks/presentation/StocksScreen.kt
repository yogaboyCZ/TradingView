package cz.yogaboy.feature.stocks.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cz.yogaboy.core.design.LocalDimens
import cz.yogaboy.core.design.AuroraBackground
import cz.yogaboy.core.design.FrostedSurface
import cz.yogaboy.core.design.theme.tradingColors
import cz.yogaboy.feature.stocks.presentation.model.DisplayPrice
import cz.yogaboy.domain.marketdata.CompanyNews
import cz.yogaboy.domain.marketdata.CompanyProfile
import cz.yogaboy.domain.marketdata.PricePoint
import cz.yogaboy.feature.stocks.presentation.preview.StocksSuccessProvider
import cz.yogaboy.core.design.R as DR

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
    val content: @Composable () -> Unit = {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (drawBackground) Brush.verticalGradient(
                                MaterialTheme.tradingColors.headerScrim,
                            ) else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent)),
                        ),
                ) {
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(
                        horizontal = LocalDimens.current.default,
                        vertical = LocalDimens.current.medium,
                    ),
                verticalArrangement = Arrangement.spacedBy(LocalDimens.current.medium),
                contentPadding = PaddingValues(bottom = LocalDimens.current.default),
            ) {
                item {
                    PriceSummaryCard(
                        ticker = ticker,
                        alphaState = state.alpha,
                        twelveState = state.twelve,
                    )
                }
                item { PriceHistorySection(state.history) }
                item { CompanyProfileSection(state.profile) }
                item { CompanyNewsSection(state.news) }
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
private fun PriceHistorySection(state: StocksUiState<List<PricePoint>>) {
    DetailSection(
        title = "Vývoj ceny",
        badge = "TD",
        modifier = Modifier.height(296.dp),
    ) {
        when (state) {
            StocksUiState.Loading -> SectionLoading()
            is StocksUiState.Error -> SectionUnavailable(state.message)
            is StocksUiState.Data -> {
                PriceHistoryChart(
                    points = state.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp),
                )
                val first = state.value.firstOrNull()?.close
                val last = state.value.lastOrNull()?.close
                if (first != null && last != null) {
                    val change = (last / first - 1.0) * 100.0
                    Text(
                        text = "Posledních ${state.value.size} obchodních dnů: " +
                            "${if (change >= 0) "+" else ""}${"%.2f".format(change)} %",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (change >= 0) MaterialTheme.tradingColors.positive else MaterialTheme.tradingColors.negative,
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceHistoryChart(
    points: List<PricePoint>,
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

    Canvas(modifier) {
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0.0 } ?: 1.0
        val step = size.width / values.lastIndex
        val verticalPadding = 12.dp.toPx()
        val usableHeight = size.height - verticalPadding * 2
        val linePath = androidx.compose.ui.graphics.Path()
        val fillPath = androidx.compose.ui.graphics.Path()

        values.forEachIndexed { index, value ->
            val x = index * step
            val y = verticalPadding + usableHeight * (1f - ((value - min) / range).toFloat())
            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, size.height)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(size.width, size.height)
        fillPath.close()

        repeat(3) { index ->
            val y = size.height * (index + 1) / 4f
            drawLine(guideColor, androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y))
        }
        drawPath(fillPath, brush = Brush.verticalGradient(listOf(fillColor, Color.Transparent)))
        drawPath(
            linePath,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun CompanyProfileSection(state: StocksUiState<CompanyProfile>) {
    DetailSection(title = "O společnosti", badge = "TD") {
        when (state) {
            StocksUiState.Loading -> SectionLoading()
            is StocksUiState.Error -> SectionUnavailable(
                "Firemní profil vyžaduje Twelve Data Grow tarif. ${state.message}"
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
private fun CompanyNewsSection(state: StocksUiState<List<CompanyNews>>) {
    DetailSection(title = "Zprávy společnosti", badge = "TD") {
        when (state) {
            StocksUiState.Loading -> SectionLoading()
            is StocksUiState.Error -> SectionUnavailable(state.message)
            is StocksUiState.Data -> {
                if (state.value.isEmpty()) {
                    SectionUnavailable("Pro tuto společnost zatím nejsou tiskové zprávy.")
                } else {
                    state.value.forEachIndexed { index, news ->
                        Text(
                            text = news.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        news.date?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }
                        news.summary?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (index != state.value.lastIndex) HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    badge: String,
    modifier: Modifier = Modifier,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
    modifier: Modifier = Modifier
) {
    var showTwelveData by rememberSaveable { mutableStateOf(false) }
    val entryRotation = remember(ticker) { Animatable(-88f) }
    LaunchedEffect(ticker) {
        entryRotation.snapTo(-88f)
        entryRotation.animateTo(
            targetValue = 0f,
            animationSpec = tween(680),
        )
    }
    val rotation by animateFloatAsState(
        targetValue = if (showTwelveData) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "provider-card-flip",
    )
    val shape = RoundedCornerShape(LocalDimens.current.radiusLarge)

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
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    )
                )
                .graphicsLayer {
                    transformOrigin = TransformOrigin.Center
                    rotationY = rotation
                    cameraDistance = 18f * density
                }
                .clip(shape)
                .clickable { showTwelveData = !showTwelveData },
        ) {
            FrostedSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = shape,
            ) {
                if (rotation <= 90f) {
                    ProviderCardFace(
                        badge = "AV",
                        provider = stringResource(DR.string.alpha_vantage_provider),
                        state = alphaState,
                    )
                } else {
                    Box(Modifier.graphicsLayer { rotationY = 180f }) {
                        ProviderCardFace(
                            badge = "TD",
                            provider = stringResource(DR.string.twelve_data_provider),
                            state = twelveState,
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
) {
    Column(
        // Reserve the space occupied by a regular quote before its asynchronous
        // data arrives. Otherwise the sections below jump up during the entry flip
        // and back down as soon as the quote replaces the shorter loading row.
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 320.dp)
            .padding(LocalDimens.current.default),
        verticalArrangement = Arrangement.spacedBy(LocalDimens.current.medium),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(DR.string.stocks_price_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = provider,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                shape = RoundedCornerShape(LocalDimens.current.radiusMedium),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = badge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        when (state) {
            is StocksUiState.Loading -> ProviderLoadingRow(provider)
            is StocksUiState.Error -> ProviderErrorRow(provider, message = state.message)
            is StocksUiState.Data -> QuoteDetails(price = state.value)
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
private fun QuoteDetails(price: DisplayPrice) {
    Column(verticalArrangement = Arrangement.spacedBy(LocalDimens.current.small)) {
        price.name?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "${price.ticker.uppercase()}  ${price.last}",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.tertiary,
        )

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
            text = "Cena ${price.ticker.uppercase()}: ${price.last}",
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
