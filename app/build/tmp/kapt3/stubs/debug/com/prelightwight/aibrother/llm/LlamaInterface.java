package com.prelightwight.aibrother.llm;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\t\u0010\u0003\u001a\u00020\u0004H\u0086 J\t\u0010\u0005\u001a\u00020\u0006H\u0086 J\u0011\u0010\u0007\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\u0004H\u0086 J\u0019\u0010\t\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u0004H\u0086 J\t\u0010\u000b\u001a\u00020\fH\u0086 \u00a8\u0006\r"}, d2 = {"Lcom/prelightwight/aibrother/llm/LlamaInterface;", "", "()V", "getModelInfo", "", "isModelLoaded", "", "loadModel", "modelPath", "runModel", "prompt", "unloadModel", "", "app_debug"})
public final class LlamaInterface {
    @org.jetbrains.annotations.NotNull()
    public static final com.prelightwight.aibrother.llm.LlamaInterface INSTANCE = null;
    
    private LlamaInterface() {
        super();
    }
    
    /**
     * Loads a model from the specified path.
     * Must be called before running inference.
     *
     * @param modelPath Absolute path to the .gguf model file
     * @return true if model loaded successfully, false otherwise
     */
    public final native boolean loadModel(@org.jetbrains.annotations.NotNull()
    java.lang.String modelPath) {
        return false;
    }
    
    /**
     * Runs inference using the loaded model.
     * Model must be loaded first using loadModel().
     *
     * @param modelPath Absolute path to the .gguf model file (for verification)
     * @param prompt User's input prompt to the model
     * @return Model-generated response as String
     */
    @org.jetbrains.annotations.NotNull()
    public final native java.lang.String runModel(@org.jetbrains.annotations.NotNull()
    java.lang.String modelPath, @org.jetbrains.annotations.NotNull()
    java.lang.String prompt) {
        return null;
    }
    
    /**
     * Unloads the current model and frees memory.
     * Should be called when switching models or closing the app.
     */
    public final native void unloadModel() {
    }
    
    /**
     * Checks if a model is currently loaded.
     *
     * @return true if a model is loaded and ready for inference
     */
    public final native boolean isModelLoaded() {
        return false;
    }
    
    /**
     * Gets information about the currently loaded model.
     *
     * @return Model information string, or error message if no model loaded
     */
    @org.jetbrains.annotations.NotNull()
    public final native java.lang.String getModelInfo() {
        return null;
    }
}