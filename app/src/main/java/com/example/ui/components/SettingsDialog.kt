package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfileEntity
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.MutedGrey
import com.example.ui.theme.PaleSage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SageGreen
import com.example.ui.theme.WarmBorder

@Composable
fun SettingsDialog(
    isVisible: Boolean,
    profile: UserProfileEntity,
    onThemeChange: (String) -> Unit,
    onWeekStartChange: (Boolean) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onExportData: () -> Unit,
    onClearData: () -> Unit,
    onClose: () -> Unit
) {
    if (!isVisible) return

    var showClearConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onClose,
        containerColor = PureWhite,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                        tint = SageGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Preferences",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = DeepCharcoal
                        )
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MutedGrey
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Profile & Subtle Level Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(PaleSage)
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PureWhite),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "L${profile.level}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SageGreen
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Level ${profile.level} • ${profile.xp} XP",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = DeepCharcoal
                                )
                            )
                            Text(
                                text = "Earn XP through focused work & mindful task completion",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MutedGrey,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // General Settings
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = DeepCharcoal
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Week starts on Monday
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Start week on Monday",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = DeepCharcoal,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = if (profile.weekStartMonday) "Calendar begins Monday" else "Calendar begins Sunday",
                            style = MaterialTheme.typography.bodySmall.copy(color = MutedGrey)
                        )
                    }
                    Switch(
                        checked = profile.weekStartMonday,
                        onCheckedChange = { onWeekStartChange(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureWhite,
                            checkedTrackColor = SageGreen
                        )
                    )
                }

                HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(vertical = 8.dp))

                // Gentle Reminders
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gentle Reminders",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = DeepCharcoal,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "Mindful prompt for daily check-in",
                            style = MaterialTheme.typography.bodySmall.copy(color = MutedGrey)
                        )
                    }
                    Switch(
                        checked = profile.notificationsEnabled,
                        onCheckedChange = { onNotificationsChange(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureWhite,
                            checkedTrackColor = SageGreen
                        )
                    )
                }

                HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(vertical = 8.dp))

                // Data Management
                Text(
                    text = "Data & Storage",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = DeepCharcoal
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Export Data
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onExportData() }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = null,
                        tint = SageGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Export Data (JSON)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = DeepCharcoal
                            )
                        )
                        Text(
                            text = "Backup your tasks, habits, and focus history",
                            style = MaterialTheme.typography.bodySmall.copy(color = MutedGrey)
                        )
                    }
                }

                // Clear All Data
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showClearConfirm = true }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Clear All Data",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                        Text(
                            text = "Erase all local tasks, habits, and focus logs",
                            style = MaterialTheme.typography.bodySmall.copy(color = MutedGrey)
                        )
                    }
                }

                HorizontalDivider(color = WarmBorder, modifier = Modifier.padding(vertical = 10.dp))

                // About & Privacy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = MutedGrey,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LEVELUP 1.0 • Device-local private storage",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MutedGrey,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SageGreen,
                    contentColor = PureWhite
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Done")
            }
        }
    )

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear all data?") },
            text = { Text("This will remove all tasks, habits, and reset your progress. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirm = false
                        onClearData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ExportDataDialog(
    isVisible: Boolean,
    jsonContent: String,
    onClose: () -> Unit
) {
    if (!isVisible) return
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onClose,
        containerColor = PureWhite,
        shape = RoundedCornerShape(18.dp),
        title = {
            Text(
                text = "Exported JSON Data",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = DeepCharcoal
                )
            )
        },
        text = {
            Column {
                Text(
                    text = "Here is a complete backup of your tasks, habits, and profile records.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MutedGrey)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(WarmBorder.copy(alpha = 0.3f))
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = jsonContent,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("LEVELUP Backup", jsonContent)
                    clipboard.setPrimaryClip(clip)
                    copied = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreen)
            ) {
                Text(if (copied) "Copied!" else "Copy to Clipboard")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onClose) {
                Text("Close", color = MutedGrey)
            }
        }
    )
}
