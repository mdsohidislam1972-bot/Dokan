package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "other_incomes")
data class OtherIncome(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val amount: Double,
    val category: String = "Other", // "Scrap Sale", "Commission", "Cashback", "Delivery", "Rent", "Interest", "Other"
    val accountId: Long? = null,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
