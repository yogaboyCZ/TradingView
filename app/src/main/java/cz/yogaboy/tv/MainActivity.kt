package cz.yogaboy.tv

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsControllerCompat
import cz.yogaboy.app.RootNavGraph
import cz.yogaboy.core.design.theme.TradingViewTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.navigationBarColor = Color.TRANSPARENT
        window.isNavigationBarContrastEnforced = false
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        val initialTicker = intent.deepLinkedTicker()

        setContent {
            TradingViewTheme {
                RootNavGraph(initialTicker = initialTicker)
            }
        }
    }
}

/**
 * Extracts a ticker from either:
 * - the local development deep link: tradingview://stocks/{ticker}
 * - the verified Android App Link: https://yogaboy.cz/stocks/{ticker}
 *
 * Unknown or incomplete URLs return null, so the app opens its home screen.
 */
private fun Intent.deepLinkedTicker(): String? {
    val uri: Uri = data ?: return null

    val isCustomDeepLink =
        uri.scheme == CUSTOM_DEEP_LINK_SCHEME &&
            uri.host == CUSTOM_STOCKS_DEEP_LINK_HOST
    val isWebAppLink =
        uri.scheme == WEB_APP_LINK_SCHEME &&
            uri.host == WEB_APP_LINK_HOST &&
            uri.pathSegments.firstOrNull() == STOCKS_PATH

    if (!isCustomDeepLink && !isWebAppLink) {
        return null
    }

    val tickerSegment = if (isWebAppLink) {
        uri.pathSegments.getOrNull(1)
    } else {
        uri.pathSegments.firstOrNull()
    }

    return tickerSegment
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?.uppercase()
}

private const val CUSTOM_DEEP_LINK_SCHEME = "tradingview"
private const val CUSTOM_STOCKS_DEEP_LINK_HOST = "stocks"
private const val WEB_APP_LINK_SCHEME = "https"
private const val WEB_APP_LINK_HOST = "yogaboy.cz"
private const val STOCKS_PATH = "stocks"
