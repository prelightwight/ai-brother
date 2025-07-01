package com.prelightwight.aibrother.brain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BrainViewModel(private val repository: BrainRepository) : ViewModel() {
    private val _memories = MutableStateFlow<List<MemoryEntity>>(emptyList())
    val memories: StateFlow<List<MemoryEntity>> = _memories

    init {
        loadMemories()
    }

    private fun loadMemories() {
        viewModelScope.launch {
            _memories.value = repository.getAllMemories()
        }
    }

    fun addMemory(text: String) {
        viewModelScope.launch {
            repository.insertMemory(MemoryEntity(text = text))
            loadMemories()
        }
    }
}
