package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Achievement
import com.example.data.model.FocusSessionEntity
import com.example.data.model.HabitCompletionEntity
import com.example.data.model.HabitEntity
import com.example.data.model.TaskEntity
import com.example.data.model.UserProfileEntity
import com.example.data.repository.LevelUpRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class Screen {
    HOME, TASKS, HABITS, PROGRESS
}

enum class TaskTab {
    TODAY, UPCOMING, COMPLETED, ALL
}

enum class TaskSort {
    DUE_DATE, PRIORITY, CREATED
}

data class DayStats(
    val dayLabel: String,
    val dateString: String,
    val completedTasksCount: Int,
    val focusMinutes: Int,
    val isToday: Boolean
)

data class UiState(
    val currentScreen: Screen = Screen.HOME,
    val tasks: List<TaskEntity> = emptyList(),
    val filteredTasks: List<TaskEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val habitCompletions: List<HabitCompletionEntity> = emptyList(),
    val focusSessions: List<FocusSessionEntity> = emptyList(),
    val userProfile: UserProfileEntity = UserProfileEntity(),
    // Tasks UI state
    val selectedTaskTab: TaskTab = TaskTab.TODAY,
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val taskSort: TaskSort = TaskSort.DUE_DATE,
    // Focus Timer State
    val isTimerRunning: Boolean = false,
    val timerSecondsLeft: Int = 25 * 60,
    val timerTotalSeconds: Int = 25 * 60,
    val timerMode: String = "Focus",
    val timerSessionName: String = "Deep Work Session",
    val showTimerDialog: Boolean = false,
    val showSessionCompletedDialog: Boolean = false,
    val lastSessionXpEarned: Int = 0,
    // Sheets & Dialogs
    val showCreateTaskSheet: Boolean = false,
    val taskToEdit: TaskEntity? = null,
    val showCreateHabitDialog: Boolean = false,
    val habitToEdit: HabitEntity? = null,
    val showSettingsDialog: Boolean = false,
    val showExportDialog: Boolean = false,
    val exportedJson: String = "",
    val snackbarMessage: String? = null,
    // Derived Statistics
    val todayCompletedTasks: Int = 0,
    val todayTotalTasks: Int = 0,
    val weekCompletedTasks: Int = 0,
    val weekFocusMinutes: Int = 0,
    val weekHabitConsistencyPercent: Int = 0,
    val weeklyDayStats: List<DayStats> = emptyList(),
    val achievements: List<Achievement> = emptyList()
)

