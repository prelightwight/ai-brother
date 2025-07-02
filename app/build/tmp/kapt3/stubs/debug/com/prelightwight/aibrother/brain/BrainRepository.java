package com.prelightwight.aibrother.brain;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\t\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u0007\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\tJ\u000e\u0010\n\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\tJ\u0016\u0010\u000b\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\rH\u0086@\u00a2\u0006\u0002\u0010\u000eJ\u0016\u0010\u000f\u001a\u00020\b2\u0006\u0010\u0010\u001a\u00020\u0011H\u0086@\u00a2\u0006\u0002\u0010\u0012J\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\r0\u0014H\u0086@\u00a2\u0006\u0002\u0010\tJ\u0012\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00170\u00140\u0016J\u0012\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u00140\u0016J\u001a\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00170\u00140\u00162\u0006\u0010\f\u001a\u00020\rJ\u000e\u0010\u001a\u001a\u00020\u001bH\u0086@\u00a2\u0006\u0002\u0010\tJ\u0016\u0010\u001c\u001a\u00020\b2\u0006\u0010\u001d\u001a\u00020\rH\u0086@\u00a2\u0006\u0002\u0010\u000eJ0\u0010\u001e\u001a\u00020\b2\u0006\u0010\u001d\u001a\u00020\r2\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010\f\u001a\u00020\r2\b\b\u0002\u0010!\u001a\u00020 H\u0086@\u00a2\u0006\u0002\u0010\"J\u001c\u0010#\u001a\u00020\b2\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\u00170\u0014H\u0086@\u00a2\u0006\u0002\u0010%J\u001c\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00170\u00142\u0006\u0010\'\u001a\u00020\rH\u0086@\u00a2\u0006\u0002\u0010\u000eJ\u001c\u0010(\u001a\b\u0012\u0004\u0012\u00020\u00110\u00142\u0006\u0010\'\u001a\u00020\rH\u0086@\u00a2\u0006\u0002\u0010\u000eR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006)"}, d2 = {"Lcom/prelightwight/aibrother/brain/BrainRepository;", "", "memoryDao", "Lcom/prelightwight/aibrother/data/MemoryDao;", "conversationDao", "Lcom/prelightwight/aibrother/data/ConversationDao;", "(Lcom/prelightwight/aibrother/data/MemoryDao;Lcom/prelightwight/aibrother/data/ConversationDao;)V", "deleteAllConversations", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAllMemories", "deleteConversation", "conversationId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteMemory", "memory", "Lcom/prelightwight/aibrother/data/MemoryEntity;", "(Lcom/prelightwight/aibrother/data/MemoryEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllConversationIds", "", "getAllConversations", "Lkotlinx/coroutines/flow/Flow;", "Lcom/prelightwight/aibrother/data/ConversationEntity;", "getAllMemories", "getConversationById", "getConversationCount", "", "insertMemory", "content", "insertMessage", "isUser", "", "isError", "(Ljava/lang/String;ZLjava/lang/String;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertMessages", "messages", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "searchConversations", "query", "searchMemories", "app_debug"})
public final class BrainRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.prelightwight.aibrother.data.MemoryDao memoryDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.prelightwight.aibrother.data.ConversationDao conversationDao = null;
    
    public BrainRepository(@org.jetbrains.annotations.NotNull()
    com.prelightwight.aibrother.data.MemoryDao memoryDao, @org.jetbrains.annotations.NotNull()
    com.prelightwight.aibrother.data.ConversationDao conversationDao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.prelightwight.aibrother.data.MemoryEntity>> getAllMemories() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertMemory(@org.jetbrains.annotations.NotNull()
    java.lang.String content, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteMemory(@org.jetbrains.annotations.NotNull()
    com.prelightwight.aibrother.data.MemoryEntity memory, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteAllMemories(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object searchMemories(@org.jetbrains.annotations.NotNull()
    java.lang.String query, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.prelightwight.aibrother.data.MemoryEntity>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.prelightwight.aibrother.data.ConversationEntity>> getAllConversations() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.prelightwight.aibrother.data.ConversationEntity>> getConversationById(@org.jetbrains.annotations.NotNull()
    java.lang.String conversationId) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getAllConversationIds(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.String>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String content, boolean isUser, @org.jetbrains.annotations.NotNull()
    java.lang.String conversationId, boolean isError, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertMessages(@org.jetbrains.annotations.NotNull()
    java.util.List<com.prelightwight.aibrother.data.ConversationEntity> messages, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteConversation(@org.jetbrains.annotations.NotNull()
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteAllConversations(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getConversationCount(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object searchConversations(@org.jetbrains.annotations.NotNull()
    java.lang.String query, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.prelightwight.aibrother.data.ConversationEntity>> $completion) {
        return null;
    }
}