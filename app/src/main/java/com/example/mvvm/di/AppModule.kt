package com.example.mvvm.di

import android.content.Context
import androidx.room.Room
import com.example.mvvm.data.local.dao.AnswerDao
import com.example.mvvm.data.local.dao.ChallengeSettingsDao
import com.example.mvvm.data.local.dao.QuestionDao
import com.example.mvvm.data.local.db.AppDatabase
import com.example.mvvm.repository.ChallengeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(appContext, AppDatabase::class.java, "flags-db")
            // .fallbackToDestructiveMigration() // optional for dev
            .build()
    }

    @Provides
    fun provideQuestionDao(db: AppDatabase): QuestionDao = db.questionDao()

    @Provides
    fun provideAnswerDao(db: AppDatabase): AnswerDao = db.answerDao()

    @Provides
    fun provideChallengeSettingsDao(db: AppDatabase): ChallengeSettingsDao = db.challengeSettingsDao()

    @Provides
    @Singleton
    fun provideChallengeRepository(db: AppDatabase): ChallengeRepository {
        return ChallengeRepository(db)
    }
}