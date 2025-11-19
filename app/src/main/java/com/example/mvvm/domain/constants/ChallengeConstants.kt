package com.example.mvvm.domain.constants

object ChallengeConstants {
    const val PRE_START_MS = 20_000L
    const val QUESTION_MS = 30_000L
    const val INTERVAL_MS = 10_000L
    const val CYCLE_MS = QUESTION_MS + INTERVAL_MS
    const val TOTAL_QUESTIONS = 15
}