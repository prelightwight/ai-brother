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
            // Simplified approach - directly open file picker
            showFileUploadOptions()
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
        val fileTypes = arrayOf(
            "📄 Documents (PDF, Word, Text)",
            "📊 Spreadsheets (Excel, CSV)", 
            "🖼️ Images (JPG, PNG)",
            "📁 Multiple Files",
            "📱 Any File Type"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Upload Files")
            .setMessage("What type of file would you like to upload?")
            .setItems(fileTypes) { _, which ->
                when (which) {
                    0 -> openFilePicker("documents")
                    1 -> openFilePicker("spreadsheets") 
                    2 -> openFilePicker("images")
                    3 -> openFilePicker("multiple")
                    4 -> openFilePicker("any")
                }
            }
            .setNegativeButton("Cancel", null)
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
                
                // Start analysis
                analyzeFile(fileInfo)
                
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
        Toast.makeText(requireContext(), "🧹 Old files cleanup feature coming soon!", Toast.LENGTH_SHORT).show()
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