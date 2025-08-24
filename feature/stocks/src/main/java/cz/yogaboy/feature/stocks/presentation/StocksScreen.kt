package cz.yogaboy.feature.stocks.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
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
                state.price != null -> Text(
                    text = stringResource(DR.string.stocks_price, state.price.ticker, state.price.last),
                    fontSize = 34.sp,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiary,
                )
            }
        }
    }
}

@Preview
@Composable
fun StocksScreenPreview() {
    StocksScreen(
        state = StocksState(price = Price("AAPL", 227.76, 2.86, 1.2717, 224.9, "2025-08-22")),
        onClick = {},
        onBackClick = {},
    )
}