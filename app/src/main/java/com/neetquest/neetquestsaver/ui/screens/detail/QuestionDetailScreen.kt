package com.neetquest.neetquestsaver.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.neetquest.neetquestsaver.data.entity.SavedQuestion
import com.neetquest.neetquestsaver.ui.screens.crop.FormDropdown
import com.neetquest.neetquestsaver.ui.screens.home.DIFFICULTIES
import com.neetquest.neetquestsaver.ui.screens.home.SUBJECTS
import com.neetquest.neetquestsaver.ui.theme.subjectColor
import com.neetquest.neetquestsaver.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionDetailScreen(
    questionId: Long,
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    var question by remember { mutableStateOf<SavedQuestion?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showZoom by remember { mutableStateOf(false) }

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
            editSubject = q.subject; editChapter = q.chapter
            editCategory = q.category; editDifficulty = q.difficulty
            editNotes = q.notes; editTags = q.tags.toSet()
        }
    }

    val q = question ?: run {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val dateStr = remember(q.timestamp) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(q.timestamp))
    }

    if (showZoom) {
        ZoomableImageViewer(
            imagePath = q.imagePath,
            onClose = { showZoom = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Question" else "Question Detail",
                    fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                actions = {
                    if (isEditing) {
                        TextButton(onClick = {
                            val u = q.copy(subject = editSubject, chapter = editChapter,
                                category = editCategory, difficulty = editDifficulty,
                                notes = editNotes, tags = editTags.toList())
                            viewModel.updateQuestion(u); question = u; isEditing = false
                        }) { Text("Save") }
                        TextButton(onClick = { isEditing = false }) { Text("Cancel") }
                    } else {
                        IconButton(onClick = { showZoom = true }) {
                            Icon(Icons.Default.ZoomIn, "Zoom",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = {
                            viewModel.toggleFavorite(q)
                            question = q.copy(isFavorite = !q.isFavorite)
                        }) {
                            Icon(if (q.isFavorite) Icons.Default.Bookmark
                                 else Icons.Default.BookmarkBorder, null)
                        }
                        IconButton(onClick
