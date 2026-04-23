package com.neetquest.neetquestsaver.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neetquest.neetquestsaver.data.entity.Category
import com.neetquest.neetquestsaver.data.entity.Chapter
import com.neetquest.neetquestsaver.data.entity.SavedQuestion
import com.neetquest.neetquestsaver.data.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class HomeUiState(
    val questions: List<SavedQuestion> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedSubject: String? = null,
    val selectedChapter: String? = null,
    val selectedCategory: String? = null,
    val error: String? = null
)

data class SaveQuestionState(
    val isSaving: Boolean = false,
    val savedId: Long? = null,
    val error: String? = null
)

private data class FilterParams(val query: String, val subject: String?, val chapter: String?, val category: String?)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(private val repository: QuestionRepository) : ViewModel() {

    private val _searchQuery      = MutableStateFlow("")
    private val _selectedSubject  = MutableStateFlow<String?>(null)
    private val _selectedChapter  = MutableStateFlow<String?>(null)
    private val _selectedCategory = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        _searchQuery, _selectedSubject, _selectedChapter, _selectedCategory
    ) { q, s, ch, ca -> FilterParams(q, s, ch, ca) }
        .flatMapLatest { p ->
            val flow = if (p.query.isNotBlank()) repository.searchQuestions(p.query)
                       else repository.getFilteredQuestions(p.subject, p.chapter, p.category)
            flow.map { list ->
                HomeUiState(questions = list, searchQuery = p.query,
                    selectedSubject = p.subject, selectedChapter = p.chapter, selectedCategory = p.category)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState(isLoading = true))

    val allChapters: StateFlow<List<Chapter>> = repository.getAllChapters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val allCategories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val physicsCount   = repository.getCountBySubject("Physics").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val chemistryCount = repository.getCountBySubject("Chemistry").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val botanyCount    = repository.getCountBySubject("Botany").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val zoologyCount   = repository.getCountBySubject("Zoology").stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val totalCount     = repository.getTotalCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _saveState = MutableStateFlow(SaveQuestionState())
    val saveState: StateFlow<SaveQuestionState> = _saveState.asStateFlow()

    fun setSearchQuery(q: String)     { _searchQuery.value = q }
    fun setSubjectFilter(s: String?)  { _selectedSubject.value = s }
    fun setChapterFilter(c: String?)  { _selectedChapter.value = c }
    fun setCategoryFilter(c: String?) { _selectedCategory.value = c }
    fun clearFilters() {
        _searchQuery.value = ""; _selectedSubject.value = null
        _selectedChapter.value = null; _selectedCategory.value = null
    }

    fun saveQuestion(bitmap: Bitmap, subject: String, chapter: String, category: String,
                     tags: List<String>, difficulty: String, notes: String) {
        viewModelScope.launch {
            _saveState.value = SaveQuestionState(isSaving = true)
            try {
                val id = repository.saveQuestion(bitmap, subject, chapter, category, tags, difficulty, notes)
                _saveState.value = SaveQuestionState(savedId = id)
            } catch (e: Exception) {
                _saveState.value = SaveQuestionState(error = e.message)
            }
        }
    }

    fun resetSaveState() { _saveState.value = SaveQuestionState() }

    suspend fun getQuestionById(id: Long): SavedQuestion? =
        withContext(Dispatchers.IO) { repository.getQuestionById(id) }

    fun updateQuestion(q: SavedQuestion) { viewModelScope.launch { repository.updateQuestion(q) } }
    fun deleteQuestion(q: SavedQuestion) { viewModelScope.launch { repository.deleteQuestion(q) } }
    fun toggleFavorite(q: SavedQuestion) {
        viewModelScope.launch { repository.updateQuestion(q.copy(isFavorite = !q.isFavorite)) }
    }
    fun addCategory(name: String, color: Long) {
        viewModelScope.launch { repository.addCategory(Category(name = name, color = color)) }
    }
    fun deleteCategory(cat: Category) { viewModelScope.launch { repository.deleteCategory(cat) } }
    suspend fun getAllQuestionsForExport(): List<SavedQuestion> =
        withContext(Dispatchers.IO) { repository.getAllQuestionsSync() }
}
