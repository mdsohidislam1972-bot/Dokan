package com.example.ui.screens.home

import androidx.compose.ui.graphics.vector.ImageVector

data class HomeSummaryMetrics(
    val totalSales: Double = 0.0,
    val totalProfit: Double = 0.0,
    val ordersCount: Int = 0,
    val itemsSoldCount: Int = 0
)

data class PeriodFigures(
    val totalSales: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val totalExpenses: Double = 0.0
)

data class CashflowState(
    val days: List<String> = emptyList(),
    val totalCashIn: Double = 0.0,
    val totalCashOut: Double = 0.0
)

data class AppShortcut(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val icon: ImageVector,
    val isEnabled: Boolean = true
) {
    fun localizedTitle(isBangla: Boolean): String = if (isBangla) titleBn else titleEn
}
