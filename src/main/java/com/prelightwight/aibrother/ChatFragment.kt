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
        
        // Welcome message - only add if no messages exist yet
        if (messages.isEmpty()) {
            addAiMessage("👋 Welcome to AI Brother! I'm your privacy-focused AI assistant. Try typing 'navigation' to test the new interface!")
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
    
    private fun sendMessage() {
        val message = messageInput.text.toString().trim()
        if (message.isEmpty()) return
        
        // Add user message
        addUserMessage(message)
        messageInput.text.clear()
        
        // Update main activity status
        updateMainActivityStatus("AI is typing...")
        
        // Generate AI response after a delay based on settings
        val responseDelay = getResponseSpeedFromSettings()
        handler.postDelayed({
            val aiResponse = generateAiResponse(message)
            addAiMessage(aiResponse)
            updateMainActivityStatus("Ready")
        }, responseDelay)
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
        
        // Check if user is asking about settings
        if (lowercaseMessage.contains("settings") || lowercaseMessage.contains("configuration")) {
            return "I can see you're interested in my settings! Head over to the Settings tab to customize my behavior, response style, and privacy preferences. You can adjust my creativity level, response speed, and much more!"
        }
        
        // Generate response based on current settings
        val responseStyle = getResponseStyleFromSettings()
        val creativity = getCreativityFromSettings()
        val memoryEnabled = isMemoryEnabledFromSettings()
        
        // Base responses categorized by style
        val baseResponses = when (responseStyle) {
            0 -> listOf( // Friendly & Conversational
                "That's really interesting! I'd love to hear more about your thoughts on this.",
                "I enjoy chatting with you! Could you tell me more about that?",
                "That's a great point! What's your take on this topic?",
                "I appreciate you sharing that with me. What else would you like to explore?",
                "Your message got me thinking! How can I help you with this?"
            )
            1 -> listOf( // Professional & Formal
                "I understand your inquiry. Could you provide additional details?",
                "Thank you for your message. I would be happy to assist you further.",
                "I acknowledge your input. Please let me know how I may be of service.",
                "Your request has been noted. How may I best address your needs?",
                "I am ready to provide assistance. What specific information do you require?"
            )
            2 -> listOf( // Creative & Playful
                "Ooh, that's fascinating! 🤔 Tell me more - I'm all ears!",
                "What a delightful thought! 🌟 Let's dive deeper into this together!",
                "That's got my creative gears turning! ⚙️ What's your next idea?",
                "I love where this conversation is going! 🚀 Keep the thoughts coming!",
                "Your mind works in wonderful ways! ✨ What else is sparking your curiosity?"
            )
            3 -> listOf( // Concise & Direct  
                "Understood. More details?",
                "Got it. What's next?",
                "Clear. How can I help?",
                "Noted. Continue.",
                "Right. What do you need?"
            )
            4 -> listOf( // Detailed & Explanatory
                "That's a compelling observation that touches on several important aspects. I'd like to understand the nuances of your perspective better. Could you elaborate on the specific elements that interest you most? This will help me provide more targeted and useful insights.",
                "Your message raises intriguing questions that merit careful consideration. There are multiple dimensions to explore here, and I'm curious about which particular angle resonates most strongly with you. What aspects would you like to examine in greater detail?",
                "I find your input quite thought-provoking, as it encompasses various interconnected concepts. To provide the most comprehensive and helpful response, I'd benefit from understanding your specific goals and interests related to this topic. What would be most valuable for you to discuss?",
                "Thank you for sharing such an engaging topic. The complexity and depth of what you've mentioned suggests there are numerous pathways we could explore together. I'm here to assist you in whichever direction would be most beneficial for your particular needs and curiosities."
            )
            else -> listOf( // Default fallback
                "That's interesting! Tell me more.",
                "I'd love to hear your thoughts on this.",
                "What would you like to explore further?"
            )
        }
        
        var response = baseResponses.random()
        
        // Add creativity variations based on creativity level
        if (creativity > 75) {
            val creativeAdditions = listOf(
                " 🎨", " ✨", " 🌟", " 💫", " 🚀"
            )
            response += creativeAdditions.random()
        }
        
        // Add memory reference if enabled
        if (memoryEnabled && Math.random() < 0.3) { // 30% chance
            val memoryAdditions = listOf(
                " (I'll remember this for our future conversations!)",
                " (Adding this to my memory for next time!)",
                " (I'm learning from our chat!)"
            )
            response += memoryAdditions.random()
        }
        
        return response
    }
    
    private fun getResponseSpeedFromSettings(): Long {
        val sharedPrefs = requireContext().getSharedPreferences(SettingsFragment.PREFS_NAME, Context.MODE_PRIVATE)
        val speed = sharedPrefs.getInt(SettingsFragment.PREF_RESPONSE_SPEED, 60)
        
        return when {
            speed < 25 -> 3000L + (Math.random() * 1000).toLong()    // Very slow: 3-4 seconds
            speed < 50 -> 2000L + (Math.random() * 1000).toLong()    // Slow: 2-3 seconds  
            speed < 75 -> 1000L + (Math.random() * 500).toLong()     // Normal: 1-1.5 seconds
            else -> 300L + (Math.random() * 400).toLong()            // Fast: 0.3-0.7 seconds
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
    
    private fun isMemoryEnabledFromSettings(): Boolean {
        val sharedPrefs = requireContext().getSharedPreferences(SettingsFragment.PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean(SettingsFragment.PREF_MEMORY_ENABLED, true)
    }
}