package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.LevelUpDatabase
import com.example.data.repository.LevelUpRepository
import com.example.ui.components.CreateHabitDialog
import com.example.ui.components.EditTaskSheet
import com.example.ui.components.ExportDataDialog
import com.example.ui.components.FocusTimerModal
import com.example.ui.components.LevelUpBottomNavBar
import com.example.ui.components.LevelUpTopBar
import com.example.ui.components.SessionCompletedDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.LevelUpTheme
import com.example.ui.theme.WarmIvory
import com.example.ui.viewmodel.LevelUpViewModel
import com.example.ui.viewmodel.LevelUpViewModelFactory
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val database = remember { LevelUpDatabase.getDatabase(context.applicationContext) }
            val repository = remember {
                LevelUpRepository(
                    taskDao = database.taskDao(),
                    habitDao = database.habitDao(),
                    focusSessionDao = database.focusSessionDao(),
                    userProfileDao = database.userProfileDao()
                )
            }
            val viewModel: LevelUpViewModel = viewModel(
                factory = LevelUpViewModelFactory(repository)
            )

            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isDark = uiState.userProfile.themeMode == "DARK"

            LevelUpTheme(darkTheme = isDark) {
                LevelUpApp(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }
}

@Composable
fun LevelUpApp(
    viewModel: LevelUpViewModel,
    uiState: com.example.ui.viewmodel.UiState
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle back button for sub-screens
    BackHandler(enabled = uiState.currentScreen != Screen.HOME) {
        viewModel.navigateTo(Screen.HOME)
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSnackbar()
        }
    }

    val topBarTitle = when (uiState.currentScreen) {
        Screen.HOME -> "LEVELUP"
        Screen.TASKS -> "Tasks"
        Screen.HABITS -> "Habits"
        Screen.PROGRESS -> "Progress"
    }

    Scaffold(
        topBar = {
            LevelUpTopBar(
                title = topBarTitle,
                subtitle = if (uiState.currentScreen == Screen.HOME) "Natural Focus" else null,
                level = uiState.userProfile.level,
                onProfileClick = { viewModel.openSettings() }
            )
        },
        bottomBar = {
            LevelUpBottomNavBar(
                currentScreen = uiState.currentScreen,
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = WarmIvory,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(WarmIvory)
        ) {
            when (uiState.currentScreen) {
                Screen.HOME -> HomeScreen(
                    uiState = uiState,
                    onAddTask = { viewModel.openCreateTask() },
                    onAddHabit = { viewModel.openCreateHabit() },
                    onStartFocus = { viewModel.openFocusTimer() },
                    onToggleTask = { task -> viewModel.toggleTaskCompletion(task) },
                    onEditTask = { task -> viewModel.openEditTask(task) },
                    onDeleteTask = { id -> viewModel.deleteTask(id) }
                )
                Screen.TASKS -> TasksScreen(
                    uiState = uiState,
                    onTabSelected = { tab -> viewModel.setTaskTab(tab) },
                    onCategorySelected = { cat -> viewModel.setSelectedCategory(cat) },
                    onSearchChanged = { query -> viewModel.setSearchQuery(query) },
                    onSortSelected = { sort -> viewModel.setTaskSort(sort) },
                    onAddTask = { viewModel.openCreateTask() },
                    onToggleTask = { task -> viewModel.toggleTaskCompletion(task) },
                    onEditTask = { task -> viewModel.openEditTask(task) },
                    onDeleteTask = { id -> viewModel.deleteTask(id) }
                )
                Screen.HABITS -> HabitsScreen(
                    uiState = uiState,
                    onAddHabit = { viewModel.openCreateHabit() },
                    onToggleHabitDay = { habitId, dateStr -> viewModel.toggleHabit(habitId, dateStr) },
                    onEditHabit = { habit -> viewModel.openEditHabit(habit) },
                    onDeleteHabit = { id -> viewModel.deleteHabit(id) }
                )
                Screen.PROGRESS -> ProgressScreen(
                    uiState = uiState
                )
            }
        }
    }

    // Modal Components
    FocusTimerModal(
        isVisible = uiState.showTimerDialog,
        isRunning = uiState.isTimerRunning,
        secondsLeft = uiState.timerSecondsLeft,
        totalSeconds = uiState.timerTotalSeconds,
        timerMode = uiState.timerMode,
        onToggle = { viewModel.toggleTimer() },
        onReset = { viewModel.resetTimer() },
        onSetPreset = { mins -> viewModel.setTimerPreset(mins) },
        onClose = { viewModel.closeFocusTimer() }
    )

    SessionCompletedDialog(
        isVisible = uiState.showSessionCompletedDialog,
        xpEarned = uiState.lastSessionXpEarned,
        onDismiss = { viewModel.dismissSessionCompletedDialog() }
    )

    EditTaskSheet(
        isVisible = uiState.showCreateTaskSheet,
        taskToEdit = uiState.taskToEdit,
        onSave = { title, desc, due, dueTime, priority, category, isRec, recRule ->
            viewModel.saveTask(title, desc, due, dueTime, priority, category, isRec, recRule)
        },
        onClose = { viewModel.closeTaskSheet() }
    )

    CreateHabitDialog(
        isVisible = uiState.showCreateHabitDialog,
        habitToEdit = uiState.habitToEdit,
        onSave = { name, category, icon, days ->
            viewModel.saveHabit(name, category, icon, days)
        },
        onClose = { viewModel.closeHabitDialog() }
    )

    SettingsDialog(
        isVisible = uiState.showSettingsDialog,
        profile = uiState.userProfile,
        onThemeChange = { mode -> viewModel.setThemeMode(mode) },
        onWeekStartChange = { monday -> viewModel.setWeekStartMonday(monday) },
        onNotificationsChange = { enabled -> viewModel.setNotificationsEnabled(enabled) },
        onExportData = { viewModel.exportData() },
        onClearData = { viewModel.clearAllData() },
        onClose = { viewModel.closeSettings() }
    )

    ExportDataDialog(
        isVisible = uiState.showExportDialog,
        jsonContent = uiState.exportedJson,
        onClose = { viewModel.closeExportDialog() }
    )
}
