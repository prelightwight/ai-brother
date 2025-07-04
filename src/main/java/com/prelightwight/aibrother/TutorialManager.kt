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
    private val sharedPrefs: SharedPreferences = activity.getSharedPreferences("tutorial_prefs", Context.MODE_PRIVATE)
    
    companion object {
        const val PREF_TUTORIAL_COMPLETED = "tutorial_completed"
        const val PREF_SKIP_TUTORIAL = "skip_tutorial"
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
            title = "Image Analysis 🖼️",
            description = "Take photos or upload images for AI analysis. Get descriptions, extract text, and more!",
            targetViewId = R.id.nav_images,
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
        showTutorialStep()
    }
    
    private fun showTutorialStep() {
        if (currentStep >= tutorialSteps.size) {
            completeTutorial()
            return
        }
        
        val step = tutorialSteps[currentStep]
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
        nextButton.text = if (currentStep == tutorialSteps.size - 1) "Get Started!" else "Next"
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
        sharedPrefs.edit().putBoolean(PREF_TUTORIAL_COMPLETED, true).apply()
        removeTutorialOverlay()
        
        // Show completion message
        AlertDialog.Builder(activity)
            .setTitle("Tutorial Complete! 🎉")
            .setMessage("Welcome to AI Brother! Start by tapping the Chat tab and saying hello to your new AI assistant.")
            .setPositiveButton("Start Chatting!") { _, _ ->
                // Navigate to chat tab if possible
                val bottomNav = activity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
                bottomNav?.selectedItemId = R.id.nav_chat
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
            .apply()
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