package com.prelightwight.aibrother.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * Manages voice input (speech-to-text) and output (text-to-speech) functionality
 */
class VoiceManager(private val context: Context) {
    private val tag = "VoiceManager"
    
    // Text-to-Speech
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false
    
    // Speech Recognition
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    
    // State flows
    private val _isListening = MutableStateFlow(false)
    val isListeningFlow: StateFlow<Boolean> = _isListening.asStateFlow()
    
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeakingFlow: StateFlow<Boolean> = _isSpeaking.asStateFlow()
    
    private val _recognizedText = MutableStateFlow("")
    val recognizedTextFlow: StateFlow<String> = _recognizedText.asStateFlow()
    
    private val _voiceError = MutableStateFlow<String?>(null)
    val voiceErrorFlow: StateFlow<String?> = _voiceError.asStateFlow()
    
    // Voice settings
    data class VoiceSettings(
        val speechRate: Float = 1.0f,
        val pitch: Float = 1.0f,
        val language: Locale = Locale.getDefault(),
        val autoListen: Boolean = false,
        val voiceCommands: Boolean = true
    )
    
    private var settings = VoiceSettings()
    
    init {
        initializeTextToSpeech()
        initializeSpeechRecognizer()
    }
    
    /**
     * Initialize Text-to-Speech
     */
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.let { tts ->
                    val result = tts.setLanguage(settings.language)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w(tag, "Language not supported, using default")
                        tts.setLanguage(Locale.US)
                    }
                    
                    tts.setSpeechRate(settings.speechRate)
                    tts.setPitch(settings.pitch)
                    
                    // Set utterance progress listener
                    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                            Log.d(tag, "TTS started speaking")
                        }
                        
                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                            Log.d(tag, "TTS finished speaking")
                            
                            // Auto-listen after speaking if enabled
                            if (settings.autoListen) {
                                startListening()
                            }
                        }
                        
                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                            _voiceError.value = "Text-to-speech error"
                            Log.e(tag, "TTS error")
                        }
                    })
                    
                    isTtsInitialized = true
                    Log.d(tag, "Text-to-Speech initialized successfully")
                }
            } else {
                Log.e(tag, "Text-to-Speech initialization failed")
                _voiceError.value = "Text-to-speech not available"
            }
        }
    }
    
    /**
     * Initialize Speech Recognizer
     */
    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                    _voiceError.value = null
                    Log.d(tag, "Ready for speech")
                }
                
                override fun onBeginningOfSpeech() {
                    Log.d(tag, "Beginning of speech detected")
                }
                
                override fun onRmsChanged(rmsdB: Float) {
                    // Volume level changed - could be used for UI feedback
                }
                
                override fun onBufferReceived(buffer: ByteArray?) {
                    // Audio buffer received
                }
                
                override fun onEndOfSpeech() {
                    Log.d(tag, "End of speech")
                }
                
                override fun onError(error: Int) {
                    _isListening.value = false
                    isListening = false
                    
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech match found"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                        else -> "Unknown recognition error"
                    }
                    
                    _voiceError.value = errorMessage
                    Log.e(tag, "Speech recognition error: $errorMessage")
                }
                
                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    isListening = false
                    
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val recognizedText = matches[0]
                        _recognizedText.value = recognizedText
                        Log.d(tag, "Speech recognized: $recognizedText")
                        
                        // Process voice commands if enabled
                        if (settings.voiceCommands) {
                            processVoiceCommand(recognizedText)
                        }
                    }
                }
                
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        _recognizedText.value = matches[0]
                    }
                }
                
                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Handle recognition events
                }
            })
            
            Log.d(tag, "Speech recognizer initialized successfully")
        } else {
            Log.e(tag, "Speech recognition not available")
            _voiceError.value = "Speech recognition not available"
        }
    }
    
    /**
     * Start listening for speech input
     */
    fun startListening() {
        if (isListening) return
        
        speechRecognizer?.let { recognizer ->
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, settings.language.toString())
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
            }
            
            try {
                recognizer.startListening(intent)
                isListening = true
                _recognizedText.value = ""
                Log.d(tag, "Started listening for speech")
            } catch (e: Exception) {
                Log.e(tag, "Error starting speech recognition", e)
                _voiceError.value = "Failed to start listening: ${e.message}"
            }
        } ?: run {
            _voiceError.value = "Speech recognizer not available"
        }
    }
    
    /**
     * Stop listening for speech input
     */
    fun stopListening() {
        if (!isListening) return
        
        speechRecognizer?.stopListening()
        isListening = false
        _isListening.value = false
        Log.d(tag, "Stopped listening for speech")
    }
    
    /**
     * Speak text using text-to-speech
     */
    fun speak(text: String, interrupt: Boolean = true) {
        if (!isTtsInitialized) {
            _voiceError.value = "Text-to-speech not initialized"
            return
        }
        
        textToSpeech?.let { tts ->
            val queueMode = if (interrupt) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val utteranceId = UUID.randomUUID().toString()
            
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            
            val result = tts.speak(text, queueMode, params, utteranceId)
            if (result == TextToSpeech.ERROR) {
                _voiceError.value = "Text-to-speech error"
                Log.e(tag, "TTS speak error")
            } else {
                Log.d(tag, "Speaking text: ${text.take(50)}...")
            }
        }
    }
    
    /**
     * Stop speaking
     */
    fun stopSpeaking() {
        textToSpeech?.stop()
        _isSpeaking.value = false
        Log.d(tag, "Stopped speaking")
    }
    
    /**
     * Process voice commands
     */
    private fun processVoiceCommand(text: String) {
        val lowerText = text.lowercase()
        
        when {
            lowerText.contains("stop listening") || lowerText.contains("stop recording") -> {
                stopListening()
                speak("Stopped listening")
            }
            lowerText.contains("start listening") || lowerText.contains("listen") -> {
                speak("Starting to listen")
                // Will auto-start after speaking if autoListen is enabled
            }
            lowerText.contains("stop talking") || lowerText.contains("quiet") -> {
                stopSpeaking()
            }
            lowerText.contains("repeat") || lowerText.contains("say again") -> {
                // This would need to be handled by the calling component
                Log.d(tag, "Repeat command detected")
            }
        }
    }
    
    /**
     * Update voice settings
     */
    fun updateSettings(newSettings: VoiceSettings) {
        settings = newSettings
        
        textToSpeech?.let { tts ->
            tts.setSpeechRate(settings.speechRate)
            tts.setPitch(settings.pitch)
            tts.setLanguage(settings.language)
        }
        
        Log.d(tag, "Voice settings updated")
    }
    
    /**
     * Get available TTS languages
     */
    fun getAvailableLanguages(): Set<Locale> {
        return textToSpeech?.availableLanguages ?: emptySet()
    }
    
    /**
     * Check if TTS is speaking
     */
    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking == true
    }
    
    /**
     * Check if speech recognizer is listening
     */
    fun isListening(): Boolean {
        return isListening
    }
    
    /**
     * Get current voice settings
     */
    fun getSettings(): VoiceSettings {
        return settings
    }
    
    /**
     * Clear voice error
     */
    fun clearError() {
        _voiceError.value = null
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        stopListening()
        stopSpeaking()
        
        speechRecognizer?.destroy()
        speechRecognizer = null
        
        textToSpeech?.shutdown()
        textToSpeech = null
        isTtsInitialized = false
        
        Log.d(tag, "Voice manager cleaned up")
    }
    
    /**
     * Voice command types for external handling
     */
    enum class VoiceCommand {
        START_LISTENING,
        STOP_LISTENING,
        REPEAT,
        CLEAR_SCREEN,
        NEW_CONVERSATION,
        SAVE_MEMORY,
        SEARCH,
        HELP
    }
    
    /**
     * Parse voice command from text
     */
    fun parseVoiceCommand(text: String): VoiceCommand? {
        val lowerText = text.lowercase()
        
        return when {
            lowerText.contains("start listening") -> VoiceCommand.START_LISTENING
            lowerText.contains("stop listening") -> VoiceCommand.STOP_LISTENING
            lowerText.contains("repeat") || lowerText.contains("say again") -> VoiceCommand.REPEAT
            lowerText.contains("clear") || lowerText.contains("new chat") -> VoiceCommand.NEW_CONVERSATION
            lowerText.contains("save") || lowerText.contains("remember") -> VoiceCommand.SAVE_MEMORY
            lowerText.contains("search") || lowerText.contains("find") -> VoiceCommand.SEARCH
            lowerText.contains("help") -> VoiceCommand.HELP
            else -> null
        }
    }
}