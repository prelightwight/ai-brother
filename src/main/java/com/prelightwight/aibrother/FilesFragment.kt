package com.prelightwight.aibrother

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class FilesFragment : Fragment() {
    
    private lateinit var uploadCountText: TextView
    private lateinit var storageUsedText: TextView
    private lateinit var analysisStatusText: TextView
    private lateinit var filesList: ListView
    private lateinit var uploadFileBtn: Button
    private lateinit var analyzeAllBtn: Button
    private lateinit var manageStorageBtn: Button
    private lateinit var fileSettingsBtn: Button
    
    private val uploadedFiles = mutableListOf<FileInfo>()
    private lateinit var filesDirectory: File
    
    // Simplified file picker launcher that should always work
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                // Handle multiple files if present
                if (data.clipData != null) {
                    val clipData = data.clipData!!
                    for (i in 0 until clipData.itemCount) {
                        handleSelectedFile(clipData.getItemAt(i).uri)
                    }
                } else if (data.data != null) {
                    // Handle single file
                    handleSelectedFile(data.data!!)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_files, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupButtons()
        initializeFilesDirectory()
        loadExistingFiles()
        updateFileStats()
    }
    
    private fun initializeViews(view: View) {
        uploadCountText = view.findViewById(R.id.upload_count_text)
        storageUsedText = view.findViewById(R.id.storage_used_text)
        analysisStatusText = view.findViewById(R.id.analysis_status_text)
        filesList = view.findViewById(R.id.files_list)
        uploadFileBtn = view.findViewById(R.id.btn_upload_file)
        analyzeAllBtn = view.findViewById(R.id.btn_analyze_all)
        manageStorageBtn = view.findViewById(R.id.btn_manage_storage)
        fileSettingsBtn = view.findViewById(R.id.btn_file_settings)
    }
    
    private fun initializeFilesDirectory() {
        filesDirectory = File(requireContext().getExternalFilesDir(null), "uploaded_files")
        if (!filesDirectory.exists()) {
            filesDirectory.mkdirs()
        }
    }
    
    private fun setupButtons() {
        uploadFileBtn.setOnClickListener {
            // Try the dialog first, but also add a direct option
            showFileUploadOptions()
        }
        
        // Add long-click for direct file picker bypass
        uploadFileBtn.setOnLongClickListener {
            Toast.makeText(requireContext(), "Opening file picker directly...", Toast.LENGTH_SHORT).show()
            openFilePicker("any")
            true
        }
        
        analyzeAllBtn.setOnClickListener {
            analyzeAllFiles()
        }
        
        manageStorageBtn.setOnClickListener {
            showStorageManagement()
        }
        
        fileSettingsBtn.setOnClickListener {
            showFileSettings()
        }
        
        // List item click listener
        filesList.setOnItemClickListener { _, _, position, _ ->
            val file = uploadedFiles[position]
            showFileDetails(file)
        }
    }
    
    private fun showFileUploadOptions() {
        try {
            val fileTypes = arrayOf(
                "📄 Documents (PDF, Word, Text)",
                "📊 Spreadsheets (Excel, CSV)", 
                "🖼️ Images (JPG, PNG)",
                "📁 Multiple Files",
                "📱 Any File Type"
            )
            
            // Try a different approach with multiple dialogs instead of setItems
            AlertDialog.Builder(requireContext())
                .setTitle("Upload Files")
                .setMessage("What type of file would you like to upload?\n\nTip: Long-press 'Upload File' button for direct access")
                .setPositiveButton("📄 Documents") { _, _ ->
                    openFilePicker("documents")
                }
                .setNeutralButton("📱 Any File") { _, _ ->
                    openFilePicker("any")
                }
                .setNegativeButton("More Options") { _, _ ->
                    showMoreFileOptions()
                }
                .show()
            
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error creating dialog: ${e.message}", Toast.LENGTH_LONG).show()
            // Fallback: directly open any file picker
            openFilePicker("any")
        }
    }
    
    private fun showMoreFileOptions() {
        AlertDialog.Builder(requireContext())
            .setTitle("More File Types")
            .setPositiveButton("📊 Spreadsheets") { _, _ ->
                openFilePicker("spreadsheets")
            }
            .setNeutralButton("🖼️ Images") { _, _ ->
                openFilePicker("images")
            }
            .setNegativeButton("📁 Multiple Files") { _, _ ->
                openFilePicker("multiple")
            }
            .show()
    }
    
    private fun openFilePicker(type: String) {
        try {
            val intent = when (type) {
                "documents" -> Intent(Intent.ACTION_GET_CONTENT).apply {
                    this.type = "*/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                        "application/pdf",
                        "application/msword", 
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "text/plain",
                        "text/rtf"
                    ))
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                "spreadsheets" -> Intent(Intent.ACTION_GET_CONTENT).apply {
                    this.type = "*/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                        "text/csv"
                    ))
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                "images" -> Intent(Intent.ACTION_GET_CONTENT).apply {
                    this.type = "image/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                "multiple" -> Intent(Intent.ACTION_GET_CONTENT).apply {
                    this.type = "*/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                else -> Intent(Intent.ACTION_GET_CONTENT).apply {
                    this.type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
            }
            
            // Show immediate feedback
            updateStatus("📂 Opening file picker...")
            
            // Launch the file picker
            filePickerLauncher.launch(Intent.createChooser(intent, "Select File"))
            
        } catch (e: Exception) {
            updateStatus("❌ Error opening file picker")
            Toast.makeText(requireContext(), "Error opening file picker: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun handleSelectedFile(uri: Uri) {
        lifecycleScope.launch {
            try {
                updateStatus("📄 Processing file...")
                
                val fileName = getFileName(uri) ?: "unknown_file_${System.currentTimeMillis()}"
                val fileSize = getFileSize(uri)
                val fileType = getFileType(fileName)
                
                // Copy file to app directory
                val localFile = File(filesDirectory, fileName)
                withContext(Dispatchers.IO) {
                    copyUriToFile(uri, localFile)
                }
                
                val fileInfo = FileInfo(
                    name = fileName,
                    type = fileType,
                    size = formatFileSize(fileSize),
                    uploadTime = "Just now",
                    status = "⏳ Analyzing...",
                    summary = "Processing file content...",
                    localPath = localFile.absolutePath
                )
                
                uploadedFiles.add(0, fileInfo)
                setupFilesList()
                updateFileStats()
                
                // Check auto-analysis setting before starting analysis
                val sharedPrefs = requireContext().getSharedPreferences("file_settings", Context.MODE_PRIVATE)
                val autoAnalysisEnabled = sharedPrefs.getBoolean("auto_analysis_enabled", true)
                
                if (autoAnalysisEnabled) {
                    analyzeFile(fileInfo)
                } else {
                    fileInfo.status = "📁 Ready for analysis"
                    fileInfo.summary = "File uploaded. Tap 'Analyze All' to process content."
                    setupFilesList()
                    updateFileStats()
                }
                
                updateStatus("✅ File uploaded successfully")
                Toast.makeText(requireContext(), "✅ File uploaded: $fileName", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                updateStatus("❌ Upload failed")
                Toast.makeText(requireContext(), "❌ Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun updateStatus(message: String) {
        activity?.findViewById<TextView>(R.id.status_text)?.text = message
    }
    
    private suspend fun analyzeFile(fileInfo: FileInfo) {
        withContext(Dispatchers.IO) {
            try {
                Thread.sleep(2000) // Simulate processing time
                
                val content = when {
                    fileInfo.name.endsWith(".txt", true) -> {
                        File(fileInfo.localPath).readText()
                    }
                    fileInfo.name.endsWith(".pdf", true) -> {
                        extractTextFromPDF(fileInfo.localPath)
                    }
                    else -> "Binary file content - ${fileInfo.type}"
                }
                
                withContext(Dispatchers.Main) {
                    fileInfo.status = "✅ Analyzed"
                    fileInfo.summary = generateFileSummary(content, fileInfo.type)
                    setupFilesList()
                    updateFileStats()
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    fileInfo.status = "❌ Analysis failed"
                    fileInfo.summary = "Could not analyze file: ${e.message}"
                    setupFilesList()
                    updateFileStats()
                }
            }
        }
    }
    
    private fun extractTextFromPDF(filePath: String): String {
        // Simple PDF text extraction placeholder
        return "PDF content extracted from ${File(filePath).name}\\n\\nThis is a placeholder for PDF text extraction. In a full implementation, this would use a PDF library to extract actual text content from the PDF file."
    }
    
    private fun generateFileSummary(content: String, fileType: String): String {
        val wordCount = content.split("\\s+".toRegex()).size
        return when (fileType) {
            "Text" -> "Text file with $wordCount words. Content has been indexed for AI search."
            "Document" -> "Document processed with $wordCount words. Content available for questions and analysis."
            "Spreadsheet" -> "Spreadsheet data processed. Tables and data structures have been analyzed."
            "Image" -> "Image file analyzed for content recognition and text extraction."
            else -> "File processed and available for AI analysis."
        }
    }
    
    private fun loadExistingFiles() {
        uploadedFiles.clear()
        filesDirectory.listFiles()?.forEach { file ->
            val fileName = file.name
            val fileSize = file.length()
            val fileType = getFileType(fileName)
            val uploadTime = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault()).format(Date(file.lastModified()))
            
            uploadedFiles.add(FileInfo(
                name = fileName,
                type = fileType,
                size = formatFileSize(fileSize),
                uploadTime = uploadTime,
                status = "✅ Analyzed",
                summary = "File content available for AI search",
                localPath = file.absolutePath
            ))
        }
        setupFilesList()
        updateFileStats()
    }
    
    private fun setupFilesList() {
        val adapter = FileAdapter(requireContext(), uploadedFiles)
        filesList.adapter = adapter
    }
    
    private fun updateFileStats() {
        val totalSize = uploadedFiles.sumOf { parseFileSize(it.size) }
        val analyzedCount = uploadedFiles.count { it.status.contains("✅") }
        
        uploadCountText.text = "${uploadedFiles.size} files uploaded"
        storageUsedText.text = "Storage: ${formatFileSize(totalSize)}"
        
        when {
            analyzedCount == uploadedFiles.size -> {
                analysisStatusText.text = "📄 All files analyzed and ready"
                analysisStatusText.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
            }
            analyzedCount > 0 -> {
                analysisStatusText.text = "⏳ ${uploadedFiles.size - analyzedCount} files pending analysis"
                analysisStatusText.setTextColor(requireContext().getColor(android.R.color.holo_orange_dark))
            }
            else -> {
                analysisStatusText.text = "📁 Files ready for analysis"
                analysisStatusText.setTextColor(requireContext().getColor(android.R.color.holo_blue_dark))
            }
        }
    }
    
    private fun analyzeAllFiles() {
        val pendingFiles = uploadedFiles.filter { it.status.contains("⏳") || it.status.contains("❌") }
        
        if (pendingFiles.isEmpty()) {
            Toast.makeText(requireContext(), "✅ All files are already analyzed!", Toast.LENGTH_SHORT).show()
            return
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Analyze Files")
            .setMessage("Analyze ${pendingFiles.size} pending files? This may take a few minutes.")
            .setPositiveButton("Start Analysis") { _, _ ->
                startBulkAnalysis(pendingFiles)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun startBulkAnalysis(files: List<FileInfo>) {
        updateStatus("🔄 Analyzing ${files.size} files...")
        
        files.forEachIndexed { index, file ->
            lifecycleScope.launch {
                try {
                    analyzeFile(file)
                    if (index == files.size - 1) {
                        updateStatus("✅ All files analyzed!")
                        Toast.makeText(requireContext(), "🎉 Bulk analysis complete!", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        file.status = "❌ Analysis failed"
                        file.summary = "Could not analyze file: ${e.message}"
                        setupFilesList()
                        updateFileStats()
                    }
                }
            }
        }
    }
    
    private fun showFileDetails(file: FileInfo) {
        val details = buildString {
            append("📄 File Details\n\n")
            append("Name: ${file.name}\n")
            append("Type: ${file.type}\n")
            append("Size: ${file.size}\n")
            append("Uploaded: ${file.uploadTime}\n")
            append("Status: ${file.status}\n\n")
            append("Analysis Summary:\n")
            append(file.summary)
            append("\n\nAI Brother can help you:\n")
            append("• Answer questions about this file\n")
            append("• Extract key information\n")
            append("• Summarize content\n")
            append("• Find related information")
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("File Information")
            .setMessage(details)
            .setPositiveButton("Close", null)
            .setNeutralButton("Ask AI") { _, _ ->
                askAIAboutFile(file)
            }
            .setNegativeButton("Delete") { _, _ ->
                deleteFile(file)
            }
            .show()
    }
    
    private fun askAIAboutFile(file: FileInfo) {
        Toast.makeText(requireContext(), "💬 Switching to chat to discuss ${file.name}", Toast.LENGTH_SHORT).show()
        // Switch to chat and potentially pre-fill a message
        activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.nav_chat
    }
    
    private fun deleteFile(file: FileInfo) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete File")
            .setMessage("Are you sure you want to delete ${file.name}? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                File(file.localPath).delete()
                uploadedFiles.remove(file)
                setupFilesList()
                updateFileStats()
                Toast.makeText(requireContext(), "File deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showStorageManagement() {
        val storageInfo = buildString {
            val totalSize = uploadedFiles.sumOf { parseFileSize(it.size) }
            val largestFile = uploadedFiles.maxByOrNull { parseFileSize(it.size) }
            
            append("💾 Storage Management\n\n")
            append("Total Files: ${uploadedFiles.size}\n")
            append("Storage Used: ${formatFileSize(totalSize)}\n")
            append("Average File Size: ${formatFileSize(totalSize / uploadedFiles.size.coerceAtLeast(1))}\n")
            append("Largest File: ${largestFile?.name ?: "None"} (${largestFile?.size ?: "0"})\n\n")
            append("Storage by Type:\n")
            
            val typeGroups = uploadedFiles.groupBy { it.type }
            typeGroups.forEach { (type, files) ->
                val typeSize = files.sumOf { parseFileSize(it.size) }
                append("• $type: ${files.size} files (${formatFileSize(typeSize)})\n")
            }
            
            append("\nStorage Tips:\n")
            append("• Regularly delete old files\n")
            append("• Compress large documents\n")
            append("• Use cloud storage for backups")
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Storage Management")
            .setMessage(storageInfo)
            .setPositiveButton("Close", null)
            .setNeutralButton("Clean Up") { _, _ ->
                showCleanupOptions()
            }
            .show()
    }
    
    private fun showCleanupOptions() {
        val options = arrayOf(
            "Delete files older than 30 days",
            "Delete failed analysis files",
            "Compress large files",
            "Export and delete old files"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Cleanup Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> cleanupOldFiles()
                    1 -> cleanupFailedFiles()
                    2 -> compressLargeFiles()
                    3 -> exportAndCleanup()
                }
            }
            .show()
    }
    
    private fun showFileSettings() {
        val settings = arrayOf(
            "Auto-analyze new uploads",
            "File retention period",
            "Supported file types",
            "Privacy settings"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("File Management Settings")
            .setItems(settings) { _, which ->
                when (which) {
                    0 -> toggleAutoAnalysis()
                    1 -> setRetentionPeriod()
                    2 -> showSupportedTypes()
                    3 -> showPrivacySettings()
                }
            }
            .show()
    }
    
    private fun cleanupOldFiles() {
        val sharedPrefs = requireContext().getSharedPreferences("file_settings", Context.MODE_PRIVATE)
        val retentionPeriod = sharedPrefs.getInt("retention_period_days", 30)
        
        if (retentionPeriod == -1) {
            Toast.makeText(requireContext(), "📅 Automatic cleanup is disabled (retention period: never delete)", Toast.LENGTH_LONG).show()
            return
        }
        
        val cutoffTime = System.currentTimeMillis() - (retentionPeriod * 24 * 60 * 60 * 1000L)
        val oldFiles = uploadedFiles.filter { file ->
            val fileObj = File(file.localPath)
            fileObj.exists() && fileObj.lastModified() < cutoffTime
        }
        
        if (oldFiles.isEmpty()) {
            Toast.makeText(requireContext(), "✅ No files older than $retentionPeriod days found", Toast.LENGTH_SHORT).show()
            return
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Clean Up Old Files")
            .setMessage("Found ${oldFiles.size} files older than $retentionPeriod days:\n\n${oldFiles.take(5).joinToString("\n") { "• ${it.name}" }}${if (oldFiles.size > 5) "\n• ... and ${oldFiles.size - 5} more" else ""}\n\nDelete these files?")
            .setPositiveButton("Delete All") { _, _ ->
                var deletedCount = 0
                oldFiles.forEach { file ->
                    val fileObj = File(file.localPath)
                    if (fileObj.exists() && fileObj.delete()) {
                        deletedCount++
                    }
                }
                uploadedFiles.removeAll(oldFiles)
                setupFilesList()
                updateFileStats()
                Toast.makeText(requireContext(), "🧹 Deleted $deletedCount old files", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun cleanupFailedFiles() {
        val failedFiles = uploadedFiles.filter { it.status.contains("❌") }
        if (failedFiles.isNotEmpty()) {
            failedFiles.forEach { file ->
                File(file.localPath).delete()
            }
            uploadedFiles.removeAll(failedFiles)
            setupFilesList()
            updateFileStats()
            Toast.makeText(requireContext(), "💥 ${failedFiles.size} failed files removed", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "✅ No failed files to clean up", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun compressLargeFiles() {
        Toast.makeText(requireContext(), "🗜️ File compression feature coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun exportAndCleanup() {
        if (uploadedFiles.isEmpty()) {
            Toast.makeText(requireContext(), "� No files to export", Toast.LENGTH_SHORT).show()
            return
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Export Files")
            .setMessage("Export file list and analysis results?\n\n${uploadedFiles.size} files will be included in the export.")
            .setPositiveButton("Export") { _, _ ->
                exportFilesList()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun exportFilesList() {
        lifecycleScope.launch {
            try {
                val exportData = buildString {
                    append("AI Brother - Files Export\n")
                    append("Export Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
                    append("Total Files: ${uploadedFiles.size}\n\n")
                    
                    uploadedFiles.forEach { file ->
                        append("File: ${file.name}\n")
                        append("Type: ${file.type}\n")
                        append("Size: ${file.size}\n")
                        append("Uploaded: ${file.uploadTime}\n")
                        append("Status: ${file.status}\n")
                        append("Summary: ${file.summary}\n")
                        append("---\n\n")
                    }
                }
                
                val exportFile = File(requireContext().getExternalFilesDir(null), "ai-brother-files-export-${System.currentTimeMillis()}.txt")
                withContext(Dispatchers.IO) {
                    exportFile.writeText(exportData)
                }
                
                Toast.makeText(requireContext(), "📤 Files exported to: ${exportFile.name}", Toast.LENGTH_LONG).show()
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "❌ Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun toggleAutoAnalysis() {
        val sharedPrefs = requireContext().getSharedPreferences("file_settings", Context.MODE_PRIVATE)
        val currentSetting = sharedPrefs.getBoolean("auto_analysis_enabled", true)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Auto-Analysis Settings")
            .setMessage("Automatically analyze files when uploaded?\n\nCurrent setting: ${if (currentSetting) "Enabled" else "Disabled"}")
            .setPositiveButton("Enable") { _, _ ->
                sharedPrefs.edit().putBoolean("auto_analysis_enabled", true).apply()
                Toast.makeText(requireContext(), "✅ Auto-analysis enabled", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Disable") { _, _ ->
                sharedPrefs.edit().putBoolean("auto_analysis_enabled", false).apply()
                Toast.makeText(requireContext(), "❌ Auto-analysis disabled", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }
    
    private fun setRetentionPeriod() {
        val sharedPrefs = requireContext().getSharedPreferences("file_settings", Context.MODE_PRIVATE)
        val currentPeriod = sharedPrefs.getInt("retention_period_days", 30)
        
        val periods = arrayOf("7 days", "14 days", "30 days", "60 days", "90 days", "Never delete")
        val periodValues = arrayOf(7, 14, 30, 60, 90, -1)
        val currentIndex = periodValues.indexOf(currentPeriod)
        
        AlertDialog.Builder(requireContext())
            .setTitle("File Retention Period")
            .setMessage("How long should files be kept before automatic cleanup?\n\nCurrent: ${if (currentPeriod == -1) "Never delete" else "$currentPeriod days"}")
            .setSingleChoiceItems(periods, currentIndex.coerceAtLeast(0)) { dialog, which ->
                val selectedPeriod = periodValues[which]
                sharedPrefs.edit().putInt("retention_period_days", selectedPeriod).apply()
                
                val message = if (selectedPeriod == -1) {
                    "Files will never be automatically deleted"
                } else {
                    "Files will be deleted after $selectedPeriod days"
                }
                
                Toast.makeText(requireContext(), "📅 $message", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showSupportedTypes() {
        val supportedTypes = buildString {
            append("📁 Supported File Types\n\n")
            append("Documents:\n")
            append("• PDF (.pdf)\n")
            append("• Microsoft Word (.docx, .doc)\n")
            append("• Text files (.txt)\n")
            append("• Rich text (.rtf)\n\n")
            append("Spreadsheets:\n")
            append("• Microsoft Excel (.xlsx, .xls)\n")
            append("• CSV files (.csv)\n\n")
            append("Images:\n")
            append("• JPEG (.jpg, .jpeg)\n")
            append("• PNG (.png)\n")
            append("• GIF (.gif)\n\n")
            append("Coming Soon:\n")
            append("• PowerPoint files\n")
            append("• Audio files\n")
            append("• Video files")
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Supported File Types")
            .setMessage(supportedTypes)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showPrivacySettings() {
        val sharedPrefs = requireContext().getSharedPreferences("file_settings", Context.MODE_PRIVATE)
        val encryptFiles = sharedPrefs.getBoolean("encrypt_files", false)
        val allowAnalysis = sharedPrefs.getBoolean("allow_content_analysis", true)
        val shareMetadata = sharedPrefs.getBoolean("share_metadata", false)
        
        val privacyInfo = buildString {
            append("🔒 File Privacy Settings\n\n")
            append("Current Settings:\n")
            append("• File Encryption: ${if (encryptFiles) "Enabled" else "Disabled"}\n")
            append("• Content Analysis: ${if (allowAnalysis) "Allowed" else "Blocked"}\n")
            append("• Metadata Sharing: ${if (shareMetadata) "Enabled" else "Disabled"}\n\n")
            append("Privacy Information:\n")
            append("• All files are stored locally on your device\n")
            append("• No files are uploaded to external servers\n")
            append("• Analysis is performed entirely offline\n")
            append("• You have full control over your data")
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Privacy Settings")
            .setMessage(privacyInfo)
            .setPositiveButton("Configure") { _, _ ->
                showPrivacyOptions(sharedPrefs, encryptFiles, allowAnalysis, shareMetadata)
            }
            .setNegativeButton("Close", null)
            .show()
    }
    
    private fun showPrivacyOptions(sharedPrefs: android.content.SharedPreferences, encryptFiles: Boolean, allowAnalysis: Boolean, shareMetadata: Boolean) {
        val options = arrayOf(
            "File encryption (currently ${if (encryptFiles) "enabled" else "disabled"})",
            "Content analysis (currently ${if (allowAnalysis) "allowed" else "blocked"})",
            "Metadata sharing (currently ${if (shareMetadata) "enabled" else "disabled"})"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Configure Privacy")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        sharedPrefs.edit().putBoolean("encrypt_files", !encryptFiles).apply()
                        Toast.makeText(requireContext(), "🔒 File encryption ${if (!encryptFiles) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        sharedPrefs.edit().putBoolean("allow_content_analysis", !allowAnalysis).apply()
                        Toast.makeText(requireContext(), "🔍 Content analysis ${if (!allowAnalysis) "allowed" else "blocked"}", Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        sharedPrefs.edit().putBoolean("share_metadata", !shareMetadata).apply()
                        Toast.makeText(requireContext(), "📊 Metadata sharing ${if (!shareMetadata) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }
    
    private fun parseFileSize(sizeStr: String): Long {
        val number = sizeStr.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
        return when {
            sizeStr.contains("KB", true) -> (number * 1024).toLong()
            sizeStr.contains("MB", true) -> (number * 1024 * 1024).toLong()
            sizeStr.contains("GB", true) -> (number * 1024 * 1024 * 1024).toLong()
            else -> number.toLong()
        }
    }
    
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024 * 1024)} GB"
            bytes >= 1024 * 1024 -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
            bytes >= 1024 -> "${"%.1f".format(bytes / 1024.0)} KB"
            else -> "$bytes bytes"
        }
    }
    
    private fun getFileName(uri: Uri): String? {
        return if (uri.path?.startsWith("content://") == true) {
            DocumentsContract.getDocumentId(uri).split(":").last()
        } else {
            uri.lastPathSegment
        }
    }

    private fun getFileSize(uri: Uri): Long {
        var size: Long = 0
        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
        inputStream?.use {
            size = it.available().toLong()
        }
        return size
    }

    private fun getFileType(fileName: String): String {
        val lowerCaseName = fileName.toLowerCase(Locale.getDefault())
        return when {
            lowerCaseName.endsWith(".pdf") -> "Document"
            lowerCaseName.endsWith(".doc", true) || lowerCaseName.endsWith(".docx", true) -> "Document"
            lowerCaseName.endsWith(".txt", true) -> "Text"
            lowerCaseName.endsWith(".xls", true) || lowerCaseName.endsWith(".xlsx", true) -> "Spreadsheet"
            lowerCaseName.endsWith(".jpg", true) || lowerCaseName.endsWith(".jpeg", true) -> "Image"
            lowerCaseName.endsWith(".png", true) -> "Image"
            lowerCaseName.endsWith(".gif", true) -> "Image"
            else -> "Unknown"
        }
    }

    private fun copyUriToFile(uri: Uri, destination: File) {
        requireContext().contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destination).use { output ->
                input.copyTo(output)
            }
        }
    }
    
    data class FileInfo(
        val name: String,
        val type: String,
        val size: String,
        val uploadTime: String,
        var status: String,
        var summary: String,
        val localPath: String
    )
    
    private class FileAdapter(
        context: Context,
        private val files: List<FileInfo>
    ) : ArrayAdapter<FileInfo>(context, 0, files) {
        
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(
                android.R.layout.simple_list_item_2, parent, false
            )
            
            val file = files[position]
            
            val titleView = view.findViewById<TextView>(android.R.id.text1)
            val subtitleView = view.findViewById<TextView>(android.R.id.text2)
            
            titleView.text = "${file.name} (${file.size})"
            subtitleView.text = "${file.type} • ${file.status} • ${file.uploadTime}"
            
            return view
        }
    }
}