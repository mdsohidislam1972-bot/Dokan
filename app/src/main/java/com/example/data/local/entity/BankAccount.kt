package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bank_accounts")
data class BankAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String, // e.g. "নগদ ক্যাশ বাক্স (Cash Drawer)", "ডাচ-বাংলা ব্যাংক (DBBL)", "বিকাশ মার্চেন্ট (bKash)"
    val accountType: String = "BANK", // "WALLET" or "BANK"
    val accountNumber: String = "",
    val currentBalance: Double = 0.0,
    val isDefault: Boolean = false,
    val note: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)
