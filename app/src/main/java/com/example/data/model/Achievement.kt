package com.example.data.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val targetCount: Int,
    val currentCount: Int,
    val isUnlocked: Boolean,
    val xpReward: Int
)
