package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.viewmodel.TaskSort
import com.example.ui.viewmodel.TaskTab
import com.example.ui.viewmodel.UiState

@Composable
fun TasksScreen(
    uiState: UiState,
    onTabSelected: (TaskTab) -> Unit,
    onCategorySelected: (String) -> Unit,
    onSearchChanged: (String) -> Unit,
    onSortSelected: (TaskSort) -> Unit,
    onAddTask: () -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Screen Title & Sort
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tasks",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = DeepCharcoal
                    )
                )

                // Sort Dropdown Trigger
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(PureWhite)
                            .border(1.dp, WarmBorder, RoundedCornerShape(10.dp))
                            .clickable { showSortMenu = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Sort,
                            contentDescription = "Sort tasks",
                            tint = SageGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (uiState.taskSort) {
                                TaskSort.DUE_DATE -> "Due Date"
                                TaskSort.PRIORITY -> "Priority"
                                TaskSort.CREATED -> "Recent"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = DeepCharcoal,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sort by Due Date") },
                            onClick = {
                                onSortSelected(TaskSort.DUE_DATE)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Priority") },
                            onClick = {
                                onSortSelected(TaskSort.PRIORITY)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Creation Date") },
                            onClick = {
                                onSortSelected(TaskSort.CREATED)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }

            // Search input
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChanged,
                placeholder = { Text("Search your tasks...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MutedGrey
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = MutedGrey
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SageGreen,
                    unfocusedBorderColor = WarmBorder,
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tasks_search_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Tabs Row: Today, Upcoming, Completed, All
            val tabs = listOf(
                Pair(TaskTab.TODAY, "Today"),
                Pair(TaskTab.UPCOMING, "Upcoming"),
                Pair(TaskTab.COMPLETED, "Completed"),
                Pair(TaskTab.ALL, "All")
            )
            val selectedTabIndex = tabs.indexOfFirst { it.first == uiState.selectedTaskTab }.coerceAtLeast(0)

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = WarmIvory,
                contentColor = SageGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = SageGreen,
                        height = 2.5.dp
                    )
                },
                divider = {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(WarmBorder))
                }
            ) {
                tabs.forEachIndexed { index, (tab, label) ->
                    val isSelected = index == selectedTabIndex
                    Tab(
                        selected = isSelected,
                        onClick = { onTabSelected(tab) },
                        text = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) SageGreen else MutedGrey,
                                    fontSize = 13.sp
                                )
                            )
                        },
                        modifier = Modifier.testTag("task_tab_${label.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Study", "Work", "Personal", "Health").forEach { category ->
                    val isSelected = uiState.selectedCategory.equals(category, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) SageGreen else PureWhite)
                            .border(1.dp, if (isSelected) SageGreen else WarmBorder, RoundedCornerShape(10.dp))
                            .clickable { onCategorySelected(category) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("category_filter_$category")
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) PureWhite else DeepCharcoal,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Task List or Empty State
            if (uiState.filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(PaleSage),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircleOutline,
                                contentDescription = null,
                                tint = SageGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = when (uiState.selectedTaskTab) {
                                TaskTab.TODAY -> "No tasks due today"
                                TaskTab.UPCOMING -> "No upcoming scheduled tasks"
                                TaskTab.COMPLETED -> "No completed tasks yet"
                                TaskTab.ALL -> "No tasks found"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = DeepCharcoal
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add tasks to keep your intentions clear and steady.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MutedGrey
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("tasks_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.filteredTasks, key = { it.id }) { task ->
                        TaskItem(
                            task = task,
                            onToggleCompletion = { onToggleTask(task) },
                            onEdit = { onEditTask(task) },
                            onDelete = { onDeleteTask(task.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddTask,
            containerColor = SageGreen,
            contentColor = PureWhite,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("tasks_add_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add new task"
            )
        }
    }
}
