package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String = "Health", // "Health", "Study", "Work", "Personal", "Mindfulness"
    val iconName: String = "leaf", // "leaf", "book", "water", "walk", "meditate", "dumbbell", "code"
    val targetDaysPerWeek: Int = 7,
    val isArchived: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "habit_completions",
    indices = [Index(value = ["habitId", "dateString"], unique = true)]
)
data class HabitCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val dateString: String, // "YYYY-MM-DD"
    val completedAtMillis: Long = System.currentTimeMillis()
)
