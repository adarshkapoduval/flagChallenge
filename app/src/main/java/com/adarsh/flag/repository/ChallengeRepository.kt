package com.adarsh.flag.repository

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.adarsh.flag.data.local.dao.AnswerDao
import com.adarsh.flag.data.local.dao.ChallengeSettingsDao
import com.adarsh.flag.data.local.dao.QuestionDao
import com.adarsh.flag.data.local.db.AppDatabase
import com.adarsh.flag.data.local.entity.ChallengeSettingsEntity
import com.adarsh.flag.data.mapper.toDomain
import com.adarsh.flag.data.mapper.toEntity
import com.adarsh.flag.domain.model.Answer
import com.adarsh.flag.domain.model.QuestionModel
import com.adarsh.flag.receiver.StartGameReceiver
import com.adarsh.flag.utils.QuestionsParser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChallengeRepository @Inject constructor(
    private val questionDao: QuestionDao,
    private val answerDao: AnswerDao,
    private val settingsDao: ChallengeSettingsDao,
    @ApplicationContext private val context: Context
)
 {
    fun getAllQuestionsFlow(): Flow<List<QuestionModel>> =
        questionDao.getAllQuestions().map { list ->
            list.map { it.toDomain() }
        }

    suspend fun getQuestion(index: Int): QuestionModel? =
        questionDao.getQuestion(index)?.toDomain()

    suspend fun saveAnswer(answer: Answer) =
        answerDao.upsert(answer.toEntity())

    suspend fun getAnswer(index: Int): Answer? =
        answerDao.getAnswer(index)?.toDomain()

    suspend fun saveScheduledStartTime(ms: Long) = settingsDao.save(
        ChallengeSettingsEntity(
            scheduledStartMs = ms
        )
    )

    suspend fun getScheduledStartTime(): Flow<Long?> = settingsDao.getStartTime()

     suspend fun questionsCount(): Int = questionDao.count()

     //Populate database from raw questions.json resource only if DB is empty.
     suspend fun populateQuestionsIfEmpty(rawResId: Int) {
        val existing = questionDao.getAllQuestions().firstOrNull()
        if (!existing.isNullOrEmpty()) return

        val entities = QuestionsParser.parseFromRawResource(context, rawResId)
        questionDao.insertAll(entities)
    }

    suspend fun getCorrectAnswerCount(): Int {
        return answerDao.countCorrect()
    }

    suspend fun resetChallengeData() {
        answerDao.clearAll()
        settingsDao.clear()
    }
}
