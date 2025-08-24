package cz.yogaboy.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import cz.yogaboy.app.RootNavGraph
import cz.yogaboy.domain.marketdata.MarketDataRepository
import cz.yogaboy.tv.ui.theme.TradingViewTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val repo: MarketDataRepository by inject()
    private var uiText by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val p = repo.getLatestPrice("AAPL")
            uiText = p?.let { "${it.ticker}: ${it.last}" } ?: "No data"
        }
        enableEdgeToEdge()

        setContent {
            TradingViewTheme {
                RootNavGraph()
            }
        }
    }
}