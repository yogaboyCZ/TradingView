package cz.yogaboy.feature.stocks.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cz.yogaboy.core.common.state.NetworkViewState
import cz.yogaboy.core.design.LocalDimens
import cz.yogaboy.feature.stocks.R
import cz.yogaboy.feature.stocks.presentation.model.DisplayPrice
import cz.yogaboy.feature.stocks.presentation.preview.StocksSuccessProvider
import cz.yogaboy.core.design.R as DR

@Composable
fun StocksScreen(
    state: StocksState,
    onClick: (StocksEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(color = Color.Transparent, contentColor = MaterialTheme.colorScheme.onPrimary) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = LocalDimens.current.default),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(LocalDimens.current.medium))
            PriceSummaryCard(
                alphaState = state.alpha,
                twelveState = state.twelve
            )

        }
    }
}

@Composable
fun PriceSummaryCard(
    alphaState: NetworkViewState<DisplayPrice>,
    twelveState: NetworkViewState<DisplayPrice>,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(LocalDimens.current.radiusLarge),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(LocalDimens.current.default),
            verticalArrangement = Arrangement.spacedBy(LocalDimens.current.small)
        ) {
            Text(
                text = stringResource(DR.string.stocks_price_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ProviderStateBlock(
                provider = stringResource(DR.string.alpha_vantage_provider),
                state = alphaState,
                content = { price ->
                    ProviderRow(
                        provider = stringResource(DR.string.alpha_vantage_provider),
                        price = price
                    )
                }
            )

            ProviderStateBlock(
                provider = stringResource(DR.string.twelve_data_provider),
                state = twelveState,
                content = { price -> TwelveRow(price = price) }
            )
        }
    }
}

@Composable
private fun ProviderStateBlock(
    provider: String,
    state: NetworkViewState<DisplayPrice>,
    content: @Composable (DisplayPrice) -> Unit
) {
    when (state) {
        is NetworkViewState.Loading -> ProviderLoadingRow(provider)
        is NetworkViewState.Error -> ProviderErrorRow(
            provider,
            message = state.uiErrorMessage ?: state.throwable.message
            ?: stringResource(DR.string.unknown_error)
        )

        is NetworkViewState.Success -> content(state.value)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LocalDimens.current.radiusMedium))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f))
            .padding(
                horizontal = LocalDimens.current.default,
                vertical = LocalDimens.current.small
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = provider,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer
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
                val changeColor = if (isUp) Color(0xFF2E7D32) else Color(0xFFC62828)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isUp) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                        contentDescription = null,
                        tint = changeColor
                    )
                    Text(stringResource(R.string.demo_count, 1))

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
        state = state,
        onClick = {},
        onBackClick = {}
    )
}