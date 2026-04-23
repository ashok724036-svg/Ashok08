package com.neetquest.neetquestsaver.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("neetquest_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val BUBBLE_ENABLED = booleanPreferencesKey("bubble_enabled")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val DEFAULT_SUBJECT = stringPreferencesKey("default_subject")
        val DEFAULT_CATEGORY = stringPreferencesKey("default_category")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    }

    val isBubbleEnabled: Flow<Boolean> = dataStore.data
        .map { it[BUBBLE_ENABLED] ?: false }

    val isDarkTheme: Flow<Boolean?> = dataStore.data
        .map { it[DARK_THEME] }  // null = system default

    val defaultSubject: Flow<String> = dataStore.data
        .map { it[DEFAULT_SUBJECT] ?: "Physics" }

    val defaultCategory: Flow<String> = dataStore.data
        .map { it[DEFAULT_CATEGORY] ?: "Important" }

    val isFirstLaunch: Flow<Boolean> = dataStore.data
        .map { it[FIRST_LAUNCH] ?: true }

    suspend fun setBubbleEnabled(enabled: Boolean) {
        dataStore.edit { it[BUBBLE_ENABLED] = enabled }
    }

    suspend fun setDarkTheme(dark: Boolean?) {
        dataStore.edit {
            if (dark == null) it.remove(DARK_THEME)
            else it[DARK_THEME] = dark
        }
    }

    suspend fun setDefaultSubject(subject: String) {
        dataStore.edit { it[DEFAULT_SUBJECT] = subject }
    }

    suspend fun setDefaultCategory(category: String) {
        dataStore.edit { it[DEFAULT_CATEGORY] = category }
    }

    suspend fun setFirstLaunchDone() {
        dataStore.edit { it[FIRST_LAUNCH] = false }
    }
}
