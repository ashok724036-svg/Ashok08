package com.neetquest.neetquestsaver.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.neetquest.neetquestsaver.data.dao.CategoryDao
import com.neetquest.neetquestsaver.data.dao.ChapterDao
import com.neetquest.neetquestsaver.data.dao.SavedQuestionDao
import com.neetquest.neetquestsaver.data.entity.Category
import com.neetquest.neetquestsaver.data.entity.Chapter
import com.neetquest.neetquestsaver.data.entity.SavedQuestion
import com.neetquest.neetquestsaver.utils.ImageStorageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val questionDao: SavedQuestionDao,
    private val chapterDao: ChapterDao,
    private val categoryDao: CategoryDao,
    private val imageStorageManager: ImageStorageManager
) {

    // ─── Questions ────────────────────────────────────────────────────────────

    fun getAllQuestions(): Flow<List<SavedQuestion>> = questionDao.getAllQuestions()

    fun getFilteredQuestions(
        subject: String? = null,
        chapter: String? = null,
        category: String? = null
    ): Flow<List<SavedQuestion>> = questionDao.getFilteredQuestions(subject, chapter, category)

    fun searchQuestions(query: String): Flow<List<SavedQuestion>> =
        questionDao.searchQuestions(query)

    fun getFavorites(): Flow<List<SavedQuestion>> = questionDao.getFavorites()

    fun getTotalCount(): Flow<Int> = questionDao.getTotalCount()

    fun getCountBySubject(subject: String): Flow<Int> = questionDao.getCountBySubject(subject)

    suspend fun getQuestionById(id: Long): SavedQuestion? = questionDao.getQuestionById(id)

    suspend fun saveQuestion(
        bitmap: Bitmap,
        subject: String,
        chapter: String,
        category: String,
        tags: List<String>,
        difficulty: String,
        notes: String,
        sourceApp: String = ""
    ): Long {
        val imagePath = imageStorageManager.saveBitmap(bitmap)
        val question = SavedQuestion(
            imagePath = imagePath,
            subject = subject,
            chapter = chapter,
            category = category,
            tags = tags,
            difficulty = difficulty,
            notes = notes,
            sourceApp = sourceApp
        )
        return questionDao.insertQuestion(question)
    }

    suspend fun updateQuestion(question: SavedQuestion) = questionDao.updateQuestion(question)

    suspend fun deleteQuestion(question: SavedQuestion) {
        // Delete image file first
        imageStorageManager.deleteImage(question.imagePath)
        questionDao.deleteQuestion(question)
    }

    suspend fun getAllQuestionsSync(): List<SavedQuestion> = questionDao.getAllQuestionsSync()

    // ─── Chapters ─────────────────────────────────────────────────────────────

    fun getAllChapters(): Flow<List<Chapter>> = chapterDao.getAllChapters()

    fun getChaptersBySubject(subject: String): Flow<List<Chapter>> =
        chapterDao.getChaptersBySubject(subject)

    suspend fun addChapter(chapter: Chapter): Long = chapterDao.insert(chapter)

    // ─── Categories ───────────────────────────────────────────────────────────

    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun addCategory(category: Category): Long = categoryDao.insert(category)

    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)
}
