package com.prelightwight.aibrother.ai

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class AIModelResult(
    val text: String? = null,
    val confidence: Float = 0f,
    val metadata: Map<String, Any> = emptyMap()
)

data class ImageAnalysisResult(
    val labels: List<String> = emptyList(),
    val objects: List<String> = emptyList(),
    val text: String? = null,
    val colors: List<String> = emptyList(),
    val description: String = "",
    val confidence: Float = 0f
)

data class TextAnalysisResult(
    val sentiment: String = "neutral", // positive, negative, neutral
    val topics: List<String> = emptyList(),
    val summary: String = "",
    val entities: List<String> = emptyList(),
    val confidence: Float = 0f
)

class AIModelManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "AIModelManager"
        
        @Volatile
        private var INSTANCE: AIModelManager? = null
        
        fun getInstance(context: Context): AIModelManager {
            return INSTANCE ?: synchronized(this) {
                val instance = AIModelManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    // TensorFlow Lite interpreters
    private var textModelInterpreter: Interpreter? = null
    private var chatModelInterpreter: Interpreter? = null
    private var summaryModelInterpreter: Interpreter? = null
    
    // MLKit components
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    private val objectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
    )

    // Model loading status
    private var isInitialized = false
    private var initializationError: String? = null

    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Initializing AI Model Manager...")
            
            // Load TensorFlow Lite models if available
            loadTextModel()
            loadChatModel()
            loadSummaryModel()
            
            isInitialized = true
            initializationError = null
            Log.d(TAG, "AI Model Manager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AI Model Manager", e)
            initializationError = e.message
            isInitialized = false
        }
    }

    private suspend fun loadTextModel() = withContext(Dispatchers.IO) {
        try {
            // Load a lightweight text classification model
            val modelBuffer = loadModelFile("text_classifier.tflite")
            if (modelBuffer != null) {
                val options = Interpreter.Options()
                if (CompatibilityList().isDelegateSupportedOnThisDevice) {
                    options.addDelegate(GpuDelegate())
                }
                textModelInterpreter = Interpreter(modelBuffer, options)
                Log.d(TAG, "Text model loaded successfully")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Text model not available: ${e.message}")
        }
    }

    private suspend fun loadChatModel() = withContext(Dispatchers.IO) {
        try {
            // Load a lightweight conversational model
            val modelBuffer = loadModelFile("chat_model.tflite")
            if (modelBuffer != null) {
                val options = Interpreter.Options()
                if (CompatibilityList().isDelegateSupportedOnThisDevice) {
                    options.addDelegate(GpuDelegate())
                }
                chatModelInterpreter = Interpreter(modelBuffer, options)
                Log.d(TAG, "Chat model loaded successfully")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Chat model not available: ${e.message}")
        }
    }

    private suspend fun loadSummaryModel() = withContext(Dispatchers.IO) {
        try {
            // Load a text summarization model
            val modelBuffer = loadModelFile("summary_model.tflite")
            if (modelBuffer != null) {
                val options = Interpreter.Options()
                summaryModelInterpreter = Interpreter(modelBuffer, options)
                Log.d(TAG, "Summary model loaded successfully")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Summary model not available: ${e.message}")
        }
    }

    private fun loadModelFile(filename: String): MappedByteBuffer? {
        return try {
            val fileDescriptor = context.assets.openFd(filename)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        } catch (e: IOException) {
            Log.w(TAG, "Model file $filename not found in assets")
            null
        }
    }

    // Image Analysis Functions
    suspend fun analyzeImage(bitmap: Bitmap): ImageAnalysisResult = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            throw IllegalStateException("AIModelManager not initialized")
        }

        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            
            // Parallel processing of different analysis tasks
            val labelsDeferred = async { getImageLabels(image) }
            val objectsDeferred = async { getDetectedObjects(image) }
            val textDeferred = async { getExtractedText(image) }
            val colorsDeferred = async { getDominantColors(bitmap) }

            val labels = labelsDeferred.await()
            val objects = objectsDeferred.await()
            val text = textDeferred.await()
            val colors = colorsDeferred.await()

            // Generate description based on analysis
            val description = generateImageDescription(labels, objects, text)
            
            // Calculate overall confidence
            val confidence = calculateImageConfidence(labels, objects, text)

            ImageAnalysisResult(
                labels = labels,
                objects = objects,
                text = text,
                colors = colors,
                description = description,
                confidence = confidence
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing image", e)
            ImageAnalysisResult(
                description = "Unable to analyze image: ${e.message}",
                confidence = 0f
            )
        }
    }

    private suspend fun getImageLabels(image: InputImage): List<String> {
        return try {
            val results = imageLabeler.process(image).await()
            results.map { it.text }.take(5)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting image labels", e)
            emptyList()
        }
    }

    private suspend fun getDetectedObjects(image: InputImage): List<String> {
        return try {
            val results = objectDetector.process(image).await()
            results.mapNotNull { detectedObject ->
                detectedObject.labels.firstOrNull()?.text
            }.take(3)
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting objects", e)
            emptyList()
        }
    }

    private suspend fun getExtractedText(image: InputImage): String? {
        return try {
            val result = textRecognizer.process(image).await()
            result.text.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting text", e)
            null
        }
    }

    private suspend fun getDominantColors(bitmap: Bitmap): List<String> = withContext(Dispatchers.Default) {
        try {
            // Simple color analysis - sample pixels and find dominant colors
            val colors = mutableMapOf<Int, Int>()
            val sampleSize = 10
            
            for (x in 0 until bitmap.width step sampleSize) {
                for (y in 0 until bitmap.height step sampleSize) {
                    val pixel = bitmap.getPixel(x, y)
                    val simplifiedColor = simplifyColor(pixel)
                    colors[simplifiedColor] = colors.getOrDefault(simplifiedColor, 0) + 1
                }
            }
            
            // Return top 3 dominant colors as color names
            colors.toList()
                .sortedByDescending { it.second }
                .take(3)
                .map { colorToName(it.first) }
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing colors", e)
            listOf("Unknown")
        }
    }

    private fun simplifyColor(color: Int): Int {
        val r = (color shr 16 and 0xFF) / 64 * 64
        val g = (color shr 8 and 0xFF) / 64 * 64
        val b = (color and 0xFF) / 64 * 64
        return (r shl 16) or (g shl 8) or b
    }

    private fun colorToName(color: Int): String {
        val r = (color shr 16 and 0xFF)
        val g = (color shr 8 and 0xFF)
        val b = (color and 0xFF)
        
        return when {
            r > 180 && g > 180 && b > 180 -> "White"
            r < 80 && g < 80 && b < 80 -> "Black"
            r > 150 && g < 100 && b < 100 -> "Red"
            r < 100 && g > 150 && b < 100 -> "Green"
            r < 100 && g < 100 && b > 150 -> "Blue"
            r > 150 && g > 150 && b < 100 -> "Yellow"
            r > 150 && g < 100 && b > 150 -> "Purple"
            r > 150 && g > 100 && b < 100 -> "Orange"
            r < 150 && g > 100 && b > 100 -> "Cyan"
            r > 100 && g > 100 && b > 100 -> "Gray"
            else -> "Mixed"
        }
    }

    private fun generateImageDescription(labels: List<String>, objects: List<String>, text: String?): String {
        val parts = mutableListOf<String>()
        
        if (objects.isNotEmpty()) {
            parts.add("Contains ${objects.joinToString(", ")}")
        }
        
        if (labels.isNotEmpty()) {
            parts.add("featuring ${labels.take(3).joinToString(", ")}")
        }
        
        if (!text.isNullOrBlank()) {
            parts.add("with visible text")
        }
        
        return if (parts.isNotEmpty()) {
            "Image ${parts.joinToString(" ")}"
        } else {
            "Clear image with good visual quality"
        }
    }

    private fun calculateImageConfidence(labels: List<String>, objects: List<String>, text: String?): Float {
        var confidence = 0.5f // Base confidence
        
        if (labels.isNotEmpty()) confidence += 0.2f
        if (objects.isNotEmpty()) confidence += 0.2f
        if (!text.isNullOrBlank()) confidence += 0.1f
        
        return confidence.coerceAtMost(0.95f)
    }

    // Text Analysis Functions
    suspend fun analyzeText(text: String): TextAnalysisResult = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            throw IllegalStateException("AIModelManager not initialized")
        }

        try {
            val sentiment = analyzeSentiment(text)
            val topics = extractTopics(text)
            val summary = generateSummary(text)
            val entities = extractEntities(text)
            
            TextAnalysisResult(
                sentiment = sentiment,
                topics = topics,
                summary = summary,
                entities = entities,
                confidence = 0.75f
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing text", e)
            TextAnalysisResult(
                summary = "Unable to analyze text: ${e.message}",
                confidence = 0f
            )
        }
    }

    private fun analyzeSentiment(text: String): String {
        // Simple sentiment analysis using keyword matching
        val positiveWords = setOf("good", "great", "excellent", "amazing", "wonderful", "fantastic", "love", "like", "happy", "joy")
        val negativeWords = setOf("bad", "terrible", "awful", "horrible", "hate", "dislike", "sad", "angry", "frustrated", "disappointed")
        
        val words = text.lowercase().split(" ")
        val positiveCount = words.count { it in positiveWords }
        val negativeCount = words.count { it in negativeWords }
        
        return when {
            positiveCount > negativeCount -> "positive"
            negativeCount > positiveCount -> "negative"
            else -> "neutral"
        }
    }

    private fun extractTopics(text: String): List<String> {
        // Simple topic extraction using keyword patterns
        val topics = mutableSetOf<String>()
        val lowercaseText = text.lowercase()
        
        val topicPatterns = mapOf(
            "technology" to listOf("ai", "artificial intelligence", "computer", "software", "app", "digital", "tech"),
            "science" to listOf("research", "study", "experiment", "theory", "scientific", "analysis"),
            "health" to listOf("medical", "health", "doctor", "treatment", "medicine", "wellness"),
            "education" to listOf("learn", "study", "education", "school", "university", "knowledge"),
            "business" to listOf("business", "company", "market", "finance", "economy", "profit")
        )
        
        topicPatterns.forEach { (topic, keywords) ->
            if (keywords.any { keyword -> lowercaseText.contains(keyword) }) {
                topics.add(topic)
            }
        }
        
        return topics.toList()
    }

    private fun generateSummary(text: String): String {
        if (text.length <= 200) return text
        
        // Simple extractive summarization
        val sentences = text.split(Regex("[.!?]+")).filter { it.trim().isNotEmpty() }
        return when {
            sentences.size <= 2 -> text.take(200)
            else -> sentences.take(2).joinToString(". ").take(200) + "..."
        }
    }

    private fun extractEntities(text: String): List<String> {
        // Simple named entity recognition using patterns
        val entities = mutableSetOf<String>()
        
        // Extract capitalized words that might be entities
        val words = text.split(" ")
        words.forEach { word ->
            if (word.length > 2 && word[0].isUpperCase() && word.substring(1).any { it.isLowerCase() }) {
                entities.add(word.trim { !it.isLetterOrDigit() })
            }
        }
        
        return entities.take(5).toList()
    }

    // Chat Response Generation
    suspend fun generateChatResponse(message: String, context: List<String> = emptyList()): AIModelResult = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            return@withContext AIModelResult(
                text = "AI Assistant is initializing. Please try again in a moment.",
                confidence = 0f
            )
        }

        try {
            // Use TensorFlow Lite model if available, otherwise use rule-based responses
            val response = if (chatModelInterpreter != null) {
                generateMLResponse(message, context)
            } else {
                generateRuleBasedResponse(message)
            }
            
            AIModelResult(
                text = response,
                confidence = 0.8f,
                metadata = mapOf(
                    "method" to if (chatModelInterpreter != null) "ml" else "rule-based",
                    "context_used" to context.isNotEmpty()
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating chat response", e)
            AIModelResult(
                text = "I'm having trouble processing your message right now. Could you try rephrasing?",
                confidence = 0f
            )
        }
    }

    private fun generateMLResponse(message: String, context: List<String>): String {
        // This would use the actual TensorFlow Lite model
        // For now, return a placeholder that indicates ML processing
        return "I understand you're asking about \"${message.take(50)}...\". Based on my analysis, I can help you with that topic."
    }

    private fun generateRuleBasedResponse(message: String): String {
        val lowercaseMessage = message.lowercase()
        
        return when {
            lowercaseMessage.contains("hello") || lowercaseMessage.contains("hi") -> 
                "Hello! I'm AI Brother, your personal AI assistant. How can I help you today?"
            
            lowercaseMessage.contains("how are you") -> 
                "I'm functioning well and ready to assist you! What would you like to know or discuss?"
            
            lowercaseMessage.contains("what can you do") -> 
                "I can help you with various tasks like analyzing documents, processing images, searching the web, and managing your knowledge base. What interests you?"
            
            lowercaseMessage.contains("thank") -> 
                "You're welcome! I'm always here to help. Is there anything else you'd like to know?"
            
            lowercaseMessage.contains("help") -> 
                "I'm here to help! You can ask me questions, upload files for analysis, search for information, or manage your personal knowledge base."
            
            lowercaseMessage.contains("?") -> 
                "That's an interesting question about \"${extractKeywords(message).joinToString(", ")}\". Let me help you explore that topic."
            
            else -> 
                "I've processed your message about \"${extractKeywords(message).take(3).joinToString(", ")}\". Could you tell me more about what specific aspect you'd like to explore?"
        }
    }

    private fun extractKeywords(text: String): List<String> {
        val stopWords = setOf("the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "is", "are", "was", "were", "be", "been", "have", "has", "had", "do", "does", "did", "will", "would", "could", "should", "may", "might", "can")
        
        return text.lowercase()
            .split(Regex("[^a-zA-Z]+"))
            .filter { it.length > 2 && it !in stopWords }
            .distinct()
            .take(5)
    }

    fun cleanup() {
        textModelInterpreter?.close()
        chatModelInterpreter?.close()
        summaryModelInterpreter?.close()
        textRecognizer.close()
        imageLabeler.close()
        objectDetector.close()
        
        textModelInterpreter = null
        chatModelInterpreter = null
        summaryModelInterpreter = null
        isInitialized = false
    }
}

// Extension function for async operations
private suspend fun <T> async(block: suspend () -> T): kotlinx.coroutines.Deferred<T> {
    return kotlinx.coroutines.async { block() }
}