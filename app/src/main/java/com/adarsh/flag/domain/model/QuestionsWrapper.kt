package com.adarsh.flag.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class QuestionsWrapper(
    val questions: List<QuestionDto>
)
