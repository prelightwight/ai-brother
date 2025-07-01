package com.prelightwight.aibrother.brain

class BrainRepository(private val dao: MemoryDao) {
    suspend fun insertMemory(memory: MemoryEntity) {
        dao.insert(memory)
    }

    suspend fun getAllMemories(): List<MemoryEntity> {
        return dao.getAll()
    }
}
