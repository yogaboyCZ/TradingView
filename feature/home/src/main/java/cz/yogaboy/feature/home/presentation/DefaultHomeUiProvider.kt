package cz.yogaboy.feature.home.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

object DefaultHomeUiProvider : HomeUiProvider {
    @Composable override fun Stocks(ticker: String, modifier: Modifier) { Text("Stocks: $ticker", modifier) }
    @Composable override fun News(query: String?, modifier: Modifier) { Text("News: ${query.orEmpty()}", modifier) }
    @Composable override fun Empty(modifier: Modifier) { }
}
