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
    
    private val mockConversations = mutableListOf<ConversationSummary>()
    
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
        loadMockData()
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
            val conversation = mockConversations[position]
            showConversationDetails(conversation)
        }
    }
    
    private fun loadMockData() {
        mockConversations.clear()
        mockConversations.addAll(listOf(
            ConversationSummary(
                "Today's Weather Chat",
                "Discussed weather patterns and climate",
                "2 hours ago",
                12
            ),
            ConversationSummary(
                "Programming Help",
                "Android development questions and solutions",
                "Yesterday",
                8
            ),
            ConversationSummary(
                "Cooking Recipes",
                "Recipe suggestions and cooking techniques",
                "2 days ago",
                15
            ),
            ConversationSummary(
                "Learning History",
                "Historical events and timeline discussions",
                "1 week ago",
                25
            ),
            ConversationSummary(
                "Tech Trends",
                "AI and technology future predictions",
                "2 weeks ago",
                18
            )
        ))
        
        setupConversationsList()
    }
    
    private fun setupConversationsList() {
        val adapter = ConversationAdapter(requireContext(), mockConversations)
        conversationsList.adapter = adapter
    }
    
    private fun updateMemoryStats() {
        val sharedPrefs = requireContext().getSharedPreferences(SettingsFragment.PREFS_NAME, Context.MODE_PRIVATE)
        val isMemoryEnabled = sharedPrefs.getBoolean(SettingsFragment.PREF_MEMORY_ENABLED, true)
        
        memoryCountText.text = "${mockConversations.size} conversations stored"
        lastActivityText.text = "Last activity: ${mockConversations.firstOrNull()?.timestamp ?: "Never"}"
        
        if (isMemoryEnabled) {
            memoryStatusText.text = "🧠 Memory Active - Learning from interactions"
            memoryStatusText.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
        } else {
            memoryStatusText.text = "💤 Memory Disabled - Enable in Settings"
            memoryStatusText.setTextColor(requireContext().getColor(android.R.color.holo_orange_dark))
        }
    }
    
    private fun showDetailedMemoryView() {
        val memoryDetails = buildString {
            append("🧠 AI Brother Memory Report\n\n")
            append("Active Conversations: ${mockConversations.size}\n")
            append("Total Messages: ${mockConversations.sumOf { it.messageCount }}\n")
            append("Memory Efficiency: 94.2%\n")
            append("Storage Used: 2.1 MB\n\n")
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
                Toast.makeText(requireContext(), "📁 Memory export feature coming soon!", Toast.LENGTH_SHORT).show()
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
        val stats = buildString {
            val totalMessages = mockConversations.sumOf { it.messageCount }
            val avgPerConversation = if (mockConversations.isNotEmpty()) totalMessages / mockConversations.size else 0
            
            append("📊 Memory Statistics\n\n")
            append("Conversations: ${mockConversations.size}\n")
            append("Total Messages: $totalMessages\n")
            append("Average per Conversation: $avgPerConversation\n")
            append("Memory Retention: 30 days\n")
            append("Learning Accuracy: 96.7%\n\n")
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
        mockConversations.clear()
        setupConversationsList()
        updateMemoryStats()
        updateStatus("🧠 All memory cleared")
        Toast.makeText(requireContext(), "Memory cleared! AI Brother will start learning fresh.", Toast.LENGTH_LONG).show()
    }
    
    private fun clearOldMemory() {
        // Remove conversations older than 7 days (for demo, remove last 2)
        if (mockConversations.size > 2) {
            repeat(2) { mockConversations.removeLastOrNull() }
            setupConversationsList()
            updateMemoryStats()
            updateStatus("🧠 Old memory cleared")
            Toast.makeText(requireContext(), "Old conversations cleared!", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun deleteConversation(conversation: ConversationSummary) {
        mockConversations.remove(conversation)
        setupConversationsList()
        updateMemoryStats()
        Toast.makeText(requireContext(), "Conversation deleted from memory", Toast.LENGTH_SHORT).show()
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
        Toast.makeText(requireContext(), "⚙️ Auto-cleanup settings coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun showLearningSettings() {
        Toast.makeText(requireContext(), "🎓 Learning preferences coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun showPrivacySettings() {
        Toast.makeText(requireContext(), "🔒 Privacy controls coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateStatus(status: String) {
        activity?.findViewById<TextView>(R.id.status_text)?.text = status
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