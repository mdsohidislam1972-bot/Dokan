package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val note: String = "",
    val totalBaki: Double = 0.0,
    val dueDate: Long? = null,
    val lastPaymentDate: Long? = null,
    val lastTransactionTime: Long = System.currentTimeMillis(),
    val category: String = "Regular" // e.g. Regular, Wholesale, VIP, Retail
)
