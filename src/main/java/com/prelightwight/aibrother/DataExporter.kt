package com.prelightwight.aibrother

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object DataExporter {
    
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    
    data class ExportData(
        val exportVersion: String = "1.0",
        val exportDate: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
        val appVersion: String = "1.1.0",
        val conversations: Map<String, Any>? = null,
        val settings: Map<String, Any>? = null,
        val processedFiles: List<Map<String, Any>>? = null,
        val privacyInfo: Map<String, Any>? = null
    )
    
    suspend fun exportAllData(context: Context): String {
        val conversationManager = ConversationManager.getInstance(context)
        val fileProcessor = FileProcessor(context)
        
        val exportData = ExportData(
            conversations = exportConversationsData(context),
            settings = exportSettingsData(context),
            processedFiles = exportProcessedFilesData(context),
            privacyInfo = generatePrivacyData(context)
        )
        
        return gson.toJson(exportData)
    }
    
    suspend fun exportConversations(context: Context): String {
        val exportData = ExportData(
            conversations = exportConversationsData(context)
        )
        return gson.toJson(exportData)
    }
    
    suspend fun exportSettings(context: Context): String {
        val exportData = ExportData(
            settings = exportSettingsData(context)
        )
        return gson.toJson(exportData)
    }
    
    suspend fun exportFilesList(context: Context): String {
        val exportData = ExportData(
            processedFiles = exportProcessedFilesData(context)
        )
        return gson.toJson(exportData)
    }
    
    suspend fun generatePrivacyReport(context: Context): String {
        val privacyReport = ExportData(
            privacyInfo = generatePrivacyData(context)
        )
        return gson.toJson(privacyReport)
    }
    
    private fun exportConversationsData(context: Context): Map<String, Any> {
        val conversationManager = ConversationManager.getInstance(context)
        val allConversations = conversationManager.getAllConversations()
        val storageStats = conversationManager.getStorageStats()
        
        return mapOf(
            "total_conversations" to allConversations.size,
            "storage_stats" to mapOf(
                "total_messages" to storageStats.totalMessages,
                "storage_size_kb" to storageStats.storageSizeKB,
                "oldest_conversation" to storageStats.oldestConversation,
                "newest_conversation" to storageStats.newestConversation
            ),
            "conversations" to allConversations.map { (id, messages) ->
                mapOf(
                    "id" to id,
                    "message_count" to messages.size,
                    "created_date" to messages.firstOrNull()?.timestamp,
                    "last_updated" to messages.lastOrNull()?.timestamp,
                    "messages" to messages.map { message ->
                        mapOf(
                            "content" to message.content,
                            "is_user" to message.isUser,
                            "timestamp" to message.timestamp
                        )
                    }
                )
            }
        )
    }
    
    private fun exportSettingsData(context: Context): Map<String, Any> {
        val sharedPrefs = context.getSharedPreferences(SettingsFragment.PREFS_NAME, Context.MODE_PRIVATE)
        val allPreferences = sharedPrefs.all
        
        return mapOf(
            "app_settings" to allPreferences,
            "tutorial_settings" to getTutorialSettings(context),
            "privacy_settings" to getPrivacySettings(sharedPrefs)
        )
    }
    
    private fun exportProcessedFilesData(context: Context): List<Map<String, Any>> {
        val fileProcessor = FileProcessor(context)
        val processedImages = fileProcessor.getProcessedImages()
        
        return processedImages.map { image ->
            mapOf(
                "file_name" to image.fileName,
                "file_type" to image.fileType,
                "file_size_bytes" to image.fileSizeBytes,
                "processed_date" to image.processedDate,
                "is_analyzed" to image.isAnalyzed,
                "summary" to image.summary,
                "extracted_text" to image.extractedText,
                "metadata" to image.metadata
            )
        }
    }
    
    private fun generatePrivacyData(context: Context): Map<String, Any> {
        val sharedPrefs = context.getSharedPreferences(SettingsFragment.PREFS_NAME, Context.MODE_PRIVATE)
        val conversationManager = ConversationManager.getInstance(context)
        val fileProcessor = FileProcessor(context)
        
        val storageStats = conversationManager.getStorageStats()
        val processedImages = fileProcessor.getProcessedImages()
        
        return mapOf(
            "privacy_summary" to mapOf(
                "local_processing_enabled" to sharedPrefs.getBoolean(SettingsFragment.PREF_LOCAL_PROCESSING, true),
                "memory_enabled" to sharedPrefs.getBoolean(SettingsFragment.PREF_MEMORY_ENABLED, true),
                "auto_backup_enabled" to sharedPrefs.getBoolean(SettingsFragment.PREF_AUTO_BACKUP, true),
                "notifications_enabled" to sharedPrefs.getBoolean(SettingsFragment.PREF_NOTIFICATIONS, true)
            ),
            "data_summary" to mapOf(
                "total_conversations" to storageStats.totalMessages,
                "total_processed_files" to processedImages.size,
                "storage_used_kb" to storageStats.storageSizeKB,
                "oldest_data" to storageStats.oldestConversation,
                "newest_data" to storageStats.newestConversation
            ),
            "storage_locations" to mapOf(
                "app_data_dir" to context.getExternalFilesDir(null)?.absolutePath,
                "processed_images_dir" to File(context.getExternalFilesDir(null), "processed_images").absolutePath,
                "exports_dir" to File(context.getExternalFilesDir(null), "exports").absolutePath
            ),
            "data_sharing" to mapOf(
                "cloud_sync" to false,
                "analytics_enabled" to false,
                "crash_reporting" to false,
                "external_services" to listOf<String>() // No external services used
            ),
            "user_rights" to mapOf(
                "data_portability" to "Full export available",
                "data_deletion" to "Complete local deletion available",
                "data_correction" to "All data user-editable",
                "data_access" to "Full access to all stored data"
            )
        )
    }
    
    private fun getTutorialSettings(context: Context): Map<String, Boolean> {
        val tutorialPrefs = context.getSharedPreferences("tutorial_prefs", Context.MODE_PRIVATE)
        return mapOf(
            "tutorial_completed" to tutorialPrefs.getBoolean(TutorialManager.PREF_TUTORIAL_COMPLETED, false),
            "skip_tutorial" to tutorialPrefs.getBoolean(TutorialManager.PREF_SKIP_TUTORIAL, false),
            "chat_tutorial_shown" to tutorialPrefs.getBoolean(TutorialManager.PREF_CHAT_TUTORIAL_SHOWN, false),
            "model_tutorial_shown" to tutorialPrefs.getBoolean(TutorialManager.PREF_MODEL_TUTORIAL_SHOWN, false),
            "file_tutorial_shown" to tutorialPrefs.getBoolean(TutorialManager.PREF_FILE_TUTORIAL_SHOWN, false),
            "camera_tutorial_shown" to tutorialPrefs.getBoolean(TutorialManager.PREF_CAMERA_TUTORIAL_SHOWN, false)
        )
    }
    
    private fun getPrivacySettings(sharedPrefs: SharedPreferences): Map<String, Any> {
        return mapOf(
            "local_processing" to sharedPrefs.getBoolean(SettingsFragment.PREF_LOCAL_PROCESSING, true),
            "memory_enabled" to sharedPrefs.getBoolean(SettingsFragment.PREF_MEMORY_ENABLED, true),
            "auto_backup" to sharedPrefs.getBoolean(SettingsFragment.PREF_AUTO_BACKUP, true),
            "notifications" to sharedPrefs.getBoolean(SettingsFragment.PREF_NOTIFICATIONS, true),
            "auto_delete_setting" to sharedPrefs.getInt(SettingsFragment.PREF_AUTO_DELETE, 0)
        )
    }
    
    // Import functionality (for future implementation)
    fun importData(context: Context, jsonData: String): Boolean {
        return try {
            val exportData = gson.fromJson(jsonData, ExportData::class.java)
            
            // Import conversations
            exportData.conversations?.let {
                importConversationsData(context, it)
            }
            
            // Import settings
            exportData.settings?.let {
                importSettingsData(context, it)
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun importConversationsData(context: Context, conversationsData: Map<String, Any>) {
        // TODO: Implement conversation import
    }
    
    private fun importSettingsData(context: Context, settingsData: Map<String, Any>) {
        // TODO: Implement settings import
    }
}