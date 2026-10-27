package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDateMillis: Long? = null,
    val dueTime: String? = null,
    val priority: String = "Medium", // "Low", "Medium", "High"
    val category: String = "Personal", // "Study", "Personal", "Health", "Work"
    val isCompleted: Boolean = false,
    val completedAtMillis: Long? = null,
    val isRecurring: Boolean = false,
    val recurrenceRule: String = "NONE", // "NONE", "DAILY", "WEEKDAYS", "WEEKLY"
    val xpAwarded: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)
