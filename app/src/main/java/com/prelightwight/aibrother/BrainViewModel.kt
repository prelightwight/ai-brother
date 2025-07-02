package com.prelightwight.aibrother.brain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prelightwight.aibrother.data.MemoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BrainViewModel(private val repository: BrainRepository) : ViewModel() {
    
    private val _memories = MutableStateFlow<List<MemoryEntity>>(emptyList())
    val memories: StateFlow<List<MemoryEntity>> = _memories.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<MemoryEntity>>(emptyList())
    val searchResults: StateFlow<List<MemoryEntity>> = _searchResults.asStateFlow()

    init {
        loadMemories()
    }

    private fun loadMemories() {
        viewModelScope.launch {
            repository.getAllMemories().collect { memoryList ->
                _memories.value = memoryList
            }
        }
    }

    fun addMemory(content: String) {
        if (content.isBlank()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.insertMemory(content.trim())
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
        }
    }
    
    fun deleteAllMemories() {
        viewModelScope.launch {
            repository.deleteAllMemories()
        }
    }
    
    fun searchMemories(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            try {
                val results = repository.searchMemories(query)
                _searchResults.value = results
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            }
        }
    }
    
    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }
}
