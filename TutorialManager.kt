package com.prelightwight.aibrother.tutorial

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// Create DataStore for tutorial preferences
private val Context.tutorialDataStore: DataStore<Preferences> by preferencesDataStore(name = "tutorial_settings")

data class TutorialStep(
    val id: String,
    val title: String,
    val description: String,
    val targetIcon: @Composable () -> Unit = { Icon(Icons.Default.Info, contentDescription = null) },
    val action: String = "Next",
    val isLast: Boolean = false
)

enum class TutorialScreen(val displayName: String) {
    WELCOME("Welcome"),
    CHAT("Chat"),
    BRAIN("Brain"),
    FILES("Files"),
    KNOWLEDGE("Knowledge"),
    SEARCH("Search"),
    IMAGES("Images"),
    SETTINGS("Settings")
}

class TutorialManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "TutorialManager"
        
        @Volatile
        private var INSTANCE: TutorialManager? = null
        
        fun getInstance(context: Context): TutorialManager {
            return INSTANCE ?: synchronized(this) {
                val instance = TutorialManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
        
        // Preference keys
        private val WELCOME_TUTORIAL_SHOWN = booleanPreferencesKey("welcome_tutorial_shown")
        private val CHAT_TUTORIAL_SHOWN = booleanPreferencesKey("chat_tutorial_shown")
        private val BRAIN_TUTORIAL_SHOWN = booleanPreferencesKey("brain_tutorial_shown")
        private val FILES_TUTORIAL_SHOWN = booleanPreferencesKey("files_tutorial_shown")
        private val KNOWLEDGE_TUTORIAL_SHOWN = booleanPreferencesKey("knowledge_tutorial_shown")
        private val SEARCH_TUTORIAL_SHOWN = booleanPreferencesKey("search_tutorial_shown")
        private val IMAGES_TUTORIAL_SHOWN = booleanPreferencesKey("images_tutorial_shown")
        private val SETTINGS_TUTORIAL_SHOWN = booleanPreferencesKey("settings_tutorial_shown")
        private val TUTORIALS_DISABLED = booleanPreferencesKey("tutorials_disabled")
    }

    private val dataStore = context.tutorialDataStore

    // Check if tutorial should be shown for a screen
    fun shouldShowTutorial(screen: TutorialScreen): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            val tutorialsDisabled = preferences[TUTORIALS_DISABLED] ?: false
            if (tutorialsDisabled) return@map false
            
            val key = when (screen) {
                TutorialScreen.WELCOME -> WELCOME_TUTORIAL_SHOWN
                TutorialScreen.CHAT -> CHAT_TUTORIAL_SHOWN
                TutorialScreen.BRAIN -> BRAIN_TUTORIAL_SHOWN
                TutorialScreen.FILES -> FILES_TUTORIAL_SHOWN
                TutorialScreen.KNOWLEDGE -> KNOWLEDGE_TUTORIAL_SHOWN
                TutorialScreen.SEARCH -> SEARCH_TUTORIAL_SHOWN
                TutorialScreen.IMAGES -> IMAGES_TUTORIAL_SHOWN
                TutorialScreen.SETTINGS -> SETTINGS_TUTORIAL_SHOWN
            }
            
            !(preferences[key] ?: false)
        }
    }

    // Mark tutorial as shown for a screen
    suspend fun markTutorialShown(screen: TutorialScreen) {
        val key = when (screen) {
            TutorialScreen.WELCOME -> WELCOME_TUTORIAL_SHOWN
            TutorialScreen.CHAT -> CHAT_TUTORIAL_SHOWN
            TutorialScreen.BRAIN -> BRAIN_TUTORIAL_SHOWN
            TutorialScreen.FILES -> FILES_TUTORIAL_SHOWN
            TutorialScreen.KNOWLEDGE -> KNOWLEDGE_TUTORIAL_SHOWN
            TutorialScreen.SEARCH -> SEARCH_TUTORIAL_SHOWN
            TutorialScreen.IMAGES -> IMAGES_TUTORIAL_SHOWN
            TutorialScreen.SETTINGS -> SETTINGS_TUTORIAL_SHOWN
        }
        
        dataStore.edit { preferences ->
            preferences[key] = true
        }
    }

    // Disable all tutorials
    suspend fun disableAllTutorials() {
        dataStore.edit { preferences ->
            preferences[TUTORIALS_DISABLED] = true
        }
    }

    // Reset all tutorials (for testing or re-showing)
    suspend fun resetAllTutorials() {
        dataStore.edit { preferences ->
            preferences[WELCOME_TUTORIAL_SHOWN] = false
            preferences[CHAT_TUTORIAL_SHOWN] = false
            preferences[BRAIN_TUTORIAL_SHOWN] = false
            preferences[FILES_TUTORIAL_SHOWN] = false
            preferences[KNOWLEDGE_TUTORIAL_SHOWN] = false
            preferences[SEARCH_TUTORIAL_SHOWN] = false
            preferences[IMAGES_TUTORIAL_SHOWN] = false
            preferences[SETTINGS_TUTORIAL_SHOWN] = false
            preferences[TUTORIALS_DISABLED] = false
        }
    }

    // Get tutorial steps for each screen
    fun getTutorialSteps(screen: TutorialScreen): List<TutorialStep> {
        return when (screen) {
            TutorialScreen.WELCOME -> getWelcomeTutorialSteps()
            TutorialScreen.CHAT -> getChatTutorialSteps()
            TutorialScreen.BRAIN -> getBrainTutorialSteps()
            TutorialScreen.FILES -> getFilesTutorialSteps()
            TutorialScreen.KNOWLEDGE -> getKnowledgeTutorialSteps()
            TutorialScreen.SEARCH -> getSearchTutorialSteps()
            TutorialScreen.IMAGES -> getImagesTutorialSteps()
            TutorialScreen.SETTINGS -> getSettingsTutorialSteps()
        }
    }

    private fun getWelcomeTutorialSteps() = listOf(
        TutorialStep(
            id = "welcome_1",
            title = "Welcome to AI Brother! 🤖",
            description = "AI Brother is your private, intelligent assistant that works completely offline. Let's explore what makes it special!",
            targetIcon = { Icon(Icons.Default.Waving, contentDescription = null) }
        ),
        TutorialStep(
            id = "welcome_2",
            title = "Privacy First 🔒",
            description = "Everything happens on your device. Your conversations, files, and data never leave your phone unless you choose to search the web.",
            targetIcon = { Icon(Icons.Default.Security, contentDescription = null) }
        ),
        TutorialStep(
            id = "welcome_3",
            title = "AI-Powered Features 🧠",
            description = "Chat with AI, analyze images, process documents, manage knowledge, and search the web - all with cutting-edge AI technology.",
            targetIcon = { Icon(Icons.Default.Psychology, contentDescription = null) }
        ),
        TutorialStep(
            id = "welcome_4",
            title = "Voice Integration 🎤",
            description = "Talk to AI Brother naturally! Use voice commands, dictate messages, and have conversations spoken back to you.",
            targetIcon = { Icon(Icons.Default.Mic, contentDescription = null) }
        ),
        TutorialStep(
            id = "welcome_5",
            title = "Let's Get Started! 🚀",
            description = "We'll show you around each feature. You can dismiss any tutorial and choose 'Don't show again' if you prefer to explore on your own.",
            targetIcon = { Icon(Icons.Default.Explore, contentDescription = null) },
            action = "Start Tour",
            isLast = true
        )
    )

    private fun getChatTutorialSteps() = listOf(
        TutorialStep(
            id = "chat_1",
            title = "AI Chat Interface 💬",
            description = "This is where you have conversations with AI Brother. Type messages or use voice input for natural conversations.",
            targetIcon = { Icon(Icons.Default.Chat, contentDescription = null) }
        ),
        TutorialStep(
            id = "chat_2",
            title = "Voice Input 🎤",
            description = "Tap the microphone button to speak your message. AI Brother will understand your speech and respond both in text and voice.",
            targetIcon = { Icon(Icons.Default.Mic, contentDescription = null) }
        ),
        TutorialStep(
            id = "chat_3",
            title = "Load AI Models 🧠",
            description = "Use 'Load Model File' to add custom AI models (.gguf files) for more advanced conversations. The app works great with built-in AI too!",
            targetIcon = { Icon(Icons.Default.ModelTraining, contentDescription = null) }
        ),
        TutorialStep(
            id = "chat_4",
            title = "Message Features 📱",
            description = "Tap the speaker icon on AI messages to hear them read aloud. All conversations are automatically saved and synced.",
            targetIcon = { Icon(Icons.Default.VolumeUp, contentDescription = null) },
            isLast = true
        )
    )

    private fun getBrainTutorialSteps() = listOf(
        TutorialStep(
            id = "brain_1",
            title = "The Brain - Memory System 🧠",
            description = "This is AI Brother's persistent memory. Store important information, facts, and knowledge that the AI can reference later.",
            targetIcon = { Icon(Icons.Default.Memory, contentDescription = null) }
        ),
        TutorialStep(
            id = "brain_2",
            title = "Add Memories 📝",
            description = "Type anything you want AI Brother to remember - personal facts, preferences, important information, or useful knowledge.",
            targetIcon = { Icon(Icons.Default.Add, contentDescription = null) }
        ),
        TutorialStep(
            id = "brain_3",
            title = "Smart Recall 🔍",
            description = "When you chat with AI Brother, it can access these memories to give more personalized and context-aware responses.",
            targetIcon = { Icon(Icons.Default.Psychology, contentDescription = null) },
            isLast = true
        )
    )

    private fun getFilesTutorialSteps() = listOf(
        TutorialStep(
            id = "files_1",
            title = "Document Processing 📂",
            description = "Upload documents (PDF, Word, text files) and AI Brother will analyze, summarize, and help you understand their content.",
            targetIcon = { Icon(Icons.Default.Folder, contentDescription = null) }
        ),
        TutorialStep(
            id = "files_2",
            title = "Select Files 📎",
            description = "Tap 'Select Files' to choose documents from your device. AI Brother supports various formats and can process multiple files at once.",
            targetIcon = { Icon(Icons.Default.Upload, contentDescription = null) }
        ),
        TutorialStep(
            id = "files_3",
            title = "AI Analysis 🔍",
            description = "Each file gets automatically summarized and analyzed. Tap any file to see the full content and AI-generated insights.",
            targetIcon = { Icon(Icons.Default.Analytics, contentDescription = null) }
        ),
        TutorialStep(
            id = "files_4",
            title = "Smart Search 🔎",
            description = "All processed files become searchable. Ask AI Brother questions about your documents in the chat interface!",
            targetIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            isLast = true
        )
    )

    private fun getKnowledgeTutorialSteps() = listOf(
        TutorialStep(
            id = "knowledge_1",
            title = "Knowledge Base 📚",
            description = "Build your personal knowledge repository with facts, articles, notes, and information organized by categories.",
            targetIcon = { Icon(Icons.Default.MenuBook, contentDescription = null) }
        ),
        TutorialStep(
            id = "knowledge_2",
            title = "Categories & Search 🏷️",
            description = "Use category filters (Science, Technology, History, etc.) and the search bar to quickly find specific information.",
            targetIcon = { Icon(Icons.Default.Category, contentDescription = null) }
        ),
        TutorialStep(
            id = "knowledge_3",
            title = "Add Knowledge ➕",
            description = "Tap the + button to add new knowledge entries. Include tags for better organization and searchability.",
            targetIcon = { Icon(Icons.Default.Add, contentDescription = null) }
        ),
        TutorialStep(
            id = "knowledge_4",
            title = "AI Integration 🤖",
            description = "AI Brother can reference your knowledge base during conversations to provide more informed and accurate responses.",
            targetIcon = { Icon(Icons.Default.SmartToy, contentDescription = null) },
            isLast = true
        )
    )

    private fun getSearchTutorialSteps() = listOf(
        TutorialStep(
            id = "search_1",
            title = "Web Search 🌐",
            description = "Search the internet with privacy protection. Choose between standard web search or anonymous Tor network searching.",
            targetIcon = { Icon(Icons.Default.Language, contentDescription = null) }
        ),
        TutorialStep(
            id = "search_2",
            title = "Tor Privacy 🔒",
            description = "Toggle the Tor switch for anonymous searching. Your searches will be routed through the Tor network for maximum privacy.",
            targetIcon = { Icon(Icons.Default.Security, contentDescription = null) }
        ),
        TutorialStep(
            id = "search_3",
            title = "Live Suggestions 💡",
            description = "As you type, get live search suggestions. Your search history is saved locally for quick access to previous searches.",
            targetIcon = { Icon(Icons.Default.TrendingUp, contentDescription = null) }
        ),
        TutorialStep(
            id = "search_4",
            title = "Rich Results 📄",
            description = "Get detailed search results with summaries and sources. Results are clearly marked as Clearnet or Tor searches.",
            targetIcon = { Icon(Icons.Default.Article, contentDescription = null) },
            isLast = true
        )
    )

    private fun getImagesTutorialSteps() = listOf(
        TutorialStep(
            id = "images_1",
            title = "AI Image Analysis 🖼️",
            description = "Take photos or select images for AI-powered analysis. Get object detection, text extraction, and detailed descriptions.",
            targetIcon = { Icon(Icons.Default.Image, contentDescription = null) }
        ),
        TutorialStep(
            id = "images_2",
            title = "Camera & Gallery 📸",
            description = "Use the Camera button to take new photos or Gallery to select existing images. Both options trigger automatic AI analysis.",
            targetIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null) }
        ),
        TutorialStep(
            id = "images_3",
            title = "Real AI Analysis 🔍",
            description = "AI Brother detects objects, extracts text (OCR), analyzes colors, and generates natural language descriptions of your images.",
            targetIcon = { Icon(Icons.Default.Analytics, contentDescription = null) }
        ),
        TutorialStep(
            id = "images_4",
            title = "Analysis History 📊",
            description = "All analyzed images are saved with their results. Tap any image to see detailed analysis including confidence scores.",
            targetIcon = { Icon(Icons.Default.History, contentDescription = null) },
            isLast = true
        )
    )

    private fun getSettingsTutorialSteps() = listOf(
        TutorialStep(
            id = "settings_1",
            title = "App Settings ⚙️",
            description = "Customize AI Brother's behavior, appearance, and privacy settings to match your preferences.",
            targetIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
        ),
        TutorialStep(
            id = "settings_2",
            title = "Theme & Appearance 🎨",
            description = "Toggle between light and dark themes. More customization options are available for personalizing your experience.",
            targetIcon = { Icon(Icons.Default.Palette, contentDescription = null) }
        ),
        TutorialStep(
            id = "settings_3",
            title = "Privacy Controls 🔐",
            description = "Control Tor usage for web searches, manage data storage, and configure privacy settings to your comfort level.",
            targetIcon = { Icon(Icons.Default.PrivacyTip, contentDescription = null) },
            isLast = true
        )
    )
}

