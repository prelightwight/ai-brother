package com.prelightwight.aibrother.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0007\bg\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u0016\u0010\t\u001a\u00020\u00032\u0006\u0010\n\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\fJ\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00070\u000eH\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0014\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\u000e0\u0010H\'J\u001c\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\u000e0\u00102\u0006\u0010\u0006\u001a\u00020\u0007H\'J\u000e\u0010\u0012\u001a\u00020\u0013H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0014\u001a\u00020\u00032\u0006\u0010\n\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\fJ\u001c\u0010\u0015\u001a\u00020\u00032\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u000b0\u000eH\u00a7@\u00a2\u0006\u0002\u0010\u0017J\u001c\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u000b0\u000e2\u0006\u0010\u0019\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\b\u00a8\u0006\u001a"}, d2 = {"Lcom/prelightwight/aibrother/data/ConversationDao;", "", "deleteAllConversations", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteConversation", "conversationId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteMessage", "message", "Lcom/prelightwight/aibrother/data/ConversationEntity;", "(Lcom/prelightwight/aibrother/data/ConversationEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllConversationIds", "", "getAllConversations", "Lkotlinx/coroutines/flow/Flow;", "getConversationById", "getConversationCount", "", "insertMessage", "insertMessages", "messages", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "searchConversations", "query", "app_debug"})
@androidx.room.Dao()
public abstract interface ConversationDao {
    
    @androidx.room.Query(value = "SELECT * FROM conversations ORDER BY timestamp ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.prelightwight.aibrother.data.ConversationEntity>> getAllConversations();
    
    @androidx.room.Query(value = "SELECT * FROM conversations WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.prelightwight.aibrother.data.ConversationEntity>> getConversationById(@org.jetbrains.annotations.NotNull()
    java.lang.String conversationId);
    
    @androidx.room.Query(value = "SELECT DISTINCT conversationId FROM conversations ORDER BY timestamp DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAllConversationIds(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.String>> $completion);
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertMessage(@org.jetbrains.annotations.NotNull()
    com.prelightwight.aibrother.data.ConversationEntity message, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertMessages(@org.jetbrains.annotations.NotNull()
    java.util.List<com.prelightwight.aibrother.data.ConversationEntity> messages, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteMessage(@org.jetbrains.annotations.NotNull()
    com.prelightwight.aibrother.data.ConversationEntity message, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM conversations WHERE conversationId = :conversationId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteConversation(@org.jetbrains.annotations.NotNull()
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM conversations")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAllConversations(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM conversations")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getConversationCount(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM conversations WHERE content LIKE \'%\' || :query || \'%\' ORDER BY timestamp DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object searchConversations(@org.jetbrains.annotations.NotNull()
    java.lang.String query, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.prelightwight.aibrother.data.ConversationEntity>> $completion);
}