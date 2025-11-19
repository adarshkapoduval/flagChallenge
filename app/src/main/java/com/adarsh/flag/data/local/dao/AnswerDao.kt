package com.adarsh.flag.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.adarsh.flag.data.local.entity.AnswerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnswerDao {
    @Query("SELECT * FROM answers ORDER BY questionIndex")
    fun getAllAnswers(): Flow<List<AnswerEntity>>

    @Query("SELECT * FROM answers WHERE questionIndex = :index LIMIT 1")
    suspend fun getAnswer(index: Int): AnswerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(answer: AnswerEntity)

    @Query("DELETE FROM answers")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM answers WHERE isCorrect = 1")
    suspend fun countCorrect(): Int
}