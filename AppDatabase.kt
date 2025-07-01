package com.prelightwight.aibrother.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import java.util.*

// Conversation Entity
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: String? = null // JSON for additional data
)

// File Processing Entity
@Entity(tableName = "processed_files")
data class ProcessedFileEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val filename: String,
    val originalUri: String,
    val content: String,
    val summary: String,
    val fileType: String,
    val fileSize: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val tags: String = "", // Comma-separated tags
    val isAnalyzed: Boolean = false
)

// Knowledge Base Entity
@Entity(tableName = "knowledge_entries")
data class KnowledgeEntryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val category: String,
    val tags: String = "", // Comma-separated tags
    val isBuiltIn: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val searchVector: String = "" // For full-text search
)

// Image Analysis Entity
@Entity(tableName = "analyzed_images")
data class AnalyzedImageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val filename: String,
    val originalUri: String,
    val localPath: String,
    val description: String,
    val detectedObjects: String = "", // JSON array
    val dominantColors: String = "", // JSON array
    val extractedText: String? = null,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val thumbnailPath: String? = null
)

// Search History Entity
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val query: String,
    val source: String, // "CLEARNET" or "TOR"
    val resultCount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val results: String = "" // JSON of search results
)

// Voice Notes Entity
@Entity(tableName = "voice_notes")
data class VoiceNoteEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val filePath: String,
    val transcription: String? = null,
    val duration: Long, // in milliseconds
    val timestamp: Long = System.currentTimeMillis(),
    val isTranscribed: Boolean = false
)

// Settings Entity
@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val key: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)

// DAOs
@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY timestamp DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>
    
    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversation(id: String): ConversationEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)
    
    @Delete
    suspend fun deleteConversation(conversation: ConversationEntity)
    
    @Query("UPDATE conversations SET isArchived = :archived WHERE id = :id")
    suspend fun updateArchiveStatus(id: String, archived: Boolean)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
    
    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)
    
    @Query("SELECT * FROM messages WHERE content LIKE '%' || :searchQuery || '%' ORDER BY timestamp DESC")
    suspend fun searchMessages(searchQuery: String): List<MessageEntity>
}

@Dao
interface ProcessedFileDao {
    @Query("SELECT * FROM processed_files ORDER BY timestamp DESC")
    fun getAllFiles(): Flow<List<ProcessedFileEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: ProcessedFileEntity)
    
    @Delete
    suspend fun deleteFile(file: ProcessedFileEntity)
    
    @Query("SELECT * FROM processed_files WHERE filename LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%'")
    suspend fun searchFiles(query: String): List<ProcessedFileEntity>
    
    @Query("UPDATE processed_files SET isAnalyzed = 1 WHERE id = :id")
    suspend fun markAsAnalyzed(id: String)
}

@Dao
interface KnowledgeDao {
    @Query("SELECT * FROM knowledge_entries ORDER BY timestamp DESC")
    fun getAllKnowledge(): Flow<List<KnowledgeEntryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnowledge(knowledge: KnowledgeEntryEntity)
    
    @Delete
    suspend fun deleteKnowledge(knowledge: KnowledgeEntryEntity)
    
    @Update
    suspend fun updateKnowledge(knowledge: KnowledgeEntryEntity)
    
    @Query("SELECT * FROM knowledge_entries WHERE category = :category ORDER BY timestamp DESC")
    fun getKnowledgeByCategory(category: String): Flow<List<KnowledgeEntryEntity>>
    
    @Query("SELECT * FROM knowledge_entries WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    suspend fun searchKnowledge(query: String): List<KnowledgeEntryEntity>
}

@Dao
interface AnalyzedImageDao {
    @Query("SELECT * FROM analyzed_images ORDER BY timestamp DESC")
    fun getAllImages(): Flow<List<AnalyzedImageEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: AnalyzedImageEntity)
    
    @Delete
    suspend fun deleteImage(image: AnalyzedImageEntity)
    
    @Query("SELECT * FROM analyzed_images WHERE description LIKE '%' || :query || '%' OR detectedObjects LIKE '%' || :query || '%' OR extractedText LIKE '%' || :query || '%'")
    suspend fun searchImages(query: String): List<AnalyzedImageEntity>
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 50")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistoryEntity)
    
    @Query("DELETE FROM search_history WHERE timestamp < :cutoffTime")
    suspend fun deleteOldSearches(cutoffTime: Long)
    
    @Query("SELECT DISTINCT query FROM search_history WHERE query LIKE '%' || :prefix || '%' ORDER BY timestamp DESC LIMIT 10")
    suspend fun getQuerySuggestions(prefix: String): List<String>
}

@Dao
interface VoiceNoteDao {
    @Query("SELECT * FROM voice_notes ORDER BY timestamp DESC")
    fun getAllVoiceNotes(): Flow<List<VoiceNoteEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceNote(note: VoiceNoteEntity)
    
    @Delete
    suspend fun deleteVoiceNote(note: VoiceNoteEntity)
    
    @Query("UPDATE voice_notes SET transcription = :transcription, isTranscribed = 1 WHERE id = :id")
    suspend fun updateTranscription(id: String, transcription: String)
}

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE key = :key")
    suspend fun getSetting(key: String): AppSettingsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingsEntity)
    
    @Query("SELECT value FROM app_settings WHERE key = :key")
    suspend fun getSettingValue(key: String): String?
}

// Database
@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        ProcessedFileEntity::class,
        KnowledgeEntryEntity::class,
        AnalyzedImageEntity::class,
        SearchHistoryEntity::class,
        VoiceNoteEntity::class,
        AppSettingsEntity::class,
        MemoryEntity::class // Keep existing entity
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun processedFileDao(): ProcessedFileDao
    abstract fun knowledgeDao(): KnowledgeDao
    abstract fun analyzedImageDao(): AnalyzedImageDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun voiceNoteDao(): VoiceNoteDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun memoryDao(): MemoryDao // Keep existing

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_brother_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration() // For development
                .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new tables for enhanced functionality
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `conversations` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `title` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `isArchived` INTEGER NOT NULL
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `messages` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `conversationId` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `isFromUser` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `metadata` TEXT
                    )
                """)
                
                // Add other tables...
            }
        }
    }
}

// Type Converters for complex data types
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }
}