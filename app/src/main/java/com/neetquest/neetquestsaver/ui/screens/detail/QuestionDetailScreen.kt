package com.neetquest.neetquestsaver.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.neetquest.neetquestsaver.data.entity.SavedQuestion
import com.neetquest.neetquestsaver.ui.screens.home.DIFFICULTIES
import com.neetquest.neetquestsaver.ui.screens.home.SUBJECTS
import com.neetquest.neetquestsaver.ui.screens.crop.FormDropdown
import com.neetquest.neetquestsaver.ui.theme.subjectColor
import com.neetquest.neetquestsaver.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionDetailScreen(questionId: Long, navController: NavController, viewModel: MainViewModel = hiltViewModel()) {
    var question by remember { mutableStateOf<SavedQuestion?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val chapters by viewModel.allChapters.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()

    LaunchedEffect(questionId) { question = viewModel.getQuestionById(questionId) }

    var editSubject by remember { mutableStateOf("") }
    var editChapter by remember { mutableStateOf("") }
    var editCategory by remember { mutableStateOf("") }
    var editDifficulty by remember { mutableStateOf("") }
    var editNotes by remember { mutableStateOf("") }
    var editTags by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(question) {
        question?.let { q ->
            editSubject = q.subject; editChapter = q.chapter; editCategory = q.category
            editDifficulty = q.difficulty; editNotes = q.notes; editTags = q.tags.toSet()
        }
    }

    val q = question ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val dateStr = remember(q.timestamp) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(q.timestamp))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Question" else "Question Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    if (isEditing) {
                        TextButton(onClick = {
                            val u = q.copy(subject=editSubject, chapter=editChapter, category=editCategory, difficulty=editDifficulty, notes=editNotes, tags=editTags.toList())
                            viewModel.updateQuestion(u); question = u; isEditing = false
                        }) { Text("Save") }
                        TextButton(onClick = { isEditing = false }) { Text("Cancel") }
                    } else {
                        IconButton(onClick = { viewModel.toggleFavorite(q); question = q.copy(isFavorite=!q.isFavorite) }) {
                            Icon(if (q.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, "Favourite")
                        }
                        IconButton(onClick = { isEditing = true }) { Icon(Icons.Default.Edit, "Edit") }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            AsyncImage(
                model = q.imagePath, contentDescription = "Question image",
                modifier = Modifier.fillMaxWidth().aspectRatio(4f/3f).background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Fit
            )
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isEditing) {
                    FormDropdown("Subject", editSubject, SUBJECTS, { editSubject = it })
                    FormDropdown("Chapter", editChapter, chapters.filter{it.subject==editSubject}.map{it.name}, { editChapter = it })
                    FormDropdown("Category", editCategory, categories.map{it.name}, { editCategory = it })
                    FormDropdown("Difficulty", editDifficulty, DIFFICULTIES, { editDifficulty = it })
                    OutlinedTextField(value=editNotes, onValueChange={editNotes=it}, label={Text("Notes")},
                        modifier=Modifier.fillMaxWidth(), minLines=3, shape=RoundedCornerShape(12.dp))
                } else {
                    val color = subjectColor(q.subject)
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Surface(shape=RoundedCornerShape(50), color=color.copy(alpha=0.12f)) {
                            Text(q.subject, Modifier.padding(horizontal=12.dp, vertical=4.dp),
                                style=MaterialTheme.typography.labelLarge, color=color, fontWeight=FontWeight.Bold)
                        }
                        Text(dateStr, style=MaterialTheme.typography.labelSmall, color=MaterialTheme.colorScheme.outline)
                    }
                    Text(q.chapter, style=MaterialTheme.typography.titleMedium, fontWeight=FontWeight.Bold)
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        InfoChip(q.category, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                        val dc = when(q.difficulty) { "Easy"->Color(0xFF2E7D32); "Hard"->Color(0xFFC62828); else->Color(0xFFE65100) }
                        InfoChip(q.difficulty, dc.copy(alpha=0.15f), dc)
                    }
                    if (q.tags.isNotEmpty()) Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                        q.tags.forEach { AssistChip(onClick={}, label={Text(it)}) }
                    }
                    if (q.notes.isNotBlank()) {
                        HorizontalDivider()
                        Text("Notes", style=MaterialTheme.typography.labelLarge, color=MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(q.notes, style=MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Question?") },
            text = { Text("This permanently deletes the question and its image.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteQuestion(q); showDeleteDialog=false; navController.popBackStack() },
                    colors = ButtonDefaults.textButtonColors(contentColor=MaterialTheme.colorScheme.error)) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick={showDeleteDialog=false}) { Text("Cancel") } }
        )
    }
}

@Composable
fun InfoChip(text: String, containerColor: Color, contentColor: Color) {
    Surface(shape = RoundedCornerShape(50), color = containerColor) {
        Text(text, Modifier.padding(horizontal=10.dp, vertical=3.dp),
            style=MaterialTheme.typography.labelSmall, color=contentColor, fontWeight=FontWeight.SemiBold)
    }
}
