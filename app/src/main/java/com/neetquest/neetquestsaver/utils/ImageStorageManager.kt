package com.neetquest.neetquestsaver.utils

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val imagesDir: File
        get() = File(context.getExternalFilesDir(null), "saved_questions").also { it.mkdirs() }

    fun saveBitmap(bitmap: Bitmap): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault())
            .format(Date())
        val fileName = "NEET_Q_$timestamp.jpg"
        val file = File(imagesDir, fileName)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.absolutePath
    }

    fun deleteImage(path: String) {
        val file = File(path)
        if (file.exists()) file.delete()
    }

    fun getImageFile(path: String): File = File(path)

    fun getTotalStorageUsed(): Long {
        return imagesDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    fun clearAll() {
        imagesDir.walkTopDown().filter { it.isFile }.forEach { it.delete() }
    }
}
