package com.neetquest.neetquestsaver.data.dao

import androidx.room.*
import com.neetquest.neetquestsaver.data.entity.SavedQuestion
import com.neetquest.neetquestsaver.data.entity.Chapter
import com.neetquest.neetquestsaver.data.entity.Category
import kotlinx.coroutines.flow.Flow

// ─── SavedQuestion DAO ────────────────────────────────────────────────────────

@Dao
interface SavedQuestionDao {

    @Query("SELECT * FROM saved_questions ORDER BY timestamp DESC")
    fun getAllQuestions(): Flow<List<SavedQuestion>>

    @Query("""
        SELECT * FROM saved_questions 
        WHERE (:subject IS NULL OR subject = :subject)
          AND (:chapter IS NULL OR chapter = :chapter)
          AND (:category IS NULL OR category = :category)
        ORDER BY timestamp DESC
    """)
    fun getFilteredQuestions(
        subject: String? = null,
        chapter: String? = null,
        category: String? = null
    ): Flow<List<SavedQuestion>>

    @Query("""
        SELECT * FROM saved_questions 
        WHERE subject LIKE '%' || :query || '%'
           OR chapter LIKE '%' || :query || '%'
           OR category LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun searchQuestions(query: String): Flow<List<SavedQuestion>>

    @Query("SELECT * FROM saved_questions WHERE id = :id")
    suspend fun getQuestionById(id: Long): SavedQuestion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: SavedQuestion): Long

    @Update
    suspend fun updateQuestion(question: SavedQuestion)

    @Delete
    suspend fun deleteQuestion(question: SavedQuestion)

    @Query("DELETE FROM saved_questions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM saved_questions")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM saved_questions WHERE subject = :subject")
    fun getCountBySubject(subject: String): Flow<Int>

    @Query("SELECT * FROM saved_questions WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<SavedQuestion>>

    @Query("SELECT * FROM saved_questions ORDER BY timestamp DESC")
    suspend fun getAllQuestionsSync(): List<SavedQuestion>
}

// ─── Chapter DAO ──────────────────────────────────────────────────────────────

@Dao
interface ChapterDao {

    @Query("SELECT * FROM chapters WHERE subject = :subject ORDER BY name ASC")
    fun getChaptersBySubject(subject: String): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters ORDER BY subject, name ASC")
    fun getAllChapters(): Flow<List<Chapter>>

    @Query("SELECT COUNT(*) FROM chapters")
    suspend fun getChapterCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(chapters: List<Chapter>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chapter: Chapter): Long

    @Delete
    suspend fun delete(chapter: Chapter)
}

// ─── Category DAO ─────────────────────────────────────────────────────────────

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY isDefault DESC, name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<Category>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
}
