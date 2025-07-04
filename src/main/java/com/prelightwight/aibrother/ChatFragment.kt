package com.prelightwight.aibrother

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatFragment : Fragment() {
    
    private lateinit var messagesRecycler: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var chatAdapter: ChatAdapter
    
    private val messages = mutableListOf<ChatMessage>()
    private val handler = Handler(Looper.getMainLooper())
    
    // Enhanced AI responses based on input
    private val aiResponses = mapOf(
        "hello" to listOf(
            "Hello! I'm AI Brother, your privacy-focused AI assistant. How can I help you today?",
            "Hi there! It's great to chat with you. What would you like to talk about?",
            "Hello! I'm here and ready to assist you with anything you need."
        ),
        "test" to listOf(
            "Everything is working perfectly! 🚀 The chat interface is fully functional.",
            "Test successful! All systems are operational and ready for conversation.",
            "✅ Chat functionality confirmed! I can understand and respond to your messages."
        ),
        "navigation" to listOf(
            "Great! You can now navigate between different sections using the bottom tabs! Try tapping Brain, Files, Images, or Settings to explore the app.",
            "The navigation is working perfectly! Each tab will have its own unique features. We're building this step by step.",
            "🎯 Navigation test successful! You can now access multiple screens and features through the bottom menu."
        ),
        "how are you" to listOf(
            "I'm doing great! All my systems are running smoothly and I'm excited to chat with you.",
            "I'm wonderful! Thanks for asking. How are you doing today?",
            "I'm functioning perfectly and feeling very chatty! How can I help you?"
        ),
        "thank you" to listOf(
            "You're very welcome! I'm always happy to help.",
            "My pleasure! Feel free to ask me anything else.",
            "You're welcome! Is there anything else I can assist you with?"
        ),
        "features" to listOf(
            "I'm designed to be your comprehensive AI assistant! I can chat, help with tasks, and keep conversations private.",
            "My features include intelligent conversation, privacy-focused design, and the ability to help with various tasks.",
            "I offer smart conversation capabilities, local processing for privacy, and I'm constantly learning to be more helpful!"
        )
    )
    
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
        
        // Welcome message
        addAiMessage("👋 Welcome to AI Brother! I'm your privacy-focused AI assistant. Try typing 'navigation' to test the new interface!")
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
    
    private fun sendMessage() {
        val message = messageInput.text.toString().trim()
        if (message.isEmpty()) return
        
        // Add user message
        addUserMessage(message)
        messageInput.text.clear()
        
        // Update main activity status
        updateMainActivityStatus("AI is typing...")
        
        // Generate AI response after a delay
        handler.postDelayed({
            val aiResponse = generateAiResponse(message)
            addAiMessage(aiResponse)
            updateMainActivityStatus("Ready")
        }, 1000 + (Math.random() * 1500).toLong()) // Random delay 1-2.5 seconds
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
    
    private fun generateAiResponse(userMessage: String): String {
        val lowercaseMessage = userMessage.lowercase()
        
        // Look for keywords in the user message
        for ((keyword, responses) in aiResponses) {
            if (lowercaseMessage.contains(keyword)) {
                return responses.random()
            }
        }
        
        // Default intelligent responses for other inputs
        val defaultResponses = listOf(
            "That's an interesting point! I'd love to hear more about your thoughts on this.",
            "I understand what you're saying. Could you tell me more about that?",
            "That's a great question! Let me think about that for a moment...",
            "I appreciate you sharing that with me. What would you like to explore further?",
            "Your message is thought-provoking. I'm here to help however I can!",
            "That's fascinating! I enjoy our conversation and would like to learn more.",
            "I'm processing what you've said. Is there a particular aspect you'd like to focus on?",
            "Thank you for that insight! How can I best assist you with this topic?"
        )
        
        return defaultResponses.random()
    }
}