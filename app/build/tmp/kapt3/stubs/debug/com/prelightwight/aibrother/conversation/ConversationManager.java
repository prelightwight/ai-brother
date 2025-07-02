package com.prelightwight.aibrother.conversation;

/**
 * Manages conversation persistence and RAG-enhanced chat
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001:\u0002)*B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u000f\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010\u0011J\u001a\u0010\u0012\u001a\u00020\u000e2\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u000eH\u0086@\u00a2\u0006\u0002\u0010\u0014J\u0016\u0010\u0015\u001a\u00020\u00102\u0006\u0010\u0016\u001a\u00020\u000eH\u0086@\u00a2\u0006\u0002\u0010\u0014J\u0016\u0010\u0017\u001a\u00020\u000e2\u0006\u0010\u0016\u001a\u00020\u000eH\u0086@\u00a2\u0006\u0002\u0010\u0014J\u0016\u0010\u0018\u001a\u00020\u000e2\u0006\u0010\u0016\u001a\u00020\u000eH\u0086@\u00a2\u0006\u0002\u0010\u0014J\u001a\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001c0\u001b0\u001a2\u0006\u0010\u0016\u001a\u00020\u000eJ\u000e\u0010\u001d\u001a\u00020\u001eH\u0086@\u00a2\u0006\u0002\u0010\u0011J\u0014\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020 0\u001bH\u0086@\u00a2\u0006\u0002\u0010\u0011J\u001c\u0010!\u001a\b\u0012\u0004\u0012\u00020\u001c0\u001b2\u0006\u0010\"\u001a\u00020\u000eH\u0086@\u00a2\u0006\u0002\u0010\u0014J2\u0010#\u001a\u00020\u000e2\u0006\u0010\u0016\u001a\u00020\u000e2\u0006\u0010$\u001a\u00020\u000e2\b\b\u0002\u0010%\u001a\u00020&2\b\b\u0002\u0010\'\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010(R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0007\u001a\u00020\b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000b\u0010\f\u001a\u0004\b\t\u0010\nR\u000e\u0010\r\u001a\u00020\u000eX\u0082D\u00a2\u0006\u0002\n\u0000\u00a8\u0006+"}, d2 = {"Lcom/prelightwight/aibrother/conversation/ConversationManager;", "", "database", "Lcom/prelightwight/aibrother/data/MemoryDatabase;", "context", "Landroid/content/Context;", "(Lcom/prelightwight/aibrother/data/MemoryDatabase;Landroid/content/Context;)V", "ragIntegration", "Lcom/prelightwight/aibrother/rag/RAGIntegration;", "getRagIntegration", "()Lcom/prelightwight/aibrother/rag/RAGIntegration;", "ragIntegration$delegate", "Lkotlin/Lazy;", "tag", "", "clearAllConversations", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createConversation", "title", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteConversation", "conversationId", "exportConversation", "generateConversationTitle", "getConversationHistory", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/prelightwight/aibrother/data/ConversationEntity;", "getConversationStats", "Lcom/prelightwight/aibrother/conversation/ConversationManager$ConversationStats;", "getConversationSummaries", "Lcom/prelightwight/aibrother/conversation/ConversationManager$ConversationSummary;", "searchConversations", "query", "sendMessage", "message", "config", "Lcom/prelightwight/aibrother/llm/InferenceConfig;", "enableRAG", "(Ljava/lang/String;Ljava/lang/String;Lcom/prelightwight/aibrother/llm/InferenceConfig;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "ConversationStats", "ConversationSummary", "app_debug"})
public final class ConversationManager {
    @org.jetbrains.annotations.NotNull()
    private final com.prelightwight.aibrother.data.MemoryDatabase database = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String tag = "ConversationManager";
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy ragIntegration$delegate = null;
    
    public ConversationManager(@org.jetbrains.annotations.NotNull()
    com.prelightwight.aibrother.data.MemoryDatabase database, @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    private final com.prelightwight.aibrother.rag.RAGIntegration getRagIntegration() {
        return null;
    }
    
    /**
     * Send a message with RAG enhancement and conversation persistence
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object sendMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull()
    java.lang.String message, @org.jetbrains.annotations.NotNull()
    com.prelightwight.aibrother.llm.InferenceConfig config, boolean enableRAG, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    /**
     * Create a new conversation
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object createConversation(@org.jetbrains.annotations.Nullable()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    /**
     * Get all conversation summaries
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getConversationSummaries(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.prelightwight.aibrother.conversation.ConversationManager.ConversationSummary>> $completion) {
        return null;
    }
    
    /**
     * Get full conversation history
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.prelightwight.aibrother.data.ConversationEntity>> getConversationHistory(@org.jetbrains.annotations.NotNull()
    java.lang.String conversationId) {
        return null;
    }
    
    /**
     * Delete a conversation
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteConversation(@org.jetbrains.annotations.NotNull()
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Clear all conversations
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object clearAllConversations(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Search across all conversations
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object searchConversations(@org.jetbrains.annotations.NotNull()
    java.lang.String query, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.prelightwight.aibrother.data.ConversationEntity>> $completion) {
        return null;
    }
    
    /**
     * Get conversation statistics
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getConversationStats(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.prelightwight.aibrother.conversation.ConversationManager.ConversationStats> $completion) {
        return null;
    }
    
    /**
     * Export conversation to text
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object exportConversation(@org.jetbrains.annotations.NotNull()
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    /**
     * Generate conversation title from content
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object generateConversationTitle(@org.jetbrains.annotations.NotNull()
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007J\t\u0010\r\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000f\u001a\u00020\u0006H\u00c6\u0003J\'\u0010\u0010\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0006H\u00c6\u0001J\u0013\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0014\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\u0015\u001a\u00020\u0016H\u00d6\u0001R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u000b\u00a8\u0006\u0017"}, d2 = {"Lcom/prelightwight/aibrother/conversation/ConversationManager$ConversationStats;", "", "totalConversations", "", "totalMessages", "averageMessagesPerConversation", "", "(IIF)V", "getAverageMessagesPerConversation", "()F", "getTotalConversations", "()I", "getTotalMessages", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "toString", "", "app_debug"})
    public static final class ConversationStats {
        private final int totalConversations = 0;
        private final int totalMessages = 0;
        private final float averageMessagesPerConversation = 0.0F;
        
        public ConversationStats(int totalConversations, int totalMessages, float averageMessagesPerConversation) {
            super();
        }
        
        public final int getTotalConversations() {
            return 0;
        }
        
        public final int getTotalMessages() {
            return 0;
        }
        
        public final float getAverageMessagesPerConversation() {
            return 0.0F;
        }
        
        public final int component1() {
            return 0;
        }
        
        public final int component2() {
            return 0;
        }
        
        public final float component3() {
            return 0.0F;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.prelightwight.aibrother.conversation.ConversationManager.ConversationStats copy(int totalConversations, int totalMessages, float averageMessagesPerConversation) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0002\b\u0010\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B-\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\tH\u00c6\u0003J;\u0010\u0018\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\tH\u00c6\u0001J\u0013\u0010\u0019\u001a\u00020\u001a2\b\u0010\u001b\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001c\u001a\u00020\tH\u00d6\u0001J\t\u0010\u001d\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\fR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\f\u00a8\u0006\u001e"}, d2 = {"Lcom/prelightwight/aibrother/conversation/ConversationManager$ConversationSummary;", "", "id", "", "title", "lastMessage", "lastMessageTime", "", "messageCount", "", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JI)V", "getId", "()Ljava/lang/String;", "getLastMessage", "getLastMessageTime", "()J", "getMessageCount", "()I", "getTitle", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "", "other", "hashCode", "toString", "app_debug"})
    public static final class ConversationSummary {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String id = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String title = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String lastMessage = null;
        private final long lastMessageTime = 0L;
        private final int messageCount = 0;
        
        public ConversationSummary(@org.jetbrains.annotations.NotNull()
        java.lang.String id, @org.jetbrains.annotations.NotNull()
        java.lang.String title, @org.jetbrains.annotations.NotNull()
        java.lang.String lastMessage, long lastMessageTime, int messageCount) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getId() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getTitle() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getLastMessage() {
            return null;
        }
        
        public final long getLastMessageTime() {
            return 0L;
        }
        
        public final int getMessageCount() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component3() {
            return null;
        }
        
        public final long component4() {
            return 0L;
        }
        
        public final int component5() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.prelightwight.aibrother.conversation.ConversationManager.ConversationSummary copy(@org.jetbrains.annotations.NotNull()
        java.lang.String id, @org.jetbrains.annotations.NotNull()
        java.lang.String title, @org.jetbrains.annotations.NotNull()
        java.lang.String lastMessage, long lastMessageTime, int messageCount) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}