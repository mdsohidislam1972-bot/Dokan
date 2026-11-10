package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sale_orders")
data class SaleOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val customerId: Long? = null,
    val customerName: String,
    val customerPhone: String = "",
    val totalAmount: Double,
    val paidAmount: Double,
    val dueAmount: Double = 0.0, // Customer Baki
    val totalCost: Double, // Cost of goods for profit calculation (totalAmount - totalCost)
    val totalItemsCount: Int,
    val summaryItems: String = "", // Short preview of items sold
    val dueDate: Long? = null,
    val isCredit: Boolean = false
) {
    val profit: Double get() = totalAmount - totalCost
    val itemsSummary: String get() = summaryItems
    val subtotal: Double get() = totalAmount
    val discount: Double get() = 0.0
}
