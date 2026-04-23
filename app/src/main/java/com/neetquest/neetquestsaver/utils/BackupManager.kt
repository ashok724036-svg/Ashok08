package com.neetquest.neetquestsaver.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.neetquest.neetquestsaver.data.entity.SavedQuestion
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

data class ExportData(
    val exportDate: String,
    val version: Int = 1,
    val questions: List<SavedQuestion>
)

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun exportToJson(questions: List<SavedQuestion>): File {
        val exportData = ExportData(
            exportDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            questions = questions
        )
        val json = gson.toJson(exportData)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(
            context.getExternalFilesDir(null),
            "NEETQuestBackup_$timestamp.json"
        )
        file.writeText(json)
        return file
    }

    fun importFromJson(file: File): List<SavedQuestion>? {
        return try {
            val json = file.readText()
            val exportData = gson.fromJson(json, ExportData::class.java)
            exportData.questions
        } catch (e: Exception) {
            null
        }
    }

    fun getStorageInfo(context: Context): String {
        val dir = context.getExternalFilesDir(null) ?: return "Unknown"
        val total = dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        return formatSize(total)
    }

    private fun formatSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
    }
}
