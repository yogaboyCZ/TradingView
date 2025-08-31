package cz.yogaboy.feature.stocks.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.yogaboy.core.design.LocalDimens
import cz.yogaboy.domain.marketdata.Price
import cz.yogaboy.core.design.R as DR


@Composable
fun StocksScreen(
    state: StocksState,
    onClick: (StocksEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(color = Color.Transparent, contentColor = MaterialTheme.colorScheme.onPrimary) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.loading -> CircularProgressIndicator()

                state.error != null -> Text(state.error)

                state.alphaPrice != null || state.twelvePrice != null -> {
                    Column(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(horizontal = LocalDimens.current.default)
                    ) {
                        PriceSummaryCard(
                            alpha = state.alphaPrice,
                            twelve = state.twelvePrice,
                            modifier = Modifier.padding(top = LocalDimens.current.medium)
                        )

                        Spacer(Modifier.height(LocalDimens.current.medium))
                    }
                }

                else -> Text(
                    text = stringResource(DR.string.stocks_no_data),
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }
        }
    }
}

@Composable
fun PriceSummaryCard(
    alpha: Price?,
    twelve: Price?,
    modifier: Modifier = Modifier
) {
    if (alpha == null && twelve == null) return

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(LocalDimens.current.radiusLarge),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(LocalDimens.current.default),
            verticalArrangement = Arrangement.spacedBy(LocalDimens.current.small)
        ) {
            Text(
                text = "Aktuální cena",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            alpha?.let { ProviderRow(provider = "AlphaVantage", price = it) }
            twelve?.let { ProviderRow(provider = "TwelveData",  price = it) }
        }
    }
}

@Composable
private fun ProviderRow(provider: String, price: Price) {
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
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
fun StocksScreenPreview() {
    StocksScreen(
        state = StocksState(alphaPrice = Price("AAPL", 227.76, 2.86, 1.2717, 224.9, "2025-08-22")),
        onClick = {},
        onBackClick = {},
    )
}