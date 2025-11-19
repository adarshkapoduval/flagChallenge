package com.example.mvvm.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class QuestionsWrapper(
    val questions: List<Question>
)
