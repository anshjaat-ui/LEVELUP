package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.MutedGrey
import com.example.ui.theme.PaleSage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SageGreen
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmIvory
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskSheet(
    isVisible: Boolean,
    taskToEdit: TaskEntity?,
    onSave: (
        title: String,
        description: String,
        dueDateMillis: Long?,
        dueTime: String?,
        priority: String,
        category: String,
        isRecurring: Boolean,
        recurrenceRule: String
    ) -> Unit,
    onClose: () -> Unit
) {
    if (!isVisible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }

    var title by remember(taskToEdit) { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember(taskToEdit) { mutableStateOf(taskToEdit?.description ?: "") }
    var priority by remember(taskToEdit) { mutableStateOf(taskToEdit?.priority ?: "Medium") }
    var category by remember(taskToEdit) { mutableStateOf(taskToEdit?.category ?: "Personal") }
    var dueTime by remember(taskToEdit) { mutableStateOf(taskToEdit?.dueTime ?: "") }

    var dateOption by remember(taskToEdit) {
        mutableStateOf(if (taskToEdit?.dueDateMillis == null) "Today" else "Today")
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

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
                .testTag("edit_task_sheet")
        ) {
            Text(
                text = if (taskToEdit == null) "New Task" else "Edit Task",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = DeepCharcoal
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("What would you like to achieve?") },
                placeholder = { Text("e.g. Draft quarterly reflections") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SageGreen,
                    unfocusedBorderColor = WarmBorder,
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .testTag("task_title_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Optional note or sub-points") },
                maxLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SageGreen,
                    unfocusedBorderColor = WarmBorder,
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_desc_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Due Date Quick Select
            Text(
                text = "When",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = DeepCharcoal
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Today", "Tomorrow", "Someday").forEach { option ->
                    val isSelected = dateOption == option
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) SageGreen else PureWhite)
                            .border(1.dp, if (isSelected) SageGreen else WarmBorder, RoundedCornerShape(10.dp))
                            .clickable { dateOption = option }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) PureWhite else DeepCharcoal
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Priority Selector
            Text(
                text = "Priority",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = DeepCharcoal
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Low", "Medium", "High").forEach { p ->
                    val isSelected = priority.equals(p, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) PaleSage else PureWhite)
                            .border(1.dp, if (isSelected) SageGreen else WarmBorder, RoundedCornerShape(10.dp))
                            .clickable { priority = p }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = p,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) SageGreen else DeepCharcoal
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selector
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = DeepCharcoal
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Study", "Work", "Personal", "Health").forEach { cat ->
                    val isSelected = category.equals(cat, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) SageGreen else PureWhite)
                            .border(1.dp, if (isSelected) SageGreen else WarmBorder, RoundedCornerShape(10.dp))
                            .clickable { category = cat }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) PureWhite else DeepCharcoal
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Save / Cancel Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onClose,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MutedGrey)
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val now = System.currentTimeMillis()
                            val dueDate = when (dateOption) {
                                "Today" -> now
                                "Tomorrow" -> now + 24 * 60 * 60 * 1000
                                else -> null
                            }
                            onSave(
                                title.trim(),
                                description.trim(),
                                dueDate,
                                if (dueTime.isBlank()) null else dueTime,
                                priority,
                                category,
                                false,
                                "NONE"
                            )
                        }
                    },
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SageGreen,
                        contentColor = PureWhite
                    ),
                    modifier = Modifier.testTag("save_task_button")
                ) {
                    Text(if (taskToEdit == null) "Create Task" else "Save Changes")
                }
            }
        }
    }
}
