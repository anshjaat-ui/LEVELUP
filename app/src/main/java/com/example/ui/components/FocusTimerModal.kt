package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.MutedGrey
import com.example.ui.theme.PaleSage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SageGreen
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmIvory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerModal(
    isVisible: Boolean,
    isRunning: Boolean,
    secondsLeft: Int,
    totalSeconds: Int,
    timerMode: String,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    onSetPreset: (minutes: Int) -> Unit,
    onClose: () -> Unit
) {
    if (!isVisible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = WarmIvory
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .testTag("focus_timer_sheet"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Focus Session",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = DeepCharcoal
                    )
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close timer",
                        tint = MutedGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf(15, 25, 45, 60).forEach { mins ->
                    val isSelected = totalSeconds == mins * 60
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) SageGreen else PureWhite)
                            .border(
                                1.dp,
                                if (isSelected) SageGreen else WarmBorder,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { onSetPreset(mins) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "${mins}m",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) PureWhite else DeepCharcoal
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Circular Timer
            val progress = if (totalSeconds > 0) {
                (secondsLeft.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
            } else 0f
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                label = "timer_progress"
            )

            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(220.dp)) {
                    val strokeWidth = 8.dp.toPx()
                    // Track circle
                    drawCircle(
                        color = PaleSage,
                        radius = (size.minDimension - strokeWidth) / 2,
                        style = Stroke(width = strokeWidth)
                    )
                    // Active arc
                    drawArc(
                        color = SageGreen,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Countdown Text
                val minutes = secondsLeft / 60
                val seconds = secondsLeft % 60
                val formattedTime = String.format("%02d:%02d", minutes, seconds)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 44.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepCharcoal,
                            letterSpacing = (-1).sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isRunning) "Deep focus in progress" else "Ready when you are",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MutedGrey
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Play / Pause and Reset Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Reset Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PureWhite)
                        .border(1.dp, WarmBorder, CircleShape)
                        .clickable { onReset() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset timer",
                        tint = MutedGrey
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Primary Start/Pause Button
                Button(
                    onClick = onToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SageGreen,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .width(140.dp)
                        .testTag("timer_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "Pause" else "Start",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SessionCompletedDialog(
    isVisible: Boolean,
    xpEarned: Int,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        shape = RoundedCornerShape(18.dp),
        title = {
            Text(
                text = "Session Complete",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = DeepCharcoal
                )
            )
        },
        text = {
            Column {
                Text(
                    text = "Wonderful focus work. Your steady dedication contributes directly to your natural clarity and momentum.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MutedGrey
                    )
                )
                if (xpEarned > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PaleSage)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+$xpEarned XP rewarded",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = SageGreen
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SageGreen,
                    contentColor = PureWhite
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Continue")
            }
        }
    )
}
