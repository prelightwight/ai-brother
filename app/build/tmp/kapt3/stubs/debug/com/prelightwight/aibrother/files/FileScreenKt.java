package com.prelightwight.aibrother.files;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000B\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0003\u001aJ\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00052\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\b2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00010\bH\u0007\u001a&\u0010\u000b\u001a\u00020\u00012\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00010\bH\u0007\u001a\b\u0010\u0011\u001a\u00020\u0001H\u0007\u001a\"\u0010\u0012\u001a\u00020\u000f2\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00162\b\u0010\u0017\u001a\u0004\u0018\u00010\u000fH\u0002\u001a\u0010\u0010\u0018\u001a\u00020\u000f2\u0006\u0010\u0019\u001a\u00020\u001aH\u0002\u001a\u001a\u0010\u001b\u001a\u0004\u0018\u00010\u00032\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0002\u001a\u001a\u0010\u001c\u001a\u0004\u0018\u00010\u000f2\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0002\u00a8\u0006\u001d"}, d2 = {"DocumentCard", "", "document", "Lcom/prelightwight/aibrother/files/DocumentFile;", "isExtracting", "", "hasExtracted", "onExtract", "Lkotlin/Function0;", "onRemove", "onView", "ExtractedContentCard", "content", "Lcom/prelightwight/aibrother/files/ExtractedContent;", "searchQuery", "", "onClick", "FileScreen", "extractTextContent", "context", "Landroid/content/Context;", "uri", "Landroid/net/Uri;", "mimeType", "formatFileSize", "bytes", "", "getDocumentInfo", "getFileNameFromUri", "app_debug"})
public final class FileScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void FileScreen() {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void DocumentCard(@org.jetbrains.annotations.NotNull()
    com.prelightwight.aibrother.files.DocumentFile document, boolean isExtracting, boolean hasExtracted, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onExtract, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onRemove, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onView) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ExtractedContentCard(@org.jetbrains.annotations.NotNull()
    com.prelightwight.aibrother.files.ExtractedContent content, @org.jetbrains.annotations.NotNull()
    java.lang.String searchQuery, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    private static final com.prelightwight.aibrother.files.DocumentFile getDocumentInfo(android.content.Context context, android.net.Uri uri) {
        return null;
    }
    
    private static final java.lang.String extractTextContent(android.content.Context context, android.net.Uri uri, java.lang.String mimeType) {
        return null;
    }
    
    private static final java.lang.String getFileNameFromUri(android.content.Context context, android.net.Uri uri) {
        return null;
    }
    
    private static final java.lang.String formatFileSize(long bytes) {
        return null;
    }
}