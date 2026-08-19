package com.example.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.core.content.FileProvider
import com.example.data.local.GeneratedImageEntity
import com.example.ui.NexoraUiState
import com.example.ui.NexoraViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.NexoraNeuralCore
import com.example.ui.theme.CyberPink
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.QuantumViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

@Composable
fun ImageStudioScreen(
    viewModel: NexoraViewModel,
    uiState: NexoraUiState,
    generatedImages: List<GeneratedImageEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedTabIndex by remember { mutableStateOf(0) } // 0: Generator, 1: Gallery
    var promptInput by remember { mutableStateOf("") }
    var selectedAspectRatio by remember { mutableStateOf("1:1") }
    var selectedStyle by remember { mutableStateOf("Futuristic Cyberpunk") }
    var selectedQuality by remember { mutableStateOf("1K") }
    var sourceImageBase64 by remember { mutableStateOf<String?>(null) }
    var sourceImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val styles = listOf(
        "Futuristic Cyberpunk",
        "Cinematic Photorealism",
        "Anime Studio",
        "3D Octane Render",
        "Dark Synthwave",
        "Minimalist Vector",
        "Holographic Neon",
        "Oil Painting"
    )

    val aspectRatios = listOf("1:1", "16:9", "9:16", "4:3", "3:4")
    val qualities = listOf("512px", "1K", "2K")

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
                sourceImageBitmap = bitmap
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                sourceImageBase64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load reference image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "NEURAL VISUAL SYNTHESIS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberPink,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "AI Image Studio",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            NexoraNeuralCore(size = 36.dp, isActive = uiState.isGeneratingImage)
        }

        // Tabs (Studio vs Gallery)
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = NeonCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = CyberPink
                )
            },
            divider = {}
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Text(
                        text = "Generator",
                        fontSize = 14.sp,
                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTabIndex == 0) TextPrimary else TextMuted
                    )
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Text(
                        text = "Gallery (${generatedImages.size})",
                        fontSize = 14.sp,
                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTabIndex == 1) TextPrimary else TextMuted
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTabIndex == 0) {
            // Generator View
            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Prompt Input Box
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = DarkCardElevated
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "IMAGE PROMPT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = promptInput,
                                onValueChange = { promptInput = it },
                                placeholder = {
                                    Text(
                                        "Describe the subject, scene, aesthetic lighting, atmosphere in detail...",
                                        color = TextMuted,
                                        fontSize = 13.sp
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                maxLines = 5
                            )

                            // Sample ideas
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                val ideas = listOf(
                                    "Futuristic neon cybernetic city at night",
                                    "Quantum neural core orb glowing in dark void",
                                    "Astronaut discovering alien bioluminescent crystals",
                                    "Sleek hypercar gliding on light highway"
                                )
                                items(ideas) { idea ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF0F172A))
                                            .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                                            .clickable { promptInput = idea }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "+ $idea", fontSize = 10.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                // Image-to-Image reference option
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { imagePickerLauncher.launch("image/*") }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (sourceImageBitmap != null) {
                                    Image(
                                        bitmap = sourceImageBitmap!!.asImageBitmap(),
                                        contentDescription = "Source",
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Reference Image Loaded",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonEmerald
                                        )
                                        Text(
                                            text = "Image-to-image remix mode active",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(QuantumViolet.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Image,
                                            contentDescription = "Upload",
                                            tint = QuantumViolet,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Image-to-Image / Style Remix",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Upload reference for background/style transformation",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }

                            if (sourceImageBitmap != null) {
                                IconButton(onClick = {
                                    sourceImageBitmap = null
                                    sourceImageBase64 = null
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                // Aspect Ratio Selector
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "ASPECT RATIO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            aspectRatios.forEach { ratio ->
                                val isSelected = selectedAspectRatio == ratio
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else DarkCard)
                                    .border(1.dp, if (isSelected) NeonCyan else GlassBorder, RoundedCornerShape(12.dp))
                                    .clickable { selectedAspectRatio = ratio }
                                    .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ratio,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) NeonCyan else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Style Selector
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "VISUAL STYLE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(styles) { style ->
                                val isSelected = selectedStyle == style
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) CyberPink.copy(alpha = 0.2f) else DarkCard)
                                        .border(1.dp, if (isSelected) CyberPink else GlassBorder, RoundedCornerShape(12.dp))
                                        .clickable { selectedStyle = style }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = style,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) CyberPink else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Quality Selector
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "IMAGE RESOLUTION & QUALITY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            qualities.forEach { q ->
                                val isSelected = selectedQuality == q
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) ElectricBlue.copy(alpha = 0.2f) else DarkCard)
                                        .border(1.dp, if (isSelected) ElectricBlue else GlassBorder, RoundedCornerShape(12.dp))
                                        .clickable { selectedQuality = q }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = q,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) ElectricBlue else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Generate Button
                item {
                    Button(
                        onClick = {
                            if (promptInput.isNotBlank()) {
                                viewModel.generateImage(
                                    prompt = promptInput,
                                    aspectRatio = selectedAspectRatio,
                                    style = selectedStyle,
                                    quality = selectedQuality,
                                    sourceImageBase64 = sourceImageBase64
                                )
                            }
                        },
                        enabled = promptInput.isNotBlank() && !uiState.isGeneratingImage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberPink,
                            contentColor = Color.White
                        )
                    ) {
                        if (uiState.isGeneratingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Synthesizing Neural Art...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Generate", modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Synthesize Visual (gemini-2.5-flash-image)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                // Error Message Banner (if any)
                if (uiState.imageGenError != null) {
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = Color(0xFFFF3366)
                        ) {
                            Text(
                                text = "Notice: ${uiState.imageGenError}",
                                color = Color(0xFFFF758C),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                // Last Generated Image Display
                if (uiState.lastGeneratedImage != null) {
                    val lastImg = uiState.lastGeneratedImage
                    item {
                        GeneratedImageCard(
                            image = lastImg,
                            onSave = { saveImageToGallery(context, lastImg.imageBase64, lastImg.prompt) },
                            onShare = { shareImage(context, lastImg.imageBase64, lastImg.prompt) },
                            onCopyPrompt = {
                                clipboardManager.setText(AnnotatedString(lastImg.prompt))
                                Toast.makeText(context, "Prompt copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = { viewModel.deleteGeneratedImage(lastImg.id) },
                            onFavorite = { viewModel.toggleFavoriteImage(lastImg.id, lastImg.isFavorite) },
                            onRemix = {
                                promptInput = lastImg.prompt
                                selectedStyle = lastImg.style
                                selectedAspectRatio = lastImg.aspectRatio
                                sourceImageBase64 = lastImg.imageBase64
                            }
                        )
                    }
                }
            }
        } else {
            // Gallery View
            if (generatedImages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Empty",
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No generated images yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Synthesize visuals in the Generator tab to build your gallery.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(generatedImages, key = { it.id }) { img ->
                        GeneratedImageCard(
                            image = img,
                            onSave = { saveImageToGallery(context, img.imageBase64, img.prompt) },
                            onShare = { shareImage(context, img.imageBase64, img.prompt) },
                            onCopyPrompt = {
                                clipboardManager.setText(AnnotatedString(img.prompt))
                                Toast.makeText(context, "Prompt copied", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = { viewModel.deleteGeneratedImage(img.id) },
                            onFavorite = { viewModel.toggleFavoriteImage(img.id, img.isFavorite) },
                            onRemix = {
                                promptInput = img.prompt
                                selectedStyle = img.style
                                selectedAspectRatio = img.aspectRatio
                                sourceImageBase64 = img.imageBase64
                                selectedTabIndex = 0
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GeneratedImageCard(
    image: GeneratedImageEntity,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onCopyPrompt: () -> Unit,
    onDelete: () -> Unit,
    onFavorite: () -> Unit,
    onRemix: () -> Unit
) {
    val bitmap = remember(image.imageBase64) {
        try {
            val bytes = Base64.decode(image.imageBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) { null }
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = GlassBorderHighlight
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = image.prompt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = image.prompt,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkCard)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = image.style, fontSize = 10.sp, color = CyberPink)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkCard)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = image.aspectRatio, fontSize = 10.sp, color = NeonCyan)
                    }
                }

                Row {
                    IconButton(onClick = onFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (image.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (image.isFavorite) CyberPink else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onRemix, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Remix / Edit",
                            tint = QuantumViolet,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onSave, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Save to device",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = ElectricBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onCopyPrompt, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy prompt",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

fun saveImageToGallery(context: Context, base64Data: String, prompt: String) {
    try {
        val bytes = Base64.decode(base64Data, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val filename = "NEXORA_AI_${System.currentTimeMillis()}.jpg"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/NexoraAI")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
            }
            Toast.makeText(context, "Image saved to Gallery!", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun shareImage(context: Context, base64Data: String, prompt: String) {
    try {
        val bytes = Base64.decode(base64Data, Base64.DEFAULT)
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "nexora_shared_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { it.write(bytes) }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Generated with NEXORA AI: $prompt")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share AI Image"))
    } catch (e: Exception) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Prompt: $prompt\nGenerated with NEXORA AI")
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Prompt"))
    }
}
