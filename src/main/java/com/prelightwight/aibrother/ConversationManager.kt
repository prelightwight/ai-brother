package com.prelightwight.aibrother

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class ConversationManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "ConversationManager"
        private const val PREFS_NAME = "conversation_storage"
        private const val KEY_CONVERSATIONS = "stored_conversations"
        private const val KEY_CURRENT_CONVERSATION_ID = "current_conversation_id"
        private const val MAX_CONVERSATIONS = 50 // Limit to prevent storage bloat
        
        @Volatile
        private var INSTANCE: ConversationManager? = null
        
        fun getInstance(context: Context): ConversationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ConversationManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private var currentConversationId: String = generateConversationId()
    
    init {
        // Load or create current conversation ID
        currentConversationId = prefs.getString(KEY_CURRENT_CONVERSATION_ID, generateConversationId()) 
            ?: generateConversationId()
    }
    
    fun addMessage(message: ChatMessage) {
        val conversations = getAllConversations().toMutableMap()
        val conversationId = message.conversationId
        
        // Get existing conversation or create new one
        val existingMessages = conversations[conversationId]?.toMutableList() ?: mutableListOf()
        existingMessages.add(message)
        
        // Update conversation
        conversations[conversationId] = existingMessages
        
        // Limit total conversations
        if (conversations.size > MAX_CONVERSATIONS) {
            val oldestConversation = conversations.keys.minByOrNull { it } 
            oldestConversation?.let { conversations.remove(it) }
        }
        
        saveConversations(conversations)
        Log.d(TAG, "Added message to conversation $conversationId")
    }
    
    fun getCurrentConversation(): List<ChatMessage> {
        return getConversation(currentConversationId)
    }
    
    fun getConversation(conversationId: String): List<ChatMessage> {
        val conversations = getAllConversations()
        return conversations[conversationId] ?: emptyList()
    }
    
    fun getAllConversations(): Map<String, List<ChatMessage>> {
        val conversationsJson = prefs.getString(KEY_CONVERSATIONS, "{}")
        return try {
            val type = object : TypeToken<Map<String, List<ChatMessage>>>() {}.type
            gson.fromJson(conversationsJson, type) ?: emptyMap()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading conversations", e)
            emptyMap()
        }
    }
    
    fun getConversationSummaries(): List<BrainFragment.ConversationSummary> {
        val conversations = getAllConversations()
        return conversations.map { (id, messages) ->
            generateConversationSummary(id, messages)
        }.sortedByDescending { parseTimestamp(it.timestamp) }
    }
    
    fun startNewConversation(): String {
        currentConversationId = generateConversationId()
        prefs.edit().putString(KEY_CURRENT_CONVERSATION_ID, currentConversationId).apply()
        Log.d(TAG, "Started new conversation: $currentConversationId")
        return currentConversationId
    }
    
    fun deleteConversation(conversationId: String) {
        val conversations = getAllConversations().toMutableMap()
        conversations.remove(conversationId)
        saveConversations(conversations)
        
        // If we deleted the current conversation, start a new one
        if (conversationId == currentConversationId) {
            startNewConversation()
        }
        
        Log.d(TAG, "Deleted conversation: $conversationId")
    }
    
    fun clearAllConversations() {
        prefs.edit().clear().apply()
        startNewConversation()
        Log.d(TAG, "Cleared all conversations")
    }
    
    fun clearOldConversations(daysOld: Int = 7) {
        val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
        val conversations = getAllConversations().toMutableMap()
        
        val conversationsToDelete = conversations.filter { (id, messages) ->
            val lastMessageTime = messages.maxOfOrNull { it.timestamp } ?: 0
            lastMessageTime < cutoffTime
        }
        
        conversationsToDelete.keys.forEach { id ->
            conversations.remove(id)
        }
        
        saveConversations(conversations)
        Log.d(TAG, "Cleared ${conversationsToDelete.size} old conversations")
    }
    
    fun getStorageStats(): StorageStats {
        val conversations = getAllConversations()
        val totalMessages = conversations.values.sumOf { it.size }
        val totalSize = calculateStorageSize(conversations)
        
        return StorageStats(
            conversationCount = conversations.size,
            totalMessages = totalMessages,
            storageSizeKB = totalSize,
            oldestConversation = findOldestConversationDate(conversations),
            newestConversation = findNewestConversationDate(conversations)
        )
    }
    
    private fun saveConversations(conversations: Map<String, List<ChatMessage>>) {
        val conversationsJson = gson.toJson(conversations)
        prefs.edit().putString(KEY_CONVERSATIONS, conversationsJson).apply()
    }
    
    private fun generateConversationId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (1000..9999).random()
        return "conv_${timestamp}_${random}"
    }
    
    private fun generateConversationSummary(id: String, messages: List<ChatMessage>): BrainFragment.ConversationSummary {
        if (messages.isEmpty()) {
            return BrainFragment.ConversationSummary(
                "Empty Conversation",
                "No messages yet",
                "Unknown",
                0
            )
        }
        
        val title = generateConversationTitle(messages)
        val summary = generateConversationDescription(messages)
        val timestamp = ChatMessage.formatTimestamp(messages.maxOfOrNull { it.timestamp } ?: 0)
        
        return BrainFragment.ConversationSummary(
            title = title,
            summary = summary,
            timestamp = timestamp,
            messageCount = messages.size
        )
    }
    
    private fun generateConversationTitle(messages: List<ChatMessage>): String {
        // Find the first substantial user message
        val firstUserMessage = messages.find { it.isUser && it.content.length > 10 }?.content
        
        return when {
            firstUserMessage != null -> {
                val words = firstUserMessage.split(" ").take(4)
                val title = words.joinToString(" ")
                if (title.length > 30) title.take(27) + "..." else title
            }
            messages.isNotEmpty() -> "Conversation ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(messages.first().timestamp))}"
            else -> "Empty Conversation"
        }
    }
    
    private fun generateConversationDescription(messages: List<ChatMessage>): String {
        val userMessages = messages.filter { it.isUser }
        val aiMessages = messages.filter { !it.isUser }
        
        val topics = extractTopics(userMessages.map { it.content })
        
        return when {
            topics.isNotEmpty() -> "Discussed ${topics.take(3).joinToString(", ")}"
            userMessages.isNotEmpty() -> "Chat conversation with ${userMessages.size} exchanges"
            else -> "AI Brother conversation"
        }
    }
    
    private fun extractTopics(userMessages: List<String>): List<String> {
        val topicKeywords = mapOf(
            "programming" to listOf("code", "programming", "software", "development", "app", "android", "java", "kotlin"),
            "science" to listOf("science", "research", "study", "experiment", "theory", "physics", "chemistry"),
            "cooking" to listOf("cooking", "recipe", "food", "cook", "bake", "ingredient", "meal"),
            "weather" to listOf("weather", "temperature", "rain", "sunny", "cloudy", "forecast"),
            "travel" to listOf("travel", "trip", "vacation", "visit", "journey", "flight", "hotel"),
            "health" to listOf("health", "medical", "doctor", "medicine", "symptom", "exercise", "fitness"),
            "technology" to listOf("technology", "tech", "AI", "artificial", "intelligence", "computer", "internet"),
            "education" to listOf("learn", "study", "education", "school", "university", "teaching", "knowledge")
        )
        
        val allText = userMessages.joinToString(" ").lowercase()
        
        return topicKeywords.filter { (_, keywords) ->
            keywords.any { keyword -> allText.contains(keyword) }
        }.keys.toList()
    }
    
    private fun calculateStorageSize(conversations: Map<String, List<ChatMessage>>): Long {
        val json = gson.toJson(conversations)
        return json.length.toLong() / 1024 // Convert to KB
    }
    
    private fun findOldestConversationDate(conversations: Map<String, List<ChatMessage>>): String {
        val oldestTimestamp = conversations.values
            .flatten()
            .minOfOrNull { it.timestamp } ?: return "Unknown"
        
        return ChatMessage.formatTimestamp(oldestTimestamp)
    }
    
    private fun findNewestConversationDate(conversations: Map<String, List<ChatMessage>>): String {
        val newestTimestamp = conversations.values
            .flatten()
            .maxOfOrNull { it.timestamp } ?: return "Unknown"
        
        return ChatMessage.formatTimestamp(newestTimestamp)
    }
    
    private fun parseTimestamp(timeString: String): Long {
        return when {
            timeString.contains("min ago") -> {
                val minutes = timeString.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0
                System.currentTimeMillis() - (minutes * 60 * 1000)
            }
            timeString.contains("h ago") -> {
                val hours = timeString.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0
                System.currentTimeMillis() - (hours * 60 * 60 * 1000)
            }
            timeString.contains("d ago") -> {
                val days = timeString.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0
                System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000)
            }
            timeString == "Just now" -> System.currentTimeMillis()
            else -> 0L
        }
    }
    
    data class StorageStats(
        val conversationCount: Int,
        val totalMessages: Int,
        val storageSizeKB: Long,
        val oldestConversation: String,
        val newestConversation: String
    )
}