package com.adarsh.flag.di

import android.content.Context
import androidx.room.Room
import com.adarsh.flag.alarm.AlarmScheduler
import com.adarsh.flag.data.local.dao.AnswerDao
import com.adarsh.flag.data.local.dao.ChallengeSettingsDao
import com.adarsh.flag.data.local.dao.QuestionDao
import com.adarsh.flag.data.local.db.AppDatabase
import com.adarsh.flag.repository.ChallengeRepository
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
    @Singleton
    fun provideQuestionDao(db: AppDatabase): QuestionDao = db.questionDao()

    @Provides
    @Singleton
    fun provideAnswerDao(db: AppDatabase): AnswerDao = db.answerDao()

    @Provides
    @Singleton
    fun provideChallengeSettingsDao(db: AppDatabase): ChallengeSettingsDao = db.challengeSettingsDao()

    @Provides
    @Singleton
    fun provideChallengeRepository(
        questionDao: QuestionDao,
        answerDao: AnswerDao,
        settingsDao: ChallengeSettingsDao,
        @ApplicationContext appContext: Context
    ): ChallengeRepository {
        return ChallengeRepository(questionDao, answerDao, settingsDao, appContext)
    }

    @Provides
    @Singleton
    fun provideAlarmScheduler(
        @ApplicationContext context: Context
    ): AlarmScheduler = AlarmScheduler(context)

}