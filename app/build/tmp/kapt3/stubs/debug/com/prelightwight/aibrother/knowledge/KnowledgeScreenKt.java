package com.prelightwight.aibrother.knowledge;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\u001a0\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\b\u0010\u0004\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0006\u001a\u00020\u00072\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00010\tH\u0007\u001a(\u0010\n\u001a\u00020\u00012\u0006\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u000b\u001a\u00020\f2\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00010\tH\u0007\u001a\b\u0010\r\u001a\u00020\u0001H\u0007\u001a\u000e\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00030\u000fH\u0002\u00a8\u0006\u0010"}, d2 = {"ArticleCard", "", "article", "Lcom/prelightwight/aibrother/knowledge/KnowledgeArticle;", "category", "Lcom/prelightwight/aibrother/knowledge/KnowledgeCategory;", "searchQuery", "", "onClick", "Lkotlin/Function0;", "CategoryCard", "modifier", "Landroidx/compose/ui/Modifier;", "KnowledgeScreen", "generateSampleArticles", "", "app_debug"})
public final class KnowledgeScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void KnowledgeScreen() {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void CategoryCard(@org.jetbrains.annotations.NotNull()
    com.prelightwight.aibrother.knowledge.KnowledgeCategory category, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ArticleCard(@org.jetbrains.annotations.NotNull()
    com.prelightwight.aibrother.knowledge.KnowledgeArticle article, @org.jetbrains.annotations.Nullable()
    com.prelightwight.aibrother.knowledge.KnowledgeCategory category, @org.jetbrains.annotations.NotNull()
    java.lang.String searchQuery, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    private static final java.util.List<com.prelightwight.aibrother.knowledge.KnowledgeArticle> generateSampleArticles() {
        return null;
    }
}