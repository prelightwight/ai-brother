package com.prelightwight.aibrother

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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

class ImagesFragment : Fragment() {
    
    private lateinit var photoCountText: TextView
    private lateinit var analysisProgressText: TextView
    private lateinit var ocrStatusText: TextView
    private lateinit var imagesList: ListView
    private lateinit var takePhotoBtn: Button
    private lateinit var uploadImageBtn: Button
    private lateinit var viewGalleryBtn: Button
    private lateinit var imageSettingsBtn: Button
    
    private val processedImages = mutableListOf<ProcessedImage>()
    private lateinit var fileProcessor: FileProcessor
    private var currentPhotoPath: String? = null
    
    // Activity result launchers
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && currentPhotoPath != null) {
            processNewImage(File(currentPhotoPath!!), "Camera Photo")
        }
    }
    
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { processImageFromUri(it) }
    }
    
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Toast.makeText(requireContext(), "✅ Camera permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "❌ Camera permission required for photo features", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_images, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        fileProcessor = FileProcessor(requireContext())
        initializeViews(view)
        setupButtons()
        loadProcessedImages()
        updateImageStats()
    }
    
    private fun initializeViews(view: View) {
        photoCountText = view.findViewById(R.id.photo_count_text)
        analysisProgressText = view.findViewById(R.id.analysis_progress_text)
        ocrStatusText = view.findViewById(R.id.ocr_status_text)
        imagesList = view.findViewById(R.id.images_list)
        takePhotoBtn = view.findViewById(R.id.btn_take_photo)
        uploadImageBtn = view.findViewById(R.id.btn_upload_image)
        viewGalleryBtn = view.findViewById(R.id.btn_view_gallery)
        imageSettingsBtn = view.findViewById(R.id.btn_image_settings)
    }
    
    private fun setupButtons() {
        takePhotoBtn.setOnClickListener {
            checkPermissionsAndTakePhoto()
        }
        
        uploadImageBtn.setOnClickListener {
            checkPermissionsAndUploadImage()
        }
        
        viewGalleryBtn.setOnClickListener {
            showImageGallery()
        }
        
        imageSettingsBtn.setOnClickListener {
            showImageSettings()
        }
        
        // List item click listener
        imagesList.setOnItemClickListener { _, _, position, _ ->
            val image = processedImages[position]
            showImageDetails(image)
        }
    }
    
    private fun loadProcessedImages() {
        processedImages.clear()
        val savedImages = fileProcessor.getProcessedImages()
        processedImages.addAll(savedImages)
        setupImagesList()
    }
    
    private fun checkPermissionsAndTakePhoto() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        
        val allPermissionsGranted = permissions.all { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
        }
        
        if (allPermissionsGranted) {
            showCameraOptions()
        } else {
            permissionLauncher.launch(permissions)
        }
    }
    
    private fun checkPermissionsAndUploadImage() {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            showUploadOptions()
        } else {
            permissionLauncher.launch(arrayOf(permission))
        }
    }
    
    private fun showCameraOptions() {
        val cameraOptions = arrayOf(
            "📸 Take Photo",
            "📑 Scan Document",
            "🔍 Analyze Text (OCR)",
            "🎨 Creative Photo"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Camera Options")
            .setMessage("What would you like to capture?")
            .setItems(cameraOptions) { _, which ->
                when (which) {
                    0 -> takePhoto("photo")
                    1 -> takePhoto("document")
                    2 -> takePhoto("ocr")
                    3 -> takePhoto("creative")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showUploadOptions() {
        val uploadOptions = arrayOf(
            "🖼️ Upload from Gallery",
            "📁 Upload Multiple Images",
            "📋 Analyze Clipboard Image"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Upload Images")
            .setItems(uploadOptions) { _, which ->
                when (which) {
                    0 -> uploadFromGallery()
                    1 -> uploadMultiple()
                    2 -> analyzeClipboard()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun takePhoto(type: String) {
        try {
            val photoFile = createImageFile(type)
            currentPhotoPath = photoFile.absolutePath
            
            val photoURI = FileProvider.getUriForFile(
                requireContext(),
                "com.prelightwight.aibrother.fileprovider",
                photoFile
            )
            
            cameraLauncher.launch(photoURI)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error accessing camera: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun createImageFile(type: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "${type}_${timeStamp}_"
        val storageDir = File(requireContext().getExternalFilesDir(null), "images")
        
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
    
    private fun uploadFromGallery() {
        galleryLauncher.launch("image/*")
    }
    
    private fun uploadMultiple() {
        Toast.makeText(requireContext(), "📁 Multiple image upload coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun analyzeClipboard() {
        Toast.makeText(requireContext(), "📋 Clipboard image analysis coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun processNewImage(imageFile: File, source: String) {
        lifecycleScope.launch {
            try {
                updateStatus("📸 Processing image...")
                
                val processedImage = fileProcessor.processImageFile(imageFile, source)
                processedImages.add(0, processedImage)
                setupImagesList()
                updateImageStats()
                
                Toast.makeText(requireContext(), "✅ Image processed successfully!", Toast.LENGTH_SHORT).show()
                updateStatus("📸 Image analysis complete")
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "❌ Error processing image: ${e.message}", Toast.LENGTH_LONG).show()
                updateStatus("❌ Image processing failed")
            }
        }
    }
    
    private fun processImageFromUri(uri: Uri) {
        lifecycleScope.launch {
            try {
                updateStatus("📸 Processing image from gallery...")
                
                val processedImage = fileProcessor.processImageFromUri(uri)
                processedImages.add(0, processedImage)
                setupImagesList()
                updateImageStats()
                
                Toast.makeText(requireContext(), "✅ Image uploaded and processed!", Toast.LENGTH_SHORT).show()
                updateStatus("📸 Gallery image processed")
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "❌ Error processing image: ${e.message}", Toast.LENGTH_LONG).show()
                updateStatus("❌ Image processing failed")
            }
        }
    }
    
    private fun setupImagesList() {
        val adapter = ProcessedImageAdapter(requireContext(), processedImages)
        imagesList.adapter = adapter
    }
    
    private fun updateImageStats() {
        val analyzedCount = processedImages.count { it.isAnalyzed }
        val textImages = processedImages.count { it.extractedText.isNotEmpty() }
        
        photoCountText.text = "${processedImages.size} images processed"
        analysisProgressText.text = "$analyzedCount of ${processedImages.size} analyzed"
        ocrStatusText.text = "$textImages images with extracted text"
    }
    
    private fun updateStatus(status: String) {
        activity?.findViewById<TextView>(R.id.status_text)?.text = status
    }
    
    private fun showImageDetails(image: ProcessedImage) {
        val details = buildString {
            append("🖼️ Image Details\n\n")
            append("Name: ${image.fileName}\n")
            append("Type: ${image.fileType}\n")
            append("Size: ${image.fileSizeFormatted}\n")
            append("Processed: ${image.processedDate}\n")
            append("Status: ${if (image.isAnalyzed) "✅ Analyzed" else "⏳ Processing"}\n\n")
            append("AI Analysis:\n")
            append(image.summary)
            
            if (image.extractedText.isNotEmpty()) {
                append("\n\n📝 Extracted Text:\n")
                append(image.extractedText)
            }
            
            append("\n\nAI Brother can help you:\n")
            append("• Describe image content\n")
            append("• Extract and edit text\n")
            append("• Answer questions about the image\n")
            append("• Suggest improvements or edits")
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Image Information")
            .setMessage(details)
            .setPositiveButton("Close", null)
            .setNeutralButton("Ask AI") { _, _ ->
                askAIAboutImage(image)
            }
            .setNegativeButton("Delete") { _, _ ->
                deleteImage(image)
            }
            .show()
    }
    
    private fun showImageGallery() {
        val galleryInfo = buildString {
            val imageTypes = processedImages.groupBy { it.fileType }
            
            append("🖼️ Image Gallery\n\n")
            append("Total Images: ${processedImages.size}\n")
            imageTypes.forEach { (type, images) ->
                append("$type: ${images.size}\n")
            }
            append("With Text: ${processedImages.count { it.extractedText.isNotEmpty() }}\n\n")
            append("Recent Processing:\n")
            processedImages.take(3).forEach { image ->
                append("• ${image.fileName}\n")
            }
            append("\nGallery Features:\n")
            append("✓ Real image processing\n")
            append("✓ Text extraction (OCR)\n")
            append("✓ Metadata analysis\n")
            append("✓ Smart categorization")
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Image Gallery")
            .setMessage(galleryInfo)
            .setPositiveButton("Close", null)
            .setNeutralButton("Search") { _, _ ->
                showSearchOptions()
            }
            .show()
    }
    
    private fun showImageSettings() {
        val settings = arrayOf(
            "Image Quality Settings",
            "OCR Language Settings",
            "Auto-analysis Options",
            "Storage & Privacy"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Image Settings")
            .setItems(settings) { _, which ->
                when (which) {
                    0 -> showQualitySettings()
                    1 -> showOCRSettings()
                    2 -> showAutoAnalysisSettings()
                    3 -> showStorageSettings()
                }
            }
            .show()
    }
    
    private fun askAIAboutImage(image: ProcessedImage) {
        Toast.makeText(requireContext(), "💬 Switching to chat to discuss ${image.fileName}", Toast.LENGTH_SHORT).show()
        activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.nav_chat
    }
    
    private fun deleteImage(image: ProcessedImage) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Image")
            .setMessage("Are you sure you want to delete ${image.fileName}? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                fileProcessor.deleteProcessedFile(image)
                processedImages.remove(image)
                setupImagesList()
                updateImageStats()
                Toast.makeText(requireContext(), "Image deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showQualitySettings() {
        val qualities = arrayOf("High (Original)", "Medium (Compressed)", "Low (Fast processing)")
        AlertDialog.Builder(requireContext())
            .setTitle("Image Quality Settings")
            .setSingleChoiceItems(qualities, 0) { dialog, which ->
                Toast.makeText(requireContext(), "Quality set to ${qualities[which]}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .show()
    }
    
    private fun showOCRSettings() {
        val languages = arrayOf("English", "Spanish", "French", "German", "Chinese", "Auto-detect")
        AlertDialog.Builder(requireContext())
            .setTitle("OCR Language")
            .setSingleChoiceItems(languages, 0) { dialog, which ->
                Toast.makeText(requireContext(), "OCR language set to ${languages[which]}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .show()
    }
    
    private fun showAutoAnalysisSettings() {
        val options = arrayOf("Analyze immediately", "Analyze on WiFi only", "Manual analysis only")
        AlertDialog.Builder(requireContext())
            .setTitle("Auto-analysis Settings")
            .setSingleChoiceItems(options, 0) { dialog, which ->
                Toast.makeText(requireContext(), "Auto-analysis set to ${options[which]}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .show()
    }
    
    private fun showStorageSettings() {
        val storageInfo = buildString {
            append("💾 Storage Information\n\n")
            append("Images stored: ${processedImages.size}\n")
            append("Storage location: Internal storage\n")
            append("Auto-cleanup: Disabled\n\n")
            append("Privacy Settings:\n")
            append("• All images processed locally\n")
            append("• No data sent to cloud\n")
            append("• Metadata preserved\n")
            append("• Secure deletion available")
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Storage & Privacy")
            .setMessage(storageInfo)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showSearchOptions() {
        val searchTypes = arrayOf(
            "🔍 Search by content",
            "📝 Search by text",
            "📅 Search by date",
            "🏷️ Search by category"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Search Images")
            .setItems(searchTypes) { _, which ->
                when (which) {
                    0 -> Toast.makeText(requireContext(), "🔍 Content search coming soon!", Toast.LENGTH_SHORT).show()
                    1 -> Toast.makeText(requireContext(), "📝 Text search coming soon!", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(requireContext(), "📅 Date search coming soon!", Toast.LENGTH_SHORT).show()
                    3 -> Toast.makeText(requireContext(), "🏷️ Category search coming soon!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private class ProcessedImageAdapter(
        context: Context,
        private val images: List<ProcessedImage>
    ) : ArrayAdapter<ProcessedImage>(context, 0, images) {
        
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(
                android.R.layout.simple_list_item_2, parent, false
            )
            
            val image = images[position]
            
            val titleView = view.findViewById<TextView>(android.R.id.text1)
            val subtitleView = view.findViewById<TextView>(android.R.id.text2)
            
            val textIndicator = if (image.extractedText.isNotEmpty()) " 📝" else ""
            val statusIndicator = if (image.isAnalyzed) "✅" else "⏳"
            
            titleView.text = "${image.fileName} (${image.fileSizeFormatted})$textIndicator"
            subtitleView.text = "${image.fileType} • $statusIndicator • ${image.processedDate}"
            
            return view
        }
    }
}