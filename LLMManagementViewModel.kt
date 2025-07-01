package com.prelightwight.aibrother.llm

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LLMManagementViewModel(application: Application) : AndroidViewModel(application) {
    
    private val llmManager = LLMManager.getInstance(application)
    
    val state: StateFlow<LLMManagerState> = llmManager.state
    val downloadProgress: StateFlow<Map<String, DownloadProgress>> = llmManager.downloadProgress
    
    fun initialize() {
        viewModelScope.launch {
            llmManager.initialize()
        }
    }
    
    fun downloadModel(model: LLMModel) {
        viewModelScope.launch {
            try {
                llmManager.downloadModel(model)
            } catch (e: Exception) {
                // Error is handled in the LLMManager and reflected in downloadProgress
            }
        }
    }
    
    fun cancelDownload(modelId: String) {
        viewModelScope.launch {
            llmManager.cancelDownload(modelId)
        }
    }
    
    fun setActiveModel(model: LocalModel) {
        viewModelScope.launch {
            llmManager.setActiveModel(model)
        }
    }
    
    fun deleteModel(model: LocalModel) {
        viewModelScope.launch {
            llmManager.deleteModel(model)
        }
    }
    
    fun addLocalModel(uri: Uri, displayName: String) {
        viewModelScope.launch {
            llmManager.addLocalModelFromFile(uri, displayName)
        }
    }
    
    fun isModelDownloaded(model: LLMModel): Boolean {
        return llmManager.isModelDownloaded(model)
    }
    
    override fun onCleared() {
        super.onCleared()
        llmManager.cleanup()
    }
}