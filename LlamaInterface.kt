package com.prelightwight.aibrother.llm

object LlamaInterface {

    init {
        // Loads the native llama library (libllama.so)
        System.loadLibrary("llama")
    }

    /**
     * Runs inference using a loaded model.
     * In the current stub, this only returns a simulated response.
     * Later, the modelPath will be passed through JNI to native code.
     *
     * @param modelPath Absolute path to the .gguf model file
     * @param prompt User's input prompt to the model
     * @return Model-generated response as String
     */
    external fun runModel(modelPath: String, prompt: String): String
}

