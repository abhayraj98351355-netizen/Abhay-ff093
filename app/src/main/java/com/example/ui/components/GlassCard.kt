package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkCard
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonCyan

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = DarkCard.copy(alpha = 0.75f),
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    glowEffect: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    val borderModifier = if (glowEffect) {
        Modifier.border(
            width = borderWidth,
            brush = Brush.linearGradient(
                listOf(NeonCyan.copy(alpha = 0.7f), GlassBorder, NeonCyan.copy(alpha = 0.3f))
            ),
            shape = shape
        )
    } else {
        Modifier.border(
            border = BorderStroke(borderWidth, borderColor),
            shape = shape
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(borderModifier)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.85f)
                    )
                )
            )
            .then(clickableModifier),
        content = content
    )
}
