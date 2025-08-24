package cz.yogaboy.feature.stocks.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import cz.yogaboy.domain.marketdata.Price


@Composable
fun StocksScreen(
    state: StocksState,
    onClick: (StocksEvent) -> Unit,// todo Next iteration
    onBackClick: () -> Unit, // todo Next iteration
    modifier: Modifier = Modifier
) {
    Surface(color = Color.Transparent, contentColor = Color.White) {
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
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