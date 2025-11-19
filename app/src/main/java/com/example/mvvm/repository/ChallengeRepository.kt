package com.example.mvvm.repository

import android.content.Context
import com.example.mvvm.data.local.db.AppDatabase
import com.example.mvvm.data.local.entity.AnswerEntity
import com.example.mvvm.data.local.entity.ChallengeSettingsEntity
import com.example.mvvm.data.local.entity.QuestionEntity
import com.example.mvvm.utils.QuestionsParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class ChallengeRepository(private val db: AppDatabase) {

    private val qDao = db.questionDao()
    private val aDao = db.answerDao()
    private val sDao = db.challengeSettingsDao()

    fun getAllQuestionsFlow(): Flow<List<QuestionEntity>> = qDao.getAllQuestions()

    suspend fun getQuestion(index: Int) = qDao.getQuestion(index)

    suspend fun saveAnswer(answer: AnswerEntity) = aDao.upsert(answer)

    suspend fun getAnswer(index: Int) = aDao.getAnswer(index)

    suspend fun saveScheduledStartTime(ms: Long) = sDao.save(
        ChallengeSettingsEntity(
            scheduledStartMs = ms
        )
    )

    suspend fun getScheduledStartTime(): Long? = sDao.getStartTime()

    suspend fun questionsCount(): Int {
        return db.questionDao().getAllQuestions().first().size
    }

    //Populate database from raw questions.json resource only if DB is empty.
    suspend fun populateQuestionsIfEmpty(context: Context, rawResId: Int) {
        val existing = db.questionDao().getAllQuestions().firstOrNull()
        if (!existing.isNullOrEmpty()) return

        val entities = QuestionsParser.parseFromRawResource(context, rawResId)
        db.questionDao().insertAll(entities)
    }

    suspend fun getCorrectAnswerCount(): Int {
        return aDao.countCorrect()
    }

    suspend fun resetChallengeData() {
        aDao.clearAll()
        sDao.clear()
    }

}