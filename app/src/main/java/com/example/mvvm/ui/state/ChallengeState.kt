package com.example.mvvm.ui.state

import com.example.mvvm.domain.model.enums.Phase

data class ChallengeState(
    val phase: Phase,
    val questionIndex: Int?, // 0-based
    val remainingMs: Long // remaining in current phase
)