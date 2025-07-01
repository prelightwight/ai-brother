package com.prelightwight.aibrother.knowledge

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

data class KnowledgeEntry(
    val id: String,
    val title: String,
    val content: String,
    val category: KnowledgeCategory,
    val tags: List<String>,
    val isBuiltIn: Boolean,
    val timestamp: Long
)

enum class KnowledgeCategory(val displayName: String, val icon: @Composable () -> Unit) {
    SCIENCE("Science", { Icon(Icons.Default.Science, contentDescription = null) }),
    TECHNOLOGY("Technology", { Icon(Icons.Default.Computer, contentDescription = null) }),
    HISTORY("History", { Icon(Icons.Default.Museum, contentDescription = null) }),
    GENERAL("General", { Icon(Icons.Default.Info, contentDescription = null) }),
    PERSONAL("Personal", { Icon(Icons.Default.Person, contentDescription = null) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<KnowledgeCategory?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var knowledgeEntries by remember { mutableStateOf(getBuiltInKnowledge()) }
    var selectedEntry by remember { mutableStateOf<KnowledgeEntry?>(null) }

    val filteredEntries = knowledgeEntries.filter { entry ->
        val matchesSearch = searchQuery.isBlank() || 
            entry.title.contains(searchQuery, ignoreCase = true) ||
            entry.content.contains(searchQuery, ignoreCase = true) ||
            entry.tags.any { it.contains(searchQuery, ignoreCase = true) }
        val matchesCategory = selectedCategory == null || entry.category == selectedCategory
        matchesSearch && matchesCategory
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(
            text = "📚 Knowledge Base",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Search bar and add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search knowledge...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* Search action */ })
            )
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Knowledge")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    onClick = { selectedCategory = null },
                    label = { Text("All") },
                    selected = selectedCategory == null,
                    leadingIcon = { Icon(Icons.Default.Apps, contentDescription = null) }
                )
            }
            items(KnowledgeCategory.values()) { category ->
                FilterChip(
                    onClick = { 
                        selectedCategory = if (selectedCategory == category) null else category 
                    },
                    label = { Text(category.displayName) },
                    selected = selectedCategory == category,
                    leadingIcon = category.icon
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results count
        Text(
            text = "${filteredEntries.size} entries found",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Knowledge entries list
        LazyColumn {
            items(filteredEntries) { entry ->
                KnowledgeItem(
                    entry = entry,
                    onClick = { selectedEntry = entry }
                )
            }
        }
    }

    // Add knowledge dialog
    if (showAddDialog) {
        AddKnowledgeDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, content, category, tags ->
                val newEntry = KnowledgeEntry(
                    id = System.currentTimeMillis().toString(),
                    title = title,
                    content = content,
                    category = category,
                    tags = tags,
                    isBuiltIn = false,
                    timestamp = System.currentTimeMillis()
                )
                knowledgeEntries = knowledgeEntries + newEntry
                showAddDialog = false
            }
        )
    }

    // Knowledge detail dialog
    selectedEntry?.let { entry ->
        KnowledgeDetailDialog(
            entry = entry,
            onDismiss = { selectedEntry = null },
            onDelete = if (!entry.isBuiltIn) {
                { 
                    knowledgeEntries = knowledgeEntries.filter { it.id != entry.id }
                    selectedEntry = null
                }
            } else null
        )
    }
}

@Composable
fun KnowledgeItem(entry: KnowledgeEntry, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                entry.category.icon()
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                if (!entry.isBuiltIn) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "User created",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = entry.content.take(150) + if (entry.content.length > 150) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (entry.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(entry.tags) { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddKnowledgeDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, KnowledgeCategory, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(KnowledgeCategory.GENERAL) }
    var tagsText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Knowledge Entry") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = { Text("Tags (comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        val tags = tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        onAdd(title, content, selectedCategory, tags)
                    }
                },
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun KnowledgeDetailDialog(
    entry: KnowledgeEntry,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                entry.category.icon()
                Spacer(modifier = Modifier.width(8.dp))
                Text(entry.title)
            }
        },
        text = {
            LazyColumn {
                item {
                    Text(
                        text = entry.content,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    if (entry.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tags:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            items(entry.tags) { tag ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(tag) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row {
                if (onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

private fun getBuiltInKnowledge(): List<KnowledgeEntry> {
    return listOf(
        KnowledgeEntry(
            id = "1",
            title = "Artificial Intelligence",
            content = "Artificial Intelligence (AI) refers to the simulation of human intelligence in machines that are programmed to think and learn like humans. AI systems can perform tasks such as visual perception, speech recognition, decision-making, and language translation.",
            category = KnowledgeCategory.TECHNOLOGY,
            tags = listOf("AI", "Machine Learning", "Technology"),
            isBuiltIn = true,
            timestamp = System.currentTimeMillis()
        ),
        KnowledgeEntry(
            id = "2",
            title = "Quantum Computing",
            content = "Quantum computing is a type of computation that harnesses quantum mechanical phenomena like superposition and entanglement to process information. Unlike classical computers that use bits, quantum computers use quantum bits (qubits) that can exist in multiple states simultaneously.",
            category = KnowledgeCategory.SCIENCE,
            tags = listOf("Quantum", "Computing", "Physics"),
            isBuiltIn = true,
            timestamp = System.currentTimeMillis()
        ),
        KnowledgeEntry(
            id = "3",
            title = "The Renaissance",
            content = "The Renaissance was a period of cultural, artistic, political and economic rebirth following the Middle Ages. It began in Italy in the 14th century and spread throughout Europe, marking the transition from medieval to modern times with renewed interest in classical learning and humanism.",
            category = KnowledgeCategory.HISTORY,
            tags = listOf("Renaissance", "History", "Art", "Culture"),
            isBuiltIn = true,
            timestamp = System.currentTimeMillis()
        ),
        KnowledgeEntry(
            id = "4",
            title = "Photosynthesis",
            content = "Photosynthesis is the process by which plants, algae, and some bacteria convert light energy into chemical energy stored in glucose. This process occurs in chloroplasts and involves two main stages: light-dependent reactions and the Calvin cycle.",
            category = KnowledgeCategory.SCIENCE,
            tags = listOf("Biology", "Plants", "Energy"),
            isBuiltIn = true,
            timestamp = System.currentTimeMillis()
        ),
        KnowledgeEntry(
            id = "5",
            title = "Blockchain Technology",
            content = "Blockchain is a distributed ledger technology that maintains a continuously growing list of records, called blocks, which are linked and secured using cryptography. Each block contains a cryptographic hash of the previous block, making the data tamper-resistant.",
            category = KnowledgeCategory.TECHNOLOGY,
            tags = listOf("Blockchain", "Cryptocurrency", "Security"),
            isBuiltIn = true,
            timestamp = System.currentTimeMillis()
        )
    )
}
