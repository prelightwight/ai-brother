package com.prelightwight.aibrother.conversation

import android.content.Context
import android.util.Log
import com.prelightwight.aibrother.data.*
import com.prelightwight.aibrother.rag.RAGIntegration
import com.prelightwight.aibrother.llm.InferenceConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Manages conversation persistence and RAG-enhanced chat
 */
class ConversationManager(
    private val database: MemoryDatabase,
    private val context: Context
) {
    private val tag = "ConversationManager"
    private val ragIntegration by lazy { 
        RAGIntegration(database.memoryDao(), database.conversationDao()) 
    }
    
    data class ConversationSummary(
        val id: String,
        val title: String,
        val lastMessage: String,
        val lastMessageTime: Long,
        val messageCount: Int
    )
    
    /**
     * Send a message with RAG enhancement and conversation persistence
     */
    suspend fun sendMessage(
        conversationId: String,
        message: String,
        config: InferenceConfig = InferenceConfig(),
        enableRAG: Boolean = true
    ): String = withContext(Dispatchers.IO) {
        
        try {
            Log.d(tag, "Sending message in conversation: $conversationId")
            Log.d(tag, "RAG enabled: $enableRAG")
            
            // Use RAG integration for enhanced response
            val response = ragIntegration.enhancedInference(
                userMessage = message,
                conversationId = conversationId,
                config = config,
                enableRAG = enableRAG
            )
            
            Log.d(tag, "Message sent successfully")
            return@withContext response
            
        } catch (e: Exception) {
            Log.e(tag, "Error sending message", e)
            throw e
        }
    }
    
    /**
     * Create a new conversation
     */
    suspend fun createConversation(title: String? = null): String = withContext(Dispatchers.IO) {
        val conversationId = UUID.randomUUID().toString()
        val conversationTitle = title ?: "New Conversation"
        
        // Insert initial system message
        val systemMessage = ConversationEntity(
            conversationId = conversationId,
            role = "system",
            content = "Conversation started: $conversationTitle",
            timestamp = System.currentTimeMillis()
        )
        
        database.conversationDao().insertMessage(systemMessage)
        Log.d(tag, "Created new conversation: $conversationId")
        
        return@withContext conversationId
    }
    
    /**
     * Get all conversation summaries
     */
    suspend fun getConversationSummaries(): List<ConversationSummary> = withContext(Dispatchers.IO) {
        try {
            val conversationIds = database.conversationDao().getAllConversationIds()
            
            conversationIds.map { id ->
                val messages = database.conversationDao().getConversationById(id).first()
                val userMessages = messages.filter { it.role == "user" }
                val lastMessage = messages.lastOrNull()
                
                val title = when {
                    userMessages.isNotEmpty() -> {
                        val firstUserMessage = userMessages.first().content
                        if (firstUserMessage.length > 30) {
                            firstUserMessage.take(30) + "..."
                        } else firstUserMessage
                    }
                    else -> "Empty Conversation"
                }
                
                ConversationSummary(
                    id = id,
                    title = title,
                    lastMessage = lastMessage?.content ?: "",
                    lastMessageTime = lastMessage?.timestamp ?: 0L,
                    messageCount = messages.size
                )
            }.sortedByDescending { it.lastMessageTime }
            
        } catch (e: Exception) {
            Log.e(tag, "Error getting conversation summaries", e)
            emptyList()
        }
    }
    
    /**
     * Get full conversation history
     */
    fun getConversationHistory(conversationId: String): Flow<List<ConversationEntity>> {
        return database.conversationDao().getConversationById(conversationId)
    }
    
    /**
     * Delete a conversation
     */
    suspend fun deleteConversation(conversationId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            database.conversationDao().deleteConversation(conversationId)
            Log.d(tag, "Deleted conversation: $conversationId")
            true
        } catch (e: Exception) {
            Log.e(tag, "Error deleting conversation", e)
            false
        }
    }
    
    /**
     * Clear all conversations
     */
    suspend fun clearAllConversations(): Boolean = withContext(Dispatchers.IO) {
        try {
            database.conversationDao().deleteAllConversations()
            Log.d(tag, "Cleared all conversations")
            true
        } catch (e: Exception) {
            Log.e(tag, "Error clearing conversations", e)
            false
        }
    }
    
    /**
     * Search across all conversations
     */
    suspend fun searchConversations(query: String): List<ConversationEntity> = withContext(Dispatchers.IO) {
        try {
            database.conversationDao().searchConversations(query)
        } catch (e: Exception) {
            Log.e(tag, "Error searching conversations", e)
            emptyList()
        }
    }
    
    /**
     * Get conversation statistics
     */
    suspend fun getConversationStats(): ConversationStats = withContext(Dispatchers.IO) {
        try {
            val totalMessages = database.conversationDao().getConversationCount()
            val conversationIds = database.conversationDao().getAllConversationIds()
            
            ConversationStats(
                totalConversations = conversationIds.size,
                totalMessages = totalMessages,
                averageMessagesPerConversation = if (conversationIds.isNotEmpty()) {
                    totalMessages.toFloat() / conversationIds.size
                } else 0f
            )
        } catch (e: Exception) {
            Log.e(tag, "Error getting conversation stats", e)
            ConversationStats(0, 0, 0f)
        }
    }
    
    /**
     * Export conversation to text
     */
    suspend fun exportConversation(conversationId: String): String = withContext(Dispatchers.IO) {
        try {
            val messages = database.conversationDao().getConversationById(conversationId).first()
            val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            
            buildString {
                appendLine("=== AI Brother Conversation Export ===")
                appendLine("Conversation ID: $conversationId")
                appendLine("Exported: ${dateFormat.format(Date())}")
                appendLine("Total Messages: ${messages.size}")
                appendLine()
                
                messages.forEach { message ->
                    val timestamp = dateFormat.format(Date(message.timestamp))
                    val role = when (message.role) {
                        "user" -> "You"
                        "assistant" -> "AI Brother"
                        "system" -> "System"
                        else -> message.role.uppercase()
                    }
                    
                    appendLine("[$timestamp] $role:")
                    appendLine(message.content)
                    appendLine()
                }
                
                appendLine("=== End of Conversation ===")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error exporting conversation", e)
            "Error exporting conversation: ${e.message}"
        }
    }
    
    /**
     * Generate conversation title from content
     */
    suspend fun generateConversationTitle(conversationId: String): String = withContext(Dispatchers.IO) {
        try {
            val messages = database.conversationDao().getConversationById(conversationId).first()
            val userMessages = messages.filter { it.role == "user" }
            
            when {
                userMessages.isEmpty() -> "Empty Conversation"
                userMessages.size == 1 -> {
                    val content = userMessages.first().content
                    if (content.length > 50) content.take(50) + "..." else content
                }
                else -> {
                    // Create title from first user message
                    val firstMessage = userMessages.first().content
                    val words = firstMessage.split(" ").take(8).joinToString(" ")
                    if (words.length > 40) words.take(40) + "..." else words
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error generating conversation title", e)
            "Conversation"
        }
    }
    
    data class ConversationStats(
        val totalConversations: Int,
        val totalMessages: Int,
        val averageMessagesPerConversation: Float
    )
}