package com.example.mvvm.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val answer_id: Int,
    val countries: List<Country>,
    val country_code: String
)
