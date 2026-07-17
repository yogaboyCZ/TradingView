package cz.yogaboy.core.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class AuroraColors(
    val background: Color,
    val gradient: List<Color>,
    val cyanGlow: List<Color>,
    val violetGlow: List<Color>,
    val pinkGlow: List<Color>,
    val vignette: Color,
)

@Immutable
data class TradingColors(
    val positive: Color,
    val negative: Color,
    val chartCrosshair: Color,
    val onBackdrop: Color,
    val headerScrim: List<Color>,
    val searchContainer: Color,
    val searchButton: Color,
    val searchDisabledContainer: Color,
    val glassSurface: List<Color>,
    val glassEdge: List<Color>,
    val glassShadow: Color,
    val glassHighlight: Color,
    val glassCyanTint: Color,
    val glassVioletTint: Color,
)

val BrandCyanLight = Color(0xFF3880E0)
val DarkCyanLight = Color(0xFF0E3C6E)
val BrandMintLight = Color(0xFF0B67C2)
val BackgroundLight = Color(0xFFF2FBFF)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF071B3D)
val SurfaceVariantLight = Color(0xFFE3F6FD)
val OutlineLight = Color(0xFF7293B5)
val onSurfaceLightMedium = Color(0xFF03123D)

val BrandCyanDark = Color(0xFF8FD8FF)
val BrandMintDark = Color(0xFFD3B6FF)
val BackgroundDark = Color(0xFF07131C)
val SurfaceDark = Color(0xFF0C1A24)
val OnSurfaceDark = Color(0xFFEAF7FF)
val SurfaceVariantDark = Color(0xFF0F2630)
val OutlineDark = Color(0xFF1A323B)
val onSurfaceDarkMedium = Color(0xFFC8E7F0)

val TradingLight = TradingColors(
    positive = Color(0xFF007A63),
    negative = Color(0xFFBA1A1A),
    chartCrosshair = Color(0xFF5842C3),
    onBackdrop = Color.White,
    headerScrim = listOf(
        Color(0xF20A1742),
        Color(0xD90E2258),
        Color(0xA60D2B67),
        Color.Transparent,
    ),
    searchContainer = Color(0x8F52658F),
    searchButton = Color(0xB92D5B91),
    searchDisabledContainer = Color(0x8F52658F),
    glassSurface = listOf(Color(0xE8F8FBFF), Color(0xDEE9F2FF), Color(0xD6E1DFFF)),
    glassEdge = listOf(Color.White, Color(0xE6BDEBFF), Color(0xD1E8CFFF), Color(0x426682B5)),
    glassShadow = Color(0xFF00143D),
    glassHighlight = Color.White.copy(alpha = 0.40f),
    glassCyanTint = Color(0x0F8BE9FF),
    glassVioletTint = Color(0x12D5A9FF),
)

val TradingDark = TradingColors(
    positive = Color(0xFF63D9B9),
    negative = Color(0xFFFFB4AB),
    chartCrosshair = Color(0xFF8FD8FF),
    onBackdrop = Color.White,
    headerScrim = listOf(
        Color(0xFA050D2C),
        Color(0xE6091944),
        Color(0xB30A2458),
        Color.Transparent,
    ),
    searchContainer = Color(0xB33A4C76),
    searchButton = Color(0xCC285486),
    searchDisabledContainer = Color(0x99404D6B),
    glassSurface = listOf(Color(0xF0263B70), Color(0xEB182D61), Color(0xE6271F5B)),
    glassEdge = listOf(Color(0xBFFFFFFF), Color(0x618FD8FF), Color(0x6BD3B6FF), Color(0x33000000)),
    glassShadow = Color(0xFF00143D),
    glassHighlight = Color.White.copy(alpha = 0.18f),
    glassCyanTint = Color(0x0F8BE9FF),
    glassVioletTint = Color(0x12D5A9FF),
)

val AuroraLight = AuroraColors(
    background = Color(0xFF081747),
    gradient = listOf(
        Color(0xFF07143E),
        Color(0xFF075BCB),
        Color(0xFF6148D2),
        Color(0xFF9C4ED8),
    ),
    cyanGlow = listOf(Color(0xF02BC8FF), Color(0x98277CFF), Color.Transparent),
    violetGlow = listOf(Color(0xD9F05EFF), Color(0x987C49ED), Color.Transparent),
    pinkGlow = listOf(Color(0xA8FF4FC8), Color(0x554D38DC), Color.Transparent),
    vignette = Color(0x4D02081F),
)

val AuroraDark = AuroraColors(
    background = Color(0xFF040B24),
    gradient = listOf(
        Color(0xFF040B24),
        Color(0xFF073D8C),
        Color(0xFF3C328F),
        Color(0xFF652D8D),
    ),
    cyanGlow = listOf(Color(0xB82BC8FF), Color(0x70277CFF), Color.Transparent),
    violetGlow = listOf(Color(0x9EF05EFF), Color(0x707C49ED), Color.Transparent),
    pinkGlow = listOf(Color(0x72FF4FC8), Color(0x404D38DC), Color.Transparent),
    vignette = Color(0x7302081F),
)
