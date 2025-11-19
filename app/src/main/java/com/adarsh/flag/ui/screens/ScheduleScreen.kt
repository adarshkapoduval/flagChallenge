package com.adarsh.flag.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adarsh.flag.ui.state.AlertState
import com.adarsh.flag.vm.ChallengeViewModel
import com.yourapp.ui.components.WarningAlert
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import com.adarsh.flag.R
import com.adarsh.flag.ui.theme.CardBg
import com.adarsh.flag.ui.theme.DarkBg
import com.adarsh.flag.ui.theme.GameBlue
import com.adarsh.flag.ui.theme.GameGreen
import com.adarsh.flag.ui.theme.GamePink
import com.adarsh.flag.ui.theme.GamePurple
import com.adarsh.flag.ui.theme.GameYellow
import java.time.Instant




@Composable
fun ScheduleScreen(
    viewModel: ChallengeViewModel,
    modifier: Modifier = Modifier
) {
    var showAlert by remember { mutableStateOf(false) }
    var alertConfig by remember { mutableStateOf(AlertState()) }

    val ctx = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val scheduledEpoch by viewModel.scheduledEpoch.collectAsState()
    var tempSelectedEpochMs by rememberSaveable { mutableStateOf<Long?>(null) }

    val displayEpoch: Long? = tempSelectedEpochMs ?: scheduledEpoch

    val pickedDate: LocalDate? = displayEpoch?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val pickedTime: LocalTime? = displayEpoch?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalTime()
    }

    var showSuccess by remember { mutableStateOf(false) }

    // Floating animation for icons
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            kotlinx.coroutines.delay(2000)
            showSuccess = false
        }
    }

    val onPickDateTime: () -> Unit = {
        showDatePicker(ctx, onDateSelected = { date ->
            showTimePicker(ctx, onTimeSelected = { time ->
                val localDateTime = LocalDateTime.of(date, time)
                val zone = ZoneId.systemDefault()
                val zdt: ZonedDateTime = ZonedDateTime.of(localDateTime, zone)
                val epochMs = zdt.toInstant().toEpochMilli()

                val now = System.currentTimeMillis()
                if (epochMs <= now) {
                    alertConfig = AlertState(
                        title = "Invalid Date/Time",
                        message = "Please choose a future date/time"
                    )
                    showAlert = true
                    return@showTimePicker
                }

                tempSelectedEpochMs = epochMs
            })
        })
    }

    val onSaveStaged: () -> Unit = {
        tempSelectedEpochMs?.let { epoch ->
            viewModel.scheduleChallenge(epoch)
            showSuccess = true
            tempSelectedEpochMs = null
        }
    }

    val onCancelStaged: () -> Unit = {
        tempSelectedEpochMs = null
    }

    val isSaved = scheduledEpoch != null

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBg,
                        Color(0xFF1E1B4B),
                        DarkBg
                    )
                )
            )
    ) {
        if (isLandscape) {
            LandscapeLayout(
                pickedDate = pickedDate,
                pickedTime = pickedTime,
                pulseScale = pulseScale,
                floatingOffset = floatingOffset,
                onPickDateTime = onPickDateTime,
                hasStaged = tempSelectedEpochMs != null,
                isSaved = isSaved,
                onSave = onSaveStaged,
                onCancel = onCancelStaged
            )
        } else {
            PortraitLayout(
                pickedDate = pickedDate,
                pickedTime = pickedTime,
                pulseScale = pulseScale,
                floatingOffset = floatingOffset,
                onPickDateTime = onPickDateTime,
                hasStaged = tempSelectedEpochMs != null,
                isSaved = isSaved,
                onSave = onSaveStaged,
                onCancel = onCancelStaged
            )
        }

        // Epic Success Animation
        AnimatedVisibility(
            visible = showSuccess,
            enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .size(240.dp)
                        .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = GameGreen)
                        .clip(RoundedCornerShape(32.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBg
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        GameGreen.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(100.dp),
                                tint = GameGreen
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "SCHEDULED!",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "Challenge Locked In",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GameGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAlert) {
        WarningAlert(
            title = alertConfig.title,
            message = alertConfig.message,
            buttonText = alertConfig.buttonText,
            onDismiss = { showAlert = false }
        )
    }
}

@Composable
private fun PortraitLayout(
    pickedDate: LocalDate?,
    pickedTime: LocalTime?,
    pulseScale: Float,
    floatingOffset: Float,
    onPickDateTime: () -> Unit,
    hasStaged: Boolean,
    isSaved: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HeaderSection(pulseScale = pulseScale, floatingOffset = floatingOffset, isCompact = false)

        Column(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScheduleCard(pickedDate = pickedDate, pickedTime = pickedTime, isCompact = false, isSaved = isSaved)

            Spacer(modifier = Modifier.height(32.dp))

            PickDateTimeButton(onPickDateTime = onPickDateTime)

            Spacer(modifier = Modifier.height(16.dp))

            SaveCancelRow(
                hasStaged = hasStaged,
                onSave = onSave,
                onCancel = onCancel
            )
        }
    }
}

@Composable
private fun LandscapeLayout(
    pickedDate: LocalDate?,
    pickedTime: LocalTime?,
    pulseScale: Float,
    floatingOffset: Float,
    onPickDateTime: () -> Unit,
    hasStaged: Boolean,
    isSaved: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HeaderSection(pulseScale = pulseScale, floatingOffset = floatingOffset, isCompact = true)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ScheduleCard(pickedDate = pickedDate, pickedTime = pickedTime, isCompact = true, isSaved = isSaved)

            Spacer(modifier = Modifier.height(20.dp))

            PickDateTimeButton(onPickDateTime = onPickDateTime)

            Spacer(modifier = Modifier.height(12.dp))

            SaveCancelRow(
                hasStaged = hasStaged,
                onSave = onSave,
                onCancel = onCancel
            )
        }
    }
}

@Composable
private fun HeaderSection(pulseScale: Float, floatingOffset: Float, isCompact: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = if (isCompact) 0.dp else 32.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(y = (-floatingOffset).dp)
                .shadow(16.dp, shape = RoundedCornerShape(50), spotColor = GamePurple)
        ) {
            Image(
                painter = painterResource(id = R.drawable.flag),
                contentDescription = null,
                modifier = Modifier.size(if (isCompact) 64.dp else 80.dp)
            )
        }

        Spacer(modifier = Modifier.height(if (isCompact) 12.dp else 16.dp))

        Text(
            text = "FLAGS CHALLENGE",
            style = if (isCompact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 2.sp
        )

        Row(
            modifier = Modifier.padding(top = if (isCompact) 6.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier
                    .size(if (isCompact) 16.dp else 18.dp)
                    .scale(pulseScale),
                tint = GameYellow,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "SET YOUR PERFECT START TIME",
                style = if (isCompact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                color = GameYellow,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun ScheduleCard(
    pickedDate: LocalDate?,
    pickedTime: LocalTime?,
    isCompact: Boolean,
    isSaved: Boolean
) {
    AnimatedVisibility(
        visible = pickedDate != null || pickedTime != null,
        enter = slideInVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isCompact) 8.dp else 16.dp)
                .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = GamePurple),
            colors = CardDefaults.cardColors(
                containerColor = CardBg
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                GamePurple.copy(alpha = 0.2f),
                                GamePink.copy(alpha = 0.1f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.padding(if (isCompact) 20.dp else 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isSaved) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(GameGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GameGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LOCKED IN",
                                style = MaterialTheme.typography.labelMedium,
                                color = GameGreen,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Challenge begins at:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "⚠️ NOT SAVED YET",
                            style = MaterialTheme.typography.labelLarge,
                            color = GameYellow,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Press SAVE to lock in your schedule",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Date Display
                    if (pickedDate != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            GameBlue.copy(alpha = 0.3f),
                                            GamePurple.copy(alpha = 0.3f)
                                        )
                                    ),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = GameBlue,
                                modifier = Modifier.size(if (isCompact) 24.dp else 28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = pickedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                style = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Time Display
                    if (pickedTime != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            GamePink.copy(alpha = 0.3f),
                                            GamePurple.copy(alpha = 0.3f)
                                        )
                                    ),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = GamePink,
                                modifier = Modifier.size(if (isCompact) 24.dp else 28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = pickedTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
                                style = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PickDateTimeButton(onPickDateTime: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }

    Button(
        onClick = {
            isPressed = true
            onPickDateTime()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .shadow(
                elevation = if (isPressed) 4.dp else 12.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = GamePurple
            )
            .scale(if (isPressed) 0.98f else 1f),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            GamePurple,
                            GamePink,
                            GameBlue
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "PICK DATE & TIME",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun SaveCancelRow(hasStaged: Boolean, onSave: () -> Unit, onCancel: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cancel Button
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .shadow(
                    elevation = if (hasStaged) 6.dp else 0.dp,
                    shape = RoundedCornerShape(14.dp),
                    spotColor = Color.Red.copy(alpha = 0.3f)
                ),
            enabled = hasStaged,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(
                width = 2.dp,
                color = if (hasStaged) Color(0xFFEF4444) else Color.Gray.copy(alpha = 0.3f)
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (hasStaged) Color(0xFFEF4444).copy(alpha = 0.1f) else Color.Transparent,
                contentColor = if (hasStaged) Color(0xFFEF4444) else Color.Gray,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.Gray.copy(alpha = 0.3f)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CANCEL",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Save Button
        Button(
            onClick = onSave,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .shadow(
                    elevation = if (hasStaged) 10.dp else 0.dp,
                    shape = RoundedCornerShape(14.dp),
                    spotColor = GameGreen
                ),
            enabled = hasStaged,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContainerColor = Color.Gray.copy(alpha = 0.2f),
                disabledContentColor = Color.Gray.copy(alpha = 0.4f)
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (hasStaged) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    GameGreen,
                                    Color(0xFF059669)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Gray.copy(alpha = 0.2f),
                                    Color.Gray.copy(alpha = 0.2f)
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SAVE",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

private fun showDatePicker(context: android.content.Context, onDateSelected: (LocalDate) -> Unit) {
    val now = java.util.Calendar.getInstance()
    val y = now.get(java.util.Calendar.YEAR)
    val m = now.get(java.util.Calendar.MONTH)
    val d = now.get(java.util.Calendar.DAY_OF_MONTH)

    val dpd = DatePickerDialog(context, { _, year, month, dayOfMonth ->
        val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
        onDateSelected(selectedDate)
    }, y, m, d)

    dpd.datePicker.minDate = System.currentTimeMillis()
    dpd.show()
}

private fun showTimePicker(context: android.content.Context, onTimeSelected: (LocalTime) -> Unit) {
    val now = java.util.Calendar.getInstance()
    val hour = now.get(java.util.Calendar.HOUR_OF_DAY)
    val minute = now.get(java.util.Calendar.MINUTE)

    val tpd = TimePickerDialog(context, { _, h, min ->
        onTimeSelected(LocalTime.of(h, min))
    }, hour, minute, true)
    tpd.show()
}