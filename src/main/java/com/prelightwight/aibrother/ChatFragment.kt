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
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {
    
    private lateinit var messagesRecycler: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var llamaInterface: LlamaInterface
    
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
        
        // Welcome message - only add if no messages exist yet
        if (messages.isEmpty()) {
            addAiMessage("👋 Welcome to AI Brother! I'm your privacy-focused AI assistant.\n\n" +
                    "💡 To get started:\n" +
                    "1. Go to the Models tab\n" +
                    "2. Download a model (try TinyLlama for quick testing)\n" +
                    "3. Come back and chat with real AI!")
        }
    }
    
    private fun initializeViews(view: View) {
        messagesRecycler = view.findViewById(R.id.messages_recycler)
        messageInput = view.findViewById(R.id.message_input)
        sendButton = view.findViewById(R.id.send_button)
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
    }
    
    private fun initializeAI() {
        llamaInterface = LlamaInterface.getInstance()
        
        // Initialize the backend in background
        lifecycleScope.launch {
            try {
                val initialized = llamaInterface.initialize()
                if (initialized) {
                    updateMainActivityStatus("AI Ready")
                } else {
                    updateMainActivityStatus("AI Init Failed")
                }
            } catch (e: Exception) {
                updateMainActivityStatus("AI Error")
            }
        }
    }
    
    private fun sendMessage() {
        if (isProcessing) return
        
        val message = messageInput.text.toString().trim()
        if (message.isEmpty()) return
        
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
                "Hello! 👋 I'd love to chat properly, but I need you to load an AI model first. Head to the Models tab to download one!"
                
            lowercaseMessage.contains("test") -> 
                "The chat interface is working perfectly! 🚀 But for real AI responses, please download and load a model from the Models tab."
                
            lowercaseMessage.contains("help") -> 
                "I'm here to help! 💪 To unlock my full AI capabilities:\n1. Go to Models tab\n2. Download a model (TinyLlama is great for testing)\n3. Load it\n4. Come back and chat with real AI!"
                
            lowercaseMessage.contains("model") -> 
                "You're asking about models! 🧠 Perfect timing - you'll need to load one for AI chat. Check the Models tab to see available downloads."
                
            else -> "I need an AI model to be loaded before I can chat intelligently. Please go to the Models tab, download a model (I recommend TinyLlama for testing), and load it!"
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