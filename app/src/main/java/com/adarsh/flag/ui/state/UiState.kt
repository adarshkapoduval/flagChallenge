package com.adarsh.flag.ui.state

import com.adarsh.flag.domain.model.enums.Phase

data class UiState(
    val phase: Phase = Phase.NOT_NEAR,
    val currentQuestionIndex: Int? = null,
    val remainingMs: Long = 0L,
    val questions: List<UiQuestion> = emptyList(),
    val finalScore: Int? = null
)
