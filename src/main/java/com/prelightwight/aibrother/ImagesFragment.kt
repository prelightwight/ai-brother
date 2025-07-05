package com.prelightwight.aibrother

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
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
    
    private val capturedImages = mutableListOf<ImageInfo>()
    private lateinit var imagesDirectory: File
    private var currentPhotoPath: String? = null
    
    // Camera permission launcher
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCameraOptions()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required to take photos", Toast.LENGTH_LONG).show()
        }
    }
    
    // Camera launcher
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoPath != null) {
            processNewPhoto(currentPhotoPath!!)
        }
    }
    
    // Gallery launcher
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { handleSelectedImage(it) }
    }
    
    // Multiple images launcher
    private val multipleImagesLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                if (data.clipData != null) {
                    // Multiple images selected
                    val clipData = data.clipData!!
                    for (i in 0 until clipData.itemCount) {
                        handleSelectedImage(clipData.getItemAt(i).uri)
                    }
                } else if (data.data != null) {
                    // Single image selected
                    handleSelectedImage(data.data!!)
                }
            }
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
        
        initializeViews(view)
        setupButtons()
        initializeImagesDirectory()
        loadExistingImages()
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
    
    private fun initializeImagesDirectory() {
        imagesDirectory = File(requireContext().getExternalFilesDir(null), "captured_images")
        if (!imagesDirectory.exists()) {
            imagesDirectory.mkdirs()
        }
    }

    private fun setupButtons() {
        takePhotoBtn.setOnClickListener {
            // Direct camera access for better user experience
            checkCameraPermissionAndTakePhoto()
        }
        
        uploadImageBtn.setOnClickListener {
            // Direct gallery access for better user experience
            showImageUploadOptions()
        }
        
        viewGalleryBtn.setOnClickListener {
            showImageGallery()
        }
        
        imageSettingsBtn.setOnClickListener {
            showImageSettings()
        }
        
        // List item click listener
        imagesList.setOnItemClickListener { _, _, position, _ ->
            val image = capturedImages[position]
            showImageDetails(image)
        }
    }
    
    private fun checkCameraPermissionAndTakePhoto() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted, show camera options
                openCameraOptions()
            }
            else -> {
                // Request permission
                updateStatus("📷 Requesting camera permission...")
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun openCameraOptions() {
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
    
    private fun takePhoto(type: String) {
        try {
            updateStatus("📷 Opening camera...")
            
            val photoFile = createImageFile(type)
            val photoURI = FileProvider.getUriForFile(
                requireContext(),
                "com.prelightwight.aibrother.fileprovider",
                photoFile
            )
            currentPhotoPath = photoFile.absolutePath
            cameraLauncher.launch(photoURI)
            
        } catch (e: Exception) {
            updateStatus("❌ Camera error")
            Toast.makeText(requireContext(), "Error opening camera: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun createImageFile(type: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${type}_${timeStamp}.jpg"
        return File(imagesDirectory, fileName)
    }
    
    private fun processNewPhoto(imagePath: String) {
        val imageFile = File(imagePath)
        if (!imageFile.exists()) {
            Toast.makeText(requireContext(), "Photo capture failed", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val fileName = imageFile.name
                val fileSize = imageFile.length()
                val imageType = when {
                    fileName.startsWith("document_") || fileName.startsWith("ocr_") -> "Document"
                    else -> "Photo"
                }
                
                val imageInfo = ImageInfo(
                    name = fileName,
                    type = imageType,
                    size = formatFileSize(fileSize),
                    captureTime = "Just now",
                    status = "⏳ Analyzing...",
                    description = "Processing captured image...",
                    hasText = fileName.startsWith("document_") || fileName.startsWith("ocr"),
                    ocrText = "",
                    localPath = imagePath
                )
                
                capturedImages.add(0, imageInfo)
                setupImagesList()
                updateImageStats()
                updateStatus("📸 Photo captured successfully")
                
                // Start analysis
                analyzeImage(imageInfo)
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error processing photo: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showImageUploadOptions() {
        val uploadOptions = arrayOf(
            "🖼️ Upload from Gallery",
            "📁 Upload Multiple Images", 
            "🎯 Import Specific Image Type"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Upload Images")
            .setItems(uploadOptions) { _, which ->
                try {
                    when (which) {
                        0 -> {
                            updateStatus("📂 Opening gallery...")
                            galleryLauncher.launch("image/*")
                        }
                        1 -> {
                            updateStatus("📂 Opening multiple image picker...")
                            openMultipleImagePicker()
                        }
                        2 -> {
                            updateStatus("📂 Opening image type selector...")
                            showImageTypeSelection()
                        }
                    }
                } catch (e: Exception) {
                    updateStatus("❌ Error opening image picker")
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun updateStatus(message: String) {
        activity?.findViewById<TextView>(R.id.status_text)?.text = message
    }
    
    private fun openMultipleImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        multipleImagesLauncher.launch(Intent.createChooser(intent, "Select Multiple Images"))
    }
    
    private fun showImageTypeSelection() {
        val types = arrayOf("Photos", "Screenshots", "Documents", "Artwork")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Image Type")
            .setItems(types) { _, which ->
                galleryLauncher.launch("image/*")
            }
            .show()
    }
    
    private fun handleSelectedImage(uri: Uri) {
        lifecycleScope.launch {
            try {
                val fileName = getImageFileName(uri)
                val localFile = File(imagesDirectory, fileName)
                
                withContext(Dispatchers.IO) {
                    copyUriToFile(uri, localFile)
                }
                
                val fileSize = localFile.length()
                val imageInfo = ImageInfo(
                    name = fileName,
                    type = "Photo",
                    size = formatFileSize(fileSize),
                    captureTime = "Just now",
                    status = "⏳ Analyzing...",
                    description = "Processing uploaded image...",
                    hasText = false,
                    ocrText = "",
                    localPath = localFile.absolutePath
                )
                
                capturedImages.add(0, imageInfo)
                setupImagesList()
                updateImageStats()
                
                // Start analysis
                analyzeImage(imageInfo)
                
                Toast.makeText(requireContext(), "✅ Image uploaded: $fileName", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "❌ Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private suspend fun analyzeImage(imageInfo: ImageInfo) {
        withContext(Dispatchers.IO) {
            try {
                Thread.sleep(2000) // Simulate processing time
                
                // Simulate image analysis
                val description = generateImageDescription(imageInfo)
                var ocrText = ""
                
                // If it's a document type, simulate OCR
                if (imageInfo.hasText || imageInfo.name.contains("document") || imageInfo.name.contains("ocr")) {
                    ocrText = performSimpleOCR(imageInfo.localPath)
                }
                
                withContext(Dispatchers.Main) {
                    imageInfo.status = "✅ Analyzed"
                    imageInfo.description = description
                    if (ocrText.isNotEmpty()) {
                        imageInfo.ocrText = ocrText
                    }
                    setupImagesList()
                    updateImageStats()
                    
                    if (ocrText.isNotEmpty()) {
                        showOCRResults(imageInfo)
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    imageInfo.status = "❌ Analysis failed"
                    imageInfo.description = "Could not analyze image: ${e.message}"
                    setupImagesList()
                    updateImageStats()
                }
            }
        }
    }
    
    private fun generateImageDescription(imageInfo: ImageInfo): String {
        // Simple image analysis based on file characteristics
        val bitmap = BitmapFactory.decodeFile(imageInfo.localPath)
        if (bitmap != null) {
            val width = bitmap.width
            val height = bitmap.height
            val isLandscape = width > height
            
            return when {
                imageInfo.name.startsWith("document_") -> 
                    "Document image ($width x $height) - appears to contain text and structured content"
                imageInfo.name.startsWith("creative_") -> 
                    "Creative photograph with artistic composition and ${if (isLandscape) "landscape" else "portrait"} orientation"
                imageInfo.name.startsWith("ocr_") -> 
                    "Text-focused image optimized for character recognition"
                else -> 
                    "Photograph ($width x $height) captured with good clarity and ${if (isLandscape) "landscape" else "portrait"} orientation"
            }
        }
        return "Image analysis completed"
    }
    
    private fun performSimpleOCR(imagePath: String): String {
        // Placeholder OCR functionality
        // In a real implementation, you'd use a library like ML Kit or Tesseract
        return when {
            File(imagePath).name.contains("business") -> 
                "John Smith\nSoftware Engineer\njohn@example.com\n555-123-4567"
            File(imagePath).name.contains("document") -> 
                "Sample document text extracted from image.\nThis is a placeholder for real OCR functionality.\nActual text would be extracted here."
            File(imagePath).name.contains("receipt") -> 
                "Store Receipt\nItem 1: $12.99\nItem 2: $8.50\nTotal: $21.49"
            else -> 
                "Text content detected and extracted from image.\nThis would contain the actual recognized text in a real implementation."
        }
    }
    
    private fun loadExistingImages() {
        // This function is no longer needed as images are loaded directly from the directory
        // but keeping it for now to avoid breaking existing calls.
        // For a real app, you'd scan the directory for existing files.
    }
    
    private fun setupImagesList() {
        val adapter = ImageAdapter(requireContext(), capturedImages)
        imagesList.adapter = adapter
    }
    
    private fun updateImageStats() {
        val analyzedCount = capturedImages.count { it.status.contains("✅") }
        val textImages = capturedImages.count { it.hasText && it.ocrText.isNotEmpty() }
        
        photoCountText.text = "${capturedImages.size} images uploaded"
        analysisProgressText.text = "$analyzedCount of ${capturedImages.size} analyzed"
        ocrStatusText.text = "$textImages images with extracted text"
    }
    
    private fun showImageDetails(image: ImageInfo) {
        val details = buildString {
            append("🖼️ Image Details\n\n")
            append("Name: ${image.name}\n")
            append("Type: ${image.type}\n")
            append("Size: ${image.size}\n")
            append("Captured: ${image.captureTime}\n")
            append("Status: ${image.status}\n\n")
            append("AI Analysis:\n")
            append(image.description)
            
            if (image.hasText && image.ocrText.isNotEmpty()) {
                append("\n\n📝 Extracted Text:\n")
                append(image.ocrText)
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
            val photos = capturedImages.filter { it.type == "Photo" }
            val documents = capturedImages.filter { it.type == "Document" }
            
            append("🖼️ Image Gallery\n\n")
            append("Total Images: ${capturedImages.size}\n")
            append("Photos: ${photos.size}\n")
            append("Documents: ${documents.size}\n")
            append("With Text: ${capturedImages.count { it.hasText }}\n\n")
            append("Recent Categories:\n")
            append("• Nature & Landscapes\n")
            append("• Documents & Text\n")
            append("• People & Events\n")
            append("• Screenshots & UI\n\n")
            append("Gallery Features:\n")
            append("✓ AI-powered search\n")
            append("✓ Automatic categorization\n")
            append("✓ Text extraction (OCR)\n")
            append("✓ Smart organization")
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
    
    private fun askAIAboutImage(image: ImageInfo) {
        Toast.makeText(requireContext(), "� Switching to chat to discuss ${image.name}", Toast.LENGTH_SHORT).show()
        activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.nav_chat
    }
    
    private fun deleteImage(image: ImageInfo) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Image")
            .setMessage("Are you sure you want to delete ${image.name}? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                capturedImages.remove(image)
                setupImagesList()
                updateImageStats()
                Toast.makeText(requireContext(), "Image deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showSearchOptions() {
        Toast.makeText(requireContext(), "🔍 AI-powered image search coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    private fun getImageFileName(uri: Uri): String {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    return it.getString(nameIndex) ?: "image_${System.currentTimeMillis()}.jpg"
                }
            }
        }
        return uri.lastPathSegment ?: "image_${System.currentTimeMillis()}.jpg"
    }

    private fun copyUriToFile(uri: Uri, localFile: File) {
        requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
            localFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
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
    

    
    private fun Double.format(digits: Int): String = "%.${digits}f".format(this)
    
    private fun showOCRResults(image: ImageInfo) {
        AlertDialog.Builder(requireContext())
            .setTitle("Text Extracted! 📝")
            .setMessage("Successfully extracted text from ${image.name}:\n\n${image.ocrText}\n\nThe extracted text is now searchable and can be edited or copied.")
            .setPositiveButton("Copy Text") { _, _ ->
                copyTextToClipboard(image.ocrText)
                Toast.makeText(requireContext(), "📋 Text copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Edit") { _, _ ->
                editExtractedText(image)
            }
            .setNegativeButton("Close", null)
            .show()
    }
    
    private fun copyTextToClipboard(text: String) {
        val clipboard = ContextCompat.getSystemService(requireContext(), android.content.ClipboardManager::class.java)
        val clip = android.content.ClipData.newPlainText("Extracted Text", text)
        clipboard?.setPrimaryClip(clip)
    }
    
    private fun editExtractedText(image: ImageInfo) {
        val editText = EditText(requireContext()).apply {
            setText(image.ocrText)
            setSingleLine(false)
            maxLines = 10
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Extracted Text")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                image.ocrText = editText.text.toString()
                setupImagesList()
                Toast.makeText(requireContext(), "✅ Text updated!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun generatePhotoDescription(): String {
        val descriptions = arrayOf(
            "A vibrant outdoor scene with natural lighting and interesting composition",
            "Indoor photograph with good lighting and clear subject focus",
            "Landscape image showing beautiful scenery and natural elements",
            "Portrait or group photo with people in a casual setting",
            "Close-up shot with detailed textures and rich colors",
            "Architectural or urban scene with geometric elements"
        )
        return descriptions.random()
    }
    
    private fun generateCreativeDescription(): String {
        val descriptions = arrayOf(
            "Artistic composition with strong visual elements and creative use of light and shadow",
            "Abstract or experimental image with unique perspective and interesting color palette",
            "Creative photography with artistic filters and enhanced visual appeal",
            "Stylized image with artistic elements and creative composition techniques"
        )
        return descriptions.random()
    }
    
    private fun showQualitySettings() {
        val qualities = arrayOf("High (Original)", "Medium (Compressed)", "Low (Fast)")
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
        val options = arrayOf(
            "Auto-analyze photos",
            "Auto-extract text",
            "Auto-categorize images",
            "Background processing"
        )
        
        val checkedItems = booleanArrayOf(true, true, false, true)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Auto-Analysis Settings")
            .setMultiChoiceItems(options, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Save") { _, _ ->
                Toast.makeText(requireContext(), "⚙️ Settings saved!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showStorageSettings() {
        val storageInfo = buildString {
            val totalSizeBytes = capturedImages.sumOf { it.size.toLongOrNull() ?: 0L }
            val totalSize = formatFileSize(totalSizeBytes)
            append("💾 Image Storage Settings\n\n")
            append("Current Usage: ${capturedImages.size} images\n")
            append("Total Size: $totalSize\n")
            append("Storage Location: Internal app storage\n")
            append("Auto-cleanup: Disabled\n\n")
            append("Options:\n")
            append("• Enable auto-cleanup after 30 days\n")
            append("• Compress images to save space\n")
            append("• Export images to external storage\n")
            append("• Clear all cached thumbnails")
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Storage Settings")
            .setMessage(storageInfo)
            .setPositiveButton("Configure") { _, _ ->
                Toast.makeText(requireContext(), "💾 Storage configuration coming soon!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    data class ImageInfo(
        val name: String,
        val type: String,
        val size: String,
        val captureTime: String,
        var status: String,
        var description: String,
        val hasText: Boolean,
        var ocrText: String,
        val localPath: String
    )

    private class ImageAdapter(
        context: Context,
        private val images: List<ImageInfo>
    ) : ArrayAdapter<ImageInfo>(context, 0, images) {
        
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(
                android.R.layout.simple_list_item_2, parent, false
            )
            
            val image = images[position]
            
            val titleView = view.findViewById<TextView>(android.R.id.text1)
            val subtitleView = view.findViewById<TextView>(android.R.id.text2)
            
            val textIndicator = if (image.hasText && image.ocrText.isNotEmpty()) " 📝" else ""
            val typeIcon = when (image.type) {
                "Document" -> "📑"
                "Photo" -> "📸"
                else -> "🖼️"
            }
            
            titleView.text = "$typeIcon ${image.name} (${image.size})$textIndicator"
            subtitleView.text = "${image.type} • ${image.status} • ${image.captureTime}"
            
            return view
        }
    }
}