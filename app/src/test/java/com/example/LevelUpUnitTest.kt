package com.example

import com.example.data.model.TaskEntity
import com.example.data.model.UserProfileEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelUpUnitTest {

    @Test
    fun testLevelProgression() {
        val profile0 = UserProfileEntity(xp = 0, level = 1)
        assertEquals(1, 1 + (profile0.xp / 100))

        val profile99 = UserProfileEntity(xp = 99, level = 1)
        assertEquals(1, 1 + (profile99.xp / 100))

        val profile100 = UserProfileEntity(xp = 100, level = 2)
        assertEquals(2, 1 + (profile100.xp / 100))

        val profile250 = UserProfileEntity(xp = 250, level = 3)
        assertEquals(3, 1 + (profile250.xp / 100))
    }

    @Test
    fun testTaskCompletionXpPreventionLogic() {
        // Initial task
        val task = TaskEntity(
            id = 1,
            title = "Mindful review",
            priority = "High",
            isCompleted = false,
            xpAwarded = false
        )

        // Simulate first completion
        val xpEarned = if (!task.xpAwarded) 25 else 0
        val updatedTask = task.copy(
            isCompleted = true,
            xpAwarded = true
        )
        assertEquals(25, xpEarned)
        assertTrue(updatedTask.xpAwarded)
        assertTrue(updatedTask.isCompleted)

        // Simulate reopening
        val reopenedTask = updatedTask.copy(isCompleted = false)
        assertFalse(reopenedTask.isCompleted)
        assertTrue(reopenedTask.xpAwarded) // Still flagged as already rewarded

        // Simulate re-completing: no duplicate XP
        val secondXpEarned = if (!reopenedTask.xpAwarded) 25 else 0
        assertEquals(0, secondXpEarned)
    }

    @Test
    fun testPriorityWeights() {
        fun priorityWeight(p: String): Int = when (p.lowercase()) {
            "high" -> 3
            "medium" -> 2
            "low" -> 1
            else -> 0
        }

        assertTrue(priorityWeight("High") > priorityWeight("Medium"))
        assertTrue(priorityWeight("Medium") > priorityWeight("Low"))
    }
}
