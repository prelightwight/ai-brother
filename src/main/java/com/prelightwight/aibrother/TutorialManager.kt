package com.prelightwight.aibrother

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

class TutorialManager(private val activity: Activity) {
    
    private var tutorialOverlay: ViewGroup? = null
    private var currentStep = 0
    private var currentTutorialType = TutorialType.MAIN_ONBOARDING
    private val sharedPrefs: SharedPreferences = activity.getSharedPreferences("tutorial_prefs", Context.MODE_PRIVATE)
    
    companion object {
        const val PREF_TUTORIAL_COMPLETED = "tutorial_completed"
        const val PREF_SKIP_TUTORIAL = "skip_tutorial"
        const val PREF_CHAT_TUTORIAL_SHOWN = "chat_tutorial_shown"
        const val PREF_MODEL_TUTORIAL_SHOWN = "model_tutorial_shown"
        const val PREF_FILE_TUTORIAL_SHOWN = "file_tutorial_shown"
        const val PREF_CAMERA_TUTORIAL_SHOWN = "camera_tutorial_shown"
    }
    
    enum class TutorialType {
        MAIN_ONBOARDING,
        CHAT_FEATURES,
        MODEL_MANAGEMENT,
        FILE_PROCESSING,
        CAMERA_FEATURES,
        SETTINGS_TOUR
    }
    
    private val tutorialSteps = listOf(
        TutorialStep(
            title = "Welcome to AI Brother! 👋",
            description = "Your privacy-focused AI assistant is ready to help. Let's take a quick tour of the features!",
            targetViewId = null,
            highlightType = HighlightType.NONE
        ),
        TutorialStep(
            title = "Chat with AI Brother 💬",
            description = "This is where you'll have conversations with your AI assistant. Try typing 'hello' to get started!",
            targetViewId = R.id.nav_chat,
            highlightType = HighlightType.CIRCLE
        ),
        TutorialStep(
            title = "AI Memory System 🧠",
            description = "The Brain section will store conversation history and help AI Brother learn from your interactions.",
            targetViewId = R.id.nav_brain,
            highlightType = HighlightType.CIRCLE
        ),
        TutorialStep(
            title = "File Management 📂",
            description = "Upload and analyze documents here. AI Brother can help you understand and work with your files.",
            targetViewId = R.id.nav_files,
            highlightType = HighlightType.CIRCLE
        ),
        TutorialStep(
            title = "File & Image Management ��🖼️",
            description = "Upload and analyze documents and images here. AI Brother can help you understand files and analyze photos!",
            targetViewId = R.id.nav_files,
            highlightType = HighlightType.CIRCLE
        ),
        TutorialStep(
            title = "Customize Your Experience ⚙️",
            description = "Personalize AI Brother's behavior, response style, and appearance in Settings. Try changing themes and response styles!",
            targetViewId = R.id.nav_settings,
            highlightType = HighlightType.CIRCLE
        ),
        TutorialStep(
            title = "Privacy First 🔒",
            description = "AI Brother keeps your data local and private. All conversations and settings stay on your device unless you choose otherwise.",
            targetViewId = null,
            highlightType = HighlightType.NONE
        ),
        TutorialStep(
            title = "You're All Set! 🚀",
            description = "Start chatting with AI Brother and explore the features. You can replay this tutorial anytime from Settings.",
            targetViewId = null,
            highlightType = HighlightType.NONE
        )
    )
    
    fun shouldShowTutorial(): Boolean {
        return !sharedPrefs.getBoolean(PREF_TUTORIAL_COMPLETED, false) && 
               !sharedPrefs.getBoolean(PREF_SKIP_TUTORIAL, false)
    }
    
    fun startTutorial() {
        if (shouldShowTutorial()) {
            currentStep = 0
            showTutorialStep()
        }
    }
    
    fun startTutorialFromSettings() {
        // Force start tutorial even if completed
        currentStep = 0
        currentTutorialType = TutorialType.MAIN_ONBOARDING
        showTutorialStep()
    }
    
    fun startChatTutorial() {
        if (!sharedPrefs.getBoolean(PREF_CHAT_TUTORIAL_SHOWN, false)) {
            currentTutorialType = TutorialType.CHAT_FEATURES
            currentStep = 0
            showTutorialStep()
        }
    }
    
