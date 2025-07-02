package com.prelightwight.aibrother.brain

import com.prelightwight.aibrother.data.MemoryDao
import com.prelightwight.aibrother.data.MemoryEntity
import com.prelightwight.aibrother.data.ConversationDao
import com.prelightwight.aibrother.data.ConversationEntity
import kotlinx.coroutines.flow.Flow

class BrainRepository(
    private val memoryDao: MemoryDao,
    private val conversationDao: ConversationDao
) {
    // Memory operations
    fun getAllMemories(): Flow<List<MemoryEntity>> = memoryDao.getAllMemories()

    suspend fun insertMemory(content: String) {
        memoryDao.insertMemory(
            MemoryEntity(
                content = content,
                type = "memory"
            )
        )
    }

    suspend fun deleteMemory(memory: MemoryEntity) {
        memoryDao.deleteMemory(memory)
    }
    
    suspend fun deleteAllMemories() {
        memoryDao.deleteAllMemories()
    }
    
    suspend fun searchMemories(query: String): List<MemoryEntity> {
        return memoryDao.searchMemories(query)
    }
    
    // Conversation operations
    fun getAllConversations(): Flow<List<ConversationEntity>> = conversationDao.getAllConversations()
    
    fun getConversationById(conversationId: String): Flow<List<ConversationEntity>> {
        return conversationDao.getConversationById(conversationId)
    }
    
    suspend fun getAllConversationIds(): List<String> {
        return conversationDao.getAllConversationIds()
    }
    
    suspend fun insertMessage(
        content: String,
        isUser: Boolean,
        conversationId: String,
        isError: Boolean = false
    ) {
        conversationDao.insertMessage(
            ConversationEntity(
                content = content,
                isUser = isUser,
                conversationId = conversationId,
                isError = isError
            )
        )
    }
    
    suspend fun insertMessages(messages: List<ConversationEntity>) {
        conversationDao.insertMessages(messages)
    }
    
    suspend fun deleteConversation(conversationId: String) {
        conversationDao.deleteConversation(conversationId)
    }
    
    suspend fun deleteAllConversations() {
        conversationDao.deleteAllConversations()
    }
    
    suspend fun getConversationCount(): Int {
        return conversationDao.getConversationCount()
    }
    
    suspend fun searchConversations(query: String): List<ConversationEntity> {
        return conversationDao.searchConversations(query)
    }
}
