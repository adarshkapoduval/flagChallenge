package com.example.mvvm.utils

import com.example.mvvm.domain.constants.ChallengeConstants
import com.example.mvvm.ui.state.ChallengeState
import com.example.mvvm.domain.model.enums.Phase

fun computeChallengeState(scheduledStartMs: Long, nowMs: Long = System.currentTimeMillis()): ChallengeState {
    val PRE = ChallengeConstants.PRE_START_MS
    val Q = ChallengeConstants.QUESTION_MS
    val I = ChallengeConstants.INTERVAL_MS
    val C = ChallengeConstants.CYCLE_MS
    val TOTAL = ChallengeConstants.TOTAL_QUESTIONS

    val delta = nowMs - scheduledStartMs

    if (delta < -PRE) {
        // Not in prestart window yet
        return ChallengeState(Phase.NOT_NEAR, null, -delta)
    }
    if (delta in -PRE until 0L) {
        return ChallengeState(Phase.PRE_START, null, -delta)
    }

    val elapsed = delta
    val totalDuration = C * TOTAL
    if (elapsed >= totalDuration) {
        return ChallengeState(Phase.FINISHED, null, 0L)
    }

    val cycleIndex = (elapsed / C).toInt().coerceIn(0, TOTAL - 1)
    val inCycleMs = elapsed % C

    return if (inCycleMs < Q) {
        ChallengeState(Phase.QUESTION, cycleIndex, Q - inCycleMs)
    } else {
        ChallengeState(Phase.INTERVAL, cycleIndex, C - inCycleMs)
    }
}