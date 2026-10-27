package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.HabitEntity
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.MutedGrey
import com.example.ui.theme.PaleSage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SageGreen
import com.example.ui.theme.WarmBorder

@Composable
fun CreateHabitDialog(
    isVisible: Boolean,
    habitToEdit: HabitEntity?,
    onSave: (name: String, category: String, iconName: String, targetDays: Int) -> Unit,
    onClose: () -> Unit
) {
    if (!isVisible) return

    var name by remember(habitToEdit) { mutableStateOf(habitToEdit?.name ?: "") }
    var category by remember(habitToEdit) { mutableStateOf(habitToEdit?.category ?: "Health") }
    var targetDays by remember(habitToEdit) { mutableIntStateOf(habitToEdit?.targetDaysPerWeek ?: 7) }

    AlertDialog(
        onDismissRequest = onClose,
        containerColor = PureWhite,
        shape = RoundedCornerShape(18.dp),
        title = {
            Text(
                text = if (habitToEdit == null) "New Habit" else "Edit Habit",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = DeepCharcoal
                )
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit name") },
                    placeholder = { Text("e.g. Read 15 minutes") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SageGreen,
                        unfocusedBorderColor = WarmBorder
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = DeepCharcoal
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Health", "Mindfulness", "Study", "Personal").forEach { cat ->
                        val isSelected = category.equals(cat, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PaleSage else PureWhite)
                                .border(1.dp, if (isSelected) SageGreen else WarmBorder, RoundedCornerShape(8.dp))
                                .clickable { category = cat }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) SageGreen else DeepCharcoal
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Weekly Goal",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = DeepCharcoal
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf(Pair("Every day", 7), Pair("5 days", 5), Pair("3 days", 3)).forEach { (label, count) ->
                        val isSelected = targetDays == count
                        Box(
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SageGreen else PureWhite)
                                .border(1.dp, if (isSelected) SageGreen else WarmBorder, RoundedCornerShape(8.dp))
                                .clickable { targetDays = count }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) PureWhite else DeepCharcoal
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), category, "leaf", targetDays)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SageGreen,
                    contentColor = PureWhite
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_habit_button")
            ) {
                Text(if (habitToEdit == null) "Create" else "Save")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onClose,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel", color = MutedGrey)
            }
        }
    )
}
