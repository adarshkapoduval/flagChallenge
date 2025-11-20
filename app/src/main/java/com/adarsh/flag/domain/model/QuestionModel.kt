package com.adarsh.flag.domain.model

data class QuestionModel(
    val questionIndex: Int,
    val countryCode: String,
    val correctAnswerId: Int,
    val options: List<Country>
)
