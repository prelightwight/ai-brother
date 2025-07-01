package com.prelightwight.aibrother.voice

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class VoiceRecognitionResult(
    val text: String,
    val confidence: Float,
    val isPartial: Boolean = false
)

data class VoiceNote(
    val id: String,
    val filePath: String,
    val duration: Long,
    val timestamp: Long,
    val transcription: String? = null,
    val isTranscribed: Boolean = false
)

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    RECORDING,
    ERROR
}

enum class TTSLanguage(val locale: Locale, val displayName: String) {
    ENGLISH_US(Locale.US, "English (US)"),
    ENGLISH_UK(Locale.UK, "English (UK)"),
    SPANISH(Locale("es", "ES"), "Spanish"),
    FRENCH(Locale.FRENCH, "French"),
    GERMAN(Locale.GERMAN, "German"),
    ITALIAN(Locale.ITALIAN, "Italian"),
    JAPANESE(Locale.JAPANESE, "Japanese"),
    KOREAN(Locale.KOREAN, "Korean"),
    CHINESE(Locale.CHINESE, "Chinese")
}

class VoiceManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "VoiceManager"
        private const val AUDIO_RECORDING_FILE_NAME = "voice_note"
        
        @Volatile
        private var INSTANCE: VoiceManager? = null
        
        fun getInstance(context: Context): VoiceManager {
            return INSTANCE ?: synchronized(this) {
                val instance = VoiceManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    // Speech Recognition
    private var speechRecognizer: SpeechRecognizer? = null
    private var recognitionIntent: Intent? = null
    
    // Text-to-Speech
    private var textToSpeech: TextToSpeech? = null
    private var ttsInitialized = false
    private var currentLanguage = TTSLanguage.ENGLISH_US
    
    // Voice Recording
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordingFile: File? = null
    private var recordingStartTime: Long = 0
    
    // State management
    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()
    
    private val _recognitionResults = MutableStateFlow<VoiceRecognitionResult?>(null)
    val recognitionResults: StateFlow<VoiceRecognitionResult?> = _recognitionResults.asStateFlow()
    
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    private var isInitialized = false
    private var initializationError: String? = null
    
    // Voice activity detection
    private var voiceActivityJob: Job? = null
    private var silenceThreshold = 1500L // 1.5 seconds of silence to auto-stop
    private var lastSpeechTime = 0L

    suspend fun initialize() = withContext(Dispatchers.Main) {
        try {
            Log.d(TAG, "Initializing Voice Manager...")
            
            initializeSpeechRecognition()
            initializeTextToSpeech()
            createAudioDirectory()
            
            isInitialized = true
            initializationError = null
            _voiceState.value = VoiceState.IDLE
            
            Log.d(TAG, "Voice Manager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Voice Manager", e)
            initializationError = e.message
            isInitialized = false
            _voiceState.value = VoiceState.ERROR
        }
    }

    private fun initializeSpeechRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            throw IllegalStateException("Speech recognition not available on this device")
        }
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(speechRecognitionListener)
        
        recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
        }
    }

    private suspend fun initializeTextToSpeech() = suspendCoroutine<Unit> { continuation ->
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.let { tts ->
                    val result = tts.setLanguage(currentLanguage.locale)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w(TAG, "Language ${currentLanguage.displayName} not supported, using default")
                        tts.setLanguage(Locale.getDefault())
                    }
                    
                    tts.setSpeechRate(1.0f)
                    tts.setPitch(1.0f)
                    
                    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _voiceState.value = VoiceState.SPEAKING
                        }
                        
                        override fun onDone(utteranceId: String?) {
                            _voiceState.value = VoiceState.IDLE
                        }
                        
                        override fun onError(utteranceId: String?) {
                            _voiceState.value = VoiceState.ERROR
                            Log.e(TAG, "TTS error for utterance: $utteranceId")
                        }
                    })
                    
                    ttsInitialized = true
                    Log.d(TAG, "Text-to-Speech initialized successfully")
                    continuation.resume(Unit)
                }
            } else {
                val error = "Text-to-Speech initialization failed"
                Log.e(TAG, error)
                ttsInitialized = false
                continuation.resumeWithException(Exception(error))
            }
        }
    }

    private fun createAudioDirectory() {
        val audioDir = File(context.filesDir, "voice_notes")
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }
    }

    // Speech Recognition Methods
    suspend fun startListening(): Boolean = withContext(Dispatchers.Main) {
        if (!isInitialized || _voiceState.value != VoiceState.IDLE) {
            return@withContext false
        }
        
        try {
            speechRecognizer?.startListening(recognitionIntent)
            _voiceState.value = VoiceState.LISTENING
            _isListening.value = true
            
            // Start voice activity detection
            startVoiceActivityDetection()
            
            Log.d(TAG, "Started speech recognition")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start speech recognition", e)
            _voiceState.value = VoiceState.ERROR
            false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _isListening.value = false
            voiceActivityJob?.cancel()
            
            if (_voiceState.value == VoiceState.LISTENING) {
                _voiceState.value = VoiceState.PROCESSING
            }
            
            Log.d(TAG, "Stopped speech recognition")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech recognition", e)
        }
    }

    private fun startVoiceActivityDetection() {
        voiceActivityJob?.cancel()
        voiceActivityJob = CoroutineScope(Dispatchers.Main).launch {
            lastSpeechTime = System.currentTimeMillis()
            
            while (_isListening.value) {
                delay(500) // Check every 500ms
                
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastSpeechTime > silenceThreshold) {
                    Log.d(TAG, "Auto-stopping due to silence")
                    stopListening()
                    break
                }
            }
        }
    }

    private val speechRecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "Ready for speech")
            _voiceState.value = VoiceState.LISTENING
        }

        override fun onBeginningOfSpeech() {
            Log.d(TAG, "Beginning of speech detected")
            lastSpeechTime = System.currentTimeMillis()
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Update last speech time if there's voice activity
            if (rmsdB > -15.0) { // Threshold for voice activity
                lastSpeechTime = System.currentTimeMillis()
            }
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            // Audio buffer received
        }

        override fun onEndOfSpeech() {
            Log.d(TAG, "End of speech detected")
            _isListening.value = false
            _voiceState.value = VoiceState.PROCESSING
        }

        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech input matched"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
                else -> "Unknown error: $error"
            }
            
            Log.e(TAG, "Speech recognition error: $errorMessage")
            _isListening.value = false
            _voiceState.value = VoiceState.IDLE
            voiceActivityJob?.cancel()
            
            // Only treat certain errors as actual failures
            if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                _voiceState.value = VoiceState.ERROR
            }
        }

        override fun onResults(results: Bundle?) {
            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { matches ->
                val confidences = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                
                if (matches.isNotEmpty()) {
                    val bestMatch = matches[0]
                    val confidence = confidences?.get(0) ?: 0.8f
                    
                    _recognitionResults.value = VoiceRecognitionResult(
                        text = bestMatch,
                        confidence = confidence,
                        isPartial = false
                    )
                    
                    Log.d(TAG, "Speech recognition result: $bestMatch (confidence: $confidence)")
                }
            }
            
            _isListening.value = false
            _voiceState.value = VoiceState.IDLE
            voiceActivityJob?.cancel()
        }

        override fun onPartialResults(partialResults: Bundle?) {
            partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { matches ->
                if (matches.isNotEmpty()) {
                    _recognitionResults.value = VoiceRecognitionResult(
                        text = matches[0],
                        confidence = 0.5f,
                        isPartial = true
                    )
                }
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            // Handle other events if needed
        }
    }

    // Text-to-Speech Methods
    suspend fun speak(text: String, language: TTSLanguage = currentLanguage): Boolean = withContext(Dispatchers.Main) {
        if (!ttsInitialized || text.isBlank()) {
            return@withContext false
        }
        
        try {
            // Set language if different
            if (language != currentLanguage) {
                setTTSLanguage(language)
            }
            
            val utteranceId = "tts_${System.currentTimeMillis()}"
            val result = textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            
            if (result == TextToSpeech.SUCCESS) {
                Log.d(TAG, "Started speaking: ${text.take(50)}...")
                true
            } else {
                Log.e(TAG, "Failed to start TTS")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in text-to-speech", e)
            false
        }
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
            if (_voiceState.value == VoiceState.SPEAKING) {
                _voiceState.value = VoiceState.IDLE
            }
            Log.d(TAG, "Stopped speaking")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
    }

    private fun setTTSLanguage(language: TTSLanguage) {
        textToSpeech?.let { tts ->
            val result = tts.setLanguage(language.locale)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                currentLanguage = language
                Log.d(TAG, "TTS language set to ${language.displayName}")
            } else {
                Log.w(TAG, "Language ${language.displayName} not supported")
            }
        }
    }

    // Voice Recording Methods
    suspend fun startRecording(): String? = withContext(Dispatchers.IO) {
        if (_voiceState.value != VoiceState.IDLE) {
            return@withContext null
        }
        
        try {
            val audioDir = File(context.filesDir, "voice_notes")
            currentRecordingFile = File(audioDir, "${AUDIO_RECORDING_FILE_NAME}_${System.currentTimeMillis()}.mp4")
            
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(currentRecordingFile!!.absolutePath)
                
                prepare()
                start()
            }
            
            recordingStartTime = System.currentTimeMillis()
            _voiceState.value = VoiceState.RECORDING
            
            Log.d(TAG, "Started voice recording: ${currentRecordingFile!!.absolutePath}")
            currentRecordingFile!!.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            _voiceState.value = VoiceState.ERROR
            null
        }
    }

    suspend fun stopRecording(): VoiceNote? = withContext(Dispatchers.IO) {
        if (_voiceState.value != VoiceState.RECORDING || currentRecordingFile == null) {
            return@withContext null
        }
        
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            
            val duration = System.currentTimeMillis() - recordingStartTime
            val voiceNote = VoiceNote(
                id = UUID.randomUUID().toString(),
                filePath = currentRecordingFile!!.absolutePath,
                duration = duration,
                timestamp = System.currentTimeMillis()
            )
            
            _voiceState.value = VoiceState.IDLE
            
            Log.d(TAG, "Stopped voice recording. Duration: ${duration}ms")
            voiceNote
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            _voiceState.value = VoiceState.ERROR
            null
        }
    }

    suspend fun playVoiceNote(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            stopVoiceNote() // Stop any currently playing audio
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setOnCompletionListener {
                    _voiceState.value = VoiceState.IDLE
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    _voiceState.value = VoiceState.ERROR
                    true
                }
                prepare()
                start()
            }
            
            _voiceState.value = VoiceState.SPEAKING // Reuse speaking state for playback
            Log.d(TAG, "Started playing voice note: $filePath")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play voice note", e)
            false
        }
    }

    fun stopVoiceNote() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
            
            if (_voiceState.value == VoiceState.SPEAKING) {
                _voiceState.value = VoiceState.IDLE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping voice note playback", e)
        }
    }

    // Utility Methods
    fun isSpeechRecognitionAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)
    
    fun isTTSAvailable(): Boolean = ttsInitialized
    
    fun getCurrentLanguage(): TTSLanguage = currentLanguage
    
    fun getAvailableLanguages(): List<TTSLanguage> {
        return TTSLanguage.values().filter { language ->
            textToSpeech?.isLanguageAvailable(language.locale) == TextToSpeech.LANG_AVAILABLE
        }
    }

    fun setVoiceSettings(speechRate: Float = 1.0f, pitch: Float = 1.0f, language: TTSLanguage = currentLanguage) {
        textToSpeech?.apply {
            setSpeechRate(speechRate.coerceIn(0.1f, 3.0f))
            setPitch(pitch.coerceIn(0.1f, 2.0f))
        }
        
        if (language != currentLanguage) {
            setTTSLanguage(language)
        }
    }

    fun cleanup() {
        stopListening()
        stopSpeaking()
        stopVoiceNote()
        voiceActivityJob?.cancel()
        
        speechRecognizer?.destroy()
        textToSpeech?.shutdown()
        
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing MediaRecorder", e)
        }
        
        speechRecognizer = null
        textToSpeech = null
        mediaRecorder = null
        mediaPlayer = null
        
        ttsInitialized = false
        isInitialized = false
        _voiceState.value = VoiceState.IDLE
    }
}