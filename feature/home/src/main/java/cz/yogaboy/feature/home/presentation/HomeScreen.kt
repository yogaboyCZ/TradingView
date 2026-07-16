package cz.yogaboy.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.yogaboy.core.design.LocalDimens
import cz.yogaboy.core.design.R as DR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    supportingPane: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = LocalDimens.current.medium,
                            vertical = LocalDimens.current.small,
                        )
                ) {
                    TopAppBar(
                        title = {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = stringResource(DR.string.home_title),
                                    color = MaterialTheme.colorScheme.onTertiary,
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = if (isSystemInDarkTheme())
                                MaterialTheme.colorScheme.onPrimary else Color.Black
                        )
                    )
                    Spacer(Modifier.height(LocalDimens.current.medium))

                    TopSearchBar(
                        value = state.query,
                        onValueChange = { onEvent(HomeEvent.QueryChanged(it)) },
                        onSearch = { if (state.query.isNotBlank()) onEvent(HomeEvent.Submit) },
                        onClear = { onEvent(HomeEvent.Clear) }
                    )
                }
            }
        ) { padding ->
            SuggestedProducts(
                onProductClick = { onEvent(HomeEvent.ProductSelected(it.ticker)) },
                supportingPane = supportingPane,
                Modifier
                    .padding(padding)
                    .fillMaxSize()
            )
        }
    }
}

private data class SuggestedProduct(
    val ticker: String,
    val companyName: String,
    val category: String,
    val trend: List<Float>,
)

private val suggestedProducts = listOf(
    SuggestedProduct("AAPL", "Apple", "Technologie", listOf(42f, 44f, 43f, 48f, 47f, 51f, 55f)),
    SuggestedProduct("MSFT", "Microsoft", "Technologie", listOf(38f, 40f, 44f, 43f, 47f, 49f, 52f)),
    SuggestedProduct("NVDA", "NVIDIA", "Polovodiče", listOf(29f, 34f, 32f, 41f, 46f, 45f, 58f)),
    SuggestedProduct("GOOGL", "Alphabet", "Internet", listOf(48f, 46f, 49f, 52f, 51f, 55f, 57f)),
    SuggestedProduct("AMZN", "Amazon", "E-commerce", listOf(35f, 39f, 37f, 42f, 46f, 44f, 49f)),
    SuggestedProduct("KO", "Coca-Cola", "Spotřební zboží", listOf(51f, 50f, 52f, 51f, 53f, 54f, 55f)),
    SuggestedProduct("BRK.B", "Berkshire Hathaway", "Finance", listOf(40f, 42f, 45f, 44f, 48f, 50f, 53f)),
    SuggestedProduct("BA", "Boeing", "Letecký průmysl", listOf(55f, 52f, 54f, 49f, 47f, 45f, 43f)),
    SuggestedProduct("TSLA", "Tesla", "Automobily", listOf(45f, 53f, 47f, 56f, 50f, 58f, 54f)),
    SuggestedProduct("JPM", "JPMorgan Chase", "Bankovnictví", listOf(41f, 43f, 42f, 46f, 49f, 48f, 52f)),
)

@Composable
private fun SuggestedProducts(
    onProductClick: (SuggestedProduct) -> Unit,
    supportingPane: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = if (supportingPane) {
            GridCells.Fixed(1)
        } else {
            GridCells.Adaptive(minSize = 160.dp)
        },
        modifier = modifier,
        contentPadding = PaddingValues(LocalDimens.current.default),
        horizontalArrangement = Arrangement.spacedBy(LocalDimens.current.small),
        verticalArrangement = Arrangement.spacedBy(LocalDimens.current.small),
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            Surface(
                shape = RoundedCornerShape(LocalDimens.current.radiusLarge),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 2.dp,
            ) {
                Column(Modifier.padding(LocalDimens.current.default)) {
                    Text(
                        text = "Doporučené produkty",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(LocalDimens.current.tiny))
                    Text(
                        text = "Vyberte akcii a zobrazte její aktuální cenu.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        items(suggestedProducts, key = SuggestedProduct::ticker) { product ->
            SuggestedProductCard(
                product = product,
                onClick = { onProductClick(product) },
            )
        }
    }
}

@Composable
private fun SuggestedProductCard(
    product: SuggestedProduct,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(LocalDimens.current.radiusLarge),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(LocalDimens.current.default),
            verticalArrangement = Arrangement.spacedBy(LocalDimens.current.small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LocalDimens.current.small),
            ) {
                Text(
                    text = product.companyName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Surface(
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
                    .height(44.dp),
            )

            Text(
                text = product.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .weight(1f)
                .clip(RoundedCornerShape(LocalDimens.current.radiusLarge)),
            singleLine = true,
            shape = RoundedCornerShape(LocalDimens.current.radiusLarge),
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
                    tint = MaterialTheme.colorScheme.onTertiary
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
                            tint = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            },
            placeholder = {
                Text(
                    text = stringResource(DR.string.home_search_placeholder),
                    color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.75f)
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onPrimary,
                focusedTextColor = MaterialTheme.colorScheme.onTertiary,
                unfocusedTextColor = MaterialTheme.colorScheme.onTertiary
            )
        )
        Spacer(Modifier.width(LocalDimens.current.medium))
        Button(
            onClick = {
                if (value.isNotBlank()) {
                    focusManager.clearFocus(force = true)
                    onSearch()
                }
            },
            enabled = value.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                disabledContentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                stringResource(DR.string.home_search_button),
                color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.75f)
            )
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
