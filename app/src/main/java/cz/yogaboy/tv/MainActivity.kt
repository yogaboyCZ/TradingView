package cz.yogaboy.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cz.yogaboy.app.RootNavGraph
import cz.yogaboy.tv.ui.theme.TradingViewTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TradingViewTheme {
                RootNavGraph()
            }
        }
    }
}