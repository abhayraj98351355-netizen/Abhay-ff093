package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.FuturisticBackground
import com.example.ui.navigation.NexoraBottomBar
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImageStudioScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.DarkBackground

@Composable
fun NexoraApp(
    viewModel: NexoraViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val messages by viewModel.currentMessages.collectAsState()
    val generatedImages by viewModel.generatedImages.collectAsState()

    // Audio Permission Request
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            NexoraBottomBar(
                currentTab = uiState.currentTab,
                onTabSelected = { tab ->
                    viewModel.setTab(tab)
                }
            )
        }
    ) { innerPadding ->
        FuturisticBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = uiState.currentTab,
                animationSpec = tween(durationMillis = 200),
                label = "tab_crossfade"
            ) { tab ->
                when (tab) {
                    "home" -> HomeScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        conversations = conversations
                    )
                    "chat" -> ChatScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        messages = messages
                    )
                    "create" -> ImageStudioScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        generatedImages = generatedImages
                    )
                    "tools" -> ToolsScreen(
                        viewModel = viewModel
                    )
                    "history" -> HistoryScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        conversations = conversations
                    )
                    "settings" -> SettingsScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    else -> HomeScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        conversations = conversations
                    )
                }
            }
        }
    }
}
