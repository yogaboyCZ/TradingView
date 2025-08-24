package cz.yogaboy.feature.stocks.presentation

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cz.yogaboy.domain.marketdata.Price


@Composable
fun StocksScreen(
    state: StocksState,
    onClick: (StocksEvent) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.loading -> CircularProgressIndicator()
                state.error != null -> Text(state.error ?: "")
                state.price != null -> Text("Stock price for ${state.price.ticker}: ${state.price.last}")
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