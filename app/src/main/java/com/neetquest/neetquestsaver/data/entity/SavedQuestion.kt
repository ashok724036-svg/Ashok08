package com.neetquest.neetquestsaver.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.neetquest.neetquestsaver.data.database.Converters

@Entity(tableName = "saved_questions")
@TypeConverters(Converters::class)
data class SavedQuestion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imagePath: String,           // Path to cropped image file
    val subject: String,             // Physics, Chemistry, Botany, Zoology
    val chapter: String,
    val category: String,            // Important, Tricky, Repeated, etc.
    val tags: List<String> = emptyList(),
    val difficulty: String = "Medium", // Easy, Medium, Hard
    val notes: String = "",
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val sourceApp: String = ""       // Which app/website question was from
)
