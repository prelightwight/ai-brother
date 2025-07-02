package com.prelightwight.aibrother.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\bg\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u0014\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u000b0\nH\'J\u0016\u0010\f\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u001c\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00070\u000b2\u0006\u0010\u000e\u001a\u00020\u000fH\u00a7@\u00a2\u0006\u0002\u0010\u0010\u00a8\u0006\u0011"}, d2 = {"Lcom/prelightwight/aibrother/data/MemoryDao;", "", "deleteAllMemories", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteMemory", "memory", "Lcom/prelightwight/aibrother/data/MemoryEntity;", "(Lcom/prelightwight/aibrother/data/MemoryEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllMemories", "Lkotlinx/coroutines/flow/Flow;", "", "insertMemory", "searchMemories", "query", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@androidx.room.Dao()
public abstract interface MemoryDao {
    
    @androidx.room.Query(value = "SELECT * FROM memories WHERE type = \'memory\' ORDER BY timestamp DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.prelightwight.aibrother.data.MemoryEntity>> getAllMemories();
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertMemory(@org.jetbrains.annotations.NotNull()
    com.prelightwight.aibrother.data.MemoryEntity memory, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteMemory(@org.jetbrains.annotations.NotNull()
    com.prelightwight.aibrother.data.MemoryEntity memory, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM memories WHERE type = \'memory\'")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAllMemories(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM memories WHERE content LIKE \'%\' || :query || \'%\' AND type = \'memory\' ORDER BY timestamp DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object searchMemories(@org.jetbrains.annotations.NotNull()
    java.lang.String query, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.prelightwight.aibrother.data.MemoryEntity>> $completion);
}