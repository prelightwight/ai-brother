package com.prelightwight.aibrother.models;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010$\n\u0002\b\u0003\u0018\u0000 \"2\u00020\u0001:\u0001\"B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\r\u001a\u00020\u000eJ\u0016\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u0013J\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00160\u00152\u0006\u0010\u0011\u001a\u00020\u0012J\u0010\u0010\u0017\u001a\u00020\u00122\u0006\u0010\u0018\u001a\u00020\u0019H\u0002J\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u001c0\u001bJ\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001c0\u001bJ\u0010\u0010\u001e\u001a\u0004\u0018\u00010\b2\u0006\u0010\u0011\u001a\u00020\u0012J\u0012\u0010\u001f\u001a\u000e\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\u00010 J\u000e\u0010!\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0007\u001a\u00020\b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000b\u0010\f\u001a\u0004\b\t\u0010\n\u00a8\u0006#"}, d2 = {"Lcom/prelightwight/aibrother/models/ModelDownloader;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "client", "Lokhttp3/OkHttpClient;", "modelsDir", "Ljava/io/File;", "getModelsDir", "()Ljava/io/File;", "modelsDir$delegate", "Lkotlin/Lazy;", "cleanup", "", "deleteModel", "", "modelId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "downloadModel", "Lkotlinx/coroutines/flow/Flow;", "Lcom/prelightwight/aibrother/models/DownloadProgress;", "formatBytes", "bytes", "", "getAvailableModels", "", "Lcom/prelightwight/aibrother/models/ModelInfo;", "getDownloadedModels", "getModelFile", "getModelStorageInfo", "", "isModelDownloaded", "Companion", "app_debug"})
public final class ModelDownloader {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "ModelDownloader";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String MODELS_DIR = "models";
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<com.prelightwight.aibrother.models.ModelInfo> AVAILABLE_MODELS = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy modelsDir$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient client = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.prelightwight.aibrother.models.ModelDownloader.Companion Companion = null;
    
    public ModelDownloader(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    private final java.io.File getModelsDir() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.prelightwight.aibrother.models.ModelInfo> getAvailableModels() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.prelightwight.aibrother.models.ModelInfo> getDownloadedModels() {
        return null;
    }
    
    public final boolean isModelDownloaded(@org.jetbrains.annotations.NotNull()
    java.lang.String modelId) {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.io.File getModelFile(@org.jetbrains.annotations.NotNull()
    java.lang.String modelId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.prelightwight.aibrother.models.DownloadProgress> downloadModel(@org.jetbrains.annotations.NotNull()
    java.lang.String modelId) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteModel(@org.jetbrains.annotations.NotNull()
    java.lang.String modelId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, java.lang.Object> getModelStorageInfo() {
        return null;
    }
    
    private final java.lang.String formatBytes(long bytes) {
        return null;
    }
    
    public final void cleanup() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u000e\u0010\b\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/prelightwight/aibrother/models/ModelDownloader$Companion;", "", "()V", "AVAILABLE_MODELS", "", "Lcom/prelightwight/aibrother/models/ModelInfo;", "getAVAILABLE_MODELS", "()Ljava/util/List;", "MODELS_DIR", "", "TAG", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.prelightwight.aibrother.models.ModelInfo> getAVAILABLE_MODELS() {
            return null;
        }
    }
}