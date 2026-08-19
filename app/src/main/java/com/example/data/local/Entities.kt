package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val personaId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val lastMessagePreview: String = "",
    val messageCount: Int = 0
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: String, // "user" or "model"
    val content: String,
    val imageBase64: String? = null,
    val attachedFileName: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val personaId: String = "nexora_core",
    val toolId: String? = null,
    val isError: Boolean = false
)

@Entity(tableName = "generated_images")
data class GeneratedImageEntity(
    @PrimaryKey val id: String,
    val prompt: String,
    val imageBase64: String,
    val aspectRatio: String = "1:1",
    val style: String = "Futuristic Cyberpunk",
    val quality: String = "1K",
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
