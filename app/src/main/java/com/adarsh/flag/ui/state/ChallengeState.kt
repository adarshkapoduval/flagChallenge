package com.adarsh.flag.ui.state

import com.adarsh.flag.domain.model.enums.Phase

data class ChallengeState(
    val phase: Phase,
    val questionIndex: Int?, // 0-based
    val remainingMs: Long // remaining in current phase
)