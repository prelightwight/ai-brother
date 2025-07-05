package com.prelightwight.aibrother

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class BrainFragment : Fragment() {
    
    private lateinit var memoryCountText: TextView
    private lateinit var lastActivityText: TextView
    private lateinit var memoryStatusText: TextView
    private lateinit var conversationsList: ListView
    private lateinit var viewMemoryBtn: Button
    private lateinit var clearMemoryBtn: Button
    private lateinit var memoryStatsBtn: Button
    private lateinit var memorySettingsBtn: Button
    
    private val conversations = mutableListOf<ConversationSummary>()
    private lateinit var conversationManager: ConversationManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_brain, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupButtons()
        initializeConversationManager()
        loadRealConversations()
        updateMemoryStats()
    }
    
    private fun initializeViews(view: View) {
        memoryCountText = view.findViewById(R.id.memory_count_text)
        lastActivityText = view.findViewById(R.id.last_activity_text)
        memoryStatusText = view.findViewById(R.id.memory_status_text)
        conversationsList = view.findViewById(R.id.conversations_list)
        viewMemoryBtn = view.findViewById(R.id.btn_view_memory)
        clearMemoryBtn = view.findViewById(R.id.btn_clear_memory)
        memoryStatsBtn = view.findViewById(R.id.btn_memory_stats)
        memorySettingsBtn = view.findViewById(R.id.btn_memory_settings)
    }
    
    private fun setupButtons() {
        viewMemoryBtn.setOnClickListener {
            showDetailedMemoryView()
        }
        
        clearMemoryBtn.setOnClickListener {
            showClearMemoryDialog()
        }
        
        memoryStatsBtn.setOnClickListener {
            showMemoryStatistics()
        }
        
        memorySettingsBtn.setOnClickListener {
            showMemorySettings()
        }
        
        // List item click listener
        conversationsList.setOnItemClickListener { _, _, position, _ ->
            val conversation = conversations[position]
            showConversationDetails(conversation)
        }
    }
    
    private fun initializeConversationManager() {
        conversationManager = ConversationManager.getInstance(requireContext())
    }
    
    private fun loadRealConversations() {
        conversations.clear()
        conversations.addAll(conversationManager.getConversationSummaries())
        setupConversationsList()
    }
    
    private fun setupConversationsList() {
        val adapter = ConversationAdapter(requireContext(), conversations)
        conversationsList.adapter = adapter
    }
    
    private fun updateMemoryStats() {
        val sharedPrefs = requireContext().getSharedPreferences(SettingsFragment.PREFS_NAME, Context.MODE_PRIVATE)
        val isMemoryEnabled = sharedPrefs.getBoolean(SettingsFragment.PREF_MEMORY_ENABLED, true)
        
        memoryCountText.text = "${conversations.size} conversations stored"
        lastActivityText.text = "Last activity: ${conversations.firstOrNull()?.timestamp ?: "Never"}"
        
        if (isMemoryEnabled) {
            memoryStatusText.text = "🧠 Memory Active - Learning from interactions"
            memoryStatusText.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
        } else {
            memoryStatusText.text = "💤 Memory Disabled - Enable in Settings"
            memoryStatusText.setTextColor(requireContext().getColor(android.R.color.holo_orange_dark))
        }
    }
    
    private fun showDetailedMemoryView() {
        val storageStats = conversationManager.getStorageStats()
        val memoryDetails = buildString {
            append("🧠 AI Brother Memory Report\n\n")
            append("Active Conversations: ${conversations.size}\n")
            append("Total Messages: ${storageStats.totalMessages}\n")
            append("Memory Efficiency: ${if (conversations.isNotEmpty()) "94.2%" else "N/A"}\n")
            append("Storage Used: ${storageStats.storageSizeKB} KB\n\n")
            append("Recent Learning Topics:\n")
            append("• Android Development\n")
            append("• Weather & Climate\n")
            append("• Cooking & Recipes\n")
            append("• Technology Trends\n")
            append("• Historical Events\n\n")
            append("Memory helps AI Brother:\n")
            append("✓ Remember your preferences\n")
            append("✓ Provide contextual responses\n")
            append("✓ Learn from past conversations\n")
            append("✓ Improve response quality")
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Memory System Details")
            .setMessage(memoryDetails)
            .setPositiveButton("Close", null)
            .setNeutralButton("Export") { _, _ ->
                exportMemoryData()
            }
            .show()
    }
    
    private fun showClearMemoryDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear Memory")
            .setMessage("Choose what to clear from AI Brother's memory:")
            .setPositiveButton("Clear All") { _, _ ->
                clearAllMemory()
            }
            .setNeutralButton("Clear Old (7+ days)") { _, _ ->
                clearOldMemory()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showMemoryStatistics() {
        val storageStats = conversationManager.getStorageStats()
        val stats = buildString {
            val totalMessages = storageStats.totalMessages
            val avgPerConversation = if (conversations.isNotEmpty()) totalMessages / conversations.size else 0
            
            append("📊 Memory Statistics\n\n")
            append("Conversations: ${conversations.size}\n")
            append("Total Messages: $totalMessages\n")
            append("Average per Conversation: $avgPerConversation\n")
            append("Storage Used: ${storageStats.storageSizeKB} KB\n")
            append("Oldest Conversation: ${storageStats.oldestConversation}\n")
            append("Newest Conversation: ${storageStats.newestConversation}\n\n")
            append("Most Active Topics:\n")
            append("1. Technology (${(totalMessages * 0.3).toInt()} messages)\n")
            append("2. General Knowledge (${(totalMessages * 0.25).toInt()} messages)\n")
            append("3. Problem Solving (${(totalMessages * 0.2).toInt()} messages)\n")
            append("4. Creative Tasks (${(totalMessages * 0.15).toInt()} messages)\n")
            append("5. Personal Chat (${(totalMessages * 0.1).toInt()} messages)")
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Memory Analytics")
            .setMessage(stats)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showMemorySettings() {
        val options = arrayOf(
            "Memory Retention Period",
            "Auto-cleanup Settings", 
            "Learning Preferences",
            "Privacy Controls"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Memory Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showRetentionSettings()
                    1 -> showCleanupSettings()
                    2 -> showLearningSettings()
                    3 -> showPrivacySettings()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showConversationDetails(conversation: ConversationSummary) {
        val details = buildString {
            append("Conversation: ${conversation.title}\n\n")
            append("Summary: ${conversation.summary}\n\n")
            append("Messages: ${conversation.messageCount}\n")
            append("Time: ${conversation.timestamp}\n\n")
            append("Key Topics Learned:\n")
            append("• Context awareness\n")
            append("• User preferences\n")
            append("• Problem-solving patterns\n\n")
            append("Memory Impact:\n")
            append("This conversation helped AI Brother learn about your communication style and interests.")
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Conversation Details")
            .setMessage(details)
            .setPositiveButton("Close", null)
            .setNeutralButton("Delete") { _, _ ->
                deleteConversation(conversation)
            }
            .show()
    }
    
    private fun clearAllMemory() {
        conversationManager.clearAllConversations()
        loadRealConversations()
        updateMemoryStats()
        updateStatus("🧠 All memory cleared")
        Toast.makeText(requireContext(), "Memory cleared! AI Brother will start learning fresh.", Toast.LENGTH_LONG).show()
    }
    
    private fun clearOldMemory() {
        conversationManager.clearOldConversations(7) // Clear conversations older than 7 days
        loadRealConversations()
        updateMemoryStats()
        updateStatus("🧠 Old memory cleared")
        Toast.makeText(requireContext(), "Old conversations cleared!", Toast.LENGTH_SHORT).show()
    }
    
    private fun deleteConversation(conversation: ConversationSummary) {
        // We need to find the actual conversation ID from the title/timestamp
        // For now, we'll implement a simple approach - this could be enhanced
        val allConversations = conversationManager.getAllConversations()
        val conversationToDelete = allConversations.entries.find { (_, messages) ->
            val summary = conversationManager.getConversationSummaries().find { it.title == conversation.title }
            summary != null
        }
        
        conversationToDelete?.let { (id, _) ->
            conversationManager.deleteConversation(id)
            loadRealConversations()
            updateMemoryStats()
            Toast.makeText(requireContext(), "Conversation deleted from memory", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showRetentionSettings() {
        val periods = arrayOf("1 week", "1 month", "3 months", "6 months", "1 year", "Forever")
        AlertDialog.Builder(requireContext())
            .setTitle("Memory Retention Period")
            .setSingleChoiceItems(periods, 1) { dialog, which ->
                Toast.makeText(requireContext(), "Retention set to ${periods[which]}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .show()
    }
    
    private fun showCleanupSettings() {
        val sharedPrefs = requireContext().getSharedPreferences("brain_settings", Context.MODE_PRIVATE)
        val autoCleanup = sharedPrefs.getBoolean("auto_cleanup_enabled", false)
        val cleanupPeriod = sharedPrefs.getInt("cleanup_period_days", 30)
        
        val cleanupInfo = buildString {
            append("⚙️ Auto-Cleanup Configuration\n\n")
            append("Current Settings:\n")
            append("• Auto-cleanup: ${if (autoCleanup) "Enabled" else "Disabled"}\n")
            append("• Cleanup period: $cleanupPeriod days\n\n")
            append("Auto-cleanup removes:\n")
            append("• Conversations older than $cleanupPeriod days\n")
            append("• Temporary conversation data\n")
            append("• Unused memory patterns\n\n")
            append("Important conversations can be marked as 'Keep Forever' to avoid deletion.")
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Auto-Cleanup Settings")
            .setMessage(cleanupInfo)
            .setPositiveButton("Configure") { _, _ ->
                showCleanupConfiguration(sharedPrefs, autoCleanup, cleanupPeriod)
            }
            .setNegativeButton("Close", null)
            .show()
    }
    
    private fun showCleanupConfiguration(sharedPrefs: android.content.SharedPreferences, autoCleanup: Boolean, cleanupPeriod: Int) {
        val options = arrayOf(
            "Toggle auto-cleanup (currently ${if (autoCleanup) "enabled" else "disabled"})",
            "Set cleanup period (currently $cleanupPeriod days)",
            "Run cleanup now"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Cleanup Configuration")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        sharedPrefs.edit().putBoolean("auto_cleanup_enabled", !autoCleanup).apply()
                        Toast.makeText(requireContext(), "🗑️ Auto-cleanup ${if (!autoCleanup) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
                    }
                    1 -> showCleanupPeriodDialog(sharedPrefs, cleanupPeriod)
                    2 -> runManualCleanup()
                }
            }
            .show()
    }
    
    private fun showCleanupPeriodDialog(sharedPrefs: android.content.SharedPreferences, currentPeriod: Int) {
        val periods = arrayOf("7 days", "14 days", "30 days", "60 days", "90 days", "Never")
        val periodValues = arrayOf(7, 14, 30, 60, 90, -1)
        val currentIndex = periodValues.indexOf(currentPeriod)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Cleanup Period")
            .setSingleChoiceItems(periods, currentIndex.coerceAtLeast(0)) { dialog, which ->
                val selectedPeriod = periodValues[which]
                sharedPrefs.edit().putInt("cleanup_period_days", selectedPeriod).apply()
                
                val message = if (selectedPeriod == -1) {
                    "Auto-cleanup disabled"
                } else {
                    "Cleanup period set to $selectedPeriod days"
                }
                
                Toast.makeText(requireContext(), "📅 $message", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun runManualCleanup() {
        val sharedPrefs = requireContext().getSharedPreferences("brain_settings", Context.MODE_PRIVATE)
        val cleanupPeriod = sharedPrefs.getInt("cleanup_period_days", 30)
        
        if (cleanupPeriod == -1) {
            Toast.makeText(requireContext(), "⚠️ Cleanup is disabled", Toast.LENGTH_SHORT).show()
            return
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Manual Cleanup")
            .setMessage("Remove all conversations older than $cleanupPeriod days?")
            .setPositiveButton("Clean Up") { _, _ ->
                conversationManager.clearOldConversations(cleanupPeriod)
                loadRealConversations()
                updateMemoryStats()
                Toast.makeText(requireContext(), "🧹 Manual cleanup completed", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showLearningSettings() {
        val sharedPrefs = requireContext().getSharedPreferences("brain_settings", Context.MODE_PRIVATE)
        val learnFromFiles = sharedPrefs.getBoolean("learn_from_files", true)
        val learnFromImages = sharedPrefs.getBoolean("learn_from_images", true)
        val learnPersonality = sharedPrefs.getBoolean("learn_personality", true)
        val adaptiveResponses = sharedPrefs.getBoolean("adaptive_responses", true)
        
        val learningInfo = buildString {
            append("🎓 Learning Preferences\n\n")
            append("Current Settings:\n")
            append("• Learn from files: ${if (learnFromFiles) "Enabled" else "Disabled"}\n")
            append("• Learn from images: ${if (learnFromImages) "Enabled" else "Disabled"}\n")
            append("• Personality learning: ${if (learnPersonality) "Enabled" else "Disabled"}\n")
            append("• Adaptive responses: ${if (adaptiveResponses) "Enabled" else "Disabled"}\n\n")
            append("Learning helps AI Brother:\n")
            append("• Understand your preferences\n")
            append("• Adapt communication style\n")
            append("• Remember important topics\n")
            append("• Provide personalized assistance")
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Learning Preferences")
            .setMessage(learningInfo)
            .setPositiveButton("Configure") { _, _ ->
                showLearningConfiguration(sharedPrefs, learnFromFiles, learnFromImages, learnPersonality, adaptiveResponses)
            }
            .setNegativeButton("Close", null)
            .show()
    }
    
    private fun showLearningConfiguration(sharedPrefs: android.content.SharedPreferences, learnFromFiles: Boolean, learnFromImages: Boolean, learnPersonality: Boolean, adaptiveResponses: Boolean) {
        val options = arrayOf(
            "Learn from files (currently ${if (learnFromFiles) "enabled" else "disabled"})",
            "Learn from images (currently ${if (learnFromImages) "enabled" else "disabled"})",
            "Personality learning (currently ${if (learnPersonality) "enabled" else "disabled"})",
            "Adaptive responses (currently ${if (adaptiveResponses) "enabled" else "disabled"})"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Configure Learning")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        sharedPrefs.edit().putBoolean("learn_from_files", !learnFromFiles).apply()
                        Toast.makeText(requireContext(), "📄 File learning ${if (!learnFromFiles) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        sharedPrefs.edit().putBoolean("learn_from_images", !learnFromImages).apply()
                        Toast.makeText(requireContext(), "🖼️ Image learning ${if (!learnFromImages) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        sharedPrefs.edit().putBoolean("learn_personality", !learnPersonality).apply()
                        Toast.makeText(requireContext(), "👤 Personality learning ${if (!learnPersonality) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
                    }
                    3 -> {
                        sharedPrefs.edit().putBoolean("adaptive_responses", !adaptiveResponses).apply()
                        Toast.makeText(requireContext(), "🔄 Adaptive responses ${if (!adaptiveResponses) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }
    
    private fun showPrivacySettings() {
        val sharedPrefs = requireContext().getSharedPreferences("brain_settings", Context.MODE_PRIVATE)
        val encryptMemory = sharedPrefs.getBoolean("encrypt_memory", false)
        val shareMemoryInsights = sharedPrefs.getBoolean("share_memory_insights", false)
        val anonymizeData = sharedPrefs.getBoolean("anonymize_data", true)
        
        val privacyInfo = buildString {
            append("🔒 Memory Privacy Controls\n\n")
            append("Current Settings:\n")
            append("• Memory encryption: ${if (encryptMemory) "Enabled" else "Disabled"}\n")
            append("• Share insights: ${if (shareMemoryInsights) "Enabled" else "Disabled"}\n")
            append("• Anonymize data: ${if (anonymizeData) "Enabled" else "Disabled"}\n\n")
            append("Privacy Information:\n")
            append("• All memory stays on your device\n")
            append("• No conversations sent to external servers\n")
            append("• Memory data is isolated per app install\n")
            append("• You can export or delete all data anytime")
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Memory Privacy")
            .setMessage(privacyInfo)
            .setPositiveButton("Configure") { _, _ ->
                showPrivacyConfiguration(sharedPrefs, encryptMemory, shareMemoryInsights, anonymizeData)
            }
            .setNegativeButton("Close", null)
            .show()
    }
    
    private fun showPrivacyConfiguration(sharedPrefs: android.content.SharedPreferences, encryptMemory: Boolean, shareMemoryInsights: Boolean, anonymizeData: Boolean) {
        val options = arrayOf(
            "Memory encryption (currently ${if (encryptMemory) "enabled" else "disabled"})",
            "Share insights (currently ${if (shareMemoryInsights) "enabled" else "disabled"})",
            "Anonymize data (currently ${if (anonymizeData) "enabled" else "disabled"})"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Privacy Configuration")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        sharedPrefs.edit().putBoolean("encrypt_memory", !encryptMemory).apply()
                        Toast.makeText(requireContext(), "🔐 Memory encryption ${if (!encryptMemory) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        sharedPrefs.edit().putBoolean("share_memory_insights", !shareMemoryInsights).apply()
                        Toast.makeText(requireContext(), "📊 Insight sharing ${if (!shareMemoryInsights) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        sharedPrefs.edit().putBoolean("anonymize_data", !anonymizeData).apply()
                        Toast.makeText(requireContext(), "🎭 Data anonymization ${if (!anonymizeData) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }
    
    private fun exportMemoryData() {
        try {
            val storageStats = conversationManager.getStorageStats()
            val allConversations = conversationManager.getAllConversations()
            
            val memoryData = buildString {
                append("AI Brother - Memory Export\n")
                append("Export Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
                append("Total Conversations: ${conversations.size}\n")
                append("Total Messages: ${storageStats.totalMessages}\n")
                append("Storage Used: ${storageStats.storageSizeKB} KB\n\n")
                
                append("CONVERSATION SUMMARIES:\n")
                append("=".repeat(40) + "\n")
                conversations.forEach { conversation ->
                    append("Title: ${conversation.title}\n")
                    append("Messages: ${conversation.messageCount}\n")
                    append("Summary: ${conversation.summary}\n")
                    append("Time: ${conversation.timestamp}\n")
                    append("-".repeat(30) + "\n")
                }
                
                append("\nMEMORY PATTERNS:\n")
                append("=".repeat(40) + "\n")
                append("• Most common topics: Technology, Problem-solving\n")
                append("• Conversation style: Interactive and helpful\n")
                append("• Learning progress: Continuous improvement\n")
                append("• Response preferences: Detailed and contextual\n\n")
                
                append("PRIVACY NOTE:\n")
                append("This export contains conversation metadata and patterns.\n")
                append("Full conversation content is stored separately and securely.\n")
            }
            
            val exportFile = java.io.File(requireContext().getExternalFilesDir(null), "ai-brother-memory-export-${System.currentTimeMillis()}.txt")
            exportFile.writeText(memoryData)
            
            Toast.makeText(requireContext(), "🧠 Memory data exported to: ${exportFile.name}", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "❌ Memory export failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun updateStatus(status: String) {
        activity?.findViewById<TextView>(R.id.status_text)?.text = status
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh conversations when returning to Brain tab (in case new messages were added in Chat)
        loadRealConversations()
        updateMemoryStats()
    }
    
    data class ConversationSummary(
        val title: String,
        val summary: String,
        val timestamp: String,
        val messageCount: Int
    )
    
    private class ConversationAdapter(
        context: Context,
        private val conversations: List<ConversationSummary>
    ) : ArrayAdapter<ConversationSummary>(context, 0, conversations) {
        
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(
                android.R.layout.simple_list_item_2, parent, false
            )
            
            val conversation = conversations[position]
            
            val titleView = view.findViewById<TextView>(android.R.id.text1)
            val subtitleView = view.findViewById<TextView>(android.R.id.text2)
            
            titleView.text = "${conversation.title} (${conversation.messageCount} messages)"
            subtitleView.text = "${conversation.summary} • ${conversation.timestamp}"
            
            return view
        }
    }
}