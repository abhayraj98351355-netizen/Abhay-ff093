package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ConversationEntity
import com.example.data.local.GeneratedImageEntity
import com.example.data.local.MessageEntity
import com.example.data.local.NexoraDatabase
import com.example.data.model.AiTool
import com.example.data.model.AiToolRegistry
import com.example.data.model.Persona
import com.example.data.model.PersonaRepository
import com.example.data.repository.NexoraRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.UUID

data class NexoraUiState(
    val currentTab: String = "home", // "home", "chat", "create", "tools", "history", "settings"
    val activeConversationId: String? = null,
    val selectedPersona: Persona = PersonaRepository.personas.first(),
    val selectedModel: String = "gemini-3.5-flash",
    val selectedTool: AiTool? = null,
    val isGenerating: Boolean = false,
    val streamingText: String = "",
    val isListeningVoice: Boolean = false,
    val voiceTranscript: String = "",
    val isPlayingTts: Boolean = false,
    val ttsMessageId: String? = null,
    val attachedImageBase64: String? = null,
    val attachedDocumentName: String? = null,
    val attachedDocumentContent: String? = null,
    val isGeneratingImage: Boolean = false,
    val imageGenError: String? = null,
    val lastGeneratedImage: GeneratedImageEntity? = null,
    val responseTemperature: Float = 0.7f,
    val voiceSpeed: Float = 1.0f,
    val voicePitch: Float = 1.0f,
    val isDarkMode: Boolean = true,
    val searchHistoryQuery: String = ""
)

class NexoraViewModel(application: Application) : AndroidViewModel(application) {

    private val db = NexoraDatabase.getDatabase(application)
    private val repository = NexoraRepository(db.nexoraDao(), application)

    private val _uiState = MutableStateFlow(NexoraUiState())
    val uiState: StateFlow<NexoraUiState> = _uiState.asStateFlow()

    val conversations: StateFlow<List<ConversationEntity>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val generatedImages: StateFlow<List<GeneratedImageEntity>> = repository.allGeneratedImages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val currentMessages: StateFlow<List<MessageEntity>> = _currentMessages.asStateFlow()

    private var currentChatJob: Job? = null
    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    init {
        initTts(application)
        initSpeechRecognizer(application)
    }

