package com.prelightwight.aibrother.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MemoryEntity::class, ConversationEntity::class],
    version = 2,
    exportSchema = false
)
abstract class MemoryDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun conversationDao(): ConversationDao

    companion object {
        @Volatile
        private var INSTANCE: MemoryDatabase? = null

        fun getDatabase(context: Context): MemoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MemoryDatabase::class.java,
                    "memory_database"
                )
                .fallbackToDestructiveMigration() // For development - in production, use proper migrations
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
