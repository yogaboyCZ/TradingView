package cz.yogaboy.tv

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import cz.yogaboy.app.RootNavGraph
import cz.yogaboy.data.marketdata.MarketDataRepository
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
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(innerPadding),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(uiText)
//                    }
//                }
            }
        }
    }
}