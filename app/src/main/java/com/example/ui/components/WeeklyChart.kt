package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.MutedGrey
import com.example.ui.theme.PaleSage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SageGreen
import com.example.ui.theme.WarmBorder
import com.example.ui.viewmodel.DayStats

@Composable
fun WeeklyActivityChart(
    dayStats: List<DayStats>,
    modifier: Modifier = Modifier
) {
    val maxCount = (dayStats.maxOfOrNull { it.completedTasksCount } ?: 1).coerceAtLeast(4)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PureWhite)
            .border(1.dp, WarmBorder, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Activity",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = DeepCharcoal
                    )
                )
                Text(
                    text = "Completed tasks",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MutedGrey
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bars Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                dayStats.forEach { day ->
                    val fillFraction = (day.completedTasksCount.toFloat() / maxCount.toFloat()).coerceIn(0.08f, 1f)

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (day.completedTasksCount > 0) {
                            Text(
                                text = "${day.completedTasksCount}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (day.isToday) SageGreen else MutedGrey
                                ),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Bar background container
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(90.dp)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                .background(PaleSage.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Active filled bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(fillFraction)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                    .background(if (day.completedTasksCount > 0) SageGreen else PaleSage)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Day label
                        Text(
                            text = day.dayLabel.take(3),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (day.isToday) DeepCharcoal else MutedGrey,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
