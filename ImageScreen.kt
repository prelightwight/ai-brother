package com.prelightwight.aibrother.images

import com.prelightwight.aibrother.ai.AIModelManager
import com.prelightwight.aibrother.data.AppDatabase
import com.prelightwight.aibrother.data.AnalyzedImageEntity
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.io.File
import java.io.FileOutputStream
import android.util.Log

data class AnalyzedImage(
    val id: String,
    val uri: Uri,
    val bitmap: Bitmap,
    val filename: String,
    val analysis: ImageAnalysis,
    val timestamp: Long
)

data class ImageAnalysis(
    val colors: List<String>,
    val objects: List<String>,
    val text: String?,
    val description: String,
    val confidence: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // AI Manager and Database
    val aiManager = remember { AIModelManager.getInstance(context) }
    val database = remember { AppDatabase.getDatabase(context) }
    
    // State
    var analyzedImages by remember { mutableStateOf(listOf<AnalyzedImageEntity>()) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<AnalyzedImageEntity?>(null) }
    
    // Initialize AI manager and load images
    LaunchedEffect(Unit) {
        aiManager.initialize()
        
        // Load analyzed images from database
        database.analyzedImageDao().getAllImages().collect { images ->
            analyzedImages = images
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                scope.launch {
                    isAnalyzing = true
                    
                    uris.forEach { uri ->
                        try {
                            val bitmap = loadBitmapFromUri(context, uri)
                            if (bitmap != null) {
                                val fileName = getImageFileName(context, uri)
                                val analysis = aiManager.analyzeImage(bitmap)
                                
                                val imageEntity = AnalyzedImageEntity(
                                    filename = fileName,
                                    originalUri = uri.toString(),
                                    localPath = saveImageToInternalStorage(context, bitmap, fileName),
                                    description = analysis.description,
                                    detectedObjects = analysis.objects.joinToString(","),
                                    dominantColors = analysis.colors.joinToString(","),
                                    extractedText = analysis.text,
                                    confidence = analysis.confidence
                                )
                                
                                database.analyzedImageDao().insertImage(imageEntity)
                            }
                        } catch (e: Exception) {
                            Log.e("ImageScreen", "Error analyzing image", e)
                        }
                    }
                    
                    isAnalyzing = false
                }
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            if (bitmap != null) {
                scope.launch {
                    isAnalyzing = true
                    val analysis = performImageAnalysis(bitmap)
                    val analyzedImage = AnalyzedImage(
                        id = System.currentTimeMillis().toString(),
                        uri = Uri.EMPTY, // Camera images don't have URIs
                        bitmap = bitmap,
                        filename = "Camera_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg",
                        analysis = analysis,
                        timestamp = System.currentTimeMillis()
                    )
                    analyzedImages = analyzedImages + analyzedImage
                    isAnalyzing = false
                }
            }
        }
    )

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(
            text = "🖼️ Image Analysis",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { cameraLauncher.launch(null) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Camera")
            }

            Button(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gallery")
            }

            Button(
                onClick = { analyzedImages = emptyList() },
                enabled = analyzedImages.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Clear, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isAnalyzing) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Analyzing images...")
                }
            }
        }

        if (analyzedImages.isNotEmpty()) {
            Text(
                text = "Analyzed Images (${analyzedImages.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn {
                items(analyzedImages) { image ->
                    ImageAnalysisItem(
                        image = image,
                        onClick = { selectedImage = image }
                    )
                }
            }
        } else if (!isAnalyzing) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No images analyzed yet",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Take a photo or select images from your gallery to get AI-powered analysis",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Image detail dialog
    selectedImage?.let { image ->
        ImageDetailDialog(
            image = image,
            onDismiss = { selectedImage = null },
            onDelete = {
                analyzedImages = analyzedImages.filter { it.id != image.id }
                selectedImage = null
            }
        )
    }
}

@Composable
fun ImageAnalysisItem(image: AnalyzedImage, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Image thumbnail
            Image(
                bitmap = image.bitmap.asImageBitmap(),
                contentDescription = image.filename,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Analysis info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = image.filename,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = image.analysis.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Confidence indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Analytics,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Confidence: ${(image.analysis.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Objects detected
                if (image.analysis.objects.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Objects: ${image.analysis.objects.take(3).joinToString(", ")}${if (image.analysis.objects.size > 3) "..." else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun ImageDetailDialog(
    image: AnalyzedImage,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(image.filename) },
        text = {
            LazyColumn {
                item {
                    // Large image preview
                    Image(
                        bitmap = image.bitmap.asImageBitmap(),
                        contentDescription = image.filename,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.5f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Analysis details
                    Text(
                        "Analysis Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Description:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        image.analysis.description,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Detected Objects:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (image.analysis.objects.isNotEmpty()) 
                            image.analysis.objects.joinToString(", ")
                        else "None detected",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Dominant Colors:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        image.analysis.colors.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (image.analysis.text?.isNotEmpty() == true) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Extracted Text:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            image.analysis.text,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Confidence: ${(image.analysis.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

private suspend fun analyzeImage(context: Context, uri: Uri): AnalyzedImage? {
    return try {
        val bitmap = loadBitmapFromUri(context, uri) ?: return null
        val filename = getImageFileName(context, uri)
        val analysis = performImageAnalysis(bitmap)

        AnalyzedImage(
            id = System.currentTimeMillis().toString(),
            uri = uri,
            bitmap = bitmap,
            filename = filename,
            analysis = analysis,
            timestamp = System.currentTimeMillis()
        )
    } catch (e: Exception) {
        null
    }
}

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        
        // Scale down if too large
        if (bitmap.width > 1024 || bitmap.height > 1024) {
            val scale = minOf(1024f / bitmap.width, 1024f / bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }
    } catch (e: Exception) {
        null
    }
}

private fun getImageFileName(context: Context, uri: Uri): String {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        cursor.getString(nameIndex)
    } ?: "Unknown_Image_${System.currentTimeMillis()}.jpg"
}

private fun performImageAnalysis(bitmap: Bitmap): ImageAnalysis {
    // Simulate AI image analysis - in real implementation, this would use actual ML models
    val randomObjects = listOf(
        listOf("person", "face", "smile"),
        listOf("cat", "animal", "pet"),
        listOf("car", "vehicle", "road"),
        listOf("tree", "nature", "outdoors"),
        listOf("food", "meal", "delicious"),
        listOf("building", "architecture", "urban"),
        listOf("flower", "plant", "garden"),
        listOf("book", "text", "reading")
    ).random()

    val randomColors = listOf(
        listOf("blue", "white", "gray"),
        listOf("green", "brown", "yellow"),
        listOf("red", "black", "white"),
        listOf("purple", "pink", "lavender"),
        listOf("orange", "gold", "cream")
    ).random()

    val descriptions = listOf(
        "A vibrant and colorful image with interesting composition",
        "Clear details visible with good lighting and contrast",
        "Natural scene with organic shapes and textures",
        "Well-composed photograph with balanced elements",
        "Dynamic image with movement and energy",
        "Peaceful and serene visual with soft tones",
        "Sharp focus with detailed subject matter",
        "Artistic composition with creative framing"
    )

    return ImageAnalysis(
        colors = randomColors,
        objects = randomObjects,
        text = if (kotlin.random.Random.nextBoolean()) "Sample detected text" else null,
        description = descriptions.random(),
        confidence = 0.75f + kotlin.random.Random.nextFloat() * 0.2f // 0.75-0.95
    )
}

private fun saveImageToInternalStorage(context: Context, bitmap: Bitmap, fileName: String): String {
    val imageDir = File(context.filesDir, "analyzed_images")
    if (!imageDir.exists()) {
        imageDir.mkdirs()
    }
    
    val imageFile = File(imageDir, fileName)
    return try {
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        imageFile.absolutePath
    } catch (e: Exception) {
        Log.e("ImageScreen", "Error saving image", e)
        ""
    }
}
