package com.neetquest.neetquestsaver.di

import android.content.Context
import com.neetquest.neetquestsaver.data.dao.CategoryDao
import com.neetquest.neetquestsaver.data.dao.ChapterDao
import com.neetquest.neetquestsaver.data.dao.SavedQuestionDao
import com.neetquest.neetquestsaver.data.database.NEETQuestDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NEETQuestDatabase {
        return NEETQuestDatabase.getDatabase(context)
    }

    @Provides
    fun provideSavedQuestionDao(db: NEETQuestDatabase): SavedQuestionDao = db.savedQuestionDao()

    @Provides
    fun provideChapterDao(db: NEETQuestDatabase): ChapterDao = db.chapterDao()

    @Provides
    fun provideCategoryDao(db: NEETQuestDatabase): CategoryDao = db.categoryDao()
}
