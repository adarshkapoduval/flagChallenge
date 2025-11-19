package com.adarsh.flag.vm

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adarsh.flag.data.local.entity.AnswerEntity
import com.adarsh.flag.domain.constants.ChallengeConstants
import com.adarsh.flag.domain.model.enums.Phase
import com.adarsh.flag.receiver.StartGameReceiver
import com.adarsh.flag.repository.ChallengeRepository
import com.adarsh.flag.ui.state.UiQuestion
import com.adarsh.flag.ui.state.UiState
import com.adarsh.flag.utils.computeChallengeState
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val repo: ChallengeRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private var tickerJob: Job? = null

    // Keep track of last phase to detect transitions
    private var lastPhase: Phase = Phase.NOT_NEAR

    // Keep a set of question indexes already finalized (so we don't finalize twice)
    private val finalizedQuestions = mutableSetOf<Int>()

    private val _scheduledEpoch = MutableStateFlow<Long?>(null)
    val scheduledEpoch: StateFlow<Long?> = _scheduledEpoch


    init {
        viewModelScope.launch {
                repo.getScheduledStartTime()
                    .collect { value ->
                        _scheduledEpoch.value = value
                    }
        }

        viewModelScope.launch {
            repo.getAllQuestionsFlow().collect { list ->
                val uiQs = list.map {
                    UiQuestion(
                        it.questionIndex,
                        it.countryCode,
                        it.correctAnswerId,
                        it.optionsJson
                    )
                }
                _ui.update { it.copy(questions = uiQs) }
            }
        }
    }

    fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                updateFromSchedule()
                delay(300L)
            }
        }
    }

    fun stopTicker() {
        tickerJob?.cancel()
    }

    private suspend fun handlePhaseTransition(old: Phase, new: Phase, questionIndex: Int?) {
        // Only finalize when we move from QUESTION -> INTERVAL for a particular question index
        if (old == Phase.QUESTION && new == Phase.INTERVAL && questionIndex != null) {
            finalizeQuestionIfNeeded(questionIndex)
        }
    }

    // finalize: ensure an AnswerEntity exists for questionIndex and isCorrect is set appropriately
    private suspend fun finalizeQuestionIfNeeded(questionIndex: Int) {
        if (finalizedQuestions.contains(questionIndex)) return

        // fetch question + any saved answer
        val question = withContext(Dispatchers.IO) { repo.getQuestion(questionIndex) } ?: run {
            finalizedQuestions.add(questionIndex)
            return
        }

        val answer = withContext(Dispatchers.IO) { repo.getAnswer(questionIndex) }
        if (answer == null) {
            // user did not answer — record as wrong (-1 or 0 as selectedOptionId)
            val newAnswer = AnswerEntity(
                questionIndex = questionIndex,
                selectedOptionId = -1, // indicates no selection
                answeredAtMs = System.currentTimeMillis(),
                isCorrect = false
            )
            withContext(Dispatchers.IO) { repo.saveAnswer(newAnswer) }
        } else {
            // user answered — ensure isCorrect flag is accurate (in case answer saved earlier without correctness or we want to update)
            val isCorrect = answer.selectedOptionId == question.correctAnswerId
            if (answer.isCorrect != isCorrect) {
                val updated = answer.copy(isCorrect = isCorrect)
                withContext(Dispatchers.IO) { repo.saveAnswer(updated) }
            }
        }

        finalizedQuestions.add(questionIndex)
    }

    private suspend fun updateFromSchedule() {
        val scheduled = _scheduledEpoch.value
        if (scheduled == null) {
            _ui.update { it.copy(phase = Phase.NOT_NEAR, currentQuestionIndex = null, remainingMs = 0L, finalScore = null) }
            lastPhase = Phase.NOT_NEAR
            return
        }

        // determine total questions dynamically from loaded questions
        val totalQuestions = _ui.value.questions.size.takeIf { it > 0 } ?: run {
            // fallback: try to get count from DB or use default constant
            val cnt = repo.questionsCount() // you added this earlier or use dao.count()
            if (cnt > 0) cnt else ChallengeConstants.TOTAL_QUESTIONS
        }

        val state = computeChallengeState(scheduled, totalQuestions)

        // handle transition/finalization (same as before)
        handlePhaseTransition(lastPhase, state.phase, state.questionIndex)
        lastPhase = state.phase

        // finished -> compute score (unchanged)
        if (state.phase == Phase.FINISHED) {
            if (_ui.value.finalScore == null) {
                val correctCount = repo.getCorrectAnswerCount()
                _ui.update { it.copy(phase = state.phase, currentQuestionIndex = null, remainingMs = 0L, finalScore = correctCount) }
                return
            }
        }

        _ui.update { it.copy(phase = state.phase, currentQuestionIndex = state.questionIndex, remainingMs = state.remainingMs) }
    }



    fun scheduleChallenge(ms: Long) {
        viewModelScope.launch {
            repo.saveScheduledStartTime(ms)
            repo.scheduleAlarm(ms)
            // update UI immediately from schedule
            updateFromSchedule() // call the suspend function to refresh UI
            startTicker()
        }
    }

    // 1) expose a suspend getter to load saved answer for a question
    suspend fun getSavedAnswer(questionIndex: Int): AnswerEntity? {
        return withContext(Dispatchers.IO) {
            repo.getAnswer(questionIndex)
        }
    }

    // 2) update selectOption to also update UI state immediately (so composable reacts)
    fun selectOption(questionIndex: Int, selectedOptionId: Int) {
        viewModelScope.launch {
            // persist
            val q = repo.getQuestion(questionIndex) ?: return@launch
            val isCorrect = selectedOptionId == q.correctAnswerId
            val answer = AnswerEntity(questionIndex, selectedOptionId, System.currentTimeMillis(), isCorrect)
            repo.saveAnswer(answer)

            // update UiState so composable can reflect saved selection immediately
            _ui.update { current ->
                // If UiState stores more per-question info in future, update accordingly.
                current.copy() // no structural change here, but composable will query repo for saved answer (see below).
            }
        }
    }

    fun resetChallenge() {
        viewModelScope.launch {
            // stop periodic updates while we clear
            stopTicker()

            // clear DB state
            repo.resetChallengeData()

            // update UI to reflect cleared state
            _ui.update {
                it.copy(
                    phase = Phase.NOT_NEAR,
                    currentQuestionIndex = null,
                    remainingMs = 0L,
                    finalScore = null
                    // optionally clear questions list or keep loaded questions
                )
            }
        }
    }
}