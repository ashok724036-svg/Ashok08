package com.neetquest.neetquestsaver.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapters")
data class Chapter(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subject: String,
    val name: String,
    val unit: String = ""
)
