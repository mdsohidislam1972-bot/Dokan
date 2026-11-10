package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "staff_members")
data class StaffMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val phone: String = "",
    val role: String = "Sales", // "Partner", "Manager", "Accountant", "Sales", "Stock", "Entry"
    val permissionLevel: String = "Limited", // "Full", "Half", "Limited", "Selected"
    val allowedFeatures: String = "Sales,Stock", // Comma-separated
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