    fun startModelTutorial() {
        if (!sharedPrefs.getBoolean(PREF_MODEL_TUTORIAL_SHOWN, false)) {
            currentTutorialType = TutorialType.MODEL_MANAGEMENT
            currentStep = 0
            showTutorialStep()
        }
    }
    
    fun startFileTutorial() {
        if (!sharedPrefs.getBoolean(PREF_FILE_TUTORIAL_SHOWN, false)) {
            currentTutorialType = TutorialType.FILE_PROCESSING
            currentStep = 0
            showTutorialStep()
        }
    }
    
    fun startCameraTutorial() {
        if (!sharedPrefs.getBoolean(PREF_CAMERA_TUTORIAL_SHOWN, false)) {
            currentTutorialType = TutorialType.CAMERA_FEATURES
            currentStep = 0
            showTutorialStep()
        }
    }
    
    private fun getCurrentTutorialSteps(): List<TutorialStep> {
        return when (currentTutorialType) {
            TutorialType.MAIN_ONBOARDING -> tutorialSteps
            TutorialType.CHAT_FEATURES -> getChatTutorialSteps()
            TutorialType.MODEL_MANAGEMENT -> getModelTutorialSteps()
            TutorialType.FILE_PROCESSING -> getFileTutorialSteps()
            TutorialType.CAMERA_FEATURES -> getCameraTutorialSteps()
            TutorialType.SETTINGS_TOUR -> getSettingsTutorialSteps()
        }
    }
    
    private fun getChatTutorialSteps(): List<TutorialStep> {
        return listOf(
            TutorialStep(
                title = "Smart Conversations 💬",
                description = "AI Brother remembers your conversation context and can help with follow-up questions. Try asking related questions!",
                targetViewId = null,
                highlightType = HighlightType.NONE
            ),
            TutorialStep(
                title = "Message Input ✏️",
                description = "Type your messages here. Try voice commands, questions, or creative requests!",
                targetViewId = R.id.message_input,
                highlightType = HighlightType.RECTANGLE
            ),
            TutorialStep(
                title = "Send Messages 📤",
                description = "Tap here to send your message to AI Brother.",
                targetViewId = R.id.send_button,
                highlightType = HighlightType.CIRCLE
            ),
            TutorialStep(
                title = "Conversation History 📜",
                description = "All your conversations are stored and can be referenced by AI Brother in future chats.",
                targetViewId = null,
                highlightType = HighlightType.NONE
            )
        )
    }
    
    private fun getModelTutorialSteps(): List<TutorialStep> {
        return listOf(
            TutorialStep(
                title = "AI Models 🤖",
                description = "Different AI models have different strengths. Smaller models are faster, larger ones are more capable.",
                targetViewId = null,
                highlightType = HighlightType.NONE
            ),
            TutorialStep(
                title = "Download Models 📥",
                description = "Tap on any model to download it. We recommend starting with TinyLlama for testing.",
                targetViewId = R.id.modelsListView,
                highlightType = HighlightType.RECTANGLE
            ),
            TutorialStep(
                title = "Model Status 📊",
                description = "See download progress, storage usage, and which model is currently loaded here.",
                targetViewId = R.id.storageInfoText,
                highlightType = HighlightType.RECTANGLE
            ),
            TutorialStep(
                title = "Refresh Models 🔄",
                description = "Refresh the list to check for new models or update status.",
                targetViewId = R.id.refreshButton,
                highlightType = HighlightType.CIRCLE
            )
        )
    }
    
    private fun getFileTutorialSteps(): List<TutorialStep> {
        return listOf(
            TutorialStep(
                title = "File Processing 📁",
                description = "AI Brother can analyze documents, extract text, and answer questions about your files.",
                targetViewId = null,
                highlightType = HighlightType.NONE
            ),
            TutorialStep(
                title = "Upload Files 📤",
                description = "Tap here to upload documents, PDFs, images, or other files for analysis.",
                targetViewId = R.id.btn_upload_file,
                highlightType = HighlightType.CIRCLE
            ),
            TutorialStep(
                title = "File List 📋",
                description = "See all your processed files here. Tap any file to view details or ask questions about it.",
                targetViewId = R.id.files_list,
                highlightType = HighlightType.RECTANGLE
            ),
            TutorialStep(
                title = "File Stats 📊",
                description = "Track how many files you've uploaded and their analysis status.",
                targetViewId = R.id.upload_count_text,
                highlightType = HighlightType.RECTANGLE
            )
        )
    }
    