class LevelUpViewModel(
    private val repository: LevelUpRepository
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(Screen.HOME)
    private val _selectedTaskTab = MutableStateFlow(TaskTab.TODAY)
    private val _selectedCategory = MutableStateFlow("All")
    private val _searchQuery = MutableStateFlow("")
    private val _taskSort = MutableStateFlow(TaskSort.DUE_DATE)

    // Timer state
    private val _isTimerRunning = MutableStateFlow(false)
    private val _timerSecondsLeft = MutableStateFlow(25 * 60)
    private val _timerTotalSeconds = MutableStateFlow(25 * 60)
    private val _timerMode = MutableStateFlow("Focus")
    private val _timerSessionName = MutableStateFlow("Deep Work Session")
    private val _showTimerDialog = MutableStateFlow(false)
    private val _showSessionCompletedDialog = MutableStateFlow(false)
    private val _lastSessionXpEarned = MutableStateFlow(0)

    // Dialog state
    private val _showCreateTaskSheet = MutableStateFlow(false)
    private val _taskToEdit = MutableStateFlow<TaskEntity?>(null)
    private val _showCreateHabitDialog = MutableStateFlow(false)
    private val _habitToEdit = MutableStateFlow<HabitEntity?>(null)
    private val _showSettingsDialog = MutableStateFlow(false)
    private val _showExportDialog = MutableStateFlow(false)
    private val _exportedJson = MutableStateFlow("")
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val uiState: StateFlow<UiState> = combine(
        combine(
            repository.allTasks,
            repository.activeHabits,
            repository.allHabitCompletions,
            repository.allFocusSessions,
            repository.userProfile
        ) { tasks, habits, completions, sessions, profile ->
            DataBundle(tasks, habits, completions, sessions, profile ?: UserProfileEntity())
        },
        combine(
            _currentScreen,
            _selectedTaskTab,
            _selectedCategory,
            _searchQuery,
            _taskSort
        ) { screen, tab, cat, query, sort ->
            FilterBundle(screen, tab, cat, query, sort)
        },
        combine(
            _isTimerRunning,
            _timerSecondsLeft,
            _timerTotalSeconds,
            _timerMode,
            _showTimerDialog
        ) { running, left, total, mode, showTimer ->
            TimerBundle(running, left, total, mode, showTimer)
        },
        combine(
            _showCreateTaskSheet,
            _taskToEdit,
            _showCreateHabitDialog,
            _habitToEdit,
            _showSettingsDialog
        ) { showTask, editTask, showHabit, editHabit, showSettings ->
            DialogBundle(showTask, editTask, showHabit, editHabit, showSettings)
        },
        combine(
            _showSessionCompletedDialog,
            _lastSessionXpEarned,
            _showExportDialog,
            _exportedJson,
            _snackbarMessage
        ) { showSessionDone, sessionXp, showExport, expJson, snack ->
            MiscBundle(showSessionDone, sessionXp, showExport, expJson, snack)
        }
    ) { data, filter, timer, dialogs, misc ->
        computeUiState(data, filter, timer, dialogs, misc)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState()
    )

    private fun computeUiState(
        data: DataBundle,
        filter: FilterBundle,
        timer: TimerBundle,
        dialogs: DialogBundle,
        misc: MiscBundle
    ): UiState {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayCalendar = getDayStartCalendar()
        val todayStr = dateFormat.format(todayCalendar.time)
        val todayStartMillis = todayCalendar.timeInMillis
        val todayEndMillis = todayStartMillis + 24L * 60L * 60L * 1000L - 1L

        val weekStartCalendar = getWeekStartCalendar(data.profile.weekStartMonday)
        val weekStartMillis = weekStartCalendar.timeInMillis

        // Filter and sort tasks
        val filtered = data.tasks.filter { task ->
            val matchesCategory = filter.category == "All" || task.category.equals(filter.category, ignoreCase = true)
            val matchesQuery = filter.query.isBlank() || task.title.contains(filter.query, ignoreCase = true) || task.description.contains(filter.query, ignoreCase = true)
            val matchesTab = when (filter.tab) {
                TaskTab.TODAY -> {
                    val due = task.dueDateMillis
                    if (due == null) {
                        !task.isCompleted
                    } else {
                        (due in todayStartMillis..todayEndMillis) || (due < todayStartMillis && !task.isCompleted)
                    }
                }
                TaskTab.UPCOMING -> {
                    val due = task.dueDateMillis
                    due != null && due > todayEndMillis && !task.isCompleted
                }
                TaskTab.COMPLETED -> task.isCompleted
                TaskTab.ALL -> true
            }
            matchesCategory && matchesQuery && matchesTab
        }.sortedWith { a, b ->
            when (filter.sort) {
                TaskSort.DUE_DATE -> {
                    val d1 = a.dueDateMillis ?: Long.MAX_VALUE
                    val d2 = b.dueDateMillis ?: Long.MAX_VALUE
                    d1.compareTo(d2)
                }
                TaskSort.PRIORITY -> {
                    val pA = priorityWeight(a.priority)
                    val pB = priorityWeight(b.priority)
                    pB.compareTo(pA)
                }
                TaskSort.CREATED -> b.createdAtMillis.compareTo(a.createdAtMillis)
            }
        }

        // Today's task metrics
        val todayTasks = data.tasks.filter { t ->
            val due = t.dueDateMillis
            due == null || (due in todayStartMillis..todayEndMillis) || (due < todayStartMillis && !t.isCompleted)
        }
        val todayCompleted = todayTasks.count { it.isCompleted }
        val todayTotal = todayTasks.size

        // Week metrics
        val weekCompleted = data.tasks.count { t ->
            t.isCompleted && (t.completedAtMillis ?: 0L) >= weekStartMillis
        }

        val weekFocusMinutes = data.sessions.filter {
            it.completedAtMillis >= weekStartMillis && it.sessionType == "Focus"
        }.sumOf { it.durationMinutes }

        // Habit weekly consistency %
        val weekDayStats = mutableListOf<DayStats>()
        val cal = Calendar.getInstance()
        cal.timeInMillis = weekStartMillis
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        var totalHabitOpportunities = 0
        var totalHabitCompletions = 0

        for (i in 0 until 7) {
            val dStr = dateFormat.format(cal.time)
            val dLabel = dayFormat.format(cal.time)
            val isToday = dStr == todayStr

            val dStart = cal.timeInMillis
            val dEnd = dStart + 24L * 60L * 60L * 1000L - 1L

            val tasksDoneOnDay = data.tasks.count { t ->
                t.isCompleted && (t.completedAtMillis ?: 0L) in dStart..dEnd
            }
            val focusMinsOnDay = data.sessions.filter { s ->
                s.completedAtMillis in dStart..dEnd && s.sessionType == "Focus"
            }.sumOf { s -> s.durationMinutes }

            val habitCompletionsOnDay = data.completions.count { c -> c.dateString == dStr }

            totalHabitCompletions += habitCompletionsOnDay
            totalHabitOpportunities += data.habits.size

            weekDayStats.add(
                DayStats(
                    dayLabel = dLabel,
                    dateString = dStr,
                    completedTasksCount = tasksDoneOnDay,
                    focusMinutes = focusMinsOnDay,
                    isToday = isToday
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val habitConsistencyPercent = if (totalHabitOpportunities > 0) {
            ((totalHabitCompletions.toFloat() / totalHabitOpportunities) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        // Achievements computation
        val totalCompletedTasksCount = data.tasks.count { it.isCompleted }
        val totalFocusSessionsCount = data.sessions.count { it.sessionType == "Focus" }
        val totalFocusMinutesCount = data.sessions.filter { it.sessionType == "Focus" }.sumOf { it.durationMinutes }
        val totalHabitDaysCount = data.completions.map { it.dateString }.distinct().size

        val distinctCategoriesCount = data.tasks.filter { it.isCompleted }.map { it.category }.distinct().size

        val achievementsList = listOf(
            Achievement(
                id = "first_task",
                title = "First Step",
                description = "Complete your first task",
                iconName = "check_circle",
                targetCount = 1,
                currentCount = totalCompletedTasksCount.coerceAtMost(1),
                isUnlocked = totalCompletedTasksCount >= 1,
                xpReward = 25
            ),
            Achievement(
                id = "ten_tasks",
                title = "Steady Rhythm",
                description = "Complete 10 tasks mindfully",
                iconName = "task_alt",
                targetCount = 10,
                currentCount = totalCompletedTasksCount.coerceAtMost(10),
                isUnlocked = totalCompletedTasksCount >= 10,
                xpReward = 50
            ),
            Achievement(
                id = "deep_work",
                title = "Focused Mind",
                description = "Complete 3 focus sessions",
                iconName = "timer",
                targetCount = 3,
                currentCount = totalFocusSessionsCount.coerceAtMost(3),
                isUnlocked = totalFocusSessionsCount >= 3,
                xpReward = 30
            ),
            Achievement(
                id = "focus_century",
                title = "Deep Hours",
                description = "Log 60+ minutes of deep work",
                iconName = "hourglass_full",
                targetCount = 60,
                currentCount = totalFocusMinutesCount.coerceAtMost(60),
                isUnlocked = totalFocusMinutesCount >= 60,
                xpReward = 75
            ),
            Achievement(
                id = "habit_consistency",
                title = "Habit Cultivator",
                description = "Complete habits on 5 different days",
                iconName = "eco",
                targetCount = 5,
                currentCount = totalHabitDaysCount.coerceAtMost(5),
                isUnlocked = totalHabitDaysCount >= 5,
                xpReward = 50
            ),
            Achievement(
                id = "balanced_life",
                title = "Balanced Days",
                description = "Complete tasks across 3 different life areas",
                iconName = "spa",
                targetCount = 3,
                currentCount = distinctCategoriesCount.coerceAtMost(3),
                isUnlocked = distinctCategoriesCount >= 3,
                xpReward = 50
            )
        )

        return UiState(
            currentScreen = filter.screen,
            tasks = data.tasks,
            filteredTasks = filtered,
            habits = data.habits,
            habitCompletions = data.completions,
            focusSessions = data.sessions,
            userProfile = data.profile,
            selectedTaskTab = filter.tab,
            selectedCategory = filter.category,
            searchQuery = filter.query,
            taskSort = filter.sort,
            isTimerRunning = timer.running,
            timerSecondsLeft = timer.secondsLeft,
            timerTotalSeconds = timer.totalSeconds,
            timerMode = timer.mode,
            showTimerDialog = timer.showTimer,
            showSessionCompletedDialog = misc.showSessionDone,
            lastSessionXpEarned = misc.sessionXp,
            showCreateTaskSheet = dialogs.showTask,
            taskToEdit = dialogs.editTask,
            showCreateHabitDialog = dialogs.showHabit,
            habitToEdit = dialogs.editHabit,
            showSettingsDialog = dialogs.showSettings,
            showExportDialog = misc.showExport,
            exportedJson = misc.exportedJson,
            snackbarMessage = misc.snack,
            todayCompletedTasks = todayCompleted,
            todayTotalTasks = todayTotal,
            weekCompletedTasks = weekCompleted,
            weekFocusMinutes = weekFocusMinutes,
            weekHabitConsistencyPercent = habitConsistencyPercent,
            weeklyDayStats = weekDayStats,
            achievements = achievementsList
        )
    }

    private fun priorityWeight(priority: String): Int = when (priority.lowercase()) {
        "high" -> 3
        "medium" -> 2
        "low" -> 1
        else -> 0
    }

    // Screen navigation
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // Task filters & actions
    fun setTaskTab(tab: TaskTab) {
        _selectedTaskTab.value = tab
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTaskSort(sort: TaskSort) {
        _taskSort.value = sort
    }

    fun openCreateTask() {
        _taskToEdit.value = null
        _showCreateTaskSheet.value = true
    }

    fun openEditTask(task: TaskEntity) {
        _taskToEdit.value = task
        _showCreateTaskSheet.value = true
    }

    fun closeTaskSheet() {
        _showCreateTaskSheet.value = false
        _taskToEdit.value = null
    }

    fun saveTask(
        title: String,
        description: String,
        dueDateMillis: Long?,
        dueTime: String?,
        priority: String,
        category: String,
        isRecurring: Boolean,
        recurrenceRule: String
    ) {
        viewModelScope.launch {
            val editing = _taskToEdit.value
            if (editing != null) {
                repository.updateTask(
                    editing.copy(
                        title = title,
                        description = description,
                        dueDateMillis = dueDateMillis,
                        dueTime = dueTime,
                        priority = priority,
                        category = category,
                        isRecurring = isRecurring,
                        recurrenceRule = recurrenceRule
                    )
                )
                showSnackbar("Task updated")
            } else {
                repository.insertTask(
                    TaskEntity(
                        title = title,
                        description = description,
                        dueDateMillis = dueDateMillis,
                        dueTime = dueTime,
                        priority = priority,
                        category = category,
                        isRecurring = isRecurring,
                        recurrenceRule = recurrenceRule
                    )
                )
                showSnackbar("Task created")
            }
            closeTaskSheet()
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val xpEarned = repository.toggleTaskCompletion(task.id)
            if (xpEarned > 0) {
                showSnackbar("Task completed • +$xpEarned XP")
            } else if (!task.isCompleted) {
                showSnackbar("Task completed")
            }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            showSnackbar("Task deleted")
        }
    }

    // Habit actions
    fun openCreateHabit() {
        _habitToEdit.value = null
        _showCreateHabitDialog.value = true
    }

    fun openEditHabit(habit: HabitEntity) {
        _habitToEdit.value = habit
        _showCreateHabitDialog.value = true
    }

    fun closeHabitDialog() {
        _showCreateHabitDialog.value = false
        _habitToEdit.value = null
    }

    fun saveHabit(
        name: String,
        category: String,
        iconName: String,
        targetDays: Int
    ) {
        viewModelScope.launch {
            val editing = _habitToEdit.value
            if (editing != null) {
                repository.updateHabit(
                    editing.copy(
                        name = name,
                        category = category,
                        iconName = iconName,
                        targetDaysPerWeek = targetDays
                    )
                )
                showSnackbar("Habit updated")
            } else {
                repository.insertHabit(
                    HabitEntity(
                        name = name,
                        category = category,
                        iconName = iconName,
                        targetDaysPerWeek = targetDays
                    )
                )
                showSnackbar("Habit created")
            }
            closeHabitDialog()
        }
    }

    fun toggleHabit(habitId: Long, dateString: String) {
        viewModelScope.launch {
            val completed = repository.toggleHabitCompletion(habitId, dateString)
            if (completed) {
                showSnackbar("Habit completed • +10 XP")
            }
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            showSnackbar("Habit removed")
        }
    }

    // Focus Timer
    fun openFocusTimer(mode: String = "Focus", durationMinutes: Int = 25) {
        _timerMode.value = mode
        val seconds = durationMinutes * 60
        _timerTotalSeconds.value = seconds
        _timerSecondsLeft.value = seconds
        _isTimerRunning.value = false
        _showTimerDialog.value = true
    }

    fun closeFocusTimer() {
        _showTimerDialog.value = false
    }

    fun setTimerPreset(durationMinutes: Int) {
        pauseTimer()
        val seconds = durationMinutes * 60
        _timerTotalSeconds.value = seconds
        _timerSecondsLeft.value = seconds
    }

    fun toggleTimer() {
        if (_isTimerRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _isTimerRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value && _timerSecondsLeft.value > 0) {
                delay(1000L)
                _timerSecondsLeft.value -= 1
            }
            if (_timerSecondsLeft.value <= 0) {
                onTimerFinished()
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _timerSecondsLeft.value = _timerTotalSeconds.value
    }

    private fun onTimerFinished() {
        pauseTimer()
        val durationMins = _timerTotalSeconds.value / 60
        val mode = _timerMode.value
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dateFormat.format(Date())

        viewModelScope.launch {
            val xp = if (mode == "Focus" && durationMins >= 10) (if (durationMins >= 45) 25 else 15) else 0
            repository.logFocusSession(
                durationMinutes = durationMins,
                sessionType = mode,
                taskTitle = _timerSessionName.value,
                dateString = dateStr
            )
            _lastSessionXpEarned.value = xp
            _showSessionCompletedDialog.value = true
        }
    }

    fun dismissSessionCompletedDialog() {
        _showSessionCompletedDialog.value = false
        _showTimerDialog.value = false
    }

    // Settings & Data
    fun openSettings() {
        _showSettingsDialog.value = true
    }

    fun closeSettings() {
        _showSettingsDialog.value = false
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            repository.updateThemeMode(mode)
        }
    }

    fun setWeekStartMonday(monday: Boolean) {
        viewModelScope.launch {
            repository.updateWeekStart(monday)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateNotifications(enabled)
            showSnackbar(if (enabled) "Notifications enabled" else "Notifications muted")
        }
    }

    fun exportData() {
        viewModelScope.launch {
            val json = repository.exportDataJson()
            _exportedJson.value = json
            _showExportDialog.value = true
        }
    }

    fun closeExportDialog() {
        _showExportDialog.value = false
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            showSnackbar("All data cleared")
            _showSettingsDialog.value = false
        }
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}

private data class DataBundle(
    val tasks: List<TaskEntity>,
    val habits: List<HabitEntity>,
    val completions: List<HabitCompletionEntity>,
    val sessions: List<FocusSessionEntity>,
    val profile: UserProfileEntity
)

private data class FilterBundle(
    val screen: Screen,
    val tab: TaskTab,
    val category: String,
    val query: String,
    val sort: TaskSort
)

private data class TimerBundle(
    val running: Boolean,
    val secondsLeft: Int,
    val totalSeconds: Int,
    val mode: String,
    val showTimer: Boolean
)

private data class DialogBundle(
    val showTask: Boolean,
    val editTask: TaskEntity?,
    val showHabit: Boolean,
    val editHabit: HabitEntity?,
    val showSettings: Boolean
)

private data class MiscBundle(
    val showSessionDone: Boolean,
    val sessionXp: Int,
    val showExport: Boolean,
    val exportedJson: String,
    val snack: String?
)

private fun getDayStartCalendar(): Calendar {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal
}

private fun getWeekStartCalendar(mondayStart: Boolean): Calendar {
    val cal = getDayStartCalendar()
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val diff = if (mondayStart) {
        if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
    } else {
        dayOfWeek - Calendar.SUNDAY
    }
    cal.add(Calendar.DAY_OF_YEAR, -diff)
    return cal
}

class LevelUpViewModelFactory(
    private val repository: LevelUpRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LevelUpViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LevelUpViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
