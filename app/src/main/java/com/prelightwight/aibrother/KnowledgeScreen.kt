package com.prelightwight.aibrother.knowledge

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

data class KnowledgeCategory(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val articleCount: Int
)

data class KnowledgeArticle(
    val id: String,
    val title: String,
    val content: String,
    val categoryId: String,
    val summary: String,
    val tags: List<String>,
    val lastUpdated: Long = System.currentTimeMillis(),
    val readCount: Int = 0,
    val isFavorite: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedArticle by remember { mutableStateOf<KnowledgeArticle?>(null) }
    var showArticleDetail by remember { mutableStateOf(false) }
    var showAddContent by remember { mutableStateOf(false) }
    var expandedCategories by remember { mutableStateOf(setOf<String>()) }
    
    // Sample knowledge base data
    val categories = remember {
        listOf(
            KnowledgeCategory("science", "Science", "🔬", "Scientific facts and discoveries", 45),
            KnowledgeCategory("history", "History", "📜", "Historical events and figures", 32),
            KnowledgeCategory("technology", "Technology", "💻", "Tech concepts and tutorials", 67),
            KnowledgeCategory("literature", "Literature", "📚", "Books, poems, and literary works", 28),
            KnowledgeCategory("geography", "Geography", "🌍", "World facts and locations", 41),
            KnowledgeCategory("math", "Mathematics", "🔢", "Mathematical concepts and formulas", 53),
            KnowledgeCategory("art", "Art & Culture", "🎨", "Art history and cultural information", 29),
            KnowledgeCategory("biology", "Biology", "🧬", "Life sciences and biology", 38)
        )
    }
    
    val articles = remember {
        generateSampleArticles()
    }
    
    val filteredArticles = remember(searchQuery, selectedCategory) {
        articles.filter { article ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                article.title.contains(searchQuery, ignoreCase = true) ||
                article.content.contains(searchQuery, ignoreCase = true) ||
                article.tags.any { it.contains(searchQuery, ignoreCase = true) }
            }
            val matchesCategory = selectedCategory?.let { article.categoryId == it } ?: true
            matchesSearch && matchesCategory
        }
    }
    
    val totalArticles = articles.size
    val totalWords = articles.sumOf { it.content.split("\\s+".toRegex()).size }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📚 Knowledge Base",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            FilledTonalButton(
                onClick = { showAddContent = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Knowledge")
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Search knowledge base...") },
            leadingIcon = { 
                Icon(Icons.Default.Search, contentDescription = "Search") 
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Statistics Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$totalArticles",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Articles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${categories.size}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${totalWords / 1000}K",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "Words",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        // Category Filter
        if (selectedCategory != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val category = categories.find { it.id == selectedCategory }
                if (category != null) {
                    FilterChip(
                        onClick = { selectedCategory = null },
                        label = { Text("${category.icon} ${category.name}") },
                        selected = true,
                        trailingIcon = {
                            Icon(Icons.Default.Clear, contentDescription = "Remove filter")
                        }
                    )
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (searchQuery.isBlank() && selectedCategory == null) {
                // Show categories when not searching
                item {
                    Text(
                        text = "📂 Categories",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(categories.chunked(2)) { categoryPair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryPair.forEach { category ->
                            CategoryCard(
                                category = category,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedCategory = category.id }
                            )
                        }
                        // Fill remaining space if odd number of categories
                        if (categoryPair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "📄 Recent Articles",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            
            // Articles
            if (filteredArticles.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No articles found" else "No articles in this category",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (searchQuery.isNotBlank()) {
                                Text(
                                    text = "Try adjusting your search terms",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredArticles) { article ->
                    ArticleCard(
                        article = article,
                        category = categories.find { it.id == article.categoryId },
                        searchQuery = searchQuery,
                        onClick = { 
                            selectedArticle = article
                            showArticleDetail = true
                        }
                    )
                }
            }
        }
    }
    
    // Article Detail Dialog
    if (showArticleDetail && selectedArticle != null) {
        AlertDialog(
            onDismissRequest = { showArticleDetail = false },
            title = { 
                Text(
                    text = selectedArticle!!.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                ) 
            },
            text = {
                LazyColumn(
                    modifier = Modifier.height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        val category = categories.find { it.id == selectedArticle!!.categoryId }
                        if (category != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${category.icon} ${category.name}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                        .format(Date(selectedArticle!!.lastUpdated)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (selectedArticle!!.tags.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                selectedArticle!!.tags.take(3).forEach { tag ->
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                    
                    item {
                        Text(
                            text = selectedArticle!!.content,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showArticleDetail = false }) {
                    Text("Close")
                }
            }
        )
    }
    
    // Add Content Dialog
    if (showAddContent) {
        var newTitle by remember { mutableStateOf("") }
        var newContent by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf(categories.first().id) }
        
        AlertDialog(
            onDismissRequest = { showAddContent = false },
            title = { Text("Add Knowledge Article") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = newContent,
                        onValueChange = { newContent = it },
                        label = { Text("Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        maxLines = 8
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        // In a real app, you would save this to the database
                        showAddContent = false
                        newTitle = ""
                        newContent = ""
                    },
                    enabled = newTitle.isNotBlank() && newContent.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContent = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CategoryCard(
    category: KnowledgeCategory,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.icon,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${category.articleCount} articles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = category.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ArticleCard(
    article: KnowledgeArticle,
    category: KnowledgeCategory?,
    searchQuery: String,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (category != null) {
                            Text(
                                text = "${category.icon} ${category.name}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " • ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = dateFormat.format(Date(article.lastUpdated)),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (article.readCount > 0) {
                            Text(
                                text = " • ${article.readCount} reads",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = article.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (article.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            article.tags.take(3).forEach { tag ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun generateSampleArticles(): List<KnowledgeArticle> {
    return listOf(
        KnowledgeArticle(
            id = "1",
            title = "Introduction to Quantum Physics",
            content = "Quantum physics is a fundamental theory in physics that provides a description of the physical properties of nature at the scale of atoms and subatomic particles. It is the foundation of all quantum physics including quantum chemistry, quantum field theory, quantum technology, and quantum information science.\n\nThe key concepts include wave-particle duality, uncertainty principle, and quantum entanglement. These phenomena only become apparent at the atomic and subatomic level, where the classical laws of physics break down.",
            categoryId = "science",
            summary = "A comprehensive introduction to the fundamental principles of quantum physics and its key concepts.",
            tags = listOf("quantum", "physics", "atoms", "particles", "science"),
            readCount = 127
        ),
        KnowledgeArticle(
            id = "2",
            title = "The Renaissance Period",
            content = "The Renaissance was a period in European history marking the transition from the Middle Ages to modernity and covering the 15th and 16th centuries. It was characterized by an effort to revive and surpass ideas and achievements of classical antiquity.\n\nKey figures included Leonardo da Vinci, Michelangelo, and Galileo Galilei. The period saw advances in art, architecture, politics, science, and literature.",
            categoryId = "history",
            summary = "An overview of the Renaissance period and its significant contributions to art, science, and culture.",
            tags = listOf("renaissance", "history", "art", "science", "europe"),
            readCount = 89
        ),
        KnowledgeArticle(
            id = "3",
            title = "Machine Learning Fundamentals",
            content = "Machine Learning is a subset of artificial intelligence (AI) that provides systems the ability to automatically learn and improve from experience without being explicitly programmed. It focuses on the development of computer programs that can access data and use it to learn for themselves.\n\nThe process of learning begins with observations or data, such as examples, direct experience, or instruction, in order to look for patterns in data and make better decisions in the future based on the examples that we provide.",
            categoryId = "technology",
            summary = "Understanding the basics of machine learning and how systems learn from data.",
            tags = listOf("machine learning", "AI", "data", "algorithms", "technology"),
            readCount = 234
        ),
        KnowledgeArticle(
            id = "4",
            title = "Shakespeare's Influence on Literature",
            content = "William Shakespeare is widely regarded as the greatest writer in the English language and the world's greatest dramatist. His works have been translated into every major living language and are performed more often than those of any other playwright.\n\nShakespeare's influence extends from theatre and literature to present-day movies, Western philosophy, and the English language itself. His innovations in characterization, plot development, and genre continue to influence writers today.",
            categoryId = "literature",
            summary = "Exploring Shakespeare's lasting impact on literature and the English language.",
            tags = listOf("shakespeare", "literature", "drama", "english", "influence"),
            readCount = 156
        ),
        KnowledgeArticle(
            id = "5",
            title = "Pythagorean Theorem",
            content = "The Pythagorean theorem is a fundamental relation in Euclidean geometry among the three sides of a right triangle. It states that the area of the square whose side is the hypotenuse is equal to the sum of the areas of the squares on the other two sides.\n\nMathematically, this can be written as: a² + b² = c², where c represents the length of the hypotenuse and a and b the lengths of the triangle's other two sides.",
            categoryId = "math",
            summary = "Understanding the famous Pythagorean theorem and its applications in geometry.",
            tags = listOf("pythagorean", "theorem", "geometry", "triangle", "mathematics"),
            readCount = 198
        )
    )
}
