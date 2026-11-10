package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_reminders")
data class AppReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val reminderDate: Long,
    val reminderTime: String,
    val type: String = "Task", // "Task", "Event", "Payment"
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
