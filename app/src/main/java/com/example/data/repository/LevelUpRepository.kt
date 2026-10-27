package com.example.data.repository

import com.example.data.local.FocusSessionDao
import com.example.data.local.HabitDao
import com.example.data.local.TaskDao
import com.example.data.local.UserProfileDao
import com.example.data.model.FocusSessionEntity
import com.example.data.model.HabitCompletionEntity
import com.example.data.model.HabitEntity
import com.example.data.model.TaskEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LevelUpRepository(
    private val taskDao: TaskDao,
    private val habitDao: HabitDao,
    private val focusSessionDao: FocusSessionDao,
    private val userProfileDao: UserProfileDao
) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val activeHabits: Flow<List<HabitEntity>> = habitDao.getActiveHabits()
    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()
    val allHabitCompletions: Flow<List<HabitCompletionEntity>> = habitDao.getAllCompletions()
    val allFocusSessions: Flow<List<FocusSessionEntity>> = focusSessionDao.getAllFocusSessions()
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getProfile()

    suspend fun getOrCreateProfile(): UserProfileEntity {
        val existing = userProfileDao.getProfileSync()
        if (existing != null) return existing
        val initial = UserProfileEntity(
            id = 1,
            xp = 0,
            level = 1,
            weekStartMonday = true,
            notificationsEnabled = true,
            themeMode = "LIGHT"
        )
        userProfileDao.insertOrUpdate(initial)
        return initial
    }

    suspend fun awardXp(amount: Int) {
        if (amount <= 0) return
        val profile = getOrCreateProfile()
        val newXp = profile.xp + amount
        val newLevel = 1 + (newXp / 100)
        userProfileDao.insertOrUpdate(
            profile.copy(
                xp = newXp,
                level = newLevel
            )
        )
    }

    suspend fun insertTask(task: TaskEntity): Long {
        return taskDao.insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(taskId: Long) {
        taskDao.deleteTaskById(taskId)
    }

    suspend fun toggleTaskCompletion(taskId: Long): Int {
        val task = taskDao.getTaskById(taskId) ?: return 0
        var xpEarned = 0
        if (!task.isCompleted) {
            val shouldAward = !task.xpAwarded
            if (shouldAward) {
                xpEarned = when (task.priority.lowercase()) {
                    "high" -> 25
                    "medium" -> 15
                    else -> 10
                }
                awardXp(xpEarned)
            }
            taskDao.updateTask(
                task.copy(
                    isCompleted = true,
                    completedAtMillis = System.currentTimeMillis(),
                    xpAwarded = true
                )
            )
        } else {
            // Reopening task does NOT deduct XP, but prevents duplicate re-awarding
            taskDao.updateTask(
                task.copy(
                    isCompleted = false,
                    completedAtMillis = null
                )
            )
        }
        return xpEarned
    }

    suspend fun insertHabit(habit: HabitEntity): Long {
        return habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habitId: Long) {
        habitDao.deleteCompletionsForHabit(habitId)
        val habits = habitDao.getAllHabits().firstOrNull() ?: emptyList()
        val target = habits.find { it.id == habitId }
        if (target != null) {
            habitDao.deleteHabit(target)
        }
    }

    suspend fun toggleHabitCompletion(habitId: Long, dateString: String): Boolean {
        val completions = habitDao.getAllCompletions().firstOrNull() ?: emptyList()
        val existing = completions.find { it.habitId == habitId && it.dateString == dateString }
        return if (existing != null) {
            habitDao.deleteCompletion(habitId, dateString)
            false
        } else {
            habitDao.insertCompletion(
                HabitCompletionEntity(
                    habitId = habitId,
                    dateString = dateString,
                    completedAtMillis = System.currentTimeMillis()
                )
            )
            awardXp(10)
            true
        }
    }

    suspend fun logFocusSession(
        durationMinutes: Int,
        sessionType: String,
        taskTitle: String?,
        dateString: String
    ): Long {
        val id = focusSessionDao.insertSession(
            FocusSessionEntity(
                durationMinutes = durationMinutes,
                sessionType = sessionType,
                taskTitle = taskTitle,
                completedAtMillis = System.currentTimeMillis(),
                dateString = dateString
            )
        )
        if (sessionType == "Focus" && durationMinutes >= 10) {
            val xp = if (durationMinutes >= 45) 25 else 15
            awardXp(xp)
        }
        return id
    }

    suspend fun updateThemeMode(mode: String) {
        val profile = getOrCreateProfile()
        userProfileDao.insertOrUpdate(profile.copy(themeMode = mode))
    }

    suspend fun updateWeekStart(weekStartMonday: Boolean) {
        val profile = getOrCreateProfile()
        userProfileDao.insertOrUpdate(profile.copy(weekStartMonday = weekStartMonday))
    }

    suspend fun updateNotifications(enabled: Boolean) {
        val profile = getOrCreateProfile()
        userProfileDao.insertOrUpdate(profile.copy(notificationsEnabled = enabled))
    }

    suspend fun clearAllData() {
        taskDao.deleteAllTasks()
        habitDao.deleteAllHabits()
        habitDao.deleteAllCompletions()
        focusSessionDao.deleteAllFocusSessions()
        val profile = getOrCreateProfile()
        userProfileDao.insertOrUpdate(profile.copy(xp = 0, level = 1))
    }

    suspend fun seedInitialDataIfEmpty() {
        val currentTasks = taskDao.getAllTasks().firstOrNull()
        val currentHabits = habitDao.getAllHabits().firstOrNull()
        if (currentTasks.isNullOrEmpty() && currentHabits.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(Date(now))

            // Seed a few thoughtful, calm tasks
            taskDao.insertTask(
                TaskEntity(
                    title = "Review weekly priorities",
                    description = "Identify 3 key outcomes for the week ahead",
                    dueDateMillis = now,
                    dueTime = "09:30",
                    priority = "High",
                    category = "Work"
                )
            )
            taskDao.insertTask(
                TaskEntity(
                    title = "Read 20 pages of a good book",
                    description = "Quiet time without digital distractions",
                    dueDateMillis = now,
                    dueTime = "20:00",
                    priority = "Medium",
                    category = "Personal"
                )
            )
            taskDao.insertTask(
                TaskEntity(
                    title = "Afternoon walk & hydration",
                    description = "Take 15 minutes of mindful fresh air",
                    dueDateMillis = now,
                    dueTime = "15:00",
                    priority = "Low",
                    category = "Health"
                )
            )

            // Seed habits
            val h1 = habitDao.insertHabit(
                HabitEntity(
                    name = "Morning Meditation",
                    category = "Mindfulness",
                    iconName = "meditate",
                    targetDaysPerWeek = 7
                )
            )
            val h2 = habitDao.insertHabit(
                HabitEntity(
                    name = "Drink 2L Water",
                    category = "Health",
                    iconName = "water",
                    targetDaysPerWeek = 7
                )
            )
            val h3 = habitDao.insertHabit(
                HabitEntity(
                    name = "Deep Focus Block",
                    category = "Study",
                    iconName = "book",
                    targetDaysPerWeek = 5
                )
            )

            // Mark one habit done for today as an encouraging start
            habitDao.insertCompletion(
                HabitCompletionEntity(
                    habitId = h2,
                    dateString = todayStr,
                    completedAtMillis = now
                )
            )
            getOrCreateProfile()
        }
    }

    suspend fun exportDataJson(): String {
        val tasks = taskDao.getAllTasks().firstOrNull() ?: emptyList()
        val habits = habitDao.getAllHabits().firstOrNull() ?: emptyList()
        val completions = habitDao.getAllCompletions().firstOrNull() ?: emptyList()
        val sessions = focusSessionDao.getAllFocusSessions().firstOrNull() ?: emptyList()
        val profile = getOrCreateProfile()

        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val pObj = JSONObject()
        pObj.put("xp", profile.xp)
        pObj.put("level", profile.level)
        root.put("profile", pObj)

        val tArray = JSONArray()
        tasks.forEach { t ->
            val obj = JSONObject()
            obj.put("title", t.title)
            obj.put("description", t.description)
            obj.put("dueDateMillis", t.dueDateMillis ?: JSONObject.NULL)
            obj.put("dueTime", t.dueTime ?: JSONObject.NULL)
            obj.put("priority", t.priority)
            obj.put("category", t.category)
            obj.put("isCompleted", t.isCompleted)
            tArray.put(obj)
        }
        root.put("tasks", tArray)

        val hArray = JSONArray()
        habits.forEach { h ->
            val obj = JSONObject()
            obj.put("name", h.name)
            obj.put("category", h.category)
            obj.put("iconName", h.iconName)
            obj.put("targetDaysPerWeek", h.targetDaysPerWeek)
            hArray.put(obj)
        }
        root.put("habits", hArray)

        return root.toString(2)
    }
}
