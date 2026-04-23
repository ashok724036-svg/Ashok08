package com.neetquest.neetquestsaver.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Long = 0xFF6750A4,   // Material 3 purple as default
    val isDefault: Boolean = false
)
