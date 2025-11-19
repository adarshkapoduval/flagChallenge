package com.example.mvvm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mvvm.domain.constants.ChallengeConstants
import com.example.mvvm.domain.model.Country
import com.example.mvvm.domain.model.enums.Phase
import com.example.mvvm.ui.state.UiQuestion
import com.example.mvvm.vm.ChallengeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Composable
fun QuestionScreen(viewModel: ChallengeViewModel) {
    val ui by viewModel.ui.collectAsState()
    val phase = ui.phase

    // local periodic UI refresh for visible countdown (non-authoritative)
    var displayRemaining by remember { mutableStateOf(ui.remainingMs) }
    LaunchedEffect(ui.remainingMs) {
        displayRemaining = ui.remainingMs
    }

    // safer ticker: runs while in QUESTION phase and stops when phase/time changes
    LaunchedEffect(ui.phase, ui.currentQuestionIndex, ui.remainingMs) {
        if (ui.phase == Phase.QUESTION && ui.remainingMs > 0L) {
            // Local loop that re-checks the authoritative state each tick
            while (viewModel.ui.value.phase == Phase.QUESTION && viewModel.ui.value.remainingMs > 0L) {
                displayRemaining = viewModel.ui.value.remainingMs
                delay(200L)
            }
        }
    }

    when (phase) {
        Phase.NOT_NEAR -> {
            ScheduleScreen(viewModel)
        }
        Phase.PRE_START -> {
            Text("Challenge starts in: ${formatMs(displayRemaining)}")
        }
        Phase.QUESTION -> {
            val idx = ui.currentQuestionIndex ?: 0
            val q = ui.questions.find { it.index == idx } ?: return
            QuestionContent(
                question = q,
                remainingMs = ui.remainingMs,
                displayRemainingMs = displayRemaining,
                viewModel = viewModel
            )
        }
        Phase.INTERVAL -> {
            Text("Next in: ${formatMs(ui.remainingMs)}")
        }
        Phase.FINISHED -> {
            Text("Challenge finished")
        }
    }
}

@Composable
private fun QuestionContent(
    question: UiQuestion,
    remainingMs: Long,
    displayRemainingMs: Long,
    viewModel: ChallengeViewModel
) {
    val scope = rememberCoroutineScope()
    val json = remember { Json { ignoreUnknownKeys = true } }

    // Parse optionsJson into list of CountryDto (stored earlier when populating DB)
    val options: List<Country> = remember(question.optionsJson) {
        try {
            json.decodeFromString(ListSerializer(Country.serializer()), question.optionsJson)
        } catch (t: Throwable) {
            // Fallback: 4 dummy options (shouldn't happen if DB populated correctly)
            listOf(
                Country(country_name = "Option 1", id = 1),
                Country(country_name = "Option 2", id = 2),
                Country(country_name = "Option 3", id = 3),
                Country(country_name = "Option 4", id = 4)
            )
        }
    }

    // Local selected state — will be restored from DB on enter
    var selectedId by remember { mutableStateOf<Int?>(null) }

    // Restore persisted answer for this question when composable enters or question changes
    LaunchedEffect(question.index) {
        try {
            val saved = viewModel.getSavedAnswer(question.index) // suspend call
            selectedId = saved?.selectedOptionId
        } catch (t: Throwable) {
            // ignore restore errors; selectedId stays null
            t.printStackTrace()
        }
    }

    // showResult becomes true when remainingMs <= 0 (question time expired)
    val showResult = remainingMs <= 0L

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Question ${question.index + 1}/${ChallengeConstants.TOTAL_QUESTIONS}")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Time left: ${formatMs(displayRemainingMs)}")
        Spacer(modifier = Modifier.height(12.dp))

        // Flag placeholder (replace with Coil Image if you have flag images)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = question.countryCode)
        }

        Spacer(modifier = Modifier.height(16.dp))

        val clickable = remainingMs > 0L

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { opt ->
                val isSelected = selectedId == opt.id
                val isCorrect = opt.id == question.correctAnswerId
                val showCorrectHighlight = showResult && isCorrect
                val showWrongHighlight = showResult && isSelected && !isCorrect

                // determine border color when showing result
                val borderColor = when {
                    showCorrectHighlight -> Color(0xFF2E7D32) // green
                    showWrongHighlight -> Color(0xFFC62828) // red
                    else -> null
                }

                OptionRow(
                    text = opt.country_name,
                    isSelected = isSelected,
                    isDisabled = !clickable,
                    borderColor = borderColor,
                    onClick = {
                        // immediate UI feedback
                        selectedId = opt.id

                        // persist selection in background
                        scope.launch {
                            viewModel.selectOption(question.index, opt.id)
                        }
                    }
                )

                // If showing result, show "Correct"/"Wrong" label under option that matches result
                if (showResult && (showCorrectHighlight || showWrongHighlight)) {
                    val label = if (showCorrectHighlight) "Correct" else "Wrong"
                    val color = if (showCorrectHighlight) Color(0xFF2E7D32) else Color(0xFFC62828)
                    Text(text = label, color = color, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
                }
            }
        }
    }
}

/**
 * Small local option row - works even if your OptionCard has different signature.
 * It accepts selection, disabled, and an optional border color (for result highlight).
 */
@Composable
private fun OptionRow(
    text: String,
    isSelected: Boolean,
    isDisabled: Boolean,
    borderColor: Color?,
    onClick: () -> Unit
) {
    val bg = if (isSelected) Color(0xFFE3F2FD) else Color.White
    val baseModifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .then(if (borderColor != null) Modifier.border(2.dp, borderColor, RoundedCornerShape(8.dp)) else Modifier)
        .background(bg, RoundedCornerShape(8.dp))

    val clickableModifier = if (!isDisabled) baseModifier.clickable { onClick() } else baseModifier.alpha(0.6f)

    Box(modifier = clickableModifier.padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
        Text(text = text)
    }
}

/** Milliseconds to "MM:SS" string */
fun formatMs(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0L)
    val m = s / 60
    val sec = s % 60
    return "%02d:%02d".format(m, sec)
}
