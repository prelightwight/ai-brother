package com.prelightwight.aibrother.llm

object LlamaInterface {

    init {
        // Loads the native llama library (libllama.so)
        System.loadLibrary("llama")
    }

    /**
     * Loads a model from the specified path.
     * Must be called before running inference.
     *
     * @param modelPath Absolute path to the .gguf model file
     * @return true if model loaded successfully, false otherwise
     */
    external fun loadModel(modelPath: String): Boolean

    /**
     * Runs inference using the loaded model.
     * Model must be loaded first using loadModel().
     *
     * @param modelPath Absolute path to the .gguf model file (for verification)
     * @param prompt User's input prompt to the model
     * @return Model-generated response as String
     */
    external fun runModel(modelPath: String, prompt: String): String

    /**
     * Unloads the current model and frees memory.
     * Should be called when switching models or closing the app.
     */
    external fun unloadModel()

    /**
     * Checks if a model is currently loaded.
     *
     * @return true if a model is loaded and ready for inference
     */
    external fun isModelLoaded(): Boolean

    /**
     * Gets information about the currently loaded model.
     *
     * @return Model information string, or error message if no model loaded
     */
    external fun getModelInfo(): String
}

