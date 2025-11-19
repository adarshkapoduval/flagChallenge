package com.example.mvvm.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mvvm.data.local.dao.AnswerDao
import com.example.mvvm.data.local.dao.ChallengeSettingsDao
import com.example.mvvm.data.local.dao.QuestionDao
import com.example.mvvm.data.local.entity.AnswerEntity
import com.example.mvvm.data.local.entity.ChallengeSettingsEntity
import com.example.mvvm.data.local.entity.QuestionEntity

@Database(entities = [QuestionEntity::class, AnswerEntity::class, ChallengeSettingsEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun answerDao(): AnswerDao
    abstract fun challengeSettingsDao(): ChallengeSettingsDao
}