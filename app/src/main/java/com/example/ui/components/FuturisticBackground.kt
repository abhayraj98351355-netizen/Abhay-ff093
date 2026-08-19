package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.QuantumViolet

@Composable
fun FuturisticBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_glow")
    val glowOffset by infiniteTransition.animateFloat(
        initialValue = -0.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_offset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Subtle cyber canvas with glowing radial orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Top-left cyan nebula glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(width * 0.15f, height * 0.1f),
                    radius = width * 0.7f
                )
            )

            // Dynamic moving violet cyber wave
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(QuantumViolet.copy(alpha = 0.09f), Color.Transparent),
                    center = Offset(width * (1f - glowOffset), height * (0.4f + glowOffset * 0.3f)),
                    radius = width * 0.85f
                )
            )

            // Bottom-right electric blue orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ElectricBlue.copy(alpha = 0.07f), Color.Transparent),
                    center = Offset(width * 0.85f, height * 0.9f),
                    radius = width * 0.75f
                )
            )

            // Subtle cyber grid dots
            val spacing = 36.dp.toPx()
            var x = spacing / 2
            while (x < width) {
                var y = spacing / 2
                while (y < height) {
                    drawCircle(
                        color = Color(0xFF1E293B).copy(alpha = 0.18f),
                        radius = 1.dp.toPx(),
                        center = Offset(x, y)
                    )
                    y += spacing
                }
                x += spacing
            }
        }

        content()
    }
}
