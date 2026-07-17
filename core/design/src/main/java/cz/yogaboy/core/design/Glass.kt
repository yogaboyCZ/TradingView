package cz.yogaboy.core.design

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import cz.yogaboy.core.design.theme.auroraColors
import cz.yogaboy.core.design.theme.tradingColors

@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val auroraColors = MaterialTheme.auroraColors
    val transition = rememberInfiniteTransition(label = "living-gradient")
    val horizontal by transition.animateFloat(
        initialValue = -0.22f,
        targetValue = 0.24f,
        animationSpec = infiniteRepeatable(tween(12_000, easing = LinearEasing), RepeatMode.Reverse),
        label = "horizontal-drift",
    )
    val vertical by transition.animateFloat(
        initialValue = -0.15f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(tween(15_000, easing = LinearEasing), RepeatMode.Reverse),
        label = "vertical-drift",
    )
    val breathe by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(tween(10_000), RepeatMode.Reverse),
        label = "gradient-breathe",
    )

    Box(modifier = modifier.background(auroraColors.background)) {
        Canvas(Modifier.matchParentSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = auroraColors.gradient,
                    start = Offset(-size.width * 0.12f, 0f),
                    end = Offset(size.width * 1.04f, size.height),
                )
            )

            drawRect(
                brush = Brush.radialGradient(
                    colors = auroraColors.cyanGlow,
                    center = Offset(
                        size.width * (0.10f + horizontal),
                        size.height * (0.16f + vertical),
                    ),
                    radius = size.maxDimension * 0.62f * breathe,
                )
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = auroraColors.violetGlow,
                    center = Offset(
                        size.width * (0.90f - horizontal),
                        size.height * (0.78f - vertical),
                    ),
                    radius = size.maxDimension * 0.68f * (1.30f - (breathe - 0.72f)),
                )
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = auroraColors.pinkGlow,
                    center = Offset(
                        size.width * (0.52f - horizontal * 0.65f),
                        size.height * (0.48f + vertical * 0.7f),
                    ),
                    radius = size.maxDimension * 0.48f * (1.45f - breathe * 0.35f),
                )
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, auroraColors.vignette),
                    center = center,
                    radius = size.maxDimension * 0.88f,
                )
            )
        }
        content()
    }
}

@Composable
fun FrostedSurface(
    shape: Shape,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit,
) {
    val tradingColors = MaterialTheme.tradingColors

    Box(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = shape,
                ambientColor = tradingColors.glassShadow,
                spotColor = tradingColors.glassShadow,
            )
            .background(Brush.linearGradient(tradingColors.glassSurface), shape)
            .border(1.25.dp, Brush.linearGradient(tradingColors.glassEdge), shape),
        contentAlignment = contentAlignment,
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        tradingColors.glassHighlight,
                        Color.Transparent,
                    ),
                    endY = size.height * 0.42f,
                )
            )
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        tradingColors.glassCyanTint,
                        Color.Transparent,
                        tradingColors.glassVioletTint,
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                )
            )
        }
        content()
    }
}
