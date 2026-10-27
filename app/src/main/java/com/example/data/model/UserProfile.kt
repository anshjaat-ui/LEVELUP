package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val xp: Int = 0,
    val level: Int = 1,
    val weekStartMonday: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val themeMode: String = "LIGHT"
)
