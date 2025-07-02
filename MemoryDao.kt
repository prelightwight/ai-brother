package com.prelightwight.aibrother.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    // Memory operations
    @Query("SELECT * FROM memories WHERE type = 'memory' ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Insert
    suspend fun insertMemory(memory: MemoryEntity)

    @Delete
    suspend fun deleteMemory(memory: MemoryEntity)
    
    @Query("DELETE FROM memories WHERE type = 'memory'")
    suspend fun deleteAllMemories()
    
    @Query("SELECT * FROM memories WHERE content LIKE '%' || :query || '%' AND type = 'memory'")
    suspend fun searchMemories(query: String): List<MemoryEntity>
}

@Dao
interface ConversationDao {
    // Conversation operations
    @Query("SELECT * FROM conversations ORDER BY timestamp ASC")
    fun getAllConversations(): Flow<List<ConversationEntity>>
    
    @Query("SELECT * FROM conversations WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getConversationById(conversationId: String): Flow<List<ConversationEntity>>
    
    @Query("SELECT DISTINCT conversationId FROM conversations ORDER BY timestamp DESC")
    suspend fun getAllConversationIds(): List<String>

    @Insert
    suspend fun insertMessage(message: ConversationEntity)
    
    @Insert
    suspend fun insertMessages(messages: List<ConversationEntity>)

    @Delete
    suspend fun deleteMessage(message: ConversationEntity)
    
    @Query("DELETE FROM conversations WHERE conversationId = :conversationId")
    suspend fun deleteConversation(conversationId: String)
    
    @Query("DELETE FROM conversations")
    suspend fun deleteAllConversations()
    
    @Query("SELECT COUNT(*) FROM conversations")
    suspend fun getConversationCount(): Int
    
    @Query("SELECT * FROM conversations WHERE content LIKE '%' || :query || '%'")
    suspend fun searchConversations(query: String): List<ConversationEntity>
}
