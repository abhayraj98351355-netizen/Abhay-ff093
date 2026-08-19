package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.QuantumViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NexoraNeuralCore(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isActive: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "nexora_core_anim")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive) 6000 else 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val counterRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive) 8000 else 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_rotation"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer glowing aura
        Box(
            modifier = Modifier
                .size(size * pulse)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = if (isActive) 0.35f else 0.15f),
                            QuantumViolet.copy(alpha = if (isActive) 0.15f else 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Orbital Ring 1
        Canvas(modifier = Modifier.size(size * 0.9f).rotate(rotation)) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val radius = this.size.width / 2 - 2.dp.toPx()
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(NeonCyan, Color.Transparent, ElectricBlue, QuantumViolet, NeonCyan)
                ),
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Orbital node
            val nodeAngle = Math.toRadians(45.0)
            val nodeX = center.x + (radius * cos(nodeAngle)).toFloat()
            val nodeY = center.y + (radius * sin(nodeAngle)).toFloat()
            drawCircle(
                color = NeonCyan,
                radius = 3.dp.toPx(),
                center = Offset(nodeX, nodeY)
            )
        }

        // Orbital Ring 2 (Counter-rotating)
        Canvas(modifier = Modifier.size(size * 0.65f).rotate(counterRotation)) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val radius = this.size.width / 2 - 1.5.dp.toPx()
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(QuantumViolet, NeonEmerald, Color.Transparent, NeonCyan, QuantumViolet)
                ),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Inner glowing quantum nucleus
        Box(
            modifier = Modifier
                .size(size * 0.35f)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(NeonCyan, ElectricBlue, QuantumViolet)
                    )
                )
        )
    }
}

@Composable
fun NexoraLogo(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    showText: Boolean = true,
    subtitle: String? = "QUANTUM AI OS"
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NexoraNeuralCore(size = size)

        if (showText) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "NEXORA",
                        fontSize = (size.value * 0.45).sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 2.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AI",
                        fontSize = (size.value * 0.45).sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 1.sp,
                        color = NeonCyan
                    )
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = (size.value * 0.22).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
