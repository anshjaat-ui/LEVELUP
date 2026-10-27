package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.MutedBlue
import com.example.ui.theme.MutedGrey
import com.example.ui.theme.PaleSage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SoftSand
import com.example.ui.theme.WarmBorder

@Composable
fun TaskItem(
    task: TaskEntity,
    onToggleCompletion: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val checkColor by animateColorAsState(
        targetValue = if (task.isCompleted) SageGreen else Color.Transparent,
        label = "task_check_color"
    )

    val checkBorderColor by animateColorAsState(
        targetValue = if (task.isCompleted) SageGreen else MutedGrey.copy(alpha = 0.5f),
        label = "task_check_border"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PureWhite)
            .border(1.dp, WarmBorder, RoundedCornerShape(14.dp))
            .clickable { onEdit() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("task_item_${task.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Checkbox
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(checkColor)
                    .border(1.5.dp, checkBorderColor, CircleShape)
                    .clickable { onToggleCompletion() }
                    .testTag("task_checkbox_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = PureWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Task Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = if (task.isCompleted) MutedGrey else DeepCharcoal,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MutedGrey
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Subtitle metadata: Category, Time, Priority
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category chip
                    CategoryChip(category = task.category)

                    if (!task.dueTime.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = MutedGrey,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = task.dueTime,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MutedGrey,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Priority indicator dot
                    Spacer(modifier = Modifier.width(8.dp))
                    PriorityDot(priority = task.priority)
                }
            }

            // Quick Delete Icon
            IconButton(
                onClick = { onDelete() },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("delete_task_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete task",
                    tint = MutedGrey.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryChip(category: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (category.lowercase()) {
        "study" -> Pair(SoftSand, DeepCharcoal)
        "work" -> Pair(Color(0xFFE2EAF1), DeepCharcoal)
        "health" -> Pair(PaleSage, SageGreen)
        else -> Pair(WarmBorder.copy(alpha = 0.5f), MutedGrey)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        )
    }
}

@Composable
fun PriorityDot(priority: String, modifier: Modifier = Modifier) {
    val (dotColor, label) = when (priority.lowercase()) {
        "high" -> Pair(Color(0xFFD97768), "High")
        "medium" -> Pair(Color(0xFFD4A373), "Med")
        else -> Pair(MutedBlue, "Low")
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 10.sp,
                color = MutedGrey
            )
        )
    }
}
