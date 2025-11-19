package com.adarsh.flag.repository

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.adarsh.flag.data.local.db.AppDatabase
import com.adarsh.flag.data.local.entity.AnswerEntity
import com.adarsh.flag.data.local.entity.ChallengeSettingsEntity
import com.adarsh.flag.data.local.entity.QuestionEntity
import com.adarsh.flag.receiver.StartGameReceiver
import com.adarsh.flag.utils.QuestionsParser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class ChallengeRepository(private val db: AppDatabase, @ApplicationContext private val context: Context) {

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

    suspend fun getScheduledStartTime(): Flow<Long?> = sDao.getStartTime()

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

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAlarm(ms: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, StartGameReceiver::class.java).apply {
            putExtra("scheduled_epoch", ms)
        }

        val pending = PendingIntent.getBroadcast(
            context,
            1001, // request code — keep unique if you schedule multiple alarms
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // exact and allow while idle (handles Doze)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ms, pending)
    }

}