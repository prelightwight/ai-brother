package com.prelightwight.aibrother.brain

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MemoryDao {
    @Insert
    suspend fun insert(memory: MemoryEntity)

    @Query("SELECT * FROM memory ORDER BY id DESC")
    suspend fun getAll(): List<MemoryEntity>
}
