package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Timer
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
import com.example.data.model.Achievement
import com.example.ui.components.WeeklyActivityChart
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.MutedGrey
import com.example.ui.theme.PaleSage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SageGreen
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmIvory
import com.example.ui.viewmodel.UiState

@Composable
fun ProgressScreen(
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
            .padding(horizontal = 20.dp)
            .testTag("progress_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                Text(
                    text = "Progress",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = DeepCharcoal
                    )
                )
                Text(
                    text = "Reflect on your consistency and steady growth.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MutedGrey
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Discreet Level & XP Card
        item {
            val level = uiState.userProfile.level
            val xp = uiState.userProfile.xp
            val xpInCurrentLevel = xp % 100
            val progressFraction = (xpInCurrentLevel.toFloat() / 100f).coerceIn(0f, 1f)
            val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "xp_progress")
            val xpToNext = 100 - xpInCurrentLevel

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(PureWhite)
                    .border(1.dp, WarmBorder, RoundedCornerShape(18.dp))
                    .padding(20.dp)
                    .testTag("progress_level_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PaleSage)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Level $level",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = SageGreen
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "$xp XP total",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MutedGrey
                                )
                            )
                        }

                        Text(
                            text = "$xpToNext XP to Level ${level + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MutedGrey
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SageGreen,
                        trackColor = PaleSage,
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Subtle motivation: XP rewards thoughtful task completion and deep work blocks.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MutedGrey,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // 4 Key Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Tasks Today",
                        value = "${uiState.todayCompletedTasks}",
                        subtitle = "of ${uiState.todayTotalTasks} planned",
                        icon = Icons.Outlined.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Tasks This Week",
                        value = "${uiState.weekCompletedTasks}",
                        subtitle = "finished on time",
                        icon = Icons.Outlined.TaskAlt,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Focus Time",
                        value = "${uiState.weekFocusMinutes}m",
                        subtitle = "logged this week",
                        icon = Icons.Outlined.Timer,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Habit Consistency",
                        value = "${uiState.weekHabitConsistencyPercent}%",
                        subtitle = "on-track rate",
                        icon = Icons.Outlined.Eco,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Weekly Activity Chart
        item {
            WeeklyActivityChart(dayStats = uiState.weeklyDayStats)
        }

        // Achievements Section
        item {
            Text(
                text = "Milestones & Consistency",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = DeepCharcoal
                ),
                modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
            )
        }

        items(uiState.achievements, key = { it.id }) { achievement ->
            AchievementRow(achievement = achievement)
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PureWhite)
            .border(1.dp, WarmBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MutedGrey,
                        fontWeight = FontWeight.Medium
                    )
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SageGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = DeepCharcoal
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MutedGrey,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun AchievementRow(
    achievement: Achievement,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PureWhite)
            .border(1.dp, WarmBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (achievement.isUnlocked) PaleSage else WarmBorder.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (achievement.isUnlocked) Icons.Outlined.Spa else Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = if (achievement.isUnlocked) SageGreen else MutedGrey,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = achievement.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = DeepCharcoal
                        )
                    )
                    if (achievement.isUnlocked) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(PaleSage)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Unlocked",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SageGreen
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MutedGrey
                    )
                )
            }

            // Progress or reward tag
            Text(
                text = "${achievement.currentCount}/${achievement.targetCount}",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = if (achievement.isUnlocked) SageGreen else MutedGrey
                )
            )
        }
    }
}
