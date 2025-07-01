#include <jni.h>
#include <string>

// Simple test native method. Replace with llama.cpp integration later.
extern "C"
JNIEXPORT jstring JNICALL
Java_com_prelightwight_aibrother_llm_LlamaInterface_runModel(
        JNIEnv* env,
        jobject /* this */,
        jstring prompt
) {
    const char* input = env->GetStringUTFChars(prompt, 0);
    std::string response = "Pretend AI response to: ";
    response += input;
    env->ReleaseStringUTFChars(prompt, input);
    return env->NewStringUTF(response.c_str());
}
