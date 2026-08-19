package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MessageEntity
import com.example.data.model.PersonaRepository
import com.example.ui.NexoraUiState
import com.example.ui.NexoraViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.MarkdownContent
import com.example.ui.components.NexoraNeuralCore
import com.example.ui.components.VoiceWaveIndicator
import com.example.ui.theme.AiBubbleBg
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
import com.example.ui.theme.UserBubbleBg
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun ChatScreen(
    viewModel: NexoraViewModel,
    uiState: NexoraUiState,
    messages: List<MessageEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf("") }
    var editingMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var editTextValue by remember { mutableStateOf("") }
    var showDocumentDialog by remember { mutableStateOf(false) }
    var docNameInput by remember { mutableStateOf("") }
    var docContentInput by remember { mutableStateOf("") }

    // Sync speech transcript into input text
    LaunchedEffect(uiState.voiceTranscript) {
        if (uiState.voiceTranscript.isNotBlank()) {
            inputText = uiState.voiceTranscript
        }
    }

    // Scroll to bottom on new message or streaming text
    LaunchedEffect(messages.size, uiState.streamingText) {
        if (messages.isNotEmpty() || uiState.streamingText.isNotEmpty()) {
            val count = messages.size + (if (uiState.isGenerating) 1 else 0)
            if (count > 0) {
                listState.animateScrollToItem(count - 1)
            }
        }
    }

    // Image Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap: Bitmap = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
                viewModel.attachImageBitmap(bitmap)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Document Picker
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = reader.readText()
                val fileName = "Document_${System.currentTimeMillis() % 1000}.txt"
                viewModel.attachDocument(fileName, content)
                Toast.makeText(context, "Document attached successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Could not read text file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        // Chat Header with Persona Selector & New Chat button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NexoraNeuralCore(size = 32.dp, isActive = uiState.isGenerating)
                Column {
                    Text(
                        text = uiState.selectedPersona.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (uiState.selectedTool != null) "Tool: ${uiState.selectedTool.title}" else uiState.selectedModel,
                        fontSize = 11.sp,
                        color = NeonCyan
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // New Chat Button
                IconButton(
                    onClick = { viewModel.startNewChat() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkCardElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Persona Switcher Bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(PersonaRepository.personas) { persona ->
                val isSelected = uiState.selectedPersona.id == persona.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) Color(persona.primaryColorHex).copy(alpha = 0.2f) else DarkCard
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(persona.primaryColorHex) else GlassBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.selectPersona(persona) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = persona.name,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(persona.primaryColorHex) else TextSecondary
                    )
                }
            }
        }

        // Messages List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty() && !uiState.isGenerating) {
                // Empty Chat State with Persona Greeting & Quick Suggestions
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    NexoraNeuralCore(size = 64.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "NEXORA AI QUANTUM CORE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = uiState.selectedPersona.greeting,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Suggested Questions:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val suggestions = if (uiState.selectedTool != null) {
                        uiState.selectedTool.samplePrompts
                    } else {
                        listOf(
                            "Synthesize modern quantum computing principles",
                            "Write a Kotlin coroutine reactive flow architecture",
                            "Explain neural attention mechanisms step by step",
                            "Draft a futuristic product launch strategy"
                        )
                    }

                    suggestions.forEach { suggestion ->
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = { viewModel.sendMessage(suggestion) }
                        ) {
                            Text(
                                text = suggestion,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            isPlayingTts = uiState.isPlayingTts && uiState.ttsMessageId == msg.id,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(msg.content))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onTtsToggle = {
                                viewModel.speakMessage(msg.id, msg.content)
                            },
                            onEdit = {
                                editingMessage = msg
                                editTextValue = msg.content
                            },
                            onRegenerate = {
                                viewModel.regenerateLastResponse()
                            }
                        )
                    }

                    // Live Streaming Bubble
                    if (uiState.isGenerating) {
                        item(key = "streaming_bubble") {
                            StreamingAiBubble(
                                text = uiState.streamingText,
                                onStop = { viewModel.stopGeneration() }
                            )
                        }
                    }
                }
            }
        }

        // Smart Follow-up Suggestions (shown when not generating and messages exist)
        if (!uiState.isGenerating && messages.isNotEmpty()) {
            val lastModelMsg = messages.lastOrNull { it.role == "model" }
            if (lastModelMsg != null) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val followUps = listOf(
                        "Explain in deeper technical detail",
                        "Provide concrete code examples",
                        "Give 3 practical action steps",
                        "Summarize key takeaways"
                    )
                    items(followUps) { item ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF131D33))
                                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                                .clickable { viewModel.sendMessage(item) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = item, fontSize = 11.sp, color = NeonCyan)
                        }
                    }
                }
            }
        }

        // Attachment Previews (Image / Document)
        AnimatedVisibility(
            visible = uiState.attachedImageBase64 != null || uiState.attachedDocumentName != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.attachedImageBase64 != null) {
                    val bitmap = remember(uiState.attachedImageBase64) {
                        try {
                            val bytes = Base64.decode(uiState.attachedImageBase64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } catch (e: Exception) { null }
                    }

                    if (bitmap != null) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, NeonCyan, RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Attached image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { viewModel.removeAttachedImage() },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                if (uiState.attachedDocumentName != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkCardElevated)
                            .border(1.dp, ElectricBlue, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Doc",
                                tint = ElectricBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.attachedDocumentName ?: "Document",
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { viewModel.removeAttachedDocument() },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove doc",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Voice Listening Bar (if mic active)
        if (uiState.isListeningVoice) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF151C30))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VoiceWaveIndicator(isListening = true, barCount = 7)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Listening to voice...",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = { viewModel.stopListening() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop recording",
                        tint = CyberPink
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Chat Input Bar
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 85.dp),
            backgroundColor = DarkCardElevated
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attach menu (Image / Doc)
                IconButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Attach image",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { showDocumentDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach text or document",
                        tint = ElectricBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = uiState.selectedTool?.placeholderPrompt ?: "Message NEXORA AI...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 4
                )

                // Voice Mic Button
                IconButton(
                    onClick = {
                        if (uiState.isListeningVoice) {
                            viewModel.stopListening()
                        } else {
                            viewModel.startListening()
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isListeningVoice) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice input",
                        tint = if (uiState.isListeningVoice) CyberPink else QuantumViolet,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Send or Stop Generation
                if (uiState.isGenerating) {
                    IconButton(
                        onClick = { viewModel.stopGeneration() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyberPink)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() || uiState.attachedImageBase64 != null || uiState.attachedDocumentContent != null) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() || uiState.attachedImageBase64 != null || uiState.attachedDocumentContent != null,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (inputText.isNotBlank() || uiState.attachedImageBase64 != null || uiState.attachedDocumentContent != null) {
                                    Brush.linearGradient(listOf(NeonCyan, ElectricBlue))
                                } else {
                                    Brush.linearGradient(listOf(DarkCard, DarkCard))
                                }
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() || uiState.attachedImageBase64 != null) DarkCard else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Edit User Message Dialog
    if (editingMessage != null) {
        AlertDialog(
            onDismissRequest = { editingMessage = null },
            title = { Text("Edit Message", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editTextValue,
                    onValueChange = { editTextValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val msg = editingMessage
                        if (msg != null && editTextValue.isNotBlank()) {
                            viewModel.editUserMessage(msg.id, editTextValue)
                        }
                        editingMessage = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkCard)
                ) {
                    Text("Save & Regenerate")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingMessage = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkCard
        )
    }

    // Attach Document / Text Dialog
    if (showDocumentDialog) {
        AlertDialog(
            onDismissRequest = { showDocumentDialog = false },
            title = { Text("Attach Document / Code", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = docNameInput,
                        onValueChange = { docNameInput = it },
                        placeholder = { Text("Document Title (e.g. Spec.md)", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = docContentInput,
                        onValueChange = { docContentInput = it },
                        placeholder = { Text("Paste document text, log file, or code here...", color = TextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        maxLines = 8
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (docContentInput.isNotBlank()) {
                            val name = docNameInput.ifBlank { "Pasted_Document.txt" }
                            viewModel.attachDocument(name, docContentInput)
                            docNameInput = ""
                            docContentInput = ""
                            showDocumentDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = DarkCard)
                ) {
                    Text("Attach to Chat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDocumentDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkCard
        )
    }
}

@Composable
fun MessageBubble(
    message: MessageEntity,
    isPlayingTts: Boolean,
    onCopy: () -> Unit,
    onTtsToggle: () -> Unit,
    onEdit: () -> Unit,
    onRegenerate: () -> Unit
) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Message Container
        Row(
            modifier = Modifier.widthIn(max = 340.dp),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!isUser) {
                NexoraNeuralCore(size = 28.dp, isActive = false)
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(if (isUser) UserBubbleBg else AiBubbleBg)
                    .border(
                        width = 1.dp,
                        color = if (isUser) GlassBorderHighlight.copy(alpha = 0.3f) else GlassBorder,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .padding(12.dp)
            ) {
                // Attached Image preview
                if (!message.imageBase64.isNullOrBlank()) {
                    val bitmap = remember(message.imageBase64) {
                        try {
                            val bytes = Base64.decode(message.imageBase64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } catch (e: Exception) { null }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Message Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .padding(bottom = 8.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Attached File Name
                if (!message.attachedFileName.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "File",
                            tint = ElectricBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = message.attachedFileName,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Markdown / Text
                MarkdownContent(content = message.content, isUser = isUser)

                // Actions row (Copy, TTS, Edit, Regenerate)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    if (!isUser) {
                        IconButton(
                            onClick = onTtsToggle,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlayingTts) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                contentDescription = "Text to speech",
                                tint = if (isPlayingTts) NeonCyan else TextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        IconButton(
                            onClick = onRegenerate,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate response",
                                tint = TextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit message",
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StreamingAiBubble(
    text: String,
    onStop: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 340.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        NexoraNeuralCore(size = 28.dp, isActive = true)
        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                .background(AiBubbleBg)
                .border(1.dp, GlassBorderHighlight, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    color = NeonCyan,
                    strokeWidth = 2.dp
                )
                Text(
                    text = "NEXORA STREAMING RESPONSE...",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (text.isNotBlank()) {
                MarkdownContent(content = text, isUser = false)
            } else {
                Text(
                    text = "Synthesizing thoughts through quantum matrix...",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(containerColor = CyberPink.copy(alpha = 0.2f), contentColor = CyberPink),
                modifier = Modifier.height(28.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Stop Generation", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
