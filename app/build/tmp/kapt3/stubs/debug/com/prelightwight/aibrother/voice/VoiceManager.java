package com.prelightwight.aibrother.voice;

/**
 * Manages voice input (speech-to-text) and output (text-to-speech) functionality
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\"\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\f\u0018\u00002\u00020\u0001:\u000223B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u001d\u001a\u00020\u001eJ\u0006\u0010\u001f\u001a\u00020\u001eJ\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\"0!J\u0006\u0010#\u001a\u00020\u0015J\b\u0010$\u001a\u00020\u001eH\u0002J\b\u0010%\u001a\u00020\u001eH\u0002J\u0006\u0010\f\u001a\u00020\u0007J\u0006\u0010&\u001a\u00020\u0007J\u0010\u0010\'\u001a\u0004\u0018\u00010(2\u0006\u0010)\u001a\u00020\nJ\u0010\u0010*\u001a\u00020\u001e2\u0006\u0010)\u001a\u00020\nH\u0002J\u0018\u0010+\u001a\u00020\u001e2\u0006\u0010)\u001a\u00020\n2\b\b\u0002\u0010,\u001a\u00020\u0007J\u0006\u0010-\u001a\u00020\u001eJ\u0006\u0010.\u001a\u00020\u001eJ\u0006\u0010/\u001a\u00020\u001eJ\u000e\u00100\u001a\u00020\u001e2\u0006\u00101\u001a\u00020\u0015R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\n0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\n0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00070\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000fR\u0017\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00070\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000fR\u000e\u0010\u0011\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\n0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u000fR\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0016\u001a\u0004\u0018\u00010\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\nX\u0082D\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0019\u001a\u0004\u0018\u00010\u001aX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u001b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\n0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u000f\u00a8\u00064"}, d2 = {"Lcom/prelightwight/aibrother/voice/VoiceManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "_isListening", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_isSpeaking", "_recognizedText", "", "_voiceError", "isListening", "isListeningFlow", "Lkotlinx/coroutines/flow/StateFlow;", "()Lkotlinx/coroutines/flow/StateFlow;", "isSpeakingFlow", "isTtsInitialized", "recognizedTextFlow", "getRecognizedTextFlow", "settings", "Lcom/prelightwight/aibrother/voice/VoiceManager$VoiceSettings;", "speechRecognizer", "Landroid/speech/SpeechRecognizer;", "tag", "textToSpeech", "Landroid/speech/tts/TextToSpeech;", "voiceErrorFlow", "getVoiceErrorFlow", "cleanup", "", "clearError", "getAvailableLanguages", "", "Ljava/util/Locale;", "getSettings", "initializeSpeechRecognizer", "initializeTextToSpeech", "isSpeaking", "parseVoiceCommand", "Lcom/prelightwight/aibrother/voice/VoiceManager$VoiceCommand;", "text", "processVoiceCommand", "speak", "interrupt", "startListening", "stopListening", "stopSpeaking", "updateSettings", "newSettings", "VoiceCommand", "VoiceSettings", "app_debug"})
public final class VoiceManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String tag = "VoiceManager";
    @org.jetbrains.annotations.Nullable()
    private android.speech.tts.TextToSpeech textToSpeech;
    private boolean isTtsInitialized = false;
    @org.jetbrains.annotations.Nullable()
    private android.speech.SpeechRecognizer speechRecognizer;
    private boolean isListening = false;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isListening = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isListeningFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isSpeaking = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isSpeakingFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _recognizedText = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> recognizedTextFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _voiceError = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> voiceErrorFlow = null;
    @org.jetbrains.annotations.NotNull()
    private com.prelightwight.aibrother.voice.VoiceManager.VoiceSettings settings;
    
    public VoiceManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isListeningFlow() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isSpeakingFlow() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getRecognizedTextFlow() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getVoiceErrorFlow() {
        return null;
    }
    
    /**
     * Initialize Text-to-Speech
     */
    private final void initializeTextToSpeech() {
    }
    
    /**
     * Initialize Speech Recognizer
     */
    private final void initializeSpeechRecognizer() {
    }
    
    /**
     * Start listening for speech input
     */
    public final void startListening() {
    }
    
    /**
     * Stop listening for speech input
     */
    public final void stopListening() {
    }
    
    /**
     * Speak text using text-to-speech
     */
    public final void speak(@org.jetbrains.annotations.NotNull()
    java.lang.String text, boolean interrupt) {
    }
    
    /**
     * Stop speaking
     */
    public final void stopSpeaking() {
    }
    
    /**
     * Process voice commands
     */
    private final void processVoiceCommand(java.lang.String text) {
    }
    
    /**
     * Update voice settings
     */
    public final void updateSettings(@org.jetbrains.annotations.NotNull()
    com.prelightwight.aibrother.voice.VoiceManager.VoiceSettings newSettings) {
    }
    
    /**
     * Get available TTS languages
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.Set<java.util.Locale> getAvailableLanguages() {
        return null;
    }
    
    /**
     * Check if TTS is speaking
     */
    public final boolean isSpeaking() {
        return false;
    }
    
    /**
     * Check if speech recognizer is listening
     */
    public final boolean isListening() {
        return false;
    }
    
    /**
     * Get current voice settings
     */
    @org.jetbrains.annotations.NotNull()
    public final com.prelightwight.aibrother.voice.VoiceManager.VoiceSettings getSettings() {
        return null;
    }
    
    /**
     * Clear voice error
     */
    public final void clearError() {
    }
    
    /**
     * Cleanup resources
     */
    public final void cleanup() {
    }
    
    /**
     * Parse voice command from text
     */
    @org.jetbrains.annotations.Nullable()
    public final com.prelightwight.aibrother.voice.VoiceManager.VoiceCommand parseVoiceCommand(@org.jetbrains.annotations.NotNull()
    java.lang.String text) {
        return null;
    }
    
    /**
     * Voice command types for external handling
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\n\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\n\u00a8\u0006\u000b"}, d2 = {"Lcom/prelightwight/aibrother/voice/VoiceManager$VoiceCommand;", "", "(Ljava/lang/String;I)V", "START_LISTENING", "STOP_LISTENING", "REPEAT", "CLEAR_SCREEN", "NEW_CONVERSATION", "SAVE_MEMORY", "SEARCH", "HELP", "app_debug"})
    public static enum VoiceCommand {
        /*public static final*/ START_LISTENING /* = new START_LISTENING() */,
        /*public static final*/ STOP_LISTENING /* = new STOP_LISTENING() */,
        /*public static final*/ REPEAT /* = new REPEAT() */,
        /*public static final*/ CLEAR_SCREEN /* = new CLEAR_SCREEN() */,
        /*public static final*/ NEW_CONVERSATION /* = new NEW_CONVERSATION() */,
        /*public static final*/ SAVE_MEMORY /* = new SAVE_MEMORY() */,
        /*public static final*/ SEARCH /* = new SEARCH() */,
        /*public static final*/ HELP /* = new HELP() */;
        
        VoiceCommand() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.prelightwight.aibrother.voice.VoiceManager.VoiceCommand> getEntries() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0013\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B7\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\b\u00a2\u0006\u0002\u0010\nJ\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\bH\u00c6\u0003J\t\u0010\u0017\u001a\u00020\bH\u00c6\u0003J;\u0010\u0018\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\bH\u00c6\u0001J\u0013\u0010\u0019\u001a\u00020\b2\b\u0010\u001a\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001b\u001a\u00020\u001cH\u00d6\u0001J\t\u0010\u001d\u001a\u00020\u001eH\u00d6\u0001R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0010R\u0011\u0010\t\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\f\u00a8\u0006\u001f"}, d2 = {"Lcom/prelightwight/aibrother/voice/VoiceManager$VoiceSettings;", "", "speechRate", "", "pitch", "language", "Ljava/util/Locale;", "autoListen", "", "voiceCommands", "(FFLjava/util/Locale;ZZ)V", "getAutoListen", "()Z", "getLanguage", "()Ljava/util/Locale;", "getPitch", "()F", "getSpeechRate", "getVoiceCommands", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "other", "hashCode", "", "toString", "", "app_debug"})
    public static final class VoiceSettings {
        private final float speechRate = 0.0F;
        private final float pitch = 0.0F;
        @org.jetbrains.annotations.NotNull()
        private final java.util.Locale language = null;
        private final boolean autoListen = false;
        private final boolean voiceCommands = false;
        
        public VoiceSettings(float speechRate, float pitch, @org.jetbrains.annotations.NotNull()
        java.util.Locale language, boolean autoListen, boolean voiceCommands) {
            super();
        }
        
        public final float getSpeechRate() {
            return 0.0F;
        }
        
        public final float getPitch() {
            return 0.0F;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.Locale getLanguage() {
            return null;
        }
        
        public final boolean getAutoListen() {
            return false;
        }
        
        public final boolean getVoiceCommands() {
            return false;
        }
        
        public VoiceSettings() {
            super();
        }
        
        public final float component1() {
            return 0.0F;
        }
        
        public final float component2() {
            return 0.0F;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.Locale component3() {
            return null;
        }
        
        public final boolean component4() {
            return false;
        }
        
        public final boolean component5() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.prelightwight.aibrother.voice.VoiceManager.VoiceSettings copy(float speechRate, float pitch, @org.jetbrains.annotations.NotNull()
        java.util.Locale language, boolean autoListen, boolean voiceCommands) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}