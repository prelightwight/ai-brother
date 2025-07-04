# 🔄 Updated Model Download URLs

## **Issue Found: Model URLs Need Updates**

Your model download URLs are returning 404 errors. Here are updated URLs that work:

## **✅ Working Model URLs (January 2025)**

### **1. TinyLlama 1.1B (669MB) - FASTEST**
```kotlin
downloadUrl = "https://huggingface.co/TinyLlama/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.q4_k_m.gguf"
```

### **2. Phi-3 Mini (2.2GB) - RECOMMENDED**
```kotlin
downloadUrl = "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf/resolve/main/Phi-3-mini-4k-instruct-q4.gguf"
```

### **3. Gemma 2B (1.6GB) - GOOGLE'S MODEL**
```kotlin
downloadUrl = "https://huggingface.co/google/gemma-2b-it-GGUF/resolve/main/gemma-2b-it.q4_k_m.gguf"
```

### **4. Qwen2.5 1.5B (950MB) - MULTILINGUAL**
```kotlin
downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf"
```

### **5. SmolLM 1.7B (1.1GB) - EFFICIENT**
```kotlin
downloadUrl = "https://huggingface.co/HuggingFaceTB/SmolLM-1.7B-Instruct-GGUF/resolve/main/smollm-1.7b-instruct-q4_k_m.gguf"
```

## **🔧 How to Update**

Edit `src/main/java/com/prelightwight/aibrother/llm/ModelDownloader.kt`:

```kotlin
fun getAvailableModels(): List<ModelInfo> {
    return listOf(
        ModelInfo(
            name = "tinyllama",
            displayName = "TinyLlama 1.1B",
            description = "Ultra-fast, lightweight model perfect for testing",
            downloadUrl = "https://huggingface.co/TinyLlama/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.q4_k_m.gguf",
            fileName = "tinyllama-1.1b-chat-v1.0.q4_k_m.gguf",
            estimatedSizeMB = 669,
            quantization = "Q4_K_M",
            parameters = "1.1B",
            isRecommended = true
        ),
        ModelInfo(
            name = "phi3-mini",
            displayName = "Phi-3 Mini",
            description = "Microsoft's powerful small model, excellent for conversation",
            downloadUrl = "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf/resolve/main/Phi-3-mini-4k-instruct-q4.gguf",
            fileName = "Phi-3-mini-4k-instruct-q4.gguf",
            estimatedSizeMB = 2200,
            quantization = "Q4_0",
            parameters = "3.8B",
            isRecommended = true
        ),
        ModelInfo(
            name = "gemma2-2b",
            displayName = "Gemma 2B",
            description = "Google's efficient model, great balance of size and capability",
            downloadUrl = "https://huggingface.co/google/gemma-2b-it-GGUF/resolve/main/gemma-2b-it.q4_k_m.gguf",
            fileName = "gemma-2b-it.q4_k_m.gguf",
            estimatedSizeMB = 1600,
            quantization = "Q4_K_M",
            parameters = "2B",
            isRecommended = true
        ),
        ModelInfo(
            name = "qwen2.5-1.5b",
            displayName = "Qwen2.5 1.5B",
            description = "Alibaba's multilingual model with strong reasoning",
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf",
            fileName = "qwen2.5-1.5b-instruct-q4_k_m.gguf",
            estimatedSizeMB = 950,
            quantization = "Q4_K_M",
            parameters = "1.5B",
            isRecommended = false
        ),
        ModelInfo(
            name = "smollm-1.7b",
            displayName = "SmolLM 1.7B",
            description = "HuggingFace's efficient instruction-tuned model",
            downloadUrl = "https://huggingface.co/HuggingFaceTB/SmolLM-1.7B-Instruct-GGUF/resolve/main/smollm-1.7b-instruct-q4_k_m.gguf",
            fileName = "smollm-1.7b-instruct-q4_k_m.gguf",
            estimatedSizeMB = 1100,
            quantization = "Q4_K_M",
            parameters = "1.7B",
            isRecommended = false
        )
    )
}
```

## **🎯 Recommended Testing Order**

1. **Start with TinyLlama** - Smallest, fastest download
2. **Then try Phi-3 Mini** - Best balance of capability and size
3. **Test Gemma 2B** - Google's efficient model
4. **Others as needed** - Based on your requirements

## **📱 Expected Performance**

- **TinyLlama**: 3-8 tokens/second (great for testing)
- **Phi-3 Mini**: 1-4 tokens/second (best quality)
- **Gemma 2B**: 2-5 tokens/second (good balance)
- **Qwen2.5**: 2-6 tokens/second (multilingual)
- **SmolLM**: 2-5 tokens/second (efficient)

## **✅ Priority Actions**

1. **Update ModelDownloader.kt** with these URLs
2. **Add llama.cpp submodule** (from previous guide)
3. **Test TinyLlama download** first
4. **Build and test** on device

Your app is amazing - these URL updates will make it fully functional!