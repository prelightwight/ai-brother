package com.prelightwight.aibrother.models;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000@\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0010\t\n\u0000\u001aL\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0012\u0010\u0005\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b0\u00062\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0012\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00010\u000bH\u0003\u001a,\u0010\f\u001a\u00020\u00012\u0006\u0010\r\u001a\u00020\u00042\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\u000f2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00010\u000fH\u0003\u001a>\u0010\u0011\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0012\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00010\u000b2\u0012\u0010\u0013\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00010\u000bH\u0003\u001a0\u0010\u0014\u001a\u00020\u00012\u0006\u0010\r\u001a\u00020\u00042\u0006\u0010\u0015\u001a\u00020\u00162\b\u0010\u0005\u001a\u0004\u0018\u00010\b2\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00010\u000fH\u0003\u001a.\u0010\u0018\u001a\u00020\u00012\u0014\b\u0002\u0010\u0019\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00010\u000b2\u000e\b\u0002\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00010\u000fH\u0007\u001a\u0010\u0010\u001b\u001a\u00020\u00072\u0006\u0010\u001c\u001a\u00020\u001dH\u0002\u00a8\u0006\u001e"}, d2 = {"AvailableModelsTab", "", "models", "", "Lcom/prelightwight/aibrother/models/ModelInfo;", "downloadProgress", "", "", "Lcom/prelightwight/aibrother/models/DownloadProgress;", "downloadedModels", "onDownload", "Lkotlin/Function1;", "DownloadedModelCard", "model", "onSelect", "Lkotlin/Function0;", "onDelete", "DownloadedModelsTab", "onSelectModel", "onDeleteModel", "ModelCard", "isDownloaded", "", "onAction", "ModelManagementScreen", "onModelSelected", "onNavigateBack", "formatBytes", "bytes", "", "app_debug"})
public final class ModelManagementScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void ModelManagementScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onModelSelected, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void AvailableModelsTab(java.util.List<com.prelightwight.aibrother.models.ModelInfo> models, java.util.Map<java.lang.String, com.prelightwight.aibrother.models.DownloadProgress> downloadProgress, java.util.List<com.prelightwight.aibrother.models.ModelInfo> downloadedModels, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onDownload) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void DownloadedModelsTab(java.util.List<com.prelightwight.aibrother.models.ModelInfo> models, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onSelectModel, kotlin.jvm.functions.Function1<? super com.prelightwight.aibrother.models.ModelInfo, kotlin.Unit> onDeleteModel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void ModelCard(com.prelightwight.aibrother.models.ModelInfo model, boolean isDownloaded, com.prelightwight.aibrother.models.DownloadProgress downloadProgress, kotlin.jvm.functions.Function0<kotlin.Unit> onAction) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void DownloadedModelCard(com.prelightwight.aibrother.models.ModelInfo model, kotlin.jvm.functions.Function0<kotlin.Unit> onSelect, kotlin.jvm.functions.Function0<kotlin.Unit> onDelete) {
    }
    
    private static final java.lang.String formatBytes(long bytes) {
        return null;
    }
}