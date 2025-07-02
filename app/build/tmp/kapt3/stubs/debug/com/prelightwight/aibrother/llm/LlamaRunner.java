package com.prelightwight.aibrother.llm;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000l\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\f\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0013\u001a\u00020\u0014J\u0010\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0004H\u0002J\u0010\u0010\u0018\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0004H\u0002J\u001a\u0010\u0019\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u001a\u001a\u00020\u00062\u0006\u0010\u001b\u001a\u00020\u0012H\u0002J\b\u0010\u001c\u001a\u0004\u0018\u00010\u001dJ\u001a\u0010\u001e\u001a\u0004\u0018\u00010\u001f2\u0006\u0010\u001a\u001a\u00020\u00062\u0006\u0010\u001b\u001a\u00020\u0012H\u0002J\u0012\u0010 \u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00010!J \u0010\"\u001a\u00020\u00042\u0006\u0010#\u001a\u00020\u00042\b\b\u0002\u0010$\u001a\u00020%H\u0086@\u00a2\u0006\u0002\u0010&J&\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u00040(2\u0006\u0010#\u001a\u00020\u00042\b\b\u0002\u0010$\u001a\u00020%H\u0086@\u00a2\u0006\u0002\u0010&J\u000e\u0010)\u001a\u00020\u00142\u0006\u0010\u001a\u001a\u00020\u0006J\u0006\u0010*\u001a\u00020\u000bJ\u0010\u0010+\u001a\u00020\u000b2\u0006\u0010,\u001a\u00020\u001fH\u0002J\u000e\u0010-\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010.J\u0016\u0010/\u001a\u00020\u00042\u0006\u00100\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u00101J\u000e\u00102\u001a\u00020\u00142\u0006\u0010\u001b\u001a\u00020\u0012J\u0006\u00103\u001a\u00020\u0014R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u00064"}, d2 = {"Lcom/prelightwight/aibrother/llm/LlamaRunner;", "", "()V", "TAG", "", "appContext", "Landroid/content/Context;", "averageTokensPerSecond", "", "currentModelPath", "isModelLoading", "", "lastInferenceTime", "", "loadingProgress", "modelDownloader", "Lcom/prelightwight/aibrother/models/ModelDownloader;", "modelUri", "Landroid/net/Uri;", "cleanup", "", "extractContextSize", "", "info", "extractVocabSize", "getFileNameFromUri", "context", "uri", "getModelInfo", "Lcom/prelightwight/aibrother/llm/ModelInfo;", "getOrCopyModelToCache", "Ljava/io/File;", "getPerformanceStats", "", "infer", "prompt", "config", "Lcom/prelightwight/aibrother/llm/InferenceConfig;", "(Ljava/lang/String;Lcom/prelightwight/aibrother/llm/InferenceConfig;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "inferStream", "Lkotlinx/coroutines/flow/Flow;", "init", "isModelLoaded", "isValidModelFile", "file", "loadModel", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "loadModelById", "modelId", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setModelUri", "unloadModel", "app_debug"})
public final class LlamaRunner {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "LlamaRunner";
    @org.jetbrains.annotations.Nullable()
    private static android.net.Uri modelUri;
    @org.jetbrains.annotations.Nullable()
    private static android.content.Context appContext;
    @org.jetbrains.annotations.Nullable()
    private static java.lang.String currentModelPath;
    private static boolean isModelLoading = false;
    private static float loadingProgress = 0.0F;
    private static long lastInferenceTime = 0L;
    private static float averageTokensPerSecond = 0.0F;
    @org.jetbrains.annotations.Nullable()
    private static com.prelightwight.aibrother.models.ModelDownloader modelDownloader;
    @org.jetbrains.annotations.NotNull()
    public static final com.prelightwight.aibrother.llm.LlamaRunner INSTANCE = null;
    
    private LlamaRunner() {
        super();
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void setModelUri(@org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object loadModelById(@org.jetbrains.annotations.NotNull()
    java.lang.String modelId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object loadModel(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object infer(@org.jetbrains.annotations.NotNull()
    java.lang.String prompt, @org.jetbrains.annotations.NotNull()
    com.prelightwight.aibrother.llm.InferenceConfig config, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object inferStream(@org.jetbrains.annotations.NotNull()
    java.lang.String prompt, @org.jetbrains.annotations.NotNull()
    com.prelightwight.aibrother.llm.InferenceConfig config, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlinx.coroutines.flow.Flow<java.lang.String>> $completion) {
        return null;
    }
    
    public final boolean isModelLoaded() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.prelightwight.aibrother.llm.ModelInfo getModelInfo() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, java.lang.Object> getPerformanceStats() {
        return null;
    }
    
    public final void unloadModel() {
    }
    
    private final boolean isValidModelFile(java.io.File file) {
        return false;
    }
    
    private final java.io.File getOrCopyModelToCache(android.content.Context context, android.net.Uri uri) {
        return null;
    }
    
    private final java.lang.String getFileNameFromUri(android.content.Context context, android.net.Uri uri) {
        return null;
    }
    
    private final int extractContextSize(java.lang.String info) {
        return 0;
    }
    
    private final int extractVocabSize(java.lang.String info) {
        return 0;
    }
    
    public final void cleanup() {
    }
}