package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.ProfileManager
import com.example.data.local.entity.PurchaseRecord
import com.example.data.local.entity.SaleOrder
import com.example.data.repository.HardwareRepository
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.formatTaka
import com.example.ui.theme.DarkSlateBackground
import com.example.ui.theme.DarkSlateCardBorder
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.DarkSlateSurfaceVariant
import com.example.ui.theme.LowStockRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VividEmeraldAccent
import com.example.ui.theme.VividEmeraldPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class HistoryDateFilter(val labelEn: String, val labelBn: String) {
    TODAY("Today", "আজকের"),
    WEEK("Week", "এই সপ্তাহ"),
    MONTH("Month", "এই মাস"),
    YEAR("Year", "এই বছর"),
    ALL("All", "সব সময়"),
    CUSTOM("Custom Date", "নির্দিষ্ট তারিখ")
}

enum class HistoryTypeFilter(val labelEn: String, val labelBn: String) {
    ALL("All", "সব লেনদেন"),
    SALES("Sales Only", "শুধু বিক্রয়"),
    PURCHASES("Purchases Only", "শুধু ক্রয়")
}

data class SoldProductSummary(
    val productName: String,
    val totalQty: Int,
    val totalRevenue: Double,
    val totalProfit: Double
)

@Composable
fun HistoryScreen(
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val repository = remember {
        val db = AppDatabase.getDatabase(context)
        HardwareRepository(
            categoryDao = db.categoryDao(),
            productDao = db.productDao(),
            customerDao = db.customerDao(),
            customerPaymentDao = db.customerPaymentDao(),
            supplierDao = db.supplierDao(),
            supplierPaymentDao = db.supplierPaymentDao(),
            saleOrderDao = db.saleOrderDao(),
            purchaseDao = db.purchaseDao(),
            expenseDao = db.expenseDao()
        )
    }

    val sales by repository.allSales.collectAsState(initial = emptyList())
    val purchases by repository.allPurchases.collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedDateFilter by remember { mutableStateOf(HistoryDateFilter.TODAY) }
    var selectedTypeFilter by remember { mutableStateOf(HistoryTypeFilter.ALL) }
    var selectedOrderForDetail by remember { mutableStateOf<SaleOrder?>(null) }
    var selectedPurchaseForDetail by remember { mutableStateOf<PurchaseRecord?>(null) }

    // Sub-view toggle: 0 = Transactions List, 1 = Products Sold
    var selectedMainTab by remember { mutableStateOf(0) }
    // Grid vs List view toggle for Products Sold
    var isProductsGridView by remember { mutableStateOf(false) }

    // Custom Date Range State
    var customStartDate by remember { mutableStateOf<Long?>(null) }
    var customEndDate by remember { mutableStateOf<Long?>(null) }
    var showCustomDatePicker by remember { mutableStateOf(false) }

    val now = System.currentTimeMillis()
    val startOfToday = remember(now) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }
    val startOfWeek = remember(now) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }
    val startOfMonth = remember(now) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }
    val startOfYear = remember(now) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_YEAR, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }

    fun isTimestampInFilter(timestamp: Long): Boolean {
        return when (selectedDateFilter) {
            HistoryDateFilter.TODAY -> timestamp >= startOfToday
            HistoryDateFilter.WEEK -> timestamp >= startOfWeek
            HistoryDateFilter.MONTH -> timestamp >= startOfMonth
            HistoryDateFilter.YEAR -> timestamp >= startOfYear
            HistoryDateFilter.ALL -> true
            HistoryDateFilter.CUSTOM -> {
                val s = customStartDate ?: 0L
                val e = customEndDate ?: Long.MAX_VALUE
                timestamp in s..e
            }
        }
    }

    // Filtered Sales
    val filteredSales = sales.filter { order ->
        val matchesDate = isTimestampInFilter(order.timestamp)
        val matchesQuery = searchQuery.isBlank() ||
                order.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                order.customerName.contains(searchQuery, ignoreCase = true) ||
                order.itemsSummary.contains(searchQuery, ignoreCase = true)
        matchesDate && matchesQuery
    }

    // Filtered Purchases
    val filteredPurchases = purchases.filter { purchase ->
        val matchesDate = isTimestampInFilter(purchase.timestamp)
        val matchesQuery = searchQuery.isBlank() ||
                purchase.productName.contains(searchQuery, ignoreCase = true) ||
                purchase.supplierName.contains(searchQuery, ignoreCase = true) ||
                purchase.note.contains(searchQuery, ignoreCase = true)
        matchesDate && matchesQuery
    }

    // Requirement 67: History summary card: bought, sold, profit
    val totalSold = filteredSales.sumOf { it.totalAmount }
    val totalBought = filteredPurchases.sumOf { it.totalCost }
    val totalProfit = filteredSales.sumOf { it.profit }

    // Requirement 68: List of products sold aggregation
    val soldProductsSummary: List<SoldProductSummary> = remember(filteredSales) {
        val map = mutableMapOf<String, Triple<Int, Double, Double>>() // Qty, Revenue, Profit
        filteredSales.forEach { sale ->
            val items = sale.itemsSummary.split(",").map { it.trim() }.filter { it.isNotBlank() }
            if (items.isNotEmpty()) {
                val perItemRevenue = if (items.isNotEmpty()) sale.totalAmount / items.size else 0.0
                val perItemProfit = if (items.isNotEmpty()) sale.profit / items.size else 0.0
                items.forEach { itemName ->
                    val cleanName = itemName.substringBefore(" x").trim().ifBlank { itemName }
                    val current = map[cleanName] ?: Triple(0, 0.0, 0.0)
                    map[cleanName] = Triple(
                        current.first + 1,
                        current.second + perItemRevenue,
                        current.third + perItemProfit
                    )
                }
            }
        }
        map.map { (name, stats) ->
            SoldProductSummary(
                productName = name,
                totalQty = stats.first,
                totalRevenue = stats.second,
                totalProfit = stats.third
            )
        }.sortedByDescending { it.totalRevenue }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Requirement 67: History Summary Card (Bought, Sold, Profit)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .testTag("history_summary_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, VividEmeraldPrimary.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bought (Total Purchases)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "ক্রয় (Bought)" else "Bought",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = formatTaka(totalBought, currentLanguage),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB74D),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Sold (Total Sales)
                    Column(modifier = Modifier.weight(1.1f)) {
                        Text(
                            text = if (isBangla) "বিক্রয় (Sold)" else "Sold",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = formatTaka(totalSold, currentLanguage),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = VividEmeraldAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Profit
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "মোট লাভ" else "Profit",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = formatTaka(totalProfit, currentLanguage, isMaskable = true),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (totalProfit >= 0) VividEmeraldAccent else LowStockRed,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_input"),
                placeholder = {
                    Text(
                        if (isBangla) "ইনভয়েস, কাস্টমার, বা পণ্য খুঁজুন..." else "Search invoice, customer, product...",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSlateSurface,
                    unfocusedContainerColor = DarkSlateSurface,
                    focusedBorderColor = VividEmeraldPrimary,
                    unfocusedBorderColor = DarkSlateCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            // Requirement 65: Date Filter Chips (Today, Week, Month, Year, All, Custom Date)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HistoryDateFilter.values().forEach { filter ->
                    FilterChip(
                        selected = selectedDateFilter == filter,
                        onClick = {
                            selectedDateFilter = filter
                            if (filter == HistoryDateFilter.CUSTOM) {
                                showCustomDatePicker = true
                            }
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (filter == HistoryDateFilter.CUSTOM) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(if (isBangla) filter.labelBn else filter.labelEn, fontSize = 12.sp)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VividEmeraldPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = DarkSlateSurface,
                            labelColor = TextMuted
                        )
                    )
                }
            }

            // Main Sub-Tab: 0 = Transactions (All / Sales / Purchases), 1 = Products Sold
            TabRow(
                selectedTabIndex = selectedMainTab,
                containerColor = DarkSlateBackground,
                contentColor = VividEmeraldPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedMainTab]),
                        color = VividEmeraldPrimary
                    )
                }
            ) {
                Tab(
                    selected = selectedMainTab == 0,
                    onClick = { selectedMainTab = 0 },
                    text = {
                        Text(
                            text = if (isBangla) "লেনদেনের তালিকা" else "Transactions",
                            fontWeight = if (selectedMainTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedMainTab == 0) VividEmeraldAccent else TextMuted
                        )
                    }
                )
                Tab(
                    selected = selectedMainTab == 1,
                    onClick = { selectedMainTab = 1 },
                    text = {
                        Text(
                            text = if (isBangla) "বিক্রিত পণ্যসমূহ (${soldProductsSummary.size})" else "Products Sold (${soldProductsSummary.size})",
                            fontWeight = if (selectedMainTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedMainTab == 1) VividEmeraldAccent else TextMuted
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (selectedMainTab == 0) {
                // Requirement 66: History Filter (All, Sales Only, Purchases Only)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HistoryTypeFilter.values().forEach { filter ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clickable { selectedTypeFilter = filter },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedTypeFilter == filter) VividEmeraldPrimary.copy(alpha = 0.2f) else DarkSlateSurface,
                            border = BorderStroke(
                                1.dp,
                                if (selectedTypeFilter == filter) VividEmeraldPrimary else DarkSlateCardBorder
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isBangla) filter.labelBn else filter.labelEn,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTypeFilter == filter) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTypeFilter == filter) VividEmeraldAccent else TextMuted
                                )
                            }
                        }
                    }
                }

                // Transactions List
                val showSales = selectedTypeFilter == HistoryTypeFilter.ALL || selectedTypeFilter == HistoryTypeFilter.SALES
                val showPurchases = selectedTypeFilter == HistoryTypeFilter.ALL || selectedTypeFilter == HistoryTypeFilter.PURCHASES

                val combinedEmpty = (showSales && filteredSales.isEmpty()) && (showPurchases && filteredPurchases.isEmpty())

                if (combinedEmpty) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = if (isBangla) "কোন লেনদেনের রেকর্ড নেই" else "No transaction records found",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        if (showSales) {
                            items(filteredSales, key = { "sale_${it.id}" }) { sale ->
                                SaleOrderCard(
                                    order = sale,
                                    currentLanguage = currentLanguage,
                                    onClick = { selectedOrderForDetail = sale }
                                )
                            }
                        }

                        if (showPurchases) {
                            items(filteredPurchases, key = { "purchase_${it.id}" }) { purchase ->
                                PurchaseItemCard(
                                    purchase = purchase,
                                    currentLanguage = currentLanguage,
                                    onClick = { selectedPurchaseForDetail = purchase }
                                )
                            }
                        }
                    }
                }
            } else {
                // Requirement 68: Products Sold with Grid and List toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "সর্বমোট বিক্রিত পণ্য তালিকা" else "Products Sold Breakdown",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isProductsGridView = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.ViewList,
                                contentDescription = "List View",
                                tint = if (!isProductsGridView) VividEmeraldAccent else TextMuted
                            )
                        }
                        IconButton(
                            onClick = { isProductsGridView = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.GridView,
                                contentDescription = "Grid View",
                                tint = if (isProductsGridView) VividEmeraldAccent else TextMuted
                            )
                        }
                    }
                }

                if (soldProductsSummary.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = if (isBangla) "এই সময়ে কোনো পণ্য বিক্রি হয়নি" else "No products sold in this period",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else if (isProductsGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(soldProductsSummary) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                                border = BorderStroke(1.dp, DarkSlateCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = item.productName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${if (isBangla) "পরিমাণ: " else "Qty: "}${item.totalQty}",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "${if (isBangla) "বিক্রি: " else "Sales: "}${formatTaka(item.totalRevenue, currentLanguage)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = VividEmeraldAccent
                                    )
                                    Text(
                                        text = "${if (isBangla) "লাভ: " else "Profit: "}${formatTaka(item.totalProfit, currentLanguage, isMaskable = true)}",
                                        fontSize = 11.sp,
                                        color = if (item.totalProfit >= 0) VividEmeraldAccent else LowStockRed
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(soldProductsSummary) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                                border = BorderStroke(1.dp, DarkSlateCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.productName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${if (isBangla) "মোট পরিমাণ: " else "Total Quantity: "}${item.totalQty}",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = formatTaka(item.totalRevenue, currentLanguage),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = VividEmeraldAccent
                                        )
                                        Text(
                                            text = "${if (isBangla) "লাভ: " else "Profit: "}${formatTaka(item.totalProfit, currentLanguage, isMaskable = true)}",
                                            fontSize = 11.sp,
                                            color = if (item.totalProfit >= 0) VividEmeraldAccent else LowStockRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom Date Range Picker Dialog
    if (showCustomDatePicker) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosenCal = Calendar.getInstance()
                chosenCal.set(year, month, dayOfMonth, 0, 0, 0)
                val start = chosenCal.timeInMillis
                chosenCal.set(year, month, dayOfMonth, 23, 59, 59)
                val end = chosenCal.timeInMillis
                customStartDate = start
                customEndDate = end
                showCustomDatePicker = false
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnCancelListener { showCustomDatePicker = false }
            show()
        }
    }

    // Invoice Detail Dialog
    selectedOrderForDetail?.let { order ->
        InvoiceDetailDialog(
            order = order,
            currentLanguage = currentLanguage,
            onDismiss = { selectedOrderForDetail = null }
        )
    }

    // Purchase Detail Dialog
    selectedPurchaseForDetail?.let { purchase ->
        PurchaseDetailDialog(
            purchase = purchase,
            currentLanguage = currentLanguage,
            onDismiss = { selectedPurchaseForDetail = null }
        )
    }
}

@Composable
fun PurchaseItemCard(
    purchase: PurchaseRecord,
    currentLanguage: AppLanguage,
    onClick: () -> Unit
) {
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(purchase.timestamp) { dateFormat.format(Date(purchase.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("purchase_item_${purchase.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = purchase.productName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = formatTaka(purchase.totalCost, currentLanguage),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFB74D)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (purchase.supplierName.isNotBlank()) "${if (isBangla) "সাপ্লায়ার: " else "Supplier: "}${purchase.supplierName}" else (if (isBangla) "নগদ ক্রয় (Cash Purchase)" else "Cash Purchase"),
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "$formattedTime • ${purchase.quantity} ${if (isBangla) "পিস" else "pcs"}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFFB74D).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFFFB74D))
                ) {
                    Text(
                        text = if (isBangla) "ক্রয় (Purchase)" else "Purchase",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFB74D),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PurchaseDetailDialog(
    purchase: PurchaseRecord,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit
) {
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(purchase.timestamp) { dateFormat.format(Date(purchase.timestamp)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) "ক্রয় বিবরণী (Purchase)" else "Purchase Details",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${if (isBangla) "পণ্য: " else "Product: "}${purchase.productName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${if (isBangla) "তারিখ: " else "Date: "}$formattedTime",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Text(
                    text = "${if (isBangla) "সাপ্লায়ার: " else "Supplier: "}${purchase.supplierName.ifBlank { "Cash Purchase" }}",
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                HorizontalDivider(color = DarkSlateCardBorder)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (isBangla) "পরিমাণ" else "Quantity", color = TextMuted)
                    Text(text = "${purchase.quantity}", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (isBangla) "একক ক্রয়মূল্য" else "Unit Buying Price", color = TextMuted)
                    Text(text = formatTaka(purchase.unitBuyingPrice, currentLanguage), color = TextPrimary)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (isBangla) "সর্বমোট ক্রয়মূল্য" else "Total Cost", color = TextMuted)
                    Text(
                        text = formatTaka(purchase.totalCost, currentLanguage),
                        color = Color(0xFFFFB74D),
                        fontWeight = FontWeight.Bold
                    )
                }
                if (purchase.note.isNotBlank()) {
                    Text(
                        text = "${if (isBangla) "নোট: " else "Note: "}${purchase.note}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
            ) {
                Text(if (isBangla) "ঠিক আছে" else "OK")
            }
        }
    )
}

@Composable
fun SaleOrderCard(
    order: SaleOrder,
    currentLanguage: AppLanguage,
    onClick: () -> Unit
) {
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(order.timestamp) { dateFormat.format(Date(order.timestamp)) }

    val statusText = when {
        order.dueAmount <= 0.0 -> if (isBangla) "পরিশোধিত" else "Paid"
        order.paidAmount > 0.0 -> if (isBangla) "আংশিক" else "Partial"
        else -> if (isBangla) "বাকি" else "Due"
    }

    val statusColor = when {
        order.dueAmount <= 0.0 -> VividEmeraldAccent
        order.paidAmount > 0.0 -> Color(0xFFFFA000)
        else -> LowStockRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("sale_order_item_${order.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        border = BorderStroke(1.dp, DarkSlateCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        tint = VividEmeraldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = order.invoiceNumber,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = formatTaka(order.totalAmount, currentLanguage),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = VividEmeraldAccent
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (order.customerName.isNotBlank() && order.customerName != "Cash Sale" && order.customerName != "নগদ বিক্রি")
                            order.customerName
                        else
                            (if (isBangla) "নগদ বিক্রি (Cash)" else "Cash Customer"),
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "$formattedTime • ${order.totalItemsCount} ${if (isBangla) "টি আইটেম" else "items"}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InvoiceDetailDialog(
    order: SaleOrder,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(order.timestamp) { dateFormat.format(Date(order.timestamp)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.invoiceNumber,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = VividEmeraldAccent
                    )
                    Text(
                        text = formattedTime,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Customer Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurfaceVariant),
                    border = BorderStroke(1.dp, DarkSlateCardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (isBangla) "গ্রাহকের তথ্য" else "Customer Information",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = if (order.customerName.isNotBlank()) order.customerName else (if (isBangla) "নগদ গ্রাহক" else "Cash Customer"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (order.customerPhone.isNotBlank()) {
                            Text(
                                text = order.customerPhone,
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Items list
                Text(
                    text = if (isBangla) "বিক্রিত পণ্যসমূহ:" else "Sold Items:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = order.itemsSummary,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSlateSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                )

                HorizontalDivider(color = DarkSlateCardBorder)

                // Financial Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (isBangla) "মোট বিল" else "Subtotal", color = TextMuted, fontSize = 13.sp)
                    Text(text = formatTaka(order.subtotal, currentLanguage), color = TextPrimary, fontSize = 13.sp)
                }

                if (order.discount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (isBangla) "ছাড় (ডিসকাউন্ট)" else "Discount", color = LowStockRed, fontSize = 13.sp)
                        Text(text = "- ${formatTaka(order.discount, currentLanguage)}", color = LowStockRed, fontSize = 13.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isBangla) "সর্বমোট প্রদেয়" else "Final Total",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = formatTaka(order.totalAmount, currentLanguage),
                        fontWeight = FontWeight.Bold,
                        color = VividEmeraldAccent,
                        fontSize = 16.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (isBangla) "পরিশোধিত" else "Paid Amount", color = VividEmeraldAccent, fontSize = 13.sp)
                    Text(text = formatTaka(order.paidAmount, currentLanguage), color = VividEmeraldAccent, fontSize = 13.sp)
                }

                if (order.dueAmount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (isBangla) "বকেয়া (বাকি)" else "Due Balance", color = LowStockRed, fontSize = 13.sp)
                        Text(text = formatTaka(order.dueAmount, currentLanguage), color = LowStockRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (isBangla) "লাভ (Profit)" else "Net Profit", color = TextMuted, fontSize = 13.sp)
                    Text(
                        text = formatTaka(order.profit, currentLanguage, isMaskable = true),
                        color = if (order.profit >= 0) VividEmeraldAccent else LowStockRed,
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val shopName = ProfileManager.activeProfile?.name ?: "Dokan"
                        val shareText = buildString {
                            appendLine("===== $shopName =====")
                            appendLine("${if (isBangla) "চালান নং: " else "Invoice: "}${order.invoiceNumber}")
                            appendLine("${if (isBangla) "তারিখ: " else "Date: "}$formattedTime")
                            appendLine("${if (isBangla) "গ্রাহক: " else "Customer: "}${order.customerName}")
                            appendLine("--------------------------")
                            appendLine(order.itemsSummary)
                            appendLine("--------------------------")
                            appendLine("${if (isBangla) "সর্বমোট: " else "Total: "}৳${order.totalAmount.toLong()}")
                            appendLine("${if (isBangla) "জমা: " else "Paid: "}৳${order.paidAmount.toLong()}")
                            if (order.dueAmount > 0) {
                                appendLine("${if (isBangla) "বকেয়া: " else "Due: "}৳${order.dueAmount.toLong()}")
                            }
                            appendLine("==========================")
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, order.invoiceNumber)
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Invoice"))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isBangla) "চালান শেয়ার" else "Share")
                }

                TextButton(onClick = onDismiss) {
                    Text(if (isBangla) "বন্ধ করুন" else "Close", color = TextMuted)
                }
            }
        }
    )
}
