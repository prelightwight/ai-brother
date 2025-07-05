package com.prelightwight.aibrother

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File
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
    
    private lateinit var fileProcessor: FileProcessor
    private val processedFiles = mutableListOf<ProcessedFile>()
    private var currentPhotoPath: String? = null
    
    // Activity result launchers
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                processSelectedFile(uri)
            }
        }
    }
    
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentPhotoPath?.let { path ->
                val photoUri = Uri.fromFile(File(path))
                processSelectedFile(photoUri)
            }
        }
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            showFileUploadOptions()
        } else {
            Toast.makeText(requireContext(), "Permissions required for file access", Toast.LENGTH_LONG).show()
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
        
        fileProcessor = FileProcessor(requireContext())
        initializeViews(view)
        setupButtons()
        loadProcessedFiles()
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
            checkPermissionsAndUpload()
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
            val file = processedFiles[position]
            showFileDetails(file)
        }
    }
    
    private fun checkPermissionsAndUpload() {
        val permissions = listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).filter { 
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        } else {
            showFileUploadOptions()
        }
    }
    
    private fun showFileUploadOptions() {
        val options = arrayOf(
            "📄 Choose Document",
            "📊 Choose Spreadsheet", 
            "🖼️ Choose Image",
            "📸 Take Photo",
            "📁 Browse All Files"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Add File")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openDocumentPicker()
                    1 -> openSpreadsheetPicker()
                    2 -> openImagePicker()
                    3 -> takePhoto()
                    4 -> openFilePicker()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun openDocumentPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            val mimeTypes = arrayOf(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "text/plain"
            )
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(intent)
    }
    
    private fun openSpreadsheetPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            val mimeTypes = arrayOf(
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "text/csv"
            )
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(intent)
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(intent)
    }
    
    private fun takePhoto() {
        val photoFile = createImageFile()
        photoFile?.let { file ->
            currentPhotoPath = file.absolutePath
            val photoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            }
            cameraLauncher.launch(intent)
        }
    }
    
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(intent)
    }
    
    private fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"
            val storageDir = requireContext().getExternalFilesDir("Pictures")
            File.createTempFile(imageFileName, ".jpg", storageDir)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun processSelectedFile(uri: Uri) {
        uploadFileBtn.isEnabled = false
        uploadFileBtn.text = "Processing..."
        updateStatus("📄 Processing file...")
        
        lifecycleScope.launch {
            try {
                val processedFile = fileProcessor.processFileFromUri(uri)
                if (processedFile != null) {
                    processedFiles.add(0, processedFile)
                    requireActivity().runOnUiThread {
                        setupFilesList()
                        updateFileStats()
                        updateStatus("✅ File processed successfully!")
                        Toast.makeText(requireContext(), "File processed: ${processedFile.name}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Failed to process file", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                requireActivity().runOnUiThread {
                    uploadFileBtn.isEnabled = true
                    uploadFileBtn.text = "Upload File"
                    updateStatus("Ready")
                }
            }
        }
    }
    
    private fun loadProcessedFiles() {
        // For now, start with empty list. In a real app, you'd load from database/storage
        processedFiles.clear()
        setupFilesList()
    }
    
    private fun setupFilesList() {
        val adapter = ProcessedFileAdapter(requireContext(), processedFiles)
        filesList.adapter = adapter
    }
    
    private fun updateFileStats() {
        val totalSize = processedFiles.sumOf { it.size }
        val analyzedCount = processedFiles.count { it.isAnalyzed }
        
        uploadCountText.text = "${processedFiles.size} files uploaded"
        storageUsedText.text = "Storage: ${formatFileSize(totalSize)}"
        
        when {
            processedFiles.isEmpty() -> {
                analysisStatusText.text = "📁 No files uploaded yet"
                analysisStatusText.setTextColor(requireContext().getColor(android.R.color.holo_blue_dark))
            }
            analyzedCount == processedFiles.size -> {
                analysisStatusText.text = "📄 All files analyzed and ready"
                analysisStatusText.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
            }
            analyzedCount > 0 -> {
                analysisStatusText.text = "⏳ ${processedFiles.size - analyzedCount} files pending analysis"
                analysisStatusText.setTextColor(requireContext().getColor(android.R.color.holo_orange_dark))
            }
            else -> {
                analysisStatusText.text = "📁 Files ready for analysis"
                analysisStatusText.setTextColor(requireContext().getColor(android.R.color.holo_blue_dark))
            }
        }
    }
    
    private fun analyzeAllFiles() {
        val unanalyzedFiles = processedFiles.filter { !it.isAnalyzed }
        
        if (unanalyzedFiles.isEmpty()) {
            Toast.makeText(requireContext(), "✅ All files are already analyzed!", Toast.LENGTH_SHORT).show()
            return
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Analyze Files")
            .setMessage("Re-analyze ${unanalyzedFiles.size} files? This will update their summaries.")
            .setPositiveButton("Analyze") { _, _ ->
                Toast.makeText(requireContext(), "� Analysis feature coming soon!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showFileDetails(file: ProcessedFile) {
        val uploadDate = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(file.uploadTime))
        
        val details = buildString {
            append("📄 File Details\n\n")
            append("Name: ${file.name}\n")
            append("Type: ${file.type.name}\n")
            append("Size: ${formatFileSize(file.size)}\n")
            append("Uploaded: $uploadDate\n")
            append("Status: ${if (file.isAnalyzed) "✅ Analyzed" else "⏳ Pending"}\n\n")
            append("Summary:\n${file.summary}\n\n")
            
            if (file.extractedText.isNotEmpty() && file.extractedText.length > 100) {
                append("Content Preview:\n")
                append("${file.extractedText.take(200)}...")
                append("\n\n")
            }
            
            append("Metadata:\n")
            file.metadata.forEach { (key, value) ->
                append("• ${key.replace("_", " ").capitalize()}: $value\n")
            }
            
            append("\nAI Brother can help you:\n")
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
    
    private fun askAIAboutFile(file: ProcessedFile) {
        Toast.makeText(requireContext(), "💬 Switching to chat to discuss ${file.name}", Toast.LENGTH_SHORT).show()
        // Switch to chat and potentially pre-fill a message
        activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.nav_chat
    }
    
    private fun deleteFile(file: ProcessedFile) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete File")
            .setMessage("Are you sure you want to delete ${file.name}? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    val success = fileProcessor.deleteFile(file)
                    if (success) {
                        processedFiles.remove(file)
                        requireActivity().runOnUiThread {
                            setupFilesList()
                            updateFileStats()
                            Toast.makeText(requireContext(), "File deleted successfully", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Failed to delete file", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showStorageManagement() {
        val storageInfo = buildString {
            val totalSize = processedFiles.sumOf { it.size }
            val largestFile = processedFiles.maxByOrNull { it.size }
            
            append("💾 Storage Management\n\n")
            append("Total Files: ${processedFiles.size}\n")
            append("Storage Used: ${formatFileSize(totalSize)}\n")
            if (processedFiles.isNotEmpty()) {
                append("Average File Size: ${formatFileSize(totalSize / processedFiles.size)}\n")
            }
            append("Largest File: ${largestFile?.name ?: "None"} (${if (largestFile != null) formatFileSize(largestFile.size) else "0"})\n\n")
            
            if (processedFiles.isNotEmpty()) {
                append("Storage by Type:\n")
                val typeGroups = processedFiles.groupBy { it.type }
                typeGroups.forEach { (type, files) ->
                    val typeSize = files.sumOf { it.size }
                    append("• $type: ${files.size} files (${formatFileSize(typeSize)})\n")
                }
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
        val failedFiles = processedFiles.filter { !it.isAnalyzed }
        if (failedFiles.isNotEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("Clean Up Failed Files")
                .setMessage("Delete ${failedFiles.size} unanalyzed files?")
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch {
                        failedFiles.forEach { file ->
                            fileProcessor.deleteFile(file)
                        }
                        processedFiles.removeAll(failedFiles)
                        requireActivity().runOnUiThread {
                            setupFilesList()
                            updateFileStats()
                            Toast.makeText(requireContext(), "🗑️ ${failedFiles.size} failed files removed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
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
    
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024 * 1024)} GB"
            bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
            else -> "$bytes bytes"
        }
    }
    
    private class ProcessedFileAdapter(
        context: Context,
        private val files: List<ProcessedFile>
    ) : ArrayAdapter<ProcessedFile>(context, 0, files) {
        
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(
                android.R.layout.simple_list_item_2, parent, false
            )
            
            val file = files[position]
            val uploadDate = SimpleDateFormat("MMM dd HH:mm", Locale.getDefault()).format(Date(file.uploadTime))
            
            val titleView = view.findViewById<TextView>(android.R.id.text1)
            val subtitleView = view.findViewById<TextView>(android.R.id.text2)
            
            titleView.text = "${file.name} (${formatFileSize(file.size)})"
            subtitleView.text = "${file.type.name} • ${if (file.isAnalyzed) "✅ Analyzed" else "⏳ Processing"} • $uploadDate"
            
            return view
        }
        
        private fun formatFileSize(bytes: Long): String {
            return when {
                bytes >= 1024 * 1024 * 1024 -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
                bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
                bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
                else -> "$bytes bytes"
            }
        }
    }
}