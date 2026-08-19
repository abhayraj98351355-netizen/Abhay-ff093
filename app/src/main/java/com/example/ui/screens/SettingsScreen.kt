package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.NexoraUiState
import com.example.ui.NexoraViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.NexoraLogo
import com.example.ui.theme.CyberPink
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.QuantumViolet
import com.example.ui.theme.RadiantAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    viewModel: NexoraViewModel,
    uiState: NexoraUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasApiKey = BuildConfig.GEMINI_API_KEY.isNotBlank()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "SYSTEM CONFIGURATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Settings & Core Controls",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        // Developer Profile / App Identity Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = GlassBorderHighlight,
                glowEffect = true
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    NexoraLogo(size = 46.dp, subtitle = "NEXT-GEN QUANTUM AI OS")

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Platform Developer",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "Developed by Abhay",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "v3.5 Quantum Release",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }
        }

        // AI Model Engine Selection
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkCardElevated
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "Model",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "AI MODEL ENGINE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val models = listOf(
                        Triple("gemini-3.5-flash", "Gemini 3.5 Flash", "Ultra-fast streaming & multimodal speed (Default)"),
                        Triple("gemini-3.1-pro-preview", "Gemini 3.1 Pro", "Deep logical synthesis, coding & scientific research")
                    )

                    models.forEach { (id, name, desc) ->
                        val isSelected = uiState.selectedModel == id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else DarkCard)
                                .border(1.dp, if (isSelected) NeonCyan else GlassBorder, RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectModel(id) }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) NeonCyan else TextPrimary
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(NeonCyan)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Temperature Slider
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkCardElevated
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Temperature",
                                tint = RadiantAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "TEMPERATURE / CREATIVITY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = RadiantAmber,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = String.format("%.2f", uiState.responseTemperature),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = RadiantAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lower values produce factual deterministic answers; higher values produce creative and expansive ideas.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Slider(
                        value = uiState.responseTemperature,
                        onValueChange = { viewModel.setResponseTemperature(it) },
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = RadiantAmber,
                            activeTrackColor = RadiantAmber,
                            inactiveTrackColor = DarkCard
                        )
                    )
                }
            }
        }

        // Voice & Audio Speech Settings
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkCardElevated
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Voice",
                            tint = QuantumViolet,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "VOICE SYNTHESIS & TTS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = QuantumViolet,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Speech Rate (${String.format("%.1f", uiState.voiceSpeed)}x)",
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                    Slider(
                        value = uiState.voiceSpeed,
                        onValueChange = { viewModel.setVoiceSettings(it, uiState.voicePitch) },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = QuantumViolet,
                            activeTrackColor = QuantumViolet,
                            inactiveTrackColor = DarkCard
                        )
                    )

                    Text(
                        text = "Pitch (${String.format("%.1f", uiState.voicePitch)}x)",
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                    Slider(
                        value = uiState.voicePitch,
                        onValueChange = { viewModel.setVoiceSettings(uiState.voiceSpeed, it) },
                        valueRange = 0.5f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = QuantumViolet,
                            activeTrackColor = QuantumViolet,
                            inactiveTrackColor = DarkCard
                        )
                    )
                }
            }
        }

        // Security & API Key Status
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkCardElevated
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security",
                            tint = if (hasApiKey) NeonEmerald else CyberPink,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Gemini API Matrix",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (hasApiKey) "Active & Secured via BuildConfig" else "API Key missing in Secrets",
                                fontSize = 11.sp,
                                color = if (hasApiKey) NeonEmerald else CyberPink
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (hasApiKey) NeonEmerald else CyberPink)
                    )
                }
            }
        }

        // Clear Storage Data
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.clearAllChats()
                    viewModel.clearAllImages()
                    Toast.makeText(context, "Local database storage cleared", Toast.LENGTH_SHORT).show()
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear",
                            tint = CyberPink,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Clear Local Data & Cache",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberPink
                            )
                            Text(
                                text = "Erase all chat logs and generated visuals from Room DB",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