    private fun getCameraTutorialSteps(): List<TutorialStep> {
        return listOf(
            TutorialStep(
                title = "Camera Features 📸",
                description = "Use your camera to capture and analyze images, scan documents, and extract text.",
                targetViewId = null,
                highlightType = HighlightType.NONE
            ),
            TutorialStep(
                title = "Take Photos 📸",
                description = "Capture new images for analysis. Choose from regular photos, document scanning, or text extraction.",
                targetViewId = R.id.btn_take_photo,
                highlightType = HighlightType.CIRCLE
            ),
            TutorialStep(
                title = "Upload Images 🖼️",
                description = "Upload existing images from your gallery for AI analysis and text extraction.",
                targetViewId = R.id.btn_upload_image,
                highlightType = HighlightType.CIRCLE
            ),
            TutorialStep(
                title = "Image Gallery 🎨",
                description = "Browse all your processed images and see analysis results.",
                targetViewId = R.id.btn_view_gallery,
                highlightType = HighlightType.CIRCLE
            ),
            TutorialStep(
                title = "Image Settings ⚙️",
                description = "Configure image quality, OCR languages, and privacy settings.",
                targetViewId = R.id.btn_image_settings,
                highlightType = HighlightType.CIRCLE
            )
        )
    }
    
    private fun getSettingsTutorialSteps(): List<TutorialStep> {
        return listOf(
            TutorialStep(
                title = "Personalize AI Brother ⚙️",
                description = "Customize response style, appearance, privacy settings, and more to make AI Brother truly yours.",
                targetViewId = null,
                highlightType = HighlightType.NONE
            ),
            TutorialStep(
                title = "AI Configuration 🤖",
                description = "Adjust response style, creativity level, and memory settings to suit your preferences.",
                targetViewId = R.id.response_style_spinner,
                highlightType = HighlightType.RECTANGLE
            ),
            TutorialStep(
                title = "Privacy Controls 🔒",
                description = "Control data retention, local processing, and backup settings for maximum privacy.",
                targetViewId = R.id.local_processing_switch,
                highlightType = HighlightType.CIRCLE
            ),
            TutorialStep(
                title = "Theme & Appearance 🎨",
                description = "Switch between light/dark themes and adjust text size for better readability.",
                targetViewId = R.id.dark_theme_switch,
                highlightType = HighlightType.CIRCLE
            )
        )
    }
    
    private fun showTutorialStep() {
        val steps = getCurrentTutorialSteps()
        if (currentStep >= steps.size) {
            completeTutorial()
            return
        }
        
        val step = steps[currentStep]
        removeTutorialOverlay()
        
        // Create overlay
        tutorialOverlay = createTutorialOverlay(step)
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        rootView.addView(tutorialOverlay)
    }
    
    private fun createTutorialOverlay(step: TutorialStep): ViewGroup {
        val overlay = FrameLayout(activity)
        overlay.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        
        // Semi-transparent background
        overlay.setBackgroundColor(Color.parseColor("#CC000000"))
        
        // Create highlight if needed
        step.targetViewId?.let { viewId ->
            val targetView = activity.findViewById<View>(viewId)
            targetView?.let { view ->
                val highlightView = createHighlightView(view, step.highlightType)
                overlay.addView(highlightView)
            }
        }
        
        // Create info card
        val infoCard = createInfoCard(step)
        overlay.addView(infoCard)
        
        // Handle tap to continue
        overlay.setOnClickListener {
            nextStep()
        }
        
        return overlay
    }
    
    private fun createHighlightView(targetView: View, highlightType: HighlightType): View {
        val highlightView = View(activity)
        
        // Get target view position
        val location = IntArray(2)
        targetView.getLocationOnScreen(location)
        
        val params = FrameLayout.LayoutParams(
            targetView.width + 32, // Add padding
            targetView.height + 32
        )
        params.leftMargin = location[0] - 16
        params.topMargin = location[1] - 16
        
        highlightView.layoutParams = params
        
        // Create highlight drawable
        val drawable = when (highlightType) {
            HighlightType.CIRCLE -> createCircleHighlight()
            HighlightType.RECTANGLE -> createRectangleHighlight()
            HighlightType.NONE -> null
        }
        
        highlightView.background = drawable
        return highlightView
    }
    
