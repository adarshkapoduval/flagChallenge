package com.adarsh.flag.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.adarsh.flag.ui.components.WarningAlert
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import com.adarsh.flag.R
import com.adarsh.flag.ui.theme.CardBg
import com.adarsh.flag.ui.theme.DarkBg
import com.adarsh.flag.ui.theme.Dimens
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

    //Initialize ONLY IF ViewModel has no value

    LaunchedEffect(scheduledEpoch) {
        if (scheduledEpoch == null && tempSelectedEpochMs == null) {
            tempSelectedEpochMs = System.currentTimeMillis()
        }
    }

    val onTimeChange: (Int, Int, Int) -> Unit = { hour, minute, second ->
        val date = pickedDate ?: LocalDate.now()
        val time = LocalTime.of(hour, minute, second)

        val localDateTime = LocalDateTime.of(date, time)
        val zone = ZoneId.systemDefault()
        val zdt: ZonedDateTime = ZonedDateTime.of(localDateTime, zone)
        val epochMs = zdt.toInstant().toEpochMilli()

        tempSelectedEpochMs = epochMs
    }

    // Has user changed the time?
    val hasStaged = tempSelectedEpochMs != scheduledEpoch

    val onSaveStaged: () -> Unit = {

        tempSelectedEpochMs?.let { epoch ->
            val now = System.currentTimeMillis()

            // ✅ Validate ONLY when saving
            if (epoch <= now) {
                alertConfig = AlertState(
                    title = "Invalid Date/Time",
                    message = "Please choose a future date/time"
                )
                showAlert = true
                return@let
            }
            viewModel.scheduleChallenge(epoch)
            showSuccess = true
            tempSelectedEpochMs = scheduledEpoch   // reset draft to saved value
        }
    }

    val onCancelStaged: () -> Unit = {
        tempSelectedEpochMs = scheduledEpoch
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
                onTimeChange = onTimeChange,
                hasStaged = hasStaged,
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
                onTimeChange = onTimeChange,
                hasStaged = hasStaged,
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
                        .size(Dimens.FlagHeight)
                        .shadow(Dimens.PaddingLarge, RoundedCornerShape(Dimens.PaddingExtraLarge), spotColor = GameGreen)
                        .clip(RoundedCornerShape(Dimens.PaddingExtraLarge)),
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
                                modifier = Modifier.size(Dimens.IconSizeHuge),
                                tint = GameGreen
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpacerLarge))
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
    onTimeChange: (Int, Int, Int) -> Unit,
    hasStaged: Boolean,
    isSaved: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.PaddingLarge),
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

            Spacer(modifier = Modifier.height(Dimens.SpacerExtraLarge))

            PickDateTime(
                hour = pickedTime?.hour ?: LocalTime.now().hour,
                minute = pickedTime?.minute ?: LocalTime.now().minute,
                second = pickedTime?.second ?: LocalTime.now().second,
                onTimeChange = onTimeChange
            )

            Spacer(modifier = Modifier.height(Dimens.SpacerLarge))

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
    onTimeChange: (Int, Int, Int) -> Unit,
    hasStaged: Boolean,
    isSaved: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.PaddingExtraLarge, vertical = Dimens.PaddingMedium),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacerExtraLarge),
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

            Spacer(modifier = Modifier.height(Dimens.SpacerExtraLarge))

            PickDateTime(
                hour = pickedTime?.hour ?: LocalTime.now().hour,
                minute = pickedTime?.minute ?: LocalTime.now().minute,
                second = pickedTime?.second ?: LocalTime.now().second,
                onTimeChange = onTimeChange
            )


        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ScheduleCard(pickedDate = pickedDate, pickedTime = pickedTime, isCompact = true, isSaved = isSaved)

            Spacer(modifier = Modifier.height(Dimens.SpacerMedium))

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
        modifier = Modifier.padding(top = if (isCompact) Dimens.PaddingZero else Dimens.PaddingExtraLarge)
    ) {
        Box(
            modifier = Modifier
                .offset(y = (-floatingOffset).dp)
                .shadow(Dimens.SpacerMediumLarge, shape = RoundedCornerShape(50), spotColor = GamePurple)
        ) {
            Image(
                painter = painterResource(id = R.drawable.flag),
                contentDescription = null,
                modifier = Modifier.size(if (isCompact) Dimens.IconSizeExtraLarge else Dimens.IconSizeUltra)
            )
        }

        Spacer(modifier = Modifier.height(if (isCompact) Dimens.SpacerMedium else Dimens.SpacerMediumLarge))

        Text(
            text = "FLAGS CHALLENGE",
            style = if (isCompact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 2.sp
        )

        Row(
            modifier = Modifier.padding(top = if (isCompact) Dimens.PaddingMediumSmall else Dimens.PaddingSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier
                    .size(if (isCompact) Dimens.SpacerMediumLarge else Dimens.SpacerLarge)
                    .scale(pulseScale),
                tint = GameYellow,
            )
            Spacer(modifier = Modifier.width(Dimens.PaddingMediumSmall))
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
                .padding(vertical = if (isCompact) Dimens.PaddingSmall else Dimens.SpacerMediumLarge)
                .shadow(Dimens.SpacerMedium, RoundedCornerShape(Dimens.SpacerExtraLarge), spotColor = GamePurple),
            colors = CardDefaults.cardColors(
                containerColor = CardBg
            ),
            shape = RoundedCornerShape(Dimens.SpacerExtraLarge)
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
                    modifier = Modifier.padding(if (isCompact) Dimens.SpacerExtraLarge else Dimens.PaddingLarge),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isSaved) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(GameGreen.copy(alpha = 0.2f), RoundedCornerShape(Dimens.PaddingSmall))
                                .padding(horizontal = Dimens.SpacerMedium, vertical = Dimens.PaddingMediumSmall)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GameGreen,
                                modifier = Modifier.size(Dimens.SpacerMediumLarge)
                            )
                            Spacer(modifier = Modifier.width(Dimens.PaddingMediumSmall))
                            Text(
                                text = "LOCKED IN",
                                style = MaterialTheme.typography.labelMedium,
                                color = GameGreen,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(Dimens.SpacerMedium))

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
                        Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
                        Text(
                            text = "Press SAVE to lock in your schedule",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(Dimens.SpacerLarge))

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
                                    RoundedCornerShape(Dimens.CardCornerRadiusMedium)
                                )
                                .padding(Dimens.PaddingMedium),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = GameBlue,
                                modifier = Modifier.size(if (isCompact) Dimens.PaddingLarge else Dimens.PaddingMediumLarge)
                            )
                            Spacer(modifier = Modifier.width(Dimens.SpacerMedium))
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
                        Spacer(modifier = Modifier.height(Dimens.SpacerMedium))
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
                                    RoundedCornerShape(Dimens.CardCornerRadiusMedium)
                                )
                                .padding(Dimens.PaddingMedium),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = GamePink,
                                modifier = Modifier.size(if (isCompact) Dimens.PaddingLarge else Dimens.PaddingMediumLarge)
                            )
                            Spacer(modifier = Modifier.width(Dimens.SpacerMedium))
                            Text(
                                text = pickedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
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
private fun PickDateTime(
    hour: Int,
    minute: Int,
    second: Int,
    onTimeChange: (Int, Int, Int) -> Unit
) {
    var currentHour by remember { mutableIntStateOf(hour) }
    var currentMinute by remember { mutableIntStateOf(minute) }
    var currentSecond by remember { mutableIntStateOf(second) }

    //Sync when parent values change
    LaunchedEffect(hour, minute, second) {
        currentHour = hour
        currentMinute = minute
        currentSecond = second
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hour
        TimeBox(
            value = currentHour,
            maxValue = 23,
            label = "HOUR",
            onValueChange = {
                currentHour = it
                onTimeChange(currentHour, currentMinute, currentSecond)
            }
        )

        Spacer(modifier = Modifier.width(Dimens.SpacerMedium))

        Text(
            text = ":",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.width(Dimens.SpacerMedium))

        // Minute
        TimeBox(
            value = currentMinute,
            maxValue = 59,
            label = "MIN",
            onValueChange = {
                currentMinute = it
                onTimeChange(currentHour, currentMinute, currentSecond)
            }
        )

        Spacer(modifier = Modifier.width(Dimens.SpacerMedium))

        Text(
            text = ":",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.width(Dimens.SpacerMedium))

        // Second
        TimeBox(
            value = currentSecond,
            maxValue = 59,
            label = "SEC",
            onValueChange = {
                currentSecond = it
                onTimeChange(currentHour, currentMinute, currentSecond)
            }
        )
    }
}

@Composable
private fun TimeBox(
    value: Int,
    maxValue: Int,
    label: String,
    onValueChange: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(Dimens.PaddingSmall))

        Box(
            modifier = Modifier
                .size(Dimens.IconSizeUltra)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(Dimens.SpacerMedium)
                )
                .border(
                    width = Dimens.BorderWidth,
                    brush = Brush.horizontalGradient(
                        colors = listOf(GamePurple, GamePink, GameBlue)
                    ),
                    shape = RoundedCornerShape(Dimens.SpacerMedium)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Up arrow
                IconButton(
                    onClick = {
                        onValueChange(if (value >= maxValue) 0 else value + 1)
                    },
                    modifier = Modifier.size(Dimens.PaddingLarge)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Increase",
                        tint = GamePurple
                    )
                }

                // Value display
                Text(
                    text = value.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Down arrow
                IconButton(
                    onClick = {
                        onValueChange(if (value <= 0) maxValue else value - 1)
                    },
                    modifier = Modifier.size(Dimens.PaddingLarge)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Decrease",
                        tint = GamePurple
                    )
                }
            }
        }
    }
}
@Composable
private fun SaveCancelRow(hasStaged: Boolean, onSave: () -> Unit, onCancel: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacerMediumLarge)
    ) {
        // Cancel Button
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .height(Dimens.ButtonHeightLarge)
                .shadow(
                    elevation = if (hasStaged) Dimens.PaddingMediumSmall else Dimens.PaddingZero,
                    shape = RoundedCornerShape(Dimens.SpacerMediumLarge),
                    spotColor = Color.Red.copy(alpha = 0.3f)
                ),
            enabled = hasStaged,
            shape = RoundedCornerShape(Dimens.SpacerMediumLarge),
            border = BorderStroke(
                width = Dimens.BorderWidth,
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
                modifier = Modifier.size(Dimens.IconSizeMedium)
            )
            Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
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
                .height(Dimens.ButtonHeightLarge)
                .shadow(
                    elevation = if (hasStaged) Dimens.ShadowElevation else Dimens.PaddingZero,
                    shape = RoundedCornerShape(Dimens.SpacerMediumLarge),
                    spotColor = GameGreen
                ),
            enabled = hasStaged,
            shape = RoundedCornerShape(Dimens.SpacerMediumLarge),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContainerColor = Color.Gray.copy(alpha = 0.2f),
                disabledContentColor = Color.Gray.copy(alpha = 0.4f)
            ),
            contentPadding = PaddingValues(Dimens.PaddingZero)
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
                        modifier = Modifier.size(Dimens.IconSizeMedium)
                    )
                    Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
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