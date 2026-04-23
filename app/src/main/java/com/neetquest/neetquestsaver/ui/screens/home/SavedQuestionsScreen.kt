package com.neetquest.neetquestsaver.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.neetquest.neetquestsaver.ui.Screen
import com.neetquest.neetquestsaver.viewmodel.MainViewModel

val SUBJECTS = listOf("Physics", "Chemistry", "Botany", "Zoology")
val DIFFICULTIES = listOf("Easy", "Medium", "Hard")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedQuestionsScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chapters by viewModel.allChapters.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Questions", fontWeight = FontWeight.Bold) },
                actions = {
                    if (uiState.selectedSubject != null ||
                        uiState.selectedCategory != null ||
                        uiState.selectedChapter != null
                    ) {
                        TextButton(onClick = viewModel::clearFilters) {
                            Text("Clear")
                        }
                    }
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::setSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Active filters display
            if (uiState.selectedSubject != null || uiState.selectedCategory != null) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.selectedSubject?.let { subject ->
                        item {
                            InputChip(
                                selected = true,
                                onClick = { viewModel.setSubjectFilter(null) },
                                label = { Text(subject) },
                                trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) }
                            )
                        }
                    }
                    uiState.selectedChapter?.let { chapter ->
                        item {
                            InputChip(
                                selected = true,
                                onClick = { viewModel.setChapterFilter(null) },
                                label = { Text(chapter) },
                                trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) }
                            )
                        }
                    }
                    uiState.selectedCategory?.let { cat ->
                        item {
                            InputChip(
                                selected = true,
                                onClick = { viewModel.setCategoryFilter(null) },
                                label = { Text(cat) },
                                trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // Results count
            Text(
                "${uiState.questions.size} questions",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                if (uiState.questions.isEmpty()) {
                    item { EmptyState(Modifier.fillMaxWidth().padding(32.dp)) }
                } else {
                    items(uiState.questions, key = { it.id }) { question ->
                        QuestionCard(
                            question = question,
                            onClick = {
                                navController.navigate(Screen.QuestionDetail.createRoute(question.id))
                            },
                            onFavoriteToggle = { viewModel.toggleFavorite(question) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }

    // ── Filter Bottom Sheet ───────────────────────────────────────────────────
    if (showFilterSheet) {
        FilterBottomSheet(
            currentSubject = uiState.selectedSubject,
            currentChapter = uiState.selectedChapter,
            currentCategory = uiState.selectedCategory,
            subjects = SUBJECTS,
            chapters = chapters.filter { uiState.selectedSubject == null || it.subject == uiState.selectedSubject },
            categories = categories,
            onSubjectSelected = viewModel::setSubjectFilter,
            onChapterSelected = viewModel::setChapterFilter,
            onCategorySelected = viewModel::setCategoryFilter,
            onDismiss = { showFilterSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentSubject: String?,
    currentChapter: String?,
    currentCategory: String?,
    subjects: List<String>,
    chapters: List<com.neetquest.neetquestsaver.data.entity.Chapter>,
    categories: List<com.neetquest.neetquestsaver.data.entity.Category>,
    onSubjectSelected: (String?) -> Unit,
    onChapterSelected: (String?) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Filter Questions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            Text("Subject", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subjects) { subject ->
                    FilterChip(
                        selected = currentSubject == subject,
                        onClick = { onSubjectSelected(if (currentSubject == subject) null else subject) },
                        label = { Text(subject) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Category", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = currentCategory == category.name,
                        onClick = { onCategorySelected(if (currentCategory == category.name) null else category.name) },
                        label = { Text(category.name) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Apply Filters")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
