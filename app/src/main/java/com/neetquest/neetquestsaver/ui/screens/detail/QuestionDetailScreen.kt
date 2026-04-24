package com.neetquest.neetquestsaver.ui.screens.detail

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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

    // ── Fullscreen Zoom Viewer ────────────────────────────────────────────────
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
                        // Zoom button
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
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, null)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, null,
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            // ── Image (tap to zoom) ───────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = q.imagePath,
                    contentDescription = "Question image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                // Zoom hint badge
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    onClick = { showZoom = true }
                ) {
                    Row(
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.ZoomIn, null, Modifier.size(14.dp),
                            tint = Color.White)
                        Text("Tap to zoom", style = MaterialTheme.typography.labelSmall,
                            color = Color.White)
                    }
                }
            }

            // ── Meta info ─────────────────────────────────────────────────────
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isEditing) {
                     FormDropdown("Subject", editSubject, SUBJECTS, onValueChange = { editSubject = it })
                    FormDropdown("Chapter", editChapter,
                        chapters.filter { it.subject == editSubject }.map { it.name },
                        onValueChange = { editChapter = it })
                    FormDropdown("Category", editCategory,
                        categories.map { it.name }, onValueChange = { editCategory = it })
                    FormDropdown("Difficulty", editDifficulty, DIFFICULTIES, onValueChange = { editDifficulty = it })
                    
                    OutlinedTextField(
                        value = editNotes, onValueChange = { editNotes = it },
                        label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(),
                        minLines = 3, shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    val color = subjectColor(q.subject)
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.12f)) {
                            Text(q.subject, Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = color, fontWeight = FontWeight.Bold)
                        }
                        Text(dateStr, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline)
                    }
                    Text(q.chapter, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoChip(q.category,
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.onPrimaryContainer)
                        val dc = when (q.difficulty) {
                            "Easy" -> Color(0xFF2E7D32)
                            "Hard" -> Color(0xFFC62828)
                            else   -> Color(0xFFE65100)
                        }
                        InfoChip(q.difficulty, dc.copy(alpha = 0.15f), dc)
                    }

                    if (q.tags.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            q.tags.forEach { tag ->
                                AssistChip(onClick = {}, label = { Text(tag) })
                            }
                        }
                    }

                    if (q.notes.isNotBlank()) {
                        HorizontalDivider()
                        Text("Notes", style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(q.notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, null,
                tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Question?") },
            text = { Text("This permanently deletes the question and its image.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteQuestion(q)
                        showDeleteDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ── Fullscreen Pinch-to-Zoom Image Viewer ─────────────────────────────────────

@Composable
fun ZoomableImageViewer(imagePath: String, onClose: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = max(1f, scale * zoom)
                    if (scale > 1f) {
                        offsetX += pan.x
                        offsetY += pan.y
                    } else {
                        offsetX = 0f; offsetY = 0f
                    }
                }
            }
    ) {
        AsyncImage(
            model = imagePath,
            contentDescription = "Zoomed question",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                ),
            contentScale = ContentScale.Fit
        )

        // Close + zoom info bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
            Text(
                "Pinch to zoom  •  ${(scale * 100).toInt()}%",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelSmall
            )
            IconButton(onClick = { scale = 1f; offsetX = 0f; offsetY = 0f }) {
                Icon(Icons.Default.FitScreen, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun InfoChip(text: String, containerColor: Color, contentColor: Color) {
    Surface(shape = RoundedCornerShape(50), color = containerColor) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor, fontWeight = FontWeight.SemiBold)
    }
}
