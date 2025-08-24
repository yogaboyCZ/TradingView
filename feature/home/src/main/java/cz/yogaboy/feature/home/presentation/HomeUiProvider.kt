package cz.yogaboy.feature.home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface HomeUiProvider {
    @Composable fun Stocks(ticker: String, modifier: Modifier)
    @Composable fun News(query: String?, modifier: Modifier)
    @Composable fun Empty(modifier: Modifier)
}
