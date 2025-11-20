package com.adarsh.flag.domain.constants

object ChallengeConstants {
    const val PRE_START_MS = 20_000L
    const val QUESTION_MS = 30_000L
    const val INTERVAL_MS = 10_000L
    const val CYCLE_MS = QUESTION_MS + INTERVAL_MS
    const val TOTAL_QUESTIONS = 15
    const val SUCCESS_PERCENTAGE = 80 // up to 100
}