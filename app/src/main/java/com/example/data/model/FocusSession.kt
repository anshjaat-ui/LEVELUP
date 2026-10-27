package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val durationMinutes: Int,
    val sessionType: String = "Focus", // "Focus", "Short Break", "Long Break"
    val taskTitle: String? = null,
    val completedAtMillis: Long = System.currentTimeMillis(),
    val dateString: String // "YYYY-MM-DD"
)
