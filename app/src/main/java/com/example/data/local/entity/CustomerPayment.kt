package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_payments")
data class CustomerPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val customerId: Long,
    val amount: Double,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