    private fun createCircleHighlight(): Drawable {
        return ContextCompat.getDrawable(activity, android.R.drawable.button_onoff_indicator_on)?.apply {
            setTint(Color.parseColor("#4CAF50"))
        } ?: ColorDrawable(Color.parseColor("#4CAF50"))
    }
    
    private fun createRectangleHighlight(): Drawable {
        return ColorDrawable(Color.parseColor("#664CAF50"))
    }
    
    private fun createInfoCard(step: TutorialStep): View {
        val cardLayout = LinearLayout(activity)
        cardLayout.orientation = LinearLayout.VERTICAL
        cardLayout.setPadding(32, 32, 32, 32)
        cardLayout.setBackgroundColor(Color.WHITE)
        
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.BOTTOM
        params.setMargins(32, 32, 32, 32)
        cardLayout.layoutParams = params
        
        // Title
        val titleView = TextView(activity)
        titleView.text = step.title
        titleView.textSize = 20f
        titleView.setTextColor(Color.BLACK)
        titleView.typeface = Typeface.DEFAULT_BOLD
        cardLayout.addView(titleView)
        
        // Description
        val descriptionView = TextView(activity)
        descriptionView.text = step.description
        descriptionView.textSize = 16f
        descriptionView.setTextColor(Color.GRAY)
        descriptionView.setPadding(0, 16, 0, 24)
        cardLayout.addView(descriptionView)
        
        // Button container
        val buttonContainer = LinearLayout(activity)
        buttonContainer.orientation = LinearLayout.HORIZONTAL
        buttonContainer.gravity = Gravity.END
        
        // Skip button (only on first step)
        if (currentStep == 0) {
            val skipButton = Button(activity)
            skipButton.text = "Skip Tutorial"
            skipButton.setTextColor(Color.GRAY)
            skipButton.setBackgroundColor(Color.TRANSPARENT)
            skipButton.setOnClickListener { skipTutorial() }
            buttonContainer.addView(skipButton)
        }
        
        // Next/Finish button
        val nextButton = Button(activity)
        val steps = getCurrentTutorialSteps()
        nextButton.text = if (currentStep == steps.size - 1) "Get Started!" else "Next"
        nextButton.setTextColor(Color.WHITE)
        nextButton.setBackgroundColor(Color.parseColor("#673AB7"))
        nextButton.setPadding(32, 16, 32, 16)
        nextButton.setOnClickListener { nextStep() }
        buttonContainer.addView(nextButton)
        
        cardLayout.addView(buttonContainer)
        
        return cardLayout
    }
    
    private fun nextStep() {
        currentStep++
        showTutorialStep()
    }
    
    private fun skipTutorial() {
        sharedPrefs.edit().putBoolean(PREF_SKIP_TUTORIAL, true).apply()
        removeTutorialOverlay()
    }
    
    private fun completeTutorial() {
        // Mark specific tutorial as completed
        when (currentTutorialType) {
            TutorialType.MAIN_ONBOARDING -> {
                sharedPrefs.edit().putBoolean(PREF_TUTORIAL_COMPLETED, true).apply()
                showMainTutorialCompleteDialog()
            }
            TutorialType.CHAT_FEATURES -> {
                sharedPrefs.edit().putBoolean(PREF_CHAT_TUTORIAL_SHOWN, true).apply()
                showFeatureTutorialCompleteDialog("Chat Features", "Start having amazing conversations with AI Brother!")
            }
            TutorialType.MODEL_MANAGEMENT -> {
                sharedPrefs.edit().putBoolean(PREF_MODEL_TUTORIAL_SHOWN, true).apply()
                showFeatureTutorialCompleteDialog("Model Management", "Try downloading and switching between different AI models!")
            }
            TutorialType.FILE_PROCESSING -> {
                sharedPrefs.edit().putBoolean(PREF_FILE_TUTORIAL_SHOWN, true).apply()
                showFeatureTutorialCompleteDialog("File Processing", "Upload your first document and see AI Brother analyze it!")
            }
            TutorialType.CAMERA_FEATURES -> {
                sharedPrefs.edit().putBoolean(PREF_CAMERA_TUTORIAL_SHOWN, true).apply()
                showFeatureTutorialCompleteDialog("Camera Features", "Take your first photo and see AI Brother analyze it!")
            }
            TutorialType.SETTINGS_TOUR -> {
                showFeatureTutorialCompleteDialog("Settings Tour", "Your AI Brother is now perfectly customized!")
            }
        }
        
        removeTutorialOverlay()
    }
    
    private fun showMainTutorialCompleteDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Welcome to AI Brother! 🎉")
            .setMessage("You're all set up! Start by tapping the Chat tab and saying hello to your new AI assistant. You can replay tutorials anytime from Settings.")
            .setPositiveButton("Start Chatting!") { _, _ ->
                val bottomNav = activity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
                bottomNav?.selectedItemId = R.id.nav_chat
            }
            .setNeutralButton("Explore Features") { _, _ ->
                // Stay on current screen
            }
            .show()
    }
    
    private fun showFeatureTutorialCompleteDialog(featureName: String, message: String) {
        AlertDialog.Builder(activity)
            .setTitle("$featureName Tutorial Complete! ✅")
            .setMessage(message)
            .setPositiveButton("Got it!") { _, _ ->
                // Close dialog
            }
            .show()
    }
    
    private fun removeTutorialOverlay() {
        tutorialOverlay?.let { overlay ->
            val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
            rootView.removeView(overlay)
        }
        tutorialOverlay = null
    }
    
    fun resetTutorial() {
        sharedPrefs.edit()
            .putBoolean(PREF_TUTORIAL_COMPLETED, false)
            .putBoolean(PREF_SKIP_TUTORIAL, false)
            .putBoolean(PREF_CHAT_TUTORIAL_SHOWN, false)
            .putBoolean(PREF_MODEL_TUTORIAL_SHOWN, false)
            .putBoolean(PREF_FILE_TUTORIAL_SHOWN, false)
            .putBoolean(PREF_CAMERA_TUTORIAL_SHOWN, false)
            .apply()
    }
    
    // Contextual Tooltips System
    fun showTooltip(targetView: View, title: String, message: String) {
        val tooltip = createTooltip(targetView, title, message)
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        rootView.addView(tooltip)
        
        // Auto-dismiss after 5 seconds
        tooltip.postDelayed({
            rootView.removeView(tooltip)
        }, 5000)
    }
    
    private fun createTooltip(targetView: View, title: String, message: String): View {
        val tooltipLayout = FrameLayout(activity)
        tooltipLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        
        // Get target view position
        val location = IntArray(2)
        targetView.getLocationOnScreen(location)
        
        // Create tooltip bubble
        val bubble = LinearLayout(activity)
        bubble.orientation = LinearLayout.VERTICAL
        bubble.setPadding(24, 16, 24, 16)
        bubble.setBackgroundColor(Color.parseColor("#333333"))
        
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = location[0]
        params.topMargin = location[1] - 120 // Position above the target
        params.setMargins(16, 16, 16, 16)
        bubble.layoutParams = params
        
        // Title
        val titleView = TextView(activity)
        titleView.text = title
        titleView.textSize = 14f
        titleView.setTextColor(Color.WHITE)
        titleView.typeface = Typeface.DEFAULT_BOLD
        bubble.addView(titleView)
        
        // Message
        val messageView = TextView(activity)
        messageView.text = message
        messageView.textSize = 12f
        messageView.setTextColor(Color.parseColor("#CCCCCC"))
        messageView.setPadding(0, 8, 0, 0)
        bubble.addView(messageView)
        
        tooltipLayout.addView(bubble)
        
        // Tap to dismiss
        tooltipLayout.setOnClickListener {
            val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
            rootView.removeView(tooltipLayout)
        }
        
        return tooltipLayout
    }
    
    // Quick help methods for common scenarios
    fun showFirstTimeHelp(feature: String) {
        when (feature) {
            "chat" -> if (!sharedPrefs.getBoolean(PREF_CHAT_TUTORIAL_SHOWN, false)) startChatTutorial()
            "models" -> if (!sharedPrefs.getBoolean(PREF_MODEL_TUTORIAL_SHOWN, false)) startModelTutorial()
            "files" -> if (!sharedPrefs.getBoolean(PREF_FILE_TUTORIAL_SHOWN, false)) startFileTutorial()
            "camera" -> if (!sharedPrefs.getBoolean(PREF_CAMERA_TUTORIAL_SHOWN, false)) startCameraTutorial()
        }
    }
    
    data class TutorialStep(
        val title: String,
        val description: String,
        val targetViewId: Int?,
        val highlightType: HighlightType
    )
    
    enum class HighlightType {
        NONE, CIRCLE, RECTANGLE
    }
}