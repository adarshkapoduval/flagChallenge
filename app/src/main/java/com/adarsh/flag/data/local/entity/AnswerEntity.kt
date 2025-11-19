package com.adarsh.flag.data.local.entity

import androidx.room.Entity

@Entity(tableName = "answers", primaryKeys = ["questionIndex"])
data class AnswerEntity(
    val questionIndex: Int,
    val selectedOptionId: Int,
    val answeredAtMs: Long,
    val isCorrect: Boolean
)
