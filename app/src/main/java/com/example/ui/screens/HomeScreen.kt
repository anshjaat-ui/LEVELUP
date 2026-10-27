package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.components.TaskItem
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.MutedGrey
import com.example.ui.theme.PaleSage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SageGreen
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmIvory
import com.example.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: UiState,
    onAddTask: () -> Unit,
    onAddHabit: () -> Unit,
    onStartFocus: () -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val greeting = rememberGreeting()
    val dateString = rememberFormattedDate()

    val todayTasks = uiState.tasks.filter { task ->
        val due = task.dueDateMillis
        due == null || isTodayOrOverdue(due, task.isCompleted)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
            .padding(horizontal = 20.dp)
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming Header
        item {
            Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                Text(
                    text = dateString.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MutedGrey,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$greeting.",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = DeepCharcoal
                    )
                )
            }
        }

        // Daily Progress Card
        item {
            val total = todayTasks.size
            val completed = todayTasks.count { it.isCompleted }
            val fraction = if (total > 0) (completed.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
            val animatedFraction by animateFloatAsState(targetValue = fraction, label = "daily_progress")

            val encouragement = when {
                total == 0 -> "Take a mindful moment to plan your day."
                completed == total -> "All set for today. Wonderful clarity achieved."
                completed >= (total / 2) -> "You're making steady, meaningful progress."
                else -> "One focused step at a time."
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(PureWhite)
                    .border(1.dp, WarmBorder, RoundedCornerShape(18.dp))
                    .padding(20.dp)
                    .testTag("home_daily_progress_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's focus",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = DeepCharcoal
                            )
                        )
                        Text(
                            text = "$completed of $total tasks completed",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MutedGrey,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { animatedFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SageGreen,
                        trackColor = PaleSage,
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = encouragement,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MutedGrey
                        )
                    )
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Outlined.Add,
                    label = "Add task",
                    onClick = onAddTask,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_quick_add_task")
                )
                QuickActionButton(
                    icon = Icons.Outlined.Eco,
                    label = "Add habit",
                    onClick = onAddHabit,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_quick_add_habit")
                )
                QuickActionButton(
                    icon = Icons.Outlined.Timer,
                    label = "Focus",
                    onClick = onStartFocus,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_quick_focus")
                )
            }
        }

        // Section Title: Today's Tasks
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scheduled for today",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = DeepCharcoal
                    )
                )
                if (todayTasks.isNotEmpty()) {
                    Text(
                        text = "${todayTasks.count { !it.isCompleted }} pending",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MutedGrey
                        )
                    )
                }
            }
        }

        // Tasks list or Empty State
        if (todayTasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PureWhite)
                        .border(1.dp, WarmBorder, RoundedCornerShape(16.dp))
                        .padding(28.dp)
                        .testTag("home_empty_state"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PaleSage),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Spa,
                                contentDescription = null,
                                tint = SageGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Clear and calm",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = DeepCharcoal
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No pending tasks for today. Enjoy the clarity or plan a gentle start.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MutedGrey
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddTask,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SageGreen,
                                contentColor = PureWhite
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create a task")
                        }
                    }
                }
            }
        } else {
            items(todayTasks, key = { it.id }) { task ->
                TaskItem(
                    task = task,
                    onToggleCompletion = { onToggleTask(task) },
                    onEdit = { onEditTask(task) },
                    onDelete = { onDeleteTask(task.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(PureWhite)
            .border(1.dp, WarmBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SageGreen,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = DeepCharcoal,
                    fontSize = 12.sp
                )
            )
        }
    }
}

private fun isTodayOrOverdue(dueDateMillis: Long, isCompleted: Boolean): Boolean {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val todayStart = cal.timeInMillis
    val todayEnd = todayStart + 24L * 60L * 60L * 1000L - 1L

    return (dueDateMillis in todayStart..todayEnd) || (dueDateMillis < todayStart && !isCompleted)
}

@Composable
private fun rememberGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
}

@Composable
private fun rememberFormattedDate(): String {
    val formatter = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
    return formatter.format(Date())
}
