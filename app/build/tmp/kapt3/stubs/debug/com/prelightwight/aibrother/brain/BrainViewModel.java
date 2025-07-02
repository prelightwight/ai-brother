package com.prelightwight.aibrother.brain;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016J\u0006\u0010\u0017\u001a\u00020\u0014J\u0006\u0010\u0018\u001a\u00020\u0014J\u000e\u0010\u0019\u001a\u00020\u00142\u0006\u0010\u001a\u001a\u00020\nJ\b\u0010\u001b\u001a\u00020\u0014H\u0002J\u000e\u0010\u001c\u001a\u00020\u00142\u0006\u0010\u001d\u001a\u00020\u0016R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00070\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u000eR\u001d\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000eR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u000e\u00a8\u0006\u001e"}, d2 = {"Lcom/prelightwight/aibrother/brain/BrainViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/prelightwight/aibrother/brain/BrainRepository;", "(Lcom/prelightwight/aibrother/brain/BrainRepository;)V", "_isLoading", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_memories", "", "Lcom/prelightwight/aibrother/data/MemoryEntity;", "_searchResults", "isLoading", "Lkotlinx/coroutines/flow/StateFlow;", "()Lkotlinx/coroutines/flow/StateFlow;", "memories", "getMemories", "searchResults", "getSearchResults", "addMemory", "", "content", "", "clearSearchResults", "deleteAllMemories", "deleteMemory", "memory", "loadMemories", "searchMemories", "query", "app_debug"})
public final class BrainViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.prelightwight.aibrother.brain.BrainRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.prelightwight.aibrother.data.MemoryEntity>> _memories = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.prelightwight.aibrother.data.MemoryEntity>> memories = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isLoading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.prelightwight.aibrother.data.MemoryEntity>> _searchResults = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.prelightwight.aibrother.data.MemoryEntity>> searchResults = null;
    
    public BrainViewModel(@org.jetbrains.annotations.NotNull()
    com.prelightwight.aibrother.brain.BrainRepository repository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.prelightwight.aibrother.data.MemoryEntity>> getMemories() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.prelightwight.aibrother.data.MemoryEntity>> getSearchResults() {
        return null;
    }
    
    private final void loadMemories() {
    }
    
    public final void addMemory(@org.jetbrains.annotations.NotNull()
    java.lang.String content) {
    }
    
    public final void deleteMemory(@org.jetbrains.annotations.NotNull()
    com.prelightwight.aibrother.data.MemoryEntity memory) {
    }
    
    public final void deleteAllMemories() {
    }
    
    public final void searchMemories(@org.jetbrains.annotations.NotNull()
    java.lang.String query) {
    }
    
    public final void clearSearchResults() {
    }
}