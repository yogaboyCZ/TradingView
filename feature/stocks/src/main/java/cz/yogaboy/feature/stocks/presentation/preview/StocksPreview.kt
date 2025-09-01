package cz.yogaboy.feature.stocks.presentation.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cz.yogaboy.feature.stocks.presentation.StocksState
import cz.yogaboy.feature.stocks.presentation.StocksUiState
import cz.yogaboy.feature.stocks.presentation.model.DisplayPrice

class StocksSuccessProvider : PreviewParameterProvider<StocksState> {
    override val values = sequenceOf(
        StocksState(
            alpha = StocksUiState.Data(
                DisplayPrice(
                    ticker = "AAPL",
                    last = 232.14,
                    change = -0.42,
                    changePercent = -0.18,
                    previousClose = 232.56,
                    asOf = "29.08.2025",
                    name = null
                )
            ),
            twelve = StocksUiState.Data(
                DisplayPrice(
                    ticker = "AAPL",
                    last = 232.10,
                    change = 0.15,
                    changePercent = 0.06,
                    previousClose = 232.56,
                    asOf = "29.08.2025",
                    name = "Apple Inc."
                )
            )
        )
    )
}