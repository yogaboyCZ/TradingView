package cz.yogaboy.feature.home.presentation

import androidx.annotation.StringRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cz.yogaboy.core.design.AuroraBackground
import cz.yogaboy.core.design.FrostedSurface
import cz.yogaboy.core.design.LocalDimens
import cz.yogaboy.core.design.formatCurrencyAmount
import cz.yogaboy.core.design.theme.tradingColors
import cz.yogaboy.domain.marketdata.PricePoint
import cz.yogaboy.domain.marketdata.ProductCategory
import cz.yogaboy.domain.marketdata.SuggestedProduct
import cz.yogaboy.core.design.R as DR

@Composable
internal fun SuggestedProducts(
    products: List<SuggestedProduct>,
    onProductClick: (SuggestedProduct) -> Unit,
    histories: Map<String, List<PricePoint>>,
    failedHistories: Set<String>,
    supportingPane: Boolean,
    selectedTicker: String?,
    modifier: Modifier = Modifier,
    topContentPadding: Dp = LocalDimens.current.default,
    gridState: LazyStaggeredGridState,
) {
    val density = LocalDensity.current
    val dimens = LocalDimens.current
    val expandedColumnCount = with(density) {
        val horizontalPadding = dimens.default.roundToPx() * 2
        val spacing = dimens.small.roundToPx()
        val availableWidth = LocalWindowInfo.current.containerSize.width - horizontalPadding
        val minimumItemWidth = 160.dp.roundToPx()
        ((availableWidth + spacing) / (minimumItemWidth + spacing)).coerceAtLeast(1)
    }
    val textColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    LazyVerticalStaggeredGrid(
        columns = if (supportingPane) {
            StaggeredGridCells.Fixed(2)
        } else {
            StaggeredGridCells.Fixed(expandedColumnCount)
        },
        state = gridState,
        modifier = modifier,
        contentPadding = PaddingValues(
            start = dimens.default,
            top = topContentPadding,
            end = dimens.default,
            bottom = dimens.default + 32.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(dimens.small),
        verticalItemSpacing = dimens.small,
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            FrostedSurface(
                shape = RoundedCornerShape(dimens.radiusLarge),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(dimens.default)) {
                    Text(
                        text = stringResource(DR.string.home_suggested_products_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = textColor,
                    )
                    Spacer(Modifier.height(dimens.tiny))
                    Text(
                        text = stringResource(DR.string.home_suggested_products_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                    )
                }
            }
        }

        items(products, key = SuggestedProduct::ticker) { productTemplate ->
            val history = histories[productTemplate.ticker].orEmpty()
            val latestClose = history.lastOrNull()?.close
            val previousClose = history.getOrNull(history.lastIndex - 1)?.close
            val product = HomeProductUiModel(
                ticker = productTemplate.ticker,
                companyName = productTemplate.companyName,
                category = productTemplate.category,
                price = formatCurrencyAmount(
                    latestClose ?: productTemplate.fallbackPrice,
                    productTemplate.currency,
                ),
                dailyChange = if (
                    latestClose != null && previousClose != null && previousClose != 0.0
                ) {
                    ((latestClose / previousClose - 1.0) * 100.0).toFloat()
                } else {
                    productTemplate.fallbackDailyChange.toFloat()
                },
                showTrendSummary = productTemplate.showTrendSummary,
                trend = history.map { it.close.toFloat() },
                historyFailed = productTemplate.ticker in failedHistories,
            )

            Box(
                modifier = Modifier.animateItem(
                    placementSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                ),
            ) {
                SuggestedProductCard(
                    product = product,
                    selected = selectedTicker == product.ticker,
                    onClick = { onProductClick(productTemplate) },
                )
            }
        }
    }
}

private data class HomeProductUiModel(
    val ticker: String,
    val companyName: String,
    val category: ProductCategory,
    val price: String,
    val dailyChange: Float,
    val showTrendSummary: Boolean,
    val trend: List<Float>,
    val historyFailed: Boolean,
) {
    val displayTicker: String get() = ticker.substringBefore(':')
}

@Composable
private fun SuggestedProductCard(
    product: HomeProductUiModel,
    selected: Boolean,
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
                    modifier = Modifier.align(Alignment.Start),
                    shape = RoundedCornerShape(LocalDimens.current.radiusMedium),
                    color = if (selected) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                ) {
                    Text(
                        text = product.displayTicker,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            when {
                product.trend.size >= 2 -> SparklineChart(
                    values = product.trend,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (product.showTrendSummary) 58.dp else 40.dp),
                )
                !product.historyFailed -> LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )
                else -> Text(
                    text = stringResource(DR.string.home_chart_unavailable),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

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
                    text = stringResource(
                        DR.string.percentage_value,
                        if (product.dailyChange >= 0) "+" else "",
                        product.dailyChange,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (product.dailyChange >= 0) {
                        MaterialTheme.tradingColors.positive
                    } else {
                        MaterialTheme.tradingColors.negative
                    },
                )
            }

            if (product.showTrendSummary && product.trend.size >= 2) {
                val change = ((product.trend.last() / product.trend.first()) - 1f) * 100f
                Text(
                    text = stringResource(
                        DR.string.home_period_change,
                        if (change >= 0f) "+" else "",
                        change,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (change >= 0f) {
                        MaterialTheme.tradingColors.positive
                    } else {
                        MaterialTheme.tradingColors.negative
                    },
                )
            }

            Text(
                text = stringResource(product.category.stringResource),
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
            )
        }
    }
}

@get:StringRes
private val ProductCategory.stringResource: Int
    get() = when (this) {
        ProductCategory.TECHNOLOGY -> DR.string.category_technology
        ProductCategory.SEMICONDUCTORS -> DR.string.category_semiconductors
        ProductCategory.INTERNET -> DR.string.category_internet
        ProductCategory.E_COMMERCE -> DR.string.category_e_commerce
        ProductCategory.CONSUMER_GOODS -> DR.string.category_consumer_goods
        ProductCategory.FINANCE -> DR.string.category_finance
        ProductCategory.AEROSPACE -> DR.string.category_aerospace
        ProductCategory.AUTOMOTIVE -> DR.string.category_automotive
        ProductCategory.BANKING -> DR.string.category_banking
        ProductCategory.ENTERPRISE_SOFTWARE -> DR.string.category_enterprise_software
        ProductCategory.SEMICONDUCTOR_EQUIPMENT -> DR.string.category_semiconductor_equipment
        ProductCategory.ENERGY -> DR.string.category_energy
    }

@Composable
private fun SparklineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
) {
    if (values.size < 2) return

    val isPositive = values.last() >= values.first()
    val lineColor = if (isPositive) {
        MaterialTheme.tradingColors.positive
    } else {
        MaterialTheme.tradingColors.negative
    }
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

        val lastY = verticalPadding + chartHeight *
            (1f - (values.last() - minValue) / valueRange)
        drawCircle(
            color = lineColor,
            radius = 3.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(size.width, lastY),
        )
    }
}
