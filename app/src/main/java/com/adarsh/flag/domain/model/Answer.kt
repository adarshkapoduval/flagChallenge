package com.adarsh.flag.domain.model

data class Answer(
    val questionIndex: Int,
    val selectedOptionId: Int,
    val answeredAtMs: Long,
    val isCorrect: Boolean
)