@Composable
fun TutorialOverlay(
    screen: TutorialScreen,
    onDismiss: () -> Unit,
    onDontShowAgain: () -> Unit
) {
    val context = LocalContext.current
    val tutorialManager = remember { TutorialManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    val steps = remember { tutorialManager.getTutorialSteps(screen) }
    var currentStep by remember { mutableStateOf(0) }
    var showTutorial by remember { mutableStateOf(true) }

    if (showTutorial && steps.isNotEmpty()) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
            ) {
                // Tutorial content card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .align(Alignment.Center),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Step indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(steps.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (index == currentStep) 
                                                MaterialTheme.colorScheme.primary 
                                            else 
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        )
                                )
                                if (index < steps.size - 1) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Icon
                        steps[currentStep].targetIcon()

                        Spacer(modifier = Modifier.height(16.dp))

                        // Title
                        Text(
                            text = steps[currentStep].title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Description
                        Text(
                            text = steps[currentStep].description,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Skip/Don't show again
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        tutorialManager.disableAllTutorials()
                                        showTutorial = false
                                        onDontShowAgain()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Don't show again")
                            }

                            // Dismiss/Previous
                            if (currentStep > 0) {
                                OutlinedButton(
                                    onClick = { currentStep-- },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Previous")
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        showTutorial = false
                                        onDismiss()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Skip")
                                }
                            }

                            // Next/Finish
                            Button(
                                onClick = {
                                    if (steps[currentStep].isLast) {
                                        scope.launch {
                                            tutorialManager.markTutorialShown(screen)
                                            showTutorial = false
                                            onDismiss()
                                        }
                                    } else {
                                        currentStep++
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    if (steps[currentStep].isLast) "Finish" 
                                    else steps[currentStep].action
                                )
                            }
                        }
                    }
                }

                // Tutorial info badge
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${screen.displayName} Tutorial",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TutorialWrapper(
    screen: TutorialScreen,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val tutorialManager = remember { TutorialManager.getInstance(context) }
    
    val shouldShowTutorial by tutorialManager.shouldShowTutorial(screen).collectAsState(initial = false)
    var showTutorial by remember { mutableStateOf(false) }

    // Show tutorial when shouldShowTutorial becomes true
    LaunchedEffect(shouldShowTutorial) {
        if (shouldShowTutorial) {
            showTutorial = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (showTutorial) {
            TutorialOverlay(
                screen = screen,
                onDismiss = { 
                    showTutorial = false
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        tutorialManager.markTutorialShown(screen)
                    }
                },
                onDontShowAgain = { 
                    showTutorial = false 
                }
            )
        }
    }
}

// Extension function for easy tutorial integration
@Composable
fun WithTutorial(
    screen: TutorialScreen,
    content: @Composable () -> Unit
) {
    TutorialWrapper(screen = screen, content = content)
}