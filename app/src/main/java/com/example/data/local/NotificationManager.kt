package com.example.data.local

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.local.entity.Customer
import com.example.data.local.entity.Product

enum class NotificationType {
    LOW_STOCK,
    OVERDUE_PAYMENT,
    REMINDER,
    SYSTEM
}

data class AppNotification(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val messageEn: String,
    val messageBn: String,
    val timestamp: Long,
    val type: NotificationType,
    val targetId: String? = null,
    val isRead: Boolean = false
)

object NotificationManager {
    private const val PREFS_NAME = "hardware_store_prefs"
    private const val KEY_READ_NOTIFICATIONS = "read_notification_ids"

    var notifications by mutableStateOf<List<AppNotification>>(emptyList())
        private set

    var unreadCount by mutableIntStateOf(0)
        private set

    private val readIds = mutableSetOf<String>()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedRead = prefs.getStringSet(KEY_READ_NOTIFICATIONS, emptySet()) ?: emptySet()
        readIds.clear()
        readIds.addAll(savedRead)
    }

    /**
     * Re-evaluates notifications based on live products and customers.
     */
    fun refresh(
        context: Context,
        lowStockProducts: List<Product>,
        customers: List<Customer>
    ) {
        val list = mutableListOf<AppNotification>()
        val now = System.currentTimeMillis()
        val sevenDaysMs = 7L * 24 * 60 * 60 * 1000L

        // 1. Low stock product alerts
        lowStockProducts.forEach { product ->
            val id = "low_stock_${product.id}"
            list.add(
                AppNotification(
                    id = id,
                    titleEn = "Low Stock Alert: ${product.nameEn}",
                    titleBn = "কম স্টকের সতর্কতা: ${product.nameBn.ifBlank { product.nameEn }}",
                    messageEn = "Only ${product.stockQuantity} ${product.unitEn} remaining (threshold: ${product.lowStockThreshold})",
                    messageBn = "মাত্র ${product.stockQuantity} ${product.unitBn} বাকি আছে (সীমা: ${product.lowStockThreshold})",
                    timestamp = now,
                    type = NotificationType.LOW_STOCK,
                    targetId = product.id.toString(),
                    isRead = readIds.contains(id)
                )
            )
        }

        // 2. Overdue customer baki reminders
        customers.filter { it.totalBaki > 0 }.forEach { customer ->
            val isOverdue = when {
                customer.dueDate != null && customer.dueDate > 0 -> customer.dueDate < now
                customer.lastPaymentDate != null && customer.lastPaymentDate > 0 -> (now - customer.lastPaymentDate) > sevenDaysMs
                else -> (now - customer.lastTransactionTime) > sevenDaysMs
            }
            if (isOverdue) {
                val id = "overdue_cust_${customer.id}"
                list.add(
                    AppNotification(
                        id = id,
                        titleEn = "Payment Due: ${customer.name}",
                        titleBn = "বকেয়া পাওনা: ${customer.name}",
                        messageEn = "Overdue balance: ৳ ${customer.totalBaki.toLong()}",
                        messageBn = "বকেয়া বাকি: ৳ ${customer.totalBaki.toLong()}",
                        timestamp = customer.dueDate ?: customer.lastTransactionTime,
                        type = NotificationType.OVERDUE_PAYMENT,
                        targetId = customer.id.toString(),
                        isRead = readIds.contains(id)
                    )
                )
            }
        }

        notifications = list
        unreadCount = list.count { !it.isRead }
    }

    fun markAsRead(context: Context, notificationId: String) {
        readIds.add(notificationId)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_READ_NOTIFICATIONS, readIds).apply()

        notifications = notifications.map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
        unreadCount = notifications.count { !it.isRead }
    }

    fun markAllAsRead(context: Context) {
        notifications.forEach { readIds.add(it.id) }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_READ_NOTIFICATIONS, readIds).apply()

        notifications = notifications.map { it.copy(isRead = true) }
        unreadCount = 0
    }
}
