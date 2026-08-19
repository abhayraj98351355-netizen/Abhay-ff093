package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.BuildConfig
import com.example.data.api.RetrofitClient
import com.example.data.local.ConversationEntity
import com.example.data.local.GeneratedImageEntity
import com.example.data.local.MessageEntity
import com.example.data.local.NexoraDao
import com.example.data.model.GeminiContent
import com.example.data.model.GeminiGenerationConfig
import com.example.data.model.GeminiImageConfig
import com.example.data.model.GeminiInlineData
import com.example.data.model.GeminiPart
import com.example.data.model.GeminiRequest
import com.example.data.model.GeminiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID

class NexoraRepository(
    private val nexoraDao: NexoraDao,
    private val context: Context
) {
    // API Key resolution: BuildConfig or user override
    fun getApiKey(): String {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNotBlank() && key != "MY_GEMINI_API_KEY") key else ""
        } catch (e: Exception) {
            ""
        }
    }

    // Conversations Flow
    val allConversations: Flow<List<ConversationEntity>> = nexoraDao.getAllConversations()
    val allGeneratedImages: Flow<List<GeneratedImageEntity>> = nexoraDao.getAllGeneratedImages()

    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>> {
        return nexoraDao.getMessagesForConversation(conversationId)
    }

    suspend fun createConversation(
        title: String,
        personaId: String,
        initialId: String = UUID.randomUUID().toString()
    ): String = withContext(Dispatchers.IO) {
        val conv = ConversationEntity(
            id = initialId,
            title = title,
            personaId = personaId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            lastMessagePreview = ""
        )
        nexoraDao.insertOrUpdateConversation(conv)
        initialId
    }

    suspend fun renameConversation(id: String, newTitle: String) = withContext(Dispatchers.IO) {
        nexoraDao.renameConversation(id, newTitle)
    }

    suspend fun togglePinConversation(id: String, currentPinned: Boolean) = withContext(Dispatchers.IO) {
        nexoraDao.setConversationPinned(id, !currentPinned)
    }

    suspend fun deleteConversation(id: String) = withContext(Dispatchers.IO) {
        nexoraDao.deleteMessagesByConversation(id)
        nexoraDao.deleteConversation(id)
    }

    suspend fun clearAllConversations() = withContext(Dispatchers.IO) {
        nexoraDao.clearAllMessages()
        nexoraDao.clearAllConversations()
    }

    suspend fun saveMessage(message: MessageEntity) = withContext(Dispatchers.IO) {
        nexoraDao.insertMessage(message)
        // Update conversation updated_at and last preview
        val conv = nexoraDao.getConversationById(message.conversationId)
        if (conv != null) {
            val preview = if (message.content.length > 80) message.content.take(80) + "..." else message.content
            nexoraDao.insertOrUpdateConversation(
                conv.copy(
                    updatedAt = System.currentTimeMillis(),
                    lastMessagePreview = preview,
                    messageCount = conv.messageCount + 1
                )
            )
        }
    }

    suspend fun deleteMessage(id: String) = withContext(Dispatchers.IO) {
        nexoraDao.deleteMessage(id)
    }

    suspend fun deleteMessagesAfter(conversationId: String, timestamp: Long) = withContext(Dispatchers.IO) {
        nexoraDao.deleteMessagesAfter(conversationId, timestamp)
    }

    /**
     * Send Multimodal Message to Gemini and stream response back.
     */
    fun streamChatResponse(
        conversationId: String,
        userPrompt: String,
        userImageBase64: String? = null,
        userDocumentText: String? = null,
        personaInstruction: String,
        modelName: String = "gemini-3.5-flash",
        temperature: Float = 0.7f
    ): Flow<String> = flow {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            emit("Error: Gemini API Key is missing. Please configure GEMINI_API_KEY in the AI Studio Secrets panel or environment variables.")
            return@flow
        }

        // Fetch past messages for conversation context (up to 12 recent messages)
        val history = nexoraDao.getMessagesSnapshot(conversationId).takeLast(12)
        val contents = mutableListOf<GeminiContent>()

        for (msg in history) {
            val parts = mutableListOf<GeminiPart>()
            if (!msg.imageBase64.isNullOrBlank()) {
                parts.add(
                    GeminiPart(
                        inlineData = GeminiInlineData(
                            mimeType = "image/jpeg",
                            data = msg.imageBase64
                        )
                    )
                )
            }
            if (msg.content.isNotBlank()) {
                parts.add(GeminiPart(text = msg.content))
            }
            if (parts.isNotEmpty()) {
                contents.add(
                    GeminiContent(
                        role = if (msg.role == "user") "user" else "model",
                        parts = parts
                    )
                )
            }
        }

        // Current turn parts
        val currentParts = mutableListOf<GeminiPart>()
        if (!userImageBase64.isNullOrBlank()) {
            currentParts.add(
                GeminiPart(
                    inlineData = GeminiInlineData(
                        mimeType = "image/jpeg",
                        data = userImageBase64
                    )
                )
            )
        }
        var combinedPrompt = userPrompt
        if (!userDocumentText.isNullOrBlank()) {
            combinedPrompt = "Attached Document Content:\n\"\"\"\n$userDocumentText\n\"\"\"\n\nUser Prompt: $userPrompt"
        }
        currentParts.add(GeminiPart(text = combinedPrompt))
        contents.add(GeminiContent(role = "user", parts = currentParts))

        val request = GeminiRequest(
            contents = contents,
            generationConfig = GeminiGenerationConfig(
                temperature = temperature,
                topP = 0.95f,
                topK = 40
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = personaInstruction))
            )
        )

        try {
            // Attempt real streaming via streamGenerateContent
            val responseBody = RetrofitClient.service.streamGenerateContent(
                model = modelName,
                apiKey = apiKey,
                request = request
            )

            val reader = responseBody.byteStream().bufferedReader()
            var line: String?
            var accumulatedText = ""

            while (reader.readLine().also { line = it } != null) {
                val currentLine = line?.trim() ?: continue
                if (currentLine.startsWith("data:") || currentLine.startsWith("{")) {
                    val jsonStr = if (currentLine.startsWith("data:")) currentLine.substring(5).trim() else currentLine
                    if (jsonStr == "[DONE]" || jsonStr.isBlank()) continue

                    try {
                        val json = JSONObject(jsonStr)
                        val candidates = json.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val candidate = candidates.getJSONObject(0)
                            val content = candidate.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                val text = parts.getJSONObject(0).optString("text", "")
                                if (text.isNotEmpty()) {
                                    accumulatedText += text
                                    emit(accumulatedText)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Skip malformed SSE chunks
                    }
                }
            }

            if (accumulatedText.isEmpty()) {
                // Fallback direct non-streaming call if stream returned empty
                val directResponse = RetrofitClient.service.generateContent(
                    model = modelName,
                    apiKey = apiKey,
                    request = request
                )
                val text = directResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "No response generated by Nexora AI."
                emit(text)
            }
        } catch (e: Exception) {
            // If streaming encounters network/endpoint limitation, fallback to direct generateContent
            try {
                val directResponse = RetrofitClient.service.generateContent(
                    model = modelName,
                    apiKey = apiKey,
                    request = request
                )
                val text = directResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "No response received."
                emit(text)
            } catch (fallbackError: Exception) {
                emit("Error communicating with NEXORA AI: ${fallbackError.localizedMessage ?: fallbackError.message}")
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Real Image Generation using gemini-2.5-flash-image
     */
    suspend fun generateImage(
        prompt: String,
        aspectRatio: String = "1:1",
        style: String = "Futuristic Cyberpunk",
        quality: String = "1K",
        sourceImageBase64: String? = null
    ): Result<GeneratedImageEntity> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                Exception("Gemini API Key is missing. Please configure GEMINI_API_KEY in the AI Studio Secrets panel.")
            )
        }

        val enrichedPrompt = if (style.isNotBlank() && style != "None") {
            "Create a high quality $style style visual. $prompt. Ultra-detailed, aesthetic lighting, 8k resolution, crisp composition."
        } else {
            prompt
        }

        val parts = mutableListOf<GeminiPart>()
        if (!sourceImageBase64.isNullOrBlank()) {
            parts.add(
                GeminiPart(
                    inlineData = GeminiInlineData(
                        mimeType = "image/jpeg",
                        data = sourceImageBase64
                    )
                )
            )
        }
        parts.add(GeminiPart(text = enrichedPrompt))

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(role = "user", parts = parts)
            ),
            generationConfig = GeminiGenerationConfig(
                responseModalities = listOf("TEXT", "IMAGE"),
                imageConfig = GeminiImageConfig(
                    aspectRatio = aspectRatio,
                    imageSize = quality
                )
            )
        )

        try {
            val response: GeminiResponse = RetrofitClient.service.generateContent(
                model = "gemini-2.5-flash-image",
                apiKey = apiKey,
                request = request
            )

            var extractedImageBase64: String? = null
            response.candidates?.firstOrNull()?.content?.parts?.forEach { part ->
                if (part.inlineData != null && part.inlineData.data.isNotBlank()) {
                    extractedImageBase64 = part.inlineData.data
                }
            }

            if (extractedImageBase64 != null) {
                val entity = GeneratedImageEntity(
                    id = UUID.randomUUID().toString(),
                    prompt = prompt,
                    imageBase64 = extractedImageBase64!!,
                    aspectRatio = aspectRatio,
                    style = style,
                    quality = quality,
                    createdAt = System.currentTimeMillis()
                )
                nexoraDao.insertGeneratedImage(entity)
                Result.success(entity)
            } else {
                val textOutput = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                Result.failure(Exception(textOutput ?: "No image returned by the vision engine."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGeneratedImage(id: String) = withContext(Dispatchers.IO) {
        nexoraDao.deleteGeneratedImage(id)
    }

    suspend fun toggleFavoriteImage(id: String, currentFav: Boolean) = withContext(Dispatchers.IO) {
        nexoraDao.toggleImageFavorite(id, !currentFav)
    }

    suspend fun clearAllGeneratedImages() = withContext(Dispatchers.IO) {
        nexoraDao.clearAllGeneratedImages()
    }
}
