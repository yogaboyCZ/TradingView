package cz.yogaboy.core.design.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import cz.yogaboy.core.design.Dimens
import cz.yogaboy.core.design.LocalDimens

private val LightColors = lightColorScheme(
    primary = BrandCyanLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E9FF),
    onPrimaryContainer = Color(0xFF001B3D),
    inversePrimary = Color(0xFFA8C8FF),
    secondary = Color(0xFF6148D2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6DEFF),
    onSecondaryContainer = Color(0xFF1D0061),
    tertiary = BrandMintLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD5E9FF),
    onTertiaryContainer = Color(0xFF001D35),
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = onSurfaceLightMedium,
    surfaceTint = BrandCyanLight,
    inverseSurface = Color(0xFF1C2B3F),
    inverseOnSurface = Color(0xFFECF2FF),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = OutlineLight,
    outlineVariant = Color(0xFFBFC8D8),
    scrim = Color.Black,
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFD5DCE8),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF0F5FF),
    surfaceContainer = Color(0xFFEAF0FA),
    surfaceContainerHigh = Color(0xFFE4EAF4),
    surfaceContainerHighest = Color(0xFFDEE4EE),
)

private val DarkColors = darkColorScheme(
    primary = BrandCyanDark,
    onPrimary = Color(0xFF03123D),
    primaryContainer = Color(0xFF0E4F67),
    onPrimaryContainer = Color(0xFFC5EFFF),
    inversePrimary = Color(0xFF00658A),
    secondary = BrandMintDark,
    onSecondary = Color(0xFF38245F),
    secondaryContainer = Color(0xFF503C77),
    onSecondaryContainer = Color(0xFFEDDCFF),
    tertiary = BrandMintDark,
    onTertiary = Color(0xFF251C6E),
    tertiaryContainer = Color(0xFF394A78),
    onTertiaryContainer = Color(0xFFDCE2FF),
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = onSurfaceDarkMedium,
    surfaceTint = BrandCyanDark,
    inverseSurface = Color(0xFFDDE4EA),
    inverseOnSurface = Color(0xFF24323A),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = OutlineDark,
    outlineVariant = Color(0xFF40515A),
    scrim = Color.Black,
    surfaceBright = Color(0xFF34434C),
    surfaceDim = Color(0xFF0C1419),
    surfaceContainerLowest = Color(0xFF071015),
    surfaceContainerLow = Color(0xFF101C22),
    surfaceContainer = Color(0xFF142027),
    surfaceContainerHigh = Color(0xFF1E2A31),
    surfaceContainerHighest = Color(0xFF29353C),
)

private val LocalAuroraColors = staticCompositionLocalOf { AuroraLight }
private val LocalTradingColors = staticCompositionLocalOf { TradingLight }

@Suppress("UnusedReceiverParameter")
val MaterialTheme.auroraColors: AuroraColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAuroraColors.current

@Suppress("UnusedReceiverParameter")
val MaterialTheme.tradingColors: TradingColors
    @Composable
    @ReadOnlyComposable
    get() = LocalTradingColors.current

@Composable
fun TradingViewTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        } else {
            if (darkTheme) DarkColors else LightColors
        }

    CompositionLocalProvider(
        LocalDimens provides Dimens(),
        LocalAuroraColors provides if (darkTheme) AuroraDark else AuroraLight,
        LocalTradingColors provides if (darkTheme) TradingDark else TradingLight,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
