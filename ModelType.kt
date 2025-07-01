package com.prelightwight.aibrother.core

enum class ModelType(val displayName: String) {
    GPT_4("GPT-4"),
    GPT_3_5("GPT-3.5"),
    CLAUDE("Claude"),
    LOCAL_GGUF("Local GGUF (llama.cpp)"),
    LOCAL_KOBOLDCPP("Local KoboldCPP");

    override fun toString(): String {
        return displayName
    }
}


