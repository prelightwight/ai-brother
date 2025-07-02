package com.prelightwight.aibrother.knowledge

import android.content.Context
import android.util.Log
import com.prelightwight.aibrother.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*

/**
 * Knowledge Graph for connecting and organizing memories, conversations, and concepts
 */
class KnowledgeGraph(
    private val database: MemoryDatabase,
    private val context: Context
) {
    private val tag = "KnowledgeGraph"
    private val json = Json { ignoreUnknownKeys = true }
    
    @Serializable
    data class KnowledgeNode(
        val id: String,
        val type: NodeType,
        val title: String,
        val content: String,
        val tags: List<String> = emptyList(),
        val connections: MutableList<String> = mutableListOf(),
        val weight: Float = 1.0f,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    @Serializable
    enum class NodeType {
        MEMORY, CONVERSATION, CONCEPT, ENTITY, TOPIC
    }
    
    @Serializable
    data class KnowledgeConnection(
        val fromNodeId: String,
        val toNodeId: String,
        val connectionType: ConnectionType,
        val strength: Float = 1.0f,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    @Serializable
    enum class ConnectionType {
        RELATES_TO, FOLLOWS_FROM, SIMILAR_TO, PART_OF, MENTIONS, TRIGGERS
    }
    
    @Serializable
    data class ConceptCluster(
        val name: String,
        val nodes: List<String>,
        val centralNode: String,
        val coherenceScore: Float
    )
    
    /**
     * Build knowledge graph from existing memories and conversations
     */
    suspend fun buildKnowledgeGraph(): KnowledgeGraphResult = withContext(Dispatchers.IO) {
        try {
            Log.d(tag, "Building knowledge graph...")
            
            val nodes = mutableListOf<KnowledgeNode>()
            val connections = mutableListOf<KnowledgeConnection>()
            
            // Add memory nodes
            val memories = database.memoryDao().getAllMemories()
            memories.forEach { memory ->
                val node = KnowledgeNode(
                    id = "memory_${memory.id}",
                    type = NodeType.MEMORY,
                    title = memory.title,
                    content = memory.content,
                    tags = memory.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    timestamp = memory.timestamp
                )
                nodes.add(node)
            }
            
            // Add conversation nodes (sample recent conversations)
            val conversationIds = database.conversationDao().getAllConversationIds()
            conversationIds.take(50).forEach { convId -> // Limit to recent conversations
                try {
                    val conversations = database.conversationDao().getConversationById(convId)
                    // Note: In real implementation, collect the flow properly
                    val node = KnowledgeNode(
                        id = "conversation_$convId",
                        type = NodeType.CONVERSATION,
                        title = "Conversation ${convId.take(8)}",
                        content = "Conversation thread with AI assistant",
                        tags = listOf("conversation", "chat"),
                        timestamp = System.currentTimeMillis()
                    )
                    nodes.add(node)
                } catch (e: Exception) {
                    Log.w(tag, "Failed to process conversation $convId", e)
                }
            }
            
            // Extract concepts and entities
            val concepts = extractConcepts(nodes)
            nodes.addAll(concepts)
            
            // Build connections
            connections.addAll(buildConnections(nodes))
            
            // Create concept clusters
            val clusters = createConceptClusters(nodes, connections)
            
            Log.d(tag, "Knowledge graph built: ${nodes.size} nodes, ${connections.size} connections, ${clusters.size} clusters")
            
            KnowledgeGraphResult(
                nodes = nodes,
                connections = connections,
                clusters = clusters,
                buildTime = System.currentTimeMillis()
            )
            
        } catch (e: Exception) {
            Log.e(tag, "Error building knowledge graph", e)
            KnowledgeGraphResult(emptyList(), emptyList(), emptyList(), System.currentTimeMillis())
        }
    }
    
    /**
     * Extract concepts and entities from content
     */
    private fun extractConcepts(nodes: List<KnowledgeNode>): List<KnowledgeNode> {
        val conceptFrequency = mutableMapOf<String, Int>()
        val conceptContent = mutableMapOf<String, MutableSet<String>>()
        
        // Analyze all content for concept extraction
        nodes.forEach { node ->
            val words = extractImportantWords("${node.title} ${node.content}")
            words.forEach { word ->
                conceptFrequency[word] = conceptFrequency.getOrDefault(word, 0) + 1
                conceptContent.getOrPut(word) { mutableSetOf() }.add(node.content.take(100))
            }
            
            // Extract from tags
            node.tags.forEach { tag ->
                conceptFrequency[tag] = conceptFrequency.getOrDefault(tag, 0) + 2 // Tags are more important
                conceptContent.getOrPut(tag) { mutableSetOf() }.add("Tag: $tag")
            }
        }
        
        // Create concept nodes for frequent terms
        return conceptFrequency.filter { it.value >= 2 } // Appear at least twice
            .map { (concept, frequency) ->
                KnowledgeNode(
                    id = "concept_${concept.lowercase().replace(" ", "_")}",
                    type = NodeType.CONCEPT,
                    title = concept,
                    content = "Concept mentioned ${frequency} times. Related content: ${conceptContent[concept]?.take(3)?.joinToString("; ")}",
                    tags = listOf("concept", "extracted"),
                    weight = frequency.toFloat() / nodes.size // Normalize weight
                )
            }
    }
    
    /**
     * Extract important words from text
     */
    private fun extractImportantWords(text: String): List<String> {
        val stopWords = setOf("the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "is", "are", "was", "were", "be", "been", "have", "has", "had", "do", "does", "did", "will", "would", "could", "should", "can", "may", "might", "what", "where", "when", "why", "how", "who", "which", "this", "that", "these", "those", "i", "you", "he", "she", "it", "we", "they", "me", "him", "her", "us", "them")
        
        // Extract multi-word phrases and single words
        val words = mutableListOf<String>()
        
        // Single words
        text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 3 && !stopWords.contains(it) }
            .forEach { words.add(it) }
        
        // Simple two-word phrases
        val textWords = text.split(Regex("\\s+"))
        for (i in 0 until textWords.size - 1) {
            val phrase = "${textWords[i]} ${textWords[i + 1]}".lowercase()
                .replace(Regex("[^a-z0-9\\s]"), "")
            if (phrase.split(" ").all { it.length > 2 && !stopWords.contains(it) }) {
                words.add(phrase)
            }
        }
        
        return words.distinct()
    }
    
    /**
     * Build connections between nodes
     */
    private fun buildConnections(nodes: List<KnowledgeNode>): List<KnowledgeConnection> {
        val connections = mutableListOf<KnowledgeConnection>()
        
        // Connect nodes with similar content
        for (i in nodes.indices) {
            for (j in i + 1 until nodes.size) {
                val node1 = nodes[i]
                val node2 = nodes[j]
                
                val similarity = calculateSimilarity(node1, node2)
                if (similarity > 0.3f) {
                    val connectionType = determineConnectionType(node1, node2, similarity)
                    connections.add(
                        KnowledgeConnection(
                            fromNodeId = node1.id,
                            toNodeId = node2.id,
                            connectionType = connectionType,
                            strength = similarity
                        )
                    )
                }
            }
        }
        
        // Connect concepts to memories/conversations that mention them
        val conceptNodes = nodes.filter { it.type == NodeType.CONCEPT }
        val contentNodes = nodes.filter { it.type in listOf(NodeType.MEMORY, NodeType.CONVERSATION) }
        
        conceptNodes.forEach { concept ->
            contentNodes.forEach { content ->
                val mentionsScore = calculateMentionsScore(concept.title, content.content + " " + content.title)
                if (mentionsScore > 0.5f) {
                    connections.add(
                        KnowledgeConnection(
                            fromNodeId = content.id,
                            toNodeId = concept.id,
                            connectionType = ConnectionType.MENTIONS,
                            strength = mentionsScore
                        )
                    )
                }
            }
        }
        
        return connections
    }
    
    /**
     * Calculate similarity between two nodes
     */
    private fun calculateSimilarity(node1: KnowledgeNode, node2: KnowledgeNode): Float {
        // Tag similarity
        val commonTags = node1.tags.intersect(node2.tags.toSet()).size
        val totalTags = (node1.tags + node2.tags).distinct().size
        val tagSimilarity = if (totalTags > 0) commonTags.toFloat() / totalTags else 0f
        
        // Content similarity (simplified)
        val words1 = extractImportantWords(node1.content).toSet()
        val words2 = extractImportantWords(node2.content).toSet()
        val commonWords = words1.intersect(words2).size
        val totalWords = words1.union(words2).size
        val contentSimilarity = if (totalWords > 0) commonWords.toFloat() / totalWords else 0f
        
        // Time proximity for temporal connections
        val timeDiff = kotlin.math.abs(node1.timestamp - node2.timestamp)
        val maxTime = 30L * 24 * 60 * 60 * 1000 // 30 days
        val timeSimilarity = if (timeDiff < maxTime) 1f - (timeDiff.toFloat() / maxTime) else 0f
        
        // Weighted combination
        return (tagSimilarity * 0.4f + contentSimilarity * 0.5f + timeSimilarity * 0.1f)
    }
    
    /**
     * Determine connection type based on nodes and similarity
     */
    private fun determineConnectionType(node1: KnowledgeNode, node2: KnowledgeNode, similarity: Float): ConnectionType {
        return when {
            node1.type == NodeType.CONCEPT || node2.type == NodeType.CONCEPT -> ConnectionType.MENTIONS
            similarity > 0.8f -> ConnectionType.SIMILAR_TO
            kotlin.math.abs(node1.timestamp - node2.timestamp) < 24 * 60 * 60 * 1000 -> ConnectionType.FOLLOWS_FROM
            node1.tags.intersect(node2.tags.toSet()).isNotEmpty() -> ConnectionType.RELATES_TO
            else -> ConnectionType.RELATES_TO
        }
    }
    
    /**
     * Calculate how much a concept is mentioned in content
     */
    private fun calculateMentionsScore(concept: String, content: String): Float {
        val conceptWords = concept.lowercase().split(" ")
        val contentLower = content.lowercase()
        
        var mentions = 0
        conceptWords.forEach { word ->
            if (contentLower.contains(word)) {
                mentions++
            }
        }
        
        return mentions.toFloat() / conceptWords.size
    }
    
    /**
     * Create concept clusters from connected nodes
     */
    private fun createConceptClusters(
        nodes: List<KnowledgeNode>,
        connections: List<KnowledgeConnection>
    ): List<ConceptCluster> {
        val clusters = mutableListOf<ConceptCluster>()
        val processedNodes = mutableSetOf<String>()
        
        // Build adjacency map
        val adjacencyMap = mutableMapOf<String, MutableList<String>>()
        connections.forEach { conn ->
            adjacencyMap.getOrPut(conn.fromNodeId) { mutableListOf() }.add(conn.toNodeId)
            adjacencyMap.getOrPut(conn.toNodeId) { mutableListOf() }.add(conn.fromNodeId)
        }
        
        // Find clusters using simple connected components
        nodes.forEach { node ->
            if (!processedNodes.contains(node.id)) {
                val cluster = findConnectedComponent(node.id, adjacencyMap, processedNodes)
                if (cluster.size >= 2) { // Minimum cluster size
                    val centralNode = findCentralNode(cluster, connections)
                    val coherence = calculateClusterCoherence(cluster, connections)
                    
                    clusters.add(
                        ConceptCluster(
                            name = generateClusterName(cluster, nodes),
                            nodes = cluster,
                            centralNode = centralNode,
                            coherenceScore = coherence
                        )
                    )
                }
            }
        }
        
        return clusters.sortedByDescending { it.coherenceScore }
    }
    
    /**
     * Find connected component using DFS
     */
    private fun findConnectedComponent(
        startNode: String,
        adjacencyMap: Map<String, List<String>>,
        processedNodes: MutableSet<String>
    ): List<String> {
        val component = mutableListOf<String>()
        val stack = mutableListOf(startNode)
        
        while (stack.isNotEmpty()) {
            val node = stack.removeAt(stack.size - 1)
            if (!processedNodes.contains(node)) {
                processedNodes.add(node)
                component.add(node)
                adjacencyMap[node]?.forEach { neighbor ->
                    if (!processedNodes.contains(neighbor)) {
                        stack.add(neighbor)
                    }
                }
            }
        }
        
        return component
    }
    
    /**
     * Find central node in cluster (most connected)
     */
    private fun findCentralNode(cluster: List<String>, connections: List<KnowledgeConnection>): String {
        val nodeDegrees = mutableMapOf<String, Int>()
        cluster.forEach { nodeDegrees[it] = 0 }
        
        connections.forEach { conn ->
            if (cluster.contains(conn.fromNodeId) && cluster.contains(conn.toNodeId)) {
                nodeDegrees[conn.fromNodeId] = nodeDegrees.getOrDefault(conn.fromNodeId, 0) + 1
                nodeDegrees[conn.toNodeId] = nodeDegrees.getOrDefault(conn.toNodeId, 0) + 1
            }
        }
        
        return nodeDegrees.maxByOrNull { it.value }?.key ?: cluster.first()
    }
    
    /**
     * Calculate cluster coherence score
     */
    private fun calculateClusterCoherence(cluster: List<String>, connections: List<KnowledgeConnection>): Float {
        if (cluster.size < 2) return 0f
        
        val internalConnections = connections.count { conn ->
            cluster.contains(conn.fromNodeId) && cluster.contains(conn.toNodeId)
        }
        
        val maxPossibleConnections = cluster.size * (cluster.size - 1) / 2
        return if (maxPossibleConnections > 0) internalConnections.toFloat() / maxPossibleConnections else 0f
    }
    
    /**
     * Generate cluster name from nodes
     */
    private fun generateClusterName(cluster: List<String>, nodes: List<KnowledgeNode>): String {
        val clusterNodes = nodes.filter { cluster.contains(it.id) }
        val conceptNodes = clusterNodes.filter { it.type == NodeType.CONCEPT }
        
        return when {
            conceptNodes.isNotEmpty() -> conceptNodes.first().title
            clusterNodes.isNotEmpty() -> "Cluster: ${clusterNodes.first().title.take(20)}"
            else -> "Unknown Cluster"
        }
    }
    
    /**
     * Search knowledge graph
     */
    suspend fun searchGraph(query: String): GraphSearchResult = withContext(Dispatchers.IO) {
        try {
            val graph = buildKnowledgeGraph()
            val queryTerms = extractImportantWords(query)
            
            val relevantNodes = graph.nodes.filter { node ->
                val nodeWords = extractImportantWords("${node.title} ${node.content}")
                queryTerms.any { term -> nodeWords.contains(term) }
            }.sortedByDescending { node ->
                val nodeWords = extractImportantWords("${node.title} ${node.content}")
                queryTerms.count { term -> nodeWords.contains(term) }.toFloat()
            }
            
            val relevantConnections = graph.connections.filter { conn ->
                relevantNodes.any { it.id == conn.fromNodeId || it.id == conn.toNodeId }
            }
            
            GraphSearchResult(
                query = query,
                nodes = relevantNodes.take(20),
                connections = relevantConnections,
                totalMatches = relevantNodes.size
            )
            
        } catch (e: Exception) {
            Log.e(tag, "Error searching graph", e)
            GraphSearchResult(query, emptyList(), emptyList(), 0)
        }
    }
    
    data class KnowledgeGraphResult(
        val nodes: List<KnowledgeNode>,
        val connections: List<KnowledgeConnection>,
        val clusters: List<ConceptCluster>,
        val buildTime: Long
    )
    
    data class GraphSearchResult(
        val query: String,
        val nodes: List<KnowledgeNode>,
        val connections: List<KnowledgeConnection>,
        val totalMatches: Int
    )
}