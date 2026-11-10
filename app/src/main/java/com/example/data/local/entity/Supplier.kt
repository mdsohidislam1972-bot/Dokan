package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val company: String = "",
    val phone: String = "",
    val address: String = "",
    val suppliedCategories: String = "",
    val note: String = "",
    val amountOwed: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val category: String = "Supplier" // e.g. Hardware, Electrical, Paint, Plumbing, Dealer
)
