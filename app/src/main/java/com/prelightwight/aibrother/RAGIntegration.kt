package com.prelightwight.aibrother.rag

import android.content.Context
import android.util.Log
import com.prelightwight.aibrother.data.*
import com.prelightwight.aibrother.llm.LlamaRunner
import com.prelightwight.aibrother.llm.InferenceConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * RAG (Retrieval-Augmented Generation) Integration
 * Connects the Brain memory system with LLM conversations for enhanced context-aware responses
 */
class RAGIntegration(
    private val memoryDao: MemoryDao,
    private val conversationDao: ConversationDao
) {
    private val tag = "RAGIntegration"
    
    data class RetrievedContext(
        val memories: List<MemoryEntity>,
        val conversations: List<ConversationEntity>,
        val relevanceScore: Float,
        val contextPrompt: String
    )
    
    /**
     * Enhanced inference with RAG support
     */
    suspend fun enhancedInference(
        userMessage: String,
        conversationId: String,
        config: InferenceConfig = InferenceConfig(),
        enableRAG: Boolean = true
    ): String = withContext(Dispatchers.IO) {
        
        try {
            // Store user message
            val userMessageEntity = ConversationEntity(
                conversationId = conversationId,
                role = "user",
                content = userMessage,
                timestamp = System.currentTimeMillis()
            )
            conversationDao.insertMessage(userMessageEntity)
            
            // Build prompt with RAG context if enabled
            val finalPrompt = if (enableRAG) {
                val context = retrieveRelevantContext(userMessage)
                buildRAGPrompt(userMessage, context)
            } else {
                userMessage
            }
            
            Log.d(tag, "Enhanced inference with RAG: ${enableRAG}")
            Log.d(tag, "Final prompt length: ${finalPrompt.length}")
            
            // Get AI response
            val aiResponse = LlamaRunner.infer(finalPrompt, config)
            
            // Store AI response
            val aiMessageEntity = ConversationEntity(
                conversationId = conversationId,
                role = "assistant",
                content = aiResponse,
                timestamp = System.currentTimeMillis()
            )
            conversationDao.insertMessage(aiMessageEntity)
            
            // Auto-save important information to brain
            if (shouldSaveToMemory(userMessage, aiResponse)) {
                saveImportantMemory(userMessage, aiResponse, conversationId)
            }
            
            return@withContext aiResponse
            
        } catch (e: Exception) {
            Log.e(tag, "Error in enhanced inference", e)
            return@withContext "Error: Unable to process request - ${e.message}"
        }
    }
    
    /**
     * Retrieve relevant context from brain and conversation history
     */
    private suspend fun retrieveRelevantContext(query: String): RetrievedContext {
        val searchTerms = extractSearchTerms(query)
        
        // Search memories
        val relevantMemories = mutableListOf<MemoryEntity>()
        searchTerms.forEach { term ->
            relevantMemories.addAll(memoryDao.searchMemories(term))
        }
        
        // Search conversation history
        val relevantConversations = mutableListOf<ConversationEntity>()
        searchTerms.forEach { term ->
            relevantConversations.addAll(conversationDao.searchConversations(term))
        }
        
        // Remove duplicates and sort by relevance
        val uniqueMemories = relevantMemories.distinctBy { it.id }.take(5)
        val uniqueConversations = relevantConversations.distinctBy { it.id }.take(3)
        
        // Calculate relevance score
        val relevanceScore = calculateRelevanceScore(query, uniqueMemories, uniqueConversations)
        
        // Build context prompt
        val contextPrompt = buildContextPrompt(uniqueMemories, uniqueConversations)
        
        return RetrievedContext(
            memories = uniqueMemories,
            conversations = uniqueConversations,
            relevanceScore = relevanceScore,
            contextPrompt = contextPrompt
        )
    }
    
    /**
     * Build enhanced prompt with RAG context
     */
    private fun buildRAGPrompt(userMessage: String, context: RetrievedContext): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        
        return buildString {
            appendLine("You are AI Brother, a helpful and knowledgeable AI assistant.")
            appendLine("You have access to the user's personal knowledge base and conversation history.")
            appendLine("Use this context to provide more personalized and informed responses.")
            appendLine()
            
            if (context.memories.isNotEmpty()) {
                appendLine("=== RELEVANT MEMORIES ===")
                context.memories.forEach { memory ->
                    appendLine("Memory (${dateFormat.format(Date(memory.timestamp))}): ${memory.title}")
                    appendLine("Content: ${memory.content}")
                    if (memory.tags.isNotBlank()) {
                        appendLine("Tags: ${memory.tags}")
                    }
                    appendLine()
                }
            }
            
            if (context.conversations.isNotEmpty()) {
                appendLine("=== RELEVANT PAST CONVERSATIONS ===")
                context.conversations.forEach { conv ->
                    appendLine("${conv.role.uppercase()} (${dateFormat.format(Date(conv.timestamp))}): ${conv.content}")
                }
                appendLine()
            }
            
            appendLine("=== CURRENT REQUEST ===")
            appendLine("User: $userMessage")
            appendLine()
            appendLine("Please provide a helpful response based on the user's request and the relevant context above.")
            appendLine("If the context is relevant, reference it naturally in your response.")
            appendLine("If no relevant context is found, provide a general helpful response.")
        }
    }
    
    /**
     * Extract search terms from user query
     */
    private fun extractSearchTerms(query: String): List<String> {
        // Simple keyword extraction - in production, use NLP libraries
        val stopWords = setOf("the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "is", "are", "was", "were", "be", "been", "have", "has", "had", "do", "does", "did", "will", "would", "could", "should", "can", "may", "might", "what", "where", "when", "why", "how", "who", "which")
        
        return query.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 && !stopWords.contains(it) }
            .distinct()
            .take(10) // Limit search terms
    }
    
    /**
     * Calculate relevance score for retrieved context
     */
    private fun calculateRelevanceScore(
        query: String,
        memories: List<MemoryEntity>,
        conversations: List<ConversationEntity>
    ): Float {
        if (memories.isEmpty() && conversations.isEmpty()) return 0.0f
        
        val queryTerms = extractSearchTerms(query).toSet()
        var totalMatches = 0
        var totalTerms = 0
        
        // Score memories
        memories.forEach { memory ->
            val memoryTerms = extractSearchTerms("${memory.title} ${memory.content} ${memory.tags}").toSet()
            totalMatches += queryTerms.intersect(memoryTerms).size
            totalTerms += memoryTerms.size
        }
        
        // Score conversations
        conversations.forEach { conv ->
            val convTerms = extractSearchTerms(conv.content).toSet()
            totalMatches += queryTerms.intersect(convTerms).size
            totalTerms += convTerms.size
        }
        
        return if (totalTerms > 0) totalMatches.toFloat() / totalTerms else 0.0f
    }
    
    /**
     * Build context prompt from retrieved information
     */
    private fun buildContextPrompt(
        memories: List<MemoryEntity>,
        conversations: List<ConversationEntity>
    ): String {
        return buildString {
            if (memories.isNotEmpty()) {
                appendLine("Relevant memories:")
                memories.forEach { memory ->
                    appendLine("- ${memory.title}: ${memory.content.take(100)}...")
                }
            }
            
            if (conversations.isNotEmpty()) {
                appendLine("Recent relevant conversations:")
                conversations.take(3).forEach { conv ->
                    appendLine("- ${conv.role}: ${conv.content.take(100)}...")
                }
            }
        }
    }
    
    /**
     * Determine if conversation should be saved to memory
     */
    private fun shouldSaveToMemory(userMessage: String, aiResponse: String): Boolean {
        val triggers = listOf(
            "remember", "save", "important", "note", "remind", "keep", "store",
            "fact", "information", "learn", "knowledge", "understand", "explain"
        )
        
        val combinedText = "$userMessage $aiResponse".lowercase()
        return triggers.any { combinedText.contains(it) } || 
               userMessage.length > 50 || // Longer messages are likely important
               aiResponse.length > 200    // Detailed responses worth saving
    }
    
    /**
     * Save important conversation as memory
     */
    private suspend fun saveImportantMemory(
        userMessage: String,
        aiResponse: String,
        conversationId: String
    ) {
        try {
            val memory = MemoryEntity(
                title = "Conversation: ${userMessage.take(50)}${if (userMessage.length > 50) "..." else ""}",
                content = "Q: $userMessage\n\nA: $aiResponse",
                tags = "conversation, auto-saved, $conversationId",
                type = "memory",
                timestamp = System.currentTimeMillis()
            )
            
            memoryDao.insertMemory(memory)
            Log.d(tag, "Auto-saved conversation to memory")
            
        } catch (e: Exception) {
            Log.e(tag, "Failed to save conversation to memory", e)
        }
    }
    
    /**
     * Get conversation summary for a conversation ID
     */
    suspend fun getConversationSummary(conversationId: String): String = withContext(Dispatchers.IO) {
        try {
            val messages = conversationDao.getConversationById(conversationId)
            // Since this returns Flow, we need to collect it differently in practice
            // For now, return a placeholder
            "Conversation summary for $conversationId"
        } catch (e: Exception) {
            "Unable to generate summary"
        }
    }
    
    /**
     * Search across all knowledge sources
     */
    suspend fun searchKnowledge(query: String): RAGSearchResult = withContext(Dispatchers.IO) {
        val context = retrieveRelevantContext(query)
        
        RAGSearchResult(
            query = query,
            memories = context.memories,
            conversations = context.conversations,
            relevanceScore = context.relevanceScore,
            totalResults = context.memories.size + context.conversations.size
        )
    }
    
    data class RAGSearchResult(
        val query: String,
        val memories: List<MemoryEntity>,
        val conversations: List<ConversationEntity>,
        val relevanceScore: Float,
        val totalResults: Int
    )
}