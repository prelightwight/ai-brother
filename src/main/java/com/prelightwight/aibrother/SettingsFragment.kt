package com.prelightwight.aibrother

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {
    
    private lateinit var sharedPrefs: SharedPreferences
    
    // AI Configuration
    private lateinit var responseStyleSpinner: Spinner
    private lateinit var creativitySeekBar: SeekBar
    private lateinit var creativityValue: TextView
    private lateinit var memorySwitch: Switch
    
    // Appearance
    private lateinit var darkThemeSwitch: Switch
    private lateinit var roundedBubblesSwitch: Switch
    private lateinit var fontSizeSeekBar: SeekBar
    private lateinit var fontSizeValue: TextView
    
    // Privacy & Data
    private lateinit var localProcessingSwitch: Switch
    private lateinit var autoDeleteSpinner: Spinner
    private lateinit var clearChatButton: Button
    private lateinit var exportDataButton: Button
    
    // App Behavior
    private lateinit var notificationsSwitch: Switch
    private lateinit var autoBackupSwitch: Switch
    private lateinit var responseSpeedSeekBar: SeekBar
    private lateinit var responseSpeedValue: TextView
    
    // Action Buttons
    private lateinit var resetSettingsButton: Button
    private lateinit var testSettingsButton: Button
    private lateinit var replayTutorialButton: Button
    
    // Preference keys
    companion object {
        const val PREFS_NAME = "AIBrotherSettings"
        const val PREF_RESPONSE_STYLE = "response_style"
        const val PREF_CREATIVITY = "creativity_level"
        const val PREF_MEMORY_ENABLED = "memory_enabled"
        const val PREF_DARK_THEME = "dark_theme"
        const val PREF_ROUNDED_BUBBLES = "rounded_bubbles"
        const val PREF_FONT_SIZE = "font_size"
        const val PREF_LOCAL_PROCESSING = "local_processing"
        const val PREF_AUTO_DELETE = "auto_delete"
        const val PREF_NOTIFICATIONS = "notifications"
        const val PREF_AUTO_BACKUP = "auto_backup"
        const val PREF_RESPONSE_SPEED = "response_speed"
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializePreferences()
        initializeViews(view)
        setupSpinners()
        setupSeekBars()
        setupSwitches()
        setupButtons()
        loadSettings()
    }
    
    private fun initializePreferences() {
        sharedPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    private fun initializeViews(view: View) {
        // AI Configuration
        responseStyleSpinner = view.findViewById(R.id.response_style_spinner)
        creativitySeekBar = view.findViewById(R.id.creativity_seekbar)
        creativityValue = view.findViewById(R.id.creativity_value)
        memorySwitch = view.findViewById(R.id.memory_switch)
        
        // Appearance
        darkThemeSwitch = view.findViewById(R.id.dark_theme_switch)
        roundedBubblesSwitch = view.findViewById(R.id.rounded_bubbles_switch)
        fontSizeSeekBar = view.findViewById(R.id.font_size_seekbar)
        fontSizeValue = view.findViewById(R.id.font_size_value)
        
        // Privacy & Data
        localProcessingSwitch = view.findViewById(R.id.local_processing_switch)
        autoDeleteSpinner = view.findViewById(R.id.auto_delete_spinner)
        clearChatButton = view.findViewById(R.id.clear_chat_button)
        exportDataButton = view.findViewById(R.id.export_data_button)
        
        // App Behavior
        notificationsSwitch = view.findViewById(R.id.notifications_switch)
        autoBackupSwitch = view.findViewById(R.id.auto_backup_switch)
        responseSpeedSeekBar = view.findViewById(R.id.response_speed_seekbar)
        responseSpeedValue = view.findViewById(R.id.response_speed_value)
        
        // Action Buttons
        resetSettingsButton = view.findViewById(R.id.reset_settings_button)
        testSettingsButton = view.findViewById(R.id.test_settings_button)
        replayTutorialButton = view.findViewById(R.id.replay_tutorial_button)
    }
    
    private fun setupSpinners() {
        // Response Style Spinner
        val responseStyles = arrayOf(
            "Friendly & Conversational",
            "Professional & Formal", 
            "Creative & Playful",
            "Concise & Direct",
            "Detailed & Explanatory"
        )
        responseStyleSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            responseStyles
        )
        
        responseStyleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                sharedPrefs.edit().putInt(PREF_RESPONSE_STYLE, position).apply()
                updateMainActivityStatus("Response style updated")
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Auto-Delete Spinner
        val autoDeleteOptions = arrayOf(
            "Never",
            "After 1 day",
            "After 1 week", 
            "After 1 month",
            "After 3 months",
            "After 1 year"
        )
        autoDeleteSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            autoDeleteOptions
        )
        
        autoDeleteSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                sharedPrefs.edit().putInt(PREF_AUTO_DELETE, position).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupSeekBars() {
        // Creativity SeekBar
        creativitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val creativityText = when {
                        progress < 25 -> "$progress% - Conservative"
                        progress < 50 -> "$progress% - Balanced"
                        progress < 75 -> "$progress% - Creative"
                        else -> "$progress% - Very Creative"
                    }
                    creativityValue.text = creativityText
                    sharedPrefs.edit().putInt(PREF_CREATIVITY, progress).apply()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Font Size SeekBar
        fontSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val sizeText = when (progress) {
                        0, 1 -> "Very Small"
                        2, 3 -> "Small"
                        4, 5 -> "Medium"
                        6, 7 -> "Large"
                        else -> "Very Large"
                    }
                    fontSizeValue.text = sizeText
                    sharedPrefs.edit().putInt(PREF_FONT_SIZE, progress).apply()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Response Speed SeekBar
        responseSpeedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val speedText = when {
                        progress < 25 -> "Very Slow"
                        progress < 50 -> "Slow"
                        progress < 75 -> "Normal Speed" 
                        else -> "Fast"
                    }
                    responseSpeedValue.text = speedText
                    sharedPrefs.edit().putInt(PREF_RESPONSE_SPEED, progress).apply()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupSwitches() {
        memorySwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean(PREF_MEMORY_ENABLED, isChecked).apply()
            updateMainActivityStatus(if (isChecked) "Memory enabled" else "Memory disabled")
        }
        
        darkThemeSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean(PREF_DARK_THEME, isChecked).apply()
            Toast.makeText(requireContext(), "Theme will change on next app restart", Toast.LENGTH_SHORT).show()
        }
        
        roundedBubblesSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean(PREF_ROUNDED_BUBBLES, isChecked).apply()
        }
        
        localProcessingSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean(PREF_LOCAL_PROCESSING, isChecked).apply()
            val message = if (isChecked) "🔒 All processing will stay local" else "⚠️ Some data may be processed externally"
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
        
        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean(PREF_NOTIFICATIONS, isChecked).apply()
        }
        
        autoBackupSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean(PREF_AUTO_BACKUP, isChecked).apply()
        }
    }
    
    private fun setupButtons() {
        clearChatButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Clear Chat History")
                .setMessage("Are you sure you want to delete all chat messages? This action cannot be undone.")
                .setPositiveButton("Clear") { _, _ ->
                    val conversationManager = ConversationManager.getInstance(requireContext())
                    conversationManager.clearAllConversations()
                    Toast.makeText(requireContext(), "🧹 All chat history cleared!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        
        exportDataButton.setOnClickListener {
            showExportDataDialog()
        }
        
        resetSettingsButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Reset All Settings")
                .setMessage("This will restore all settings to their default values. Continue?")
                .setPositiveButton("Reset") { _, _ ->
                    resetAllSettings()
                    loadSettings()
                    Toast.makeText(requireContext(), "Settings reset to defaults", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        
        testSettingsButton.setOnClickListener {
            val currentSettings = getCurrentSettingsInfo()
            AlertDialog.Builder(requireContext())
                .setTitle("⚙️ Current Settings")
                .setMessage(currentSettings)
                .setPositiveButton("OK", null)
                .show()
        }
        
        replayTutorialButton.setOnClickListener {
            startTutorial()
        }
    }
    
    private fun loadSettings() {
        // AI Configuration
        responseStyleSpinner.setSelection(sharedPrefs.getInt(PREF_RESPONSE_STYLE, 0))
        creativitySeekBar.progress = sharedPrefs.getInt(PREF_CREATIVITY, 75)
        memorySwitch.isChecked = sharedPrefs.getBoolean(PREF_MEMORY_ENABLED, true)
        
        // Appearance
        darkThemeSwitch.isChecked = sharedPrefs.getBoolean(PREF_DARK_THEME, false)
        roundedBubblesSwitch.isChecked = sharedPrefs.getBoolean(PREF_ROUNDED_BUBBLES, true)
        fontSizeSeekBar.progress = sharedPrefs.getInt(PREF_FONT_SIZE, 5)
        
        // Privacy & Data
        localProcessingSwitch.isChecked = sharedPrefs.getBoolean(PREF_LOCAL_PROCESSING, true)
        autoDeleteSpinner.setSelection(sharedPrefs.getInt(PREF_AUTO_DELETE, 0))
        
        // App Behavior
        notificationsSwitch.isChecked = sharedPrefs.getBoolean(PREF_NOTIFICATIONS, true)
        autoBackupSwitch.isChecked = sharedPrefs.getBoolean(PREF_AUTO_BACKUP, true)
        responseSpeedSeekBar.progress = sharedPrefs.getInt(PREF_RESPONSE_SPEED, 60)
        
        // Update display values
        updateSeekBarDisplays()
    }
    
    private fun updateSeekBarDisplays() {
        // Update creativity display
        val creativity = creativitySeekBar.progress
        creativityValue.text = when {
            creativity < 25 -> "$creativity% - Conservative"
            creativity < 50 -> "$creativity% - Balanced"
            creativity < 75 -> "$creativity% - Creative"
            else -> "$creativity% - Very Creative"
        }
        
        // Update font size display
        val fontSize = fontSizeSeekBar.progress
        fontSizeValue.text = when (fontSize) {
            0, 1 -> "Very Small"
            2, 3 -> "Small"
            4, 5 -> "Medium"
            6, 7 -> "Large"
            else -> "Very Large"
        }
        
        // Update response speed display
        val speed = responseSpeedSeekBar.progress
        responseSpeedValue.text = when {
            speed < 25 -> "Very Slow"
            speed < 50 -> "Slow"
            speed < 75 -> "Normal Speed"
            else -> "Fast"
        }
    }
    
    private fun resetAllSettings() {
        sharedPrefs.edit().clear().apply()
    }
    
    private fun getCurrentSettingsInfo(): String {
        val responseStyle = responseStyleSpinner.selectedItem.toString()
        val creativity = creativitySeekBar.progress
        val memoryEnabled = memorySwitch.isChecked
        val darkTheme = darkThemeSwitch.isChecked
        val localProcessing = localProcessingSwitch.isChecked
        val responseSpeed = responseSpeedSeekBar.progress
        
        return """
            🤖 AI Configuration:
            • Response Style: $responseStyle
            • Creativity Level: $creativity%
            • Memory: ${if (memoryEnabled) "Enabled" else "Disabled"}
            
            🎨 Appearance:
            • Dark Theme: ${if (darkTheme) "On" else "Off"}
            • Font Size: ${fontSizeValue.text}
            
            🔒 Privacy:
            • Local Processing: ${if (localProcessing) "Enabled" else "Disabled"}
            
            ⚙️ Behavior:
            • Response Speed: ${responseSpeedValue.text}
            
            All settings are automatically saved!
        """.trimIndent()
    }
    
    private fun updateMainActivityStatus(status: String) {
        activity?.findViewById<TextView>(R.id.status_text)?.text = status
    }
    
    // Utility method to get current settings for use by other parts of the app
    fun getResponseSpeed(): Long {
        val speed = sharedPrefs.getInt(PREF_RESPONSE_SPEED, 60)
        return when {
            speed < 25 -> 3000L    // Very slow: 3 seconds
            speed < 50 -> 2000L    // Slow: 2 seconds  
            speed < 75 -> 1500L    // Normal: 1.5 seconds
            else -> 500L           // Fast: 0.5 seconds
        }
    }
    
    fun getCreativityLevel(): Int {
        return sharedPrefs.getInt(PREF_CREATIVITY, 75)
    }
    
    fun isMemoryEnabled(): Boolean {
        return sharedPrefs.getBoolean(PREF_MEMORY_ENABLED, true)
    }
    
    fun getResponseStyle(): Int {
        return sharedPrefs.getInt(PREF_RESPONSE_STYLE, 0)
    }
    
    private fun startTutorial() {
        val mainActivity = activity as? MainActivity
        mainActivity?.let { activity ->
            val tutorialManager = TutorialManager(activity)
            tutorialManager.startTutorialFromSettings()
        }
    }
    
    private fun showExportDataDialog() {
        val exportOptions = arrayOf(
            "📁 Export All Data",
            "💬 Export Conversations Only",
            "⚙️ Export Settings Only",
            "📄 Export Processed Files List",
            "📊 Generate Privacy Report"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Export Data")
            .setMessage("Choose what data to export:")
            .setItems(exportOptions) { _, which ->
                when (which) {
                    0 -> exportAllData()
                    1 -> exportConversations()
                    2 -> exportSettings()
                    3 -> exportFilesList()
                    4 -> generatePrivacyReport()
                }
            }
            .setNeutralButton("Import Data") { _, _ ->
                showImportDataDialog()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showImportDataDialog() {
        val importOptions = arrayOf(
            "📁 Import All Data",
            "💬 Import Conversations",
            "⚙️ Import Settings",
            "🔄 Restore from Backup"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Import Data")
            .setMessage("Choose what data to import:")
            .setItems(importOptions) { _, which ->
                when (which) {
                    0 -> importAllData()
                    1 -> importConversations()
                    2 -> importSettings()
                    3 -> restoreFromBackup()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun exportAllData() {
        lifecycleScope.launch {
            try {
                val exportData = DataExporter.exportAllData(requireContext())
                saveExportData("ai_brother_full_export", exportData)
                Toast.makeText(requireContext(), "✅ All data exported successfully!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "❌ Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun exportConversations() {
        lifecycleScope.launch {
            try {
                val exportData = DataExporter.exportConversations(requireContext())
                saveExportData("ai_brother_conversations", exportData)
                Toast.makeText(requireContext(), "✅ Conversations exported successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "❌ Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun exportSettings() {
        lifecycleScope.launch {
            try {
                val exportData = DataExporter.exportSettings(requireContext())
                saveExportData("ai_brother_settings", exportData)
                Toast.makeText(requireContext(), "✅ Settings exported successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "❌ Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun exportFilesList() {
        lifecycleScope.launch {
            try {
                val exportData = DataExporter.exportFilesList(requireContext())
                saveExportData("ai_brother_files", exportData)
                Toast.makeText(requireContext(), "✅ Files list exported successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "❌ Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun generatePrivacyReport() {
        lifecycleScope.launch {
            try {
                val reportData = DataExporter.generatePrivacyReport(requireContext())
                saveExportData("ai_brother_privacy_report", reportData)
                Toast.makeText(requireContext(), "✅ Privacy report generated!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "❌ Report generation failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun importAllData() {
        // TODO: Implement file picker for import
        Toast.makeText(requireContext(), "📁 Import functionality coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun importConversations() {
        Toast.makeText(requireContext(), "💬 Conversation import coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun importSettings() {
        Toast.makeText(requireContext(), "⚙️ Settings import coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun restoreFromBackup() {
        Toast.makeText(requireContext(), "🔄 Backup restore coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun saveExportData(fileName: String, data: String) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val finalFileName = "${fileName}_${timestamp}.json"
            
            val exportsDir = File(requireContext().getExternalFilesDir(null), "exports")
            if (!exportsDir.exists()) exportsDir.mkdirs()
            
            val exportFile = File(exportsDir, finalFileName)
            exportFile.writeText(data)
            
            // Show save location
            AlertDialog.Builder(requireContext())
                .setTitle("Export Complete ✅")
                .setMessage("Data exported to:\n${exportFile.absolutePath}\n\nYou can now share or backup this file.")
                .setPositiveButton("Share File") { _, _ ->
                    shareExportFile(exportFile)
                }
                .setNeutralButton("OK", null)
                .show()
                
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "❌ Failed to save export: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun shareExportFile(file: File) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "application/json"
            
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "com.prelightwight.aibrother.fileprovider",
                file
            )
            
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.putExtra(Intent.EXTRA_SUBJECT, "AI Brother Data Export")
            intent.putExtra(Intent.EXTRA_TEXT, "AI Brother data export file")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            
            startActivity(Intent.createChooser(intent, "Share Export File"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "❌ Failed to share file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}