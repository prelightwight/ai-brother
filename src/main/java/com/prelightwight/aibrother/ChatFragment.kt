package com.prelightwight.aibrother

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prelightwight.aibrother.llm.LlamaInterface
import com.prelightwight.aibrother.llm.ModelDownloader
import com.prelightwight.aibrother.llm.ModelInfo
import kotlinx.coroutines.launch
import java.io.File

class ChatFragment : Fragment() {
    
    private lateinit var messagesRecycler: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var currentModelText: TextView
    private lateinit var selectModelButton: Button
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var llamaInterface: LlamaInterface
    private lateinit var modelDownloader: ModelDownloader
    
    private val messages = mutableListOf<ChatMessage>()
    private val handler = Handler(Looper.getMainLooper())
    private var isProcessing = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupRecyclerView()
        setupClickListeners()
        initializeAI()
        updateModelDisplay()
        
        // Welcome message - only add if no messages exist yet
        if (messages.isEmpty()) {
            addAiMessage("👋 Welcome to AI Brother! I'm your privacy-focused AI assistant.\n\n" +
                    "💡 To get started:\n" +
                    "1. Click 'Select Model' above to choose an AI model\n" +
                    "2. If no models are available, go to the Models tab to download one\n" +
                    "3. Chat with real AI!")
        }
    }
    
    private fun initializeViews(view: View) {
        messagesRecycler = view.findViewById(R.id.messages_recycler)
        messageInput = view.findViewById(R.id.message_input)
        sendButton = view.findViewById(R.id.send_button)
        currentModelText = view.findViewById(R.id.current_model_text)
        selectModelButton = view.findViewById(R.id.select_model_button)
        
        modelDownloader = ModelDownloader(requireContext())
    }
    
    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messages)
        messagesRecycler.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            sendMessage()
        }
        
        messageInput.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
        
        selectModelButton.setOnClickListener {
            showModelSelectionDialog()
        }
    }
    
    private fun initializeAI() {
        llamaInterface = LlamaInterface.getInstance()
        
        // Initialize the backend in background
        lifecycleScope.launch {
            try {
                val initialized = llamaInterface.initialize()
                if (initialized) {
                    updateMainActivityStatus("AI Ready")
                    updateModelDisplay()
                } else {
                    updateMainActivityStatus("AI Init Failed")
                }
            } catch (e: Exception) {
                updateMainActivityStatus("AI Error")
            }
        }
    }
    
    private fun updateModelDisplay() {
        val isLoaded = llamaInterface.isModelLoaded()
        val modelPath = llamaInterface.getLoadedModelPath()
        android.util.Log.d("ChatFragment", "updateModelDisplay - isLoaded: $isLoaded, modelPath: '$modelPath'")
        
        if (isLoaded && modelPath.isNotEmpty()) {
            val modelName = getModelDisplayName(modelPath)
            currentModelText.text = "✓ $modelName"
            currentModelText.setTextColor(0xFF4CAF50.toInt()) // Green
            selectModelButton.text = "Change Model"
        } else {
            currentModelText.text = "No model loaded"
            currentModelText.setTextColor(0xFF757575.toInt()) // Gray
            selectModelButton.text = "Select Model"
        }
    }
    
    private fun getModelDisplayName(modelPath: String): String {
        val fileName = File(modelPath).nameWithoutExtension
        return when {
            fileName.contains("tinyllama", ignoreCase = true) -> "TinyLlama"
            fileName.contains("phi-3", ignoreCase = true) -> "Phi-3 Mini"
            fileName.contains("gemma-2", ignoreCase = true) -> "Gemma 2"
            fileName.contains("llama-3.2", ignoreCase = true) -> "Llama 3.2"
            fileName.contains("qwen2", ignoreCase = true) -> "Qwen2"
            fileName.contains("smollm", ignoreCase = true) -> "SmolLM"
            fileName.contains("minicpm", ignoreCase = true) -> "MiniCPM"
            fileName.contains("orca", ignoreCase = true) -> "Orca Mini"
            else -> fileName.take(20) + if (fileName.length > 20) "..." else ""
        }
    }
    
    private fun showModelSelectionDialog() {
        val availableModels = modelDownloader.getAvailableModels()
        val downloadedModels = availableModels.filter { modelDownloader.isModelDownloaded(it) }
        
        if (downloadedModels.isEmpty()) {
            Toast.makeText(requireContext(), 
                "No models downloaded yet! Go to Models tab to download some.", 
                Toast.LENGTH_LONG).show()
            return
        }
        
        val modelNames = downloadedModels.map { it.displayName }.toTypedArray()
        val currentModelPath = llamaInterface.getLoadedModelPath()
        var selectedIndex = -1
        
        // Find currently selected model
        downloadedModels.forEachIndexed { index, model ->
            val modelPath = modelDownloader.getModelFilePath(model).absolutePath
            if (modelPath == currentModelPath) {
                selectedIndex = index
            }
        }
        
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Select AI Model")
        builder.setSingleChoiceItems(modelNames, selectedIndex) { dialog, which ->
            val selectedModel = downloadedModels[which]
            loadSelectedModel(selectedModel)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel", null)
        builder.setNeutralButton("Go to Models Tab") { _, _ ->
            // Switch to models tab
            val bottomNav = activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
            bottomNav?.selectedItemId = R.id.nav_models
        }
        builder.show()
    }
    
    private fun loadSelectedModel(model: ModelInfo) {
        selectModelButton.isEnabled = false
        selectModelButton.text = "Loading..."
        currentModelText.text = "Loading ${model.displayName}..."
        
        lifecycleScope.launch {
            try {
                val modelPath = modelDownloader.getModelFilePath(model).absolutePath
                android.util.Log.d("ChatFragment", "Loading model: ${model.displayName} from path: $modelPath")
                
                val success = llamaInterface.loadModel(modelPath)
                
                // Double-check the loading result
                val isLoaded = llamaInterface.isModelLoaded()
                val loadedPath = llamaInterface.getLoadedModelPath()
                android.util.Log.d("ChatFragment", "Load result: success=$success, isLoaded=$isLoaded, loadedPath=$loadedPath")
                
                handler.post {
                    if (success && isLoaded) {
                        Toast.makeText(requireContext(), "Model loaded: ${model.displayName}", Toast.LENGTH_SHORT).show()
                        updateMainActivityStatus("AI Ready")
                        updateModelDisplay()
                        
                        // Add a system message about the model change
                        addAiMessage("🧠 Switched to ${model.displayName}! I'm ready to chat with enhanced AI capabilities.")
                    } else {
                        Toast.makeText(requireContext(), "Failed to load model (success=$success, loaded=$isLoaded)", Toast.LENGTH_LONG).show()
                        updateModelDisplay()
                    }
                    selectModelButton.isEnabled = true
                }
                
            } catch (e: Exception) {
                android.util.Log.e("ChatFragment", "Exception loading model", e)
                handler.post {
                    Toast.makeText(requireContext(), "Error loading model: ${e.message}", Toast.LENGTH_LONG).show()
                    updateModelDisplay()
                    selectModelButton.isEnabled = true
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Update model display when returning to chat (in case model was loaded in Models tab)
        updateModelDisplay()
    }
    
    private fun sendMessage() {
        if (isProcessing) return
        
        val message = messageInput.text.toString().trim()
        if (message.isEmpty()) return
        
        // Check if model is loaded with debugging
        val modelLoaded = llamaInterface.isModelLoaded()
        val modelPath = llamaInterface.getLoadedModelPath()
        android.util.Log.d("ChatFragment", "Sending message - Model loaded: $modelLoaded, Path: $modelPath")
        
        if (!modelLoaded) {
            Toast.makeText(requireContext(), "Please select a model first! (Debug: path='$modelPath')", Toast.LENGTH_LONG).show()
            showModelSelectionDialog()
            return
        }
        
        // Add user message
        addUserMessage(message)
        messageInput.text.clear()
        isProcessing = true
        
        // Update status and generate response
        updateMainActivityStatus("AI is thinking...")
        
        lifecycleScope.launch {
            try {
                val response = if (llamaInterface.isModelLoaded()) {
                    // Use real AI
                    generateRealAiResponse(message)
                } else {
                    // Use enhanced fallback
                    generateEnhancedFallbackResponse(message)
                }
                
                // Add AI response on main thread
                handler.post {
                    addAiMessage(response)
                    updateMainActivityStatus("Ready")
                    isProcessing = false
                }
                
            } catch (e: Exception) {
                // Error handling
                handler.post {
                    addAiMessage("Sorry, I encountered an error: ${e.message}")
                    updateMainActivityStatus("Error")
                    isProcessing = false
                }
            }
        }
    }
    
    private suspend fun generateRealAiResponse(userMessage: String): String {
        return try {
            val systemPrompt = buildSystemPrompt()
            llamaInterface.generateChatResponse(userMessage, systemPrompt)
        } catch (e: Exception) {
            "I'm having trouble generating a response right now: ${e.message}"
        }
    }
    
    private fun buildSystemPrompt(): String {
        val responseStyle = getResponseStyleFromSettings()
        val creativity = getCreativityFromSettings()
        
        val styleInstructions = when (responseStyle) {
            0 -> "Be friendly, conversational, and warm in your responses."
            1 -> "Respond in a professional, formal manner."
            2 -> "Be creative, playful, and use emojis occasionally."
            3 -> "Keep responses concise and direct."
            4 -> "Provide detailed, explanatory responses."
            else -> "Be helpful and natural in your responses."
        }
        
        val creativityInstructions = when {
            creativity < 25 -> "Be very factual and straightforward."
            creativity < 50 -> "Be somewhat creative but mostly factual."
            creativity < 75 -> "Balance creativity with factual information."
            else -> "Be creative and imaginative in your responses."
        }
        
        return """You are AI Brother, a helpful and privacy-focused AI assistant. 
$styleInstructions $creativityInstructions 
Keep responses reasonably concise unless specifically asked for detail. 
You run locally on the user's device for privacy."""
    }
    
    private fun generateEnhancedFallbackResponse(userMessage: String): String {
        val lowercaseMessage = userMessage.lowercase()
        
        // Special responses for common queries when AI is not loaded
        return when {
            lowercaseMessage.contains("hello") || lowercaseMessage.contains("hi") -> 
                "Hello! 👋 I'd love to chat properly, but I need you to load an AI model first. Click 'Select Model' above!"
                
            lowercaseMessage.contains("test") -> 
                "The chat interface is working perfectly! 🚀 But for real AI responses, please click 'Select Model' above to choose a downloaded model."
                
            lowercaseMessage.contains("help") -> 
                "I'm here to help! 💪 To unlock my full AI capabilities:\n1. Click 'Select Model' above\n2. Choose a downloaded model\n3. If no models are available, go to Models tab to download one\n4. Chat with real AI!"
                
            lowercaseMessage.contains("model") -> 
                "You're asking about models! 🧠 Click the 'Select Model' button above to choose from your downloaded models, or visit the Models tab to download new ones."
                
            else -> "I need an AI model to be loaded before I can chat intelligently. Please click 'Select Model' above to choose from your downloaded models!"
        }
    }
    
    private fun updateMainActivityStatus(status: String) {
        activity?.findViewById<TextView>(R.id.status_text)?.text = status
    }
    
    private fun addUserMessage(content: String) {
        val message = ChatMessage(content, true)
        chatAdapter.addMessage(message)
        scrollToBottom()
    }
    
    private fun addAiMessage(content: String) {
        val message = ChatMessage(content, false)
        chatAdapter.addMessage(message)
        scrollToBottom()
    }
    
    private fun scrollToBottom() {
        if (messages.isNotEmpty()) {
            messagesRecycler.scrollToPosition(messages.size - 1)
        }
    }
    
    private fun getCreativityFromSettings(): Int {
        val sharedPrefs = requireContext().getSharedPreferences(SettingsFragment.PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getInt(SettingsFragment.PREF_CREATIVITY, 75)
    }
    
    private fun getResponseStyleFromSettings(): Int {
        val sharedPrefs = requireContext().getSharedPreferences(SettingsFragment.PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getInt(SettingsFragment.PREF_RESPONSE_STYLE, 0)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up AI resources if needed
        lifecycleScope.launch {
            try {
                // Don't shutdown completely as other fragments might use it
                // llamaInterface.shutdown() 
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }
}