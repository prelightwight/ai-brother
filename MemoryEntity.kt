package com.prelightwight.aibrother.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "memory" // "memory" or "conversation"
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val conversationId: String, // Group messages by conversation
    val isError: Boolean = false
)
