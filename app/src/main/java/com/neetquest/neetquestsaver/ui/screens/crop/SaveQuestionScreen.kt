package com.neetquest.neetquestsaver.ui.screens.crop

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.neetquest.neetquestsaver.ui.Screen
import com.neetquest.neetquestsaver.ui.screens.home.DIFFICULTIES
import com.neetquest.neetquestsaver.ui.screens.home.SUBJECTS
import com.neetquest.neetquestsaver.viewmodel.CropViewModel
import com.neetquest.neetquestsaver.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveQuestionScreen(
    navController: NavController,
    isManual: Boolean = false,
    mainViewModel: MainViewModel = hiltViewModel(),
    cropViewModel: CropViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val croppedBitmap by cropViewModel.croppedBitmap.collectAsStateWithLifecycle()
    val chapters by mainViewModel.allChapters.collectAsStateWithLifecycle()
    val categories by mainViewModel.allCategories.collectAsStateWithLifecycle()
    val saveState by mainViewModel.saveState.collectAsStateWithLifecycle()

    // Form state
    var selectedSubject by remember { mutableStateOf("Physics") }
    var selectedChapter by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Important") }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var selectedDifficulty by remember { mutableStateOf("Medium") }
    var notes by remember { mutableStateOf("") }
    var galleryImageUri by remember { mutableStateOf<Uri?>(null) }

    // For manual add from gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> galleryImageUri = uri }

    // Navigate away on save success
    LaunchedEffect(saveState.savedId) {
        if (saveState.savedId != null) {
            mainViewModel.resetSaveState()
            cropViewModel.clearCroppedBitmap()
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = false }
            }
        }
    }

    // Subject changes -> reset chapter
    LaunchedEffect(selectedSubject) { selectedChapter = "" }

    val chaptersForSubject = remember(chapters, selectedSubject) {
        chapters.filter { it.subject == selectedSubject }.map { it.name }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isManual) "Add from Gallery" else "Save Question",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = {
                        val bitmap = croppedBitmap ?: return@Button
                        mainViewModel.saveQuestion(
                            bitmap = bitmap,
                            subject = selectedSubject,
                            chapter = selectedChapter.ifBlank { chaptersForSubject.firstOrNull() ?: "" },
                            category = selectedCategory,
                            tags = selectedTags.toList(),
                            difficulty = selectedDifficulty,
                            notes = notes
                        )
                    },
                    enabled = (croppedBitmap != null || galleryImageUri != null) && !saveState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (saveState.isSaving) {
                        CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Default.Save, null)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Save Question", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Image Preview ─────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (croppedBitmap != null) {
                        AsyncImage(
                            model = croppedBitmap,
                            contentDescription = "Cropped question",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else if (galleryImageUri != null) {
                        AsyncImage(
                            model = galleryImageUri,
                            contentDescription = "Selected image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else if (isManual) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = { galleryLauncher.launch("image/*") }) {
                                Text("Pick from Gallery")
                            }
                        }
                    }
                }
            }

            // ── Subject Dropdown ──────────────────────────────────────────────
            FormDropdown(
                label = "Subject *",
                value = selectedSubject,
                options = SUBJECTS,
                onValueChange = { selectedSubject = it }
            )

            // ── Chapter Dropdown ──────────────────────────────────────────────
            FormDropdown(
                label = "Chapter *",
                value = selectedChapter,
                options = chaptersForSubject,
                onValueChange = { selectedChapter = it },
                placeholder = "Select chapter…"
            )

            // ── Category Dropdown ─────────────────────────────────────────────
            FormDropdown(
                label = "Category *",
                value = selectedCategory,
                options = categories.map { it.name },
                onValueChange = { selectedCategory = it }
            )

            // ── Difficulty ────────────────────────────────────────────────────
            Column {
                Text(
                    "Difficulty",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DIFFICULTIES.forEach { diff ->
                        val color = when (diff) {
                            "Easy" -> Color(0xFF2E7D32)
                            "Hard" -> Color(0xFFC62828)
                            else -> Color(0xFFE65100)
                        }
                        FilterChip(
                            selected = selectedDifficulty == diff,
                            onClick = { selectedDifficulty = diff },
                            label = { Text(diff) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color.copy(alpha = 0.15f),
                                selectedLabelColor = color
                            )
                        )
                    }
                }
            }

            // ── Tags Multi-select ─────────────────────────────────────────────
            val availableTags = listOf("PYQ", "Formula", "Diagram", "Shortcut", "Derivation", "Graph", "Exception", "Important")
            Column {
                Text(
                    "Tags (optional)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableTags) { tag ->
                        FilterChip(
                            selected = tag in selectedTags,
                            onClick = {
                                selectedTags = if (tag in selectedTags)
                                    selectedTags - tag
                                else
                                    selectedTags + tag
                            },
                            label = { Text(tag) }
                        )
                    }
                }
            }

            // ── Notes ─────────────────────────────────────────────────────────
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                placeholder = { Text("Add personal notes, mnemonics, key points…") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp)
            )

            // Error message
            saveState.error?.let { err ->
                Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDropdown(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    placeholder: String = "Select…"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                    leadingIcon = if (option == value) {
                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                    } else null
                )
            }
        }
    }
}
