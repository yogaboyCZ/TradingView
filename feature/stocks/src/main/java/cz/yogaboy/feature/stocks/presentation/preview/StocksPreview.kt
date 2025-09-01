package cz.yogaboy.feature.stocks.presentation.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cz.yogaboy.core.common.state.NetworkViewState
import cz.yogaboy.feature.stocks.presentation.StocksState
import cz.yogaboy.feature.stocks.presentation.model.DisplayPrice

class StocksSuccessProvider : PreviewParameterProvider<StocksState> {
    override val values = sequenceOf(
        StocksState(
            alpha = NetworkViewState.Success(StocksPreviewData.alpha),
            twelve = NetworkViewState.Success(StocksPreviewData.twelve)
        )
    )
}

object StocksPreviewData {
    val alpha = DisplayPrice(
        ticker = "AAPL",
        last = 227.76,
        change = 2.86,
        changePercent = 1.2717,
        previousClose = 224.90,
        asOf = "29.08.2025",
        name = ""
    )

    val twelve = DisplayPrice(
        ticker = "AAPL",
        last = 227.76,
        change = -0.12,
        changePercent = -0.05,
        previousClose = 227.88,
        asOf = "29.08.2025",
        name = "Apple Inc."
    )
}