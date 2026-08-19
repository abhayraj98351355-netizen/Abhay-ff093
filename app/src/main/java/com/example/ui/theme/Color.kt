package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Obsidian & Cyber Canvas
val DarkBackground = Color(0xFF06080F)
val DarkSurface = Color(0xFF0B101D)
val DarkCard = Color(0xFF12192C)
val DarkCardElevated = Color(0xFF18233C)
val GlassSurface = Color(0xD90E1626)
val GlassBorder = Color(0x334FACFE)
val GlassBorderHighlight = Color(0x6600F2FE)

// Neon & Quantum Accents
val NeonCyan = Color(0xFF00F2FE)
val ElectricBlue = Color(0xFF4FACFE)
val QuantumViolet = Color(0xFF8A2387)
val DeepViolet = Color(0xFF7F00FF)
val NeonEmerald = Color(0xFF00FFA3)
val CyberPink = Color(0xFFFF007F)
val RadiantAmber = Color(0xFFFFB300)
val CyberCrimson = Color(0xFFFF3366)

// Typography
val TextPrimary = Color(0xFFF3F6FD)
val TextSecondary = Color(0xFF9FB2D0)
val TextMuted = Color(0xFF627597)
val UserBubbleBg = Color(0xFF1D2A47)
val AiBubbleBg = Color(0xFF0E1627)

// Glowing Gradients
val NexoraQuantumGradient = Brush.linearGradient(
    colors = listOf(NeonCyan, ElectricBlue, DeepViolet)
)

val NexoraHeroGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF00F2FE), Color(0xFF4FACFE), Color(0xFF8A2387))
)

val CyberCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0x261E293B), Color(0x1A0F172A))
)

val GlowAuraGradient = Brush.radialGradient(
    colors = listOf(Color(0x3300F2FE), Color(0x0006080F))
)