    private fun initTts(context: Context) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
                textToSpeech?.setSpeechRate(_uiState.value.voiceSpeed)
                textToSpeech?.setPitch(_uiState.value.voicePitch)
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _uiState.update { it.copy(isPlayingTts = true, ttsMessageId = utteranceId) }
                    }
                    override fun onDone(utteranceId: String?) {
                        _uiState.update { it.copy(isPlayingTts = false, ttsMessageId = null) }
                    }
                    override fun onError(utteranceId: String?) {
                        _uiState.update { it.copy(isPlayingTts = false, ttsMessageId = null) }
                    }
                })
            }
        }
    }

    private fun initSpeechRecognizer(context: Context) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    _uiState.update { it.copy(isListeningVoice = false) }
                }
                override fun onError(error: Int) {
                    _uiState.update { it.copy(isListeningVoice = false) }
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull() ?: ""
                    _uiState.update { it.copy(isListeningVoice = false, voiceTranscript = spoken) }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partial = matches?.firstOrNull() ?: ""
                    if (partial.isNotBlank()) {
                        _uiState.update { it.copy(voiceTranscript = partial) }
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    fun startListening() {
        if (speechRecognizer != null) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            try {
                speechRecognizer?.startListening(intent)
                _uiState.update { it.copy(isListeningVoice = true, voiceTranscript = "") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isListeningVoice = false) }
            }
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {}
        _uiState.update { it.copy(isListeningVoice = false) }
    }

    fun speakMessage(messageId: String, text: String) {
        if (_uiState.value.isPlayingTts && _uiState.value.ttsMessageId == messageId) {
            stopSpeech()
            return
        }
        stopSpeech()
        val cleanText = text.replace(Regex("```[\\s\\S]*?```"), "Code snippet omitted.")
            .replace(Regex("[*#_`]"), "")
        textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, messageId)
    }

    fun stopSpeech() {
        textToSpeech?.stop()
        _uiState.update { it.copy(isPlayingTts = false, ttsMessageId = null) }
    }

    fun setTab(tab: String) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun selectPersona(persona: Persona) {
        _uiState.update { it.copy(selectedPersona = persona) }
    }

    fun selectModel(model: String) {
        _uiState.update { it.copy(selectedModel = model) }
    }

    fun setResponseTemperature(temp: Float) {
        _uiState.update { it.copy(responseTemperature = temp) }
    }

    fun setVoiceSettings(speed: Float, pitch: Float) {
        _uiState.update { it.copy(voiceSpeed = speed, voicePitch = pitch) }
        textToSpeech?.setSpeechRate(speed)
        textToSpeech?.setPitch(pitch)
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchHistoryQuery = query) }
    }

    fun launchTool(tool: AiTool) {
        _uiState.update {
            it.copy(
                selectedTool = tool,
                currentTab = if (tool.id == "image_generator") "create" else "chat"
            )
        }
        if (tool.id != "image_generator") {
            startNewChat(personaId = "nexora_core", tool = tool)
        }
    }

    fun startNewChat(personaId: String? = null, tool: AiTool? = null) {
        stopGeneration()
        viewModelScope.launch {
            val pId = personaId ?: _uiState.value.selectedPersona.id
            val title = tool?.title?.let { "$it Session" } ?: "New Nexora Chat"
            val newId = repository.createConversation(title = title, personaId = pId)
            _uiState.update {
                it.copy(
                    activeConversationId = newId,
                    selectedPersona = PersonaRepository.getById(pId),
                    selectedTool = tool,
                    currentTab = "chat",
                    attachedImageBase64 = null,
                    attachedDocumentName = null,
                    attachedDocumentContent = null,
                    streamingText = ""
                )
            }
            loadMessages(newId)
        }
    }

    fun selectConversation(id: String) {
        stopGeneration()
        viewModelScope.launch {
            val conv = db.nexoraDao().getConversationById(id)
            val persona = if (conv != null) PersonaRepository.getById(conv.personaId) else _uiState.value.selectedPersona
            _uiState.update {
                it.copy(
                    activeConversationId = id,
                    selectedPersona = persona,
                    currentTab = "chat",
                    streamingText = "",
                    attachedImageBase64 = null,
                    attachedDocumentName = null,
                    attachedDocumentContent = null
                )
            }
            loadMessages(id)
        }
    }

    private fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            repository.getMessagesForConversation(conversationId).collect { msgs ->
                _currentMessages.value = msgs
            }
        }
    }

    fun attachImageBitmap(bitmap: Bitmap) {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        _uiState.update { it.copy(attachedImageBase64 = base64) }
    }

    fun removeAttachedImage() {
        _uiState.update { it.copy(attachedImageBase64 = null) }
    }

    fun attachDocument(name: String, content: String) {
        _uiState.update {
            it.copy(
                attachedDocumentName = name,
                attachedDocumentContent = content
            )
        }
    }

    fun removeAttachedDocument() {
        _uiState.update {
            it.copy(
                attachedDocumentName = null,
                attachedDocumentContent = null
            )
        }
    }

    fun sendMessage(
        prompt: String,
        overrideTool: AiTool? = null
    ) {
        if (prompt.isBlank() && _uiState.value.attachedImageBase64 == null && _uiState.value.attachedDocumentContent == null) return

        var activeConvId = _uiState.value.activeConversationId
        val tool = overrideTool ?: _uiState.value.selectedTool
        val persona = _uiState.value.selectedPersona
        val model = if (tool?.id in listOf("coding_assistant", "math_solver", "research_mode")) {
            "gemini-3.1-pro-preview"
        } else {
            _uiState.value.selectedModel
        }

        viewModelScope.launch {
            if (activeConvId == null) {
                val title = if (prompt.length > 30) prompt.take(30) + "..." else prompt
                activeConvId = repository.createConversation(title = title, personaId = persona.id)
                _uiState.update { it.copy(activeConversationId = activeConvId) }
                loadMessages(activeConvId)
            }

            val userMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = activeConvId!!,
                role = "user",
                content = prompt,
                imageBase64 = _uiState.value.attachedImageBase64,
                attachedFileName = _uiState.value.attachedDocumentName,
                timestamp = System.currentTimeMillis(),
                personaId = persona.id,
                toolId = tool?.id
            )
            repository.saveMessage(userMsg)

            val attachedImg = _uiState.value.attachedImageBase64
            val attachedDoc = _uiState.value.attachedDocumentContent

            // Clear inputs
            _uiState.update {
                it.copy(
                    attachedImageBase64 = null,
                    attachedDocumentName = null,
                    attachedDocumentContent = null,
                    isGenerating = true,
                    streamingText = ""
                )
            }

            val systemInstruction = tool?.systemInstruction ?: persona.systemInstruction

            currentChatJob = launch {
                var accumulated = ""
                repository.streamChatResponse(
                    conversationId = activeConvId!!,
                    userPrompt = prompt,
                    userImageBase64 = attachedImg,
                    userDocumentText = attachedDoc,
                    personaInstruction = systemInstruction,
                    modelName = model,
                    temperature = _uiState.value.responseTemperature
                ).collect { chunk ->
                    accumulated = chunk
                    _uiState.update { it.copy(streamingText = accumulated) }
                }

                val aiMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    conversationId = activeConvId!!,
                    role = "model",
                    content = accumulated.ifBlank { "Nexora AI processing completed." },
                    timestamp = System.currentTimeMillis(),
                    personaId = persona.id,
                    toolId = tool?.id
                )
                repository.saveMessage(aiMsg)

                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        streamingText = ""
                    )
                }
            }
        }
    }

    fun regenerateLastResponse() {
        val msgs = _currentMessages.value
        val lastModelMsg = msgs.lastOrNull { it.role == "model" }
        val lastUserMsg = msgs.lastOrNull { it.role == "user" }

        if (lastUserMsg != null) {
            viewModelScope.launch {
                if (lastModelMsg != null) {
                    repository.deleteMessage(lastModelMsg.id)
                }
                sendMessage(lastUserMsg.content)
            }
        }
    }

    fun editUserMessage(messageId: String, newText: String) {
        val convId = _uiState.value.activeConversationId ?: return
        val target = _currentMessages.value.find { it.id == messageId } ?: return

        viewModelScope.launch {
            // Delete all messages after this timestamp
            repository.deleteMessagesAfter(convId, target.timestamp)
            // Re-send updated text
            sendMessage(newText)
        }
    }

    fun stopGeneration() {
        currentChatJob?.cancel()
        currentChatJob = null
        if (_uiState.value.isGenerating && _uiState.value.streamingText.isNotBlank()) {
            val convId = _uiState.value.activeConversationId
            if (convId != null) {
                val partialText = _uiState.value.streamingText
                viewModelScope.launch {
                    val aiMsg = MessageEntity(
                        id = UUID.randomUUID().toString(),
                        conversationId = convId,
                        role = "model",
                        content = "$partialText\n\n*(Generation stopped by user)*",
                        timestamp = System.currentTimeMillis(),
                        personaId = _uiState.value.selectedPersona.id
                    )
                    repository.saveMessage(aiMsg)
                }
            }
        }
        _uiState.update { it.copy(isGenerating = false, streamingText = "") }
    }

    fun renameConversation(id: String, newTitle: String) {
        viewModelScope.launch {
            repository.renameConversation(id, newTitle)
        }
    }

    fun togglePinConversation(id: String, currentPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePinConversation(id, currentPinned)
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_uiState.value.activeConversationId == id) {
                _uiState.update { it.copy(activeConversationId = null) }
                _currentMessages.value = emptyList()
            }
        }
    }

    fun clearAllChats() {
        viewModelScope.launch {
            repository.clearAllConversations()
            _uiState.update { it.copy(activeConversationId = null) }
            _currentMessages.value = emptyList()
        }
    }

    fun generateImage(
        prompt: String,
        aspectRatio: String = "1:1",
        style: String = "Futuristic Cyberpunk",
        quality: String = "1K",
        sourceImageBase64: String? = null
    ) {
        if (prompt.isBlank()) return
        _uiState.update { it.copy(isGeneratingImage = true, imageGenError = null) }

        viewModelScope.launch {
            val result = repository.generateImage(
                prompt = prompt,
                aspectRatio = aspectRatio,
                style = style,
                quality = quality,
                sourceImageBase64 = sourceImageBase64
            )
            result.onSuccess { generated ->
                _uiState.update {
                    it.copy(
                        isGeneratingImage = false,
                        lastGeneratedImage = generated,
                        imageGenError = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isGeneratingImage = false,
                        imageGenError = error.localizedMessage ?: "Failed to generate visual."
                    )
                }
            }
        }
    }

    fun deleteGeneratedImage(id: String) {
        viewModelScope.launch {
            repository.deleteGeneratedImage(id)
        }
    }

    fun toggleFavoriteImage(id: String, currentFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavoriteImage(id, currentFav)
        }
    }

    fun clearAllImages() {
        viewModelScope.launch {
            repository.clearAllGeneratedImages()
        }
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        speechRecognizer?.destroy()
    }
}
