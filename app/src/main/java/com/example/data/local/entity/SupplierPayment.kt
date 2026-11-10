package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supplier_payments")
data class SupplierPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val supplierId: Long,
    val amount: Double,
    val paymentMethod: String = "Cash",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
