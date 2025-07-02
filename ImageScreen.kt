package com.prelightwight.aibrother.images

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class AnalyzedImage(
    val uri: Uri,
    val bitmap: Bitmap?,
    val fileName: String,
    val analysis: String,
    val confidence: Float,
    val tags: List<String>,
    val analyzedAt: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageScreen() {
    var images by remember { mutableStateOf(listOf<AnalyzedImage>()) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<AnalyzedImage?>(null) }
    var showImageDetail by remember { mutableStateOf(false) }
    var showCaptureMethods by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Create a temporary file for camera capture
    val tempImageFile = remember {
        File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
    }
    
    val tempImageUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempImageFile
        )
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                coroutineScope.launch {
                    analyzeImages(context, uris) { analyzedImage ->
                        images = images + analyzedImage
                    }
                }
            }
        }
    )
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                coroutineScope.launch {
                    analyzeImages(context, listOf(tempImageUri)) { analyzedImage ->
                        images = images + analyzedImage
                    }
                }
            }
        }
    )
    
    fun analyzeImages(
        context: Context,
        uris: List<Uri>,
        onAnalyzed: (AnalyzedImage) -> Unit
    ) {
        coroutineScope.launch {
            isAnalyzing = true
            try {
                uris.forEach { uri ->
                    val bitmap = loadBitmapFromUri(context, uri)
                    val fileName = getFileNameFromUri(context, uri)
                    val analysis = analyzeImage(bitmap)
                    
                    val analyzedImage = AnalyzedImage(
                        uri = uri,
                        bitmap = bitmap,
                        fileName = fileName,
                        analysis = analysis.description,
                        confidence = analysis.confidence,
                        tags = analysis.tags
                    )
                    onAnalyzed(analyzedImage)
                }
            } finally {
                isAnalyzing = false
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🖼️ Image Analysis",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                if (images.isNotEmpty()) {
                    IconButton(onClick = { images = emptyList() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear All")
                    }
                }
                FilledTonalButton(
                    onClick = { showCaptureMethods = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Image")
                }
            }
        }

        // Statistics Cards
        if (images.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${images.size}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Images",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val avgConfidence = if (images.isNotEmpty()) {
                            (images.sumOf { it.confidence.toDouble() } / images.size * 100).toInt()
                        } else 0
                        Text(
                            text = "$avgConfidence%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Confidence",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val totalTags = images.flatMap { it.tags }.distinct().size
                        Text(
                            text = "$totalTags",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Tags",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        // Content Area
        if (images.isEmpty() && !isAnalyzing) {
            // Empty State
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No images analyzed yet",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Capture photos or select images from your gallery to get AI-powered analysis and descriptions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isAnalyzing) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Analyzing images...",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Using AI to identify objects, scenes, and generate descriptions",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                items(images) { image ->
                    ImageAnalysisCard(
                        image = image,
                        onClick = { 
                            selectedImage = image
                            showImageDetail = true
                        }
                    )
                }
            }
        }
    }
    
    // Capture Methods Bottom Sheet
    if (showCaptureMethods) {
        AlertDialog(
            onDismissRequest = { showCaptureMethods = false },
            title = { Text("Add Images") },
            text = {
                Column {
                    OutlinedButton(
                        onClick = {
                            showCaptureMethods = false
                            cameraLauncher.launch(tempImageUri)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Camera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Take Photo")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = {
                            showCaptureMethods = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose from Gallery")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCaptureMethods = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Image Detail Dialog
    if (showImageDetail && selectedImage != null) {
        AlertDialog(
            onDismissRequest = { showImageDetail = false },
            title = { Text(selectedImage!!.fileName) },
            text = {
                Column {
                    selectedImage!!.bitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    Text(
                        text = "Analysis:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedImage!!.analysis,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Confidence: ${(selectedImage!!.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (selectedImage!!.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tags: ${selectedImage!!.tags.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImageDetail = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ImageAnalysisCard(
    image: AnalyzedImage,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            // Image Thumbnail
            if (image.bitmap != null) {
                Image(
                    bitmap = image.bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = image.fileName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = dateFormat.format(Date(image.analyzedAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = image.analysis,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(image.confidence * 100).toInt()}% confidence",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (image.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${image.tags.size} tags",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Icon(
                Icons.Default.Visibility,
                contentDescription = "View Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper Functions
private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        inputStream?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    } catch (e: Exception) {
        null
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    return try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    it.getString(nameIndex) ?: "Unknown"
                } else "Unknown"
            } else "Unknown"
        } ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
}

data class ImageAnalysisResult(
    val description: String,
    val confidence: Float,
    val tags: List<String>
)

private fun analyzeImage(bitmap: Bitmap?): ImageAnalysisResult {
    // Enhanced mock implementation with realistic computer vision analysis
    // In production, use ML Kit, TensorFlow Lite, or cloud vision APIs
    
    if (bitmap == null) {
        return ImageAnalysisResult(
            description = "Unable to analyze: Image could not be loaded",
            confidence = 0.0f,
            tags = listOf("error")
        )
    }
    
    // Analyze image properties for realistic mock analysis
    val width = bitmap.width
    val height = bitmap.height
    val aspectRatio = width.toFloat() / height.toFloat()
    val isLandscape = aspectRatio > 1.3f
    val isPortrait = aspectRatio < 0.75f
    val isSquare = aspectRatio in 0.75f..1.3f
    
    // Sample pixel analysis for color characteristics
    val centerPixel = bitmap.getPixel(width / 2, height / 2)
    val red = (centerPixel shr 16) and 0xFF
    val green = (centerPixel shr 8) and 0xFF
    val blue = centerPixel and 0xFF
    val brightness = (red + green + blue) / 3
    
    val isDark = brightness < 80
    val isBright = brightness > 180
    val isColorful = kotlin.math.max(kotlin.math.max(red, green), blue) - 
                    kotlin.math.min(kotlin.math.min(red, green), blue) > 100
    
    // Determine dominant color characteristics
    val dominantColor = when {
        red > green + 30 && red > blue + 30 -> "reddish"
        green > red + 30 && green > blue + 30 -> "greenish"
        blue > red + 30 && blue > green + 30 -> "bluish"
        red + green > blue + 60 -> "warm"
        blue + green > red + 60 -> "cool"
        else -> "neutral"
    }
    
    // Generate realistic descriptions based on analysis
    val sceneTypes = when {
        isLandscape && isBright -> listOf(
            "outdoor landscape", "scenic view", "nature scene", "outdoor photography"
        )
        isPortrait && isDark -> listOf(
            "portrait photography", "indoor scene", "artistic composition", "dramatic lighting"
        )
        isSquare && isColorful -> listOf(
            "social media image", "artistic composition", "creative photography", "lifestyle image"
        )
        else -> listOf(
            "general photography", "digital image", "captured moment", "photographic content"
        )
    }
    
    val sceneType = sceneTypes.random()
    val lightingDesc = when {
        isBright -> "well-lit"
        isDark -> "low-light"
        else -> "balanced lighting"
    }
    
    val colorDesc = when {
        isColorful && dominantColor != "neutral" -> "vibrant $dominantColor tones"
        isColorful -> "rich and vibrant colors"
        dominantColor != "neutral" -> "$dominantColor color palette"
        else -> "natural color balance"
    }
    
    val description = "This appears to be ${sceneType.lowercase()} with $lightingDesc and $colorDesc. " +
            "The image shows good composition and clarity at ${width}x${height} resolution."
    
    // Generate relevant tags
    val tags = mutableListOf<String>().apply {
        add("photograph")
        add("digital")
        add(if (isLandscape) "landscape" else if (isPortrait) "portrait" else "square")
        add(if (isDark) "dark" else if (isBright) "bright" else "balanced")
        add(dominantColor)
        if (isColorful) add("colorful")
        add("${width}x${height}")
        if (width > 1920 || height > 1920) add("high-resolution")
        if (aspectRatio > 2.0f || aspectRatio < 0.5f) add("wide-format")
        when (sceneType) {
            "outdoor landscape" -> addAll(listOf("outdoor", "nature", "landscape"))
            "portrait photography" -> addAll(listOf("portrait", "people", "closeup"))
            "social media image" -> addAll(listOf("social", "lifestyle", "square"))
            else -> addAll(listOf("general", "scene"))
        }
        add("analyzed")
    }
    
    // Simulate realistic confidence based on image quality
    val confidence = when {
        width < 300 || height < 300 -> 0.60f + (Math.random() * 0.15f).toFloat()
        brightness in 50..200 && isColorful -> 0.85f + (Math.random() * 0.1f).toFloat()
        isDark || isBright -> 0.70f + (Math.random() * 0.15f).toFloat()
        else -> 0.75f + (Math.random() * 0.15f).toFloat()
    }
    
    return ImageAnalysisResult(
        description = description,
        confidence = confidence.coerceIn(0.0f, 1.0f),
        tags = tags.distinct().take(8) // Limit to most relevant tags
    )
}
