package com.prelightwight.aibrother

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
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
    
    private val mockFiles = mutableListOf<FileInfo>()
    
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
        loadMockFiles()
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
    
    private fun setupButtons() {
        uploadFileBtn.setOnClickListener {
            simulateFileUpload()
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
            val file = mockFiles[position]
            showFileDetails(file)
        }
    }
    
    private fun loadMockFiles() {
        mockFiles.clear()
        mockFiles.addAll(listOf(
            FileInfo(
                "Meeting_Notes_2024.pdf",
                "Document",
                "1.2 MB",
                "2 hours ago",
                "✅ Analyzed",
                "Work notes and action items from team meeting"
            ),
            FileInfo(
                "Recipe_Collection.docx",
                "Document",
                "850 KB",
                "Yesterday",
                "✅ Analyzed", 
                "Family recipes and cooking instructions"
            ),
            FileInfo(
                "Research_Paper.pdf",
                "Document",
                "3.1 MB",
                "2 days ago",
                "✅ Analyzed",
                "Academic paper on artificial intelligence"
            ),
            FileInfo(
                "Budget_Spreadsheet.xlsx",
                "Spreadsheet",
                "512 KB",
                "1 week ago",
                "⏳ Pending",
                "Monthly budget and expense tracking"
            ),
            FileInfo(
                "Travel_Itinerary.txt",
                "Text",
                "45 KB",
                "2 weeks ago",
                "❌ Failed",
                "Vacation plans and booking confirmations"
            )
        ))
        
        setupFilesList()
    }
    
    private fun setupFilesList() {
        val adapter = FileAdapter(requireContext(), mockFiles)
        filesList.adapter = adapter
    }
    
    private fun updateFileStats() {
        val totalSize = mockFiles.sumOf { parseFileSize(it.size) }
        val analyzedCount = mockFiles.count { it.status.contains("✅") }
        
        uploadCountText.text = "${mockFiles.size} files uploaded"
        storageUsedText.text = "Storage: ${formatFileSize(totalSize)}"
        
        when {
            analyzedCount == mockFiles.size -> {
                analysisStatusText.text = "📄 All files analyzed and ready"
                analysisStatusText.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
            }
            analyzedCount > 0 -> {
                analysisStatusText.text = "⏳ ${mockFiles.size - analyzedCount} files pending analysis"
                analysisStatusText.setTextColor(requireContext().getColor(android.R.color.holo_orange_dark))
            }
            else -> {
                analysisStatusText.text = "📁 Files ready for analysis"
                analysisStatusText.setTextColor(requireContext().getColor(android.R.color.holo_blue_dark))
            }
        }
    }
    
    private fun simulateFileUpload() {
        val fileTypes = arrayOf(
            "📄 Document (.pdf, .docx, .txt)",
            "📊 Spreadsheet (.xlsx, .csv)",
            "🖼️ Image (.jpg, .png)",
            "📁 Multiple Files"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Upload Files")
            .setMessage("What type of file would you like to upload?")
            .setItems(fileTypes) { _, which ->
                when (which) {
                    0 -> uploadDocument()
                    1 -> uploadSpreadsheet()
                    2 -> uploadImage()
                    3 -> uploadMultipleFiles()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun uploadDocument() {
        val fileName = "New_Document_${System.currentTimeMillis()}.pdf"
        val newFile = FileInfo(
            fileName,
            "Document",
            "1.8 MB",
            "Just now",
            "⏳ Analyzing...",
            "Recently uploaded document"
        )
        
        mockFiles.add(0, newFile)
        setupFilesList()
        updateFileStats()
        updateStatus("📄 Document uploaded successfully")
        
        // Simulate analysis completion
        Timer().schedule(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    newFile.status = "✅ Analyzed"
                    newFile.summary = "Document contains important information and has been indexed for AI search"
                    setupFilesList()
                    updateFileStats()
                    Toast.makeText(requireContext(), "✅ Analysis complete!", Toast.LENGTH_SHORT).show()
                }
            }
        }, 3000)
        
        Toast.makeText(requireContext(), "📄 File uploaded! Analysis in progress...", Toast.LENGTH_LONG).show()
    }
    
    private fun uploadSpreadsheet() {
        val fileName = "Data_Sheet_${System.currentTimeMillis()}.xlsx"
        val newFile = FileInfo(
            fileName,
            "Spreadsheet",
            "640 KB",
            "Just now",
            "⏳ Analyzing...",
            "Recently uploaded spreadsheet"
        )
        
        mockFiles.add(0, newFile)
        setupFilesList()
        updateFileStats()
        
        Timer().schedule(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    newFile.status = "✅ Analyzed"
                    newFile.summary = "Spreadsheet data has been processed and can be queried by AI Brother"
                    setupFilesList()
                    updateFileStats()
                }
            }
        }, 2500)
        
        Toast.makeText(requireContext(), "📊 Spreadsheet uploaded! Analyzing data structure...", Toast.LENGTH_LONG).show()
    }
    
    private fun uploadImage() {
        val fileName = "Photo_${System.currentTimeMillis()}.jpg"
        val newFile = FileInfo(
            fileName,
            "Image",
            "2.1 MB",
            "Just now",
            "⏳ Analyzing...",
            "Recently uploaded image"
        )
        
        mockFiles.add(0, newFile)
        setupFilesList()
        updateFileStats()
        updateStatus("📸 Image uploaded successfully")
        
        // Simulate analysis completion
        Timer().schedule(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    newFile.status = "✅ Analyzed"
                    newFile.summary = "Image has been analyzed for content recognition and text extraction"
                    setupFilesList()
                    updateFileStats()
                    Toast.makeText(requireContext(), "✅ Image analysis complete!", Toast.LENGTH_SHORT).show()
                }
            }
        }, 3500)
        
        Toast.makeText(requireContext(), "📸 Image uploaded! Analyzing visual content...", Toast.LENGTH_LONG).show()
    }
    
    private fun uploadMultipleFiles() {
        Toast.makeText(requireContext(), "📁 Bulk upload feature coming soon! You can upload files one at a time for now.", Toast.LENGTH_LONG).show()
    }
    
    private fun analyzeAllFiles() {
        val pendingFiles = mockFiles.filter { it.status.contains("⏳") || it.status.contains("❌") }
        
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
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    activity?.runOnUiThread {
                        file.status = "✅ Analyzed"
                        file.summary = "File has been processed and indexed for AI search"
                        setupFilesList()
                        updateFileStats()
                        
                        if (index == files.size - 1) {
                            updateStatus("✅ All files analyzed!")
                            Toast.makeText(requireContext(), "🎉 Bulk analysis complete!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }, (index + 1) * 1500L)
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
                mockFiles.remove(file)
                setupFilesList()
                updateFileStats()
                Toast.makeText(requireContext(), "File deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showStorageManagement() {
        val storageInfo = buildString {
            val totalSize = mockFiles.sumOf { parseFileSize(it.size) }
            val largestFile = mockFiles.maxByOrNull { parseFileSize(it.size) }
            
            append("💾 Storage Management\n\n")
            append("Total Files: ${mockFiles.size}\n")
            append("Storage Used: ${formatFileSize(totalSize)}\n")
            append("Average File Size: ${formatFileSize(totalSize / mockFiles.size.coerceAtLeast(1))}\n")
            append("Largest File: ${largestFile?.name ?: "None"} (${largestFile?.size ?: "0"})\n\n")
            append("Storage by Type:\n")
            
            val typeGroups = mockFiles.groupBy { it.type }
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
        Toast.makeText(requireContext(), "🧹 Old files cleanup feature coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun cleanupFailedFiles() {
        val failedFiles = mockFiles.filter { it.status.contains("❌") }
        if (failedFiles.isNotEmpty()) {
            mockFiles.removeAll(failedFiles)
            setupFilesList()
            updateFileStats()
            Toast.makeText(requireContext(), "�️ ${failedFiles.size} failed files removed", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "✅ No failed files to clean up", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun compressLargeFiles() {
        Toast.makeText(requireContext(), "🗜️ File compression feature coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun exportAndCleanup() {
        Toast.makeText(requireContext(), "📤 Export and cleanup feature coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun toggleAutoAnalysis() {
        Toast.makeText(requireContext(), "⚙️ Auto-analysis toggle coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun setRetentionPeriod() {
        Toast.makeText(requireContext(), "📅 Retention period settings coming soon!", Toast.LENGTH_SHORT).show()
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
        Toast.makeText(requireContext(), "🔒 Privacy settings coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateStatus(status: String) {
        activity?.findViewById<TextView>(R.id.status_text)?.text = status
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
    
    data class FileInfo(
        val name: String,
        val type: String,
        val size: String,
        val uploadTime: String,
        var status: String,
        var summary: String
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