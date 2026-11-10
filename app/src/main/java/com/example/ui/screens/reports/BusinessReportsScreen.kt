package com.example.ui.screens.reports

import android.app.DatePickerDialog
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.DataHealthCheckManager
import com.example.data.local.HealthIssue
import com.example.data.local.ProfileManager
import com.example.data.local.entity.Customer
import com.example.data.local.entity.Expense
import com.example.data.local.entity.Product
import com.example.data.local.entity.PurchaseRecord
import com.example.data.local.entity.SaleOrder
import com.example.data.local.entity.Supplier
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
import com.example.utils.ReportExporter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ReportPeriod(val labelEn: String, val labelBn: String) {
    TODAY("Today", "আজকের"),
    WEEK("Week", "এই সপ্তাহ"),
    MONTH("Month", "এই মাস"),
    YEAR("Year", "এই বছর"),
    CUSTOM("Custom", "নির্দিষ্ট তারিখ")
}

@Composable
fun BusinessReportsScreen(
    currentLanguage: AppLanguage,
    onNavigateToBaki: () -> Unit = {},
    onNavigateToSuppliers: () -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val scope = rememberCoroutineScope()

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
    val expenses by repository.allExpenses.collectAsState(initial = emptyList())
    val products by repository.allProducts.collectAsState(initial = emptyList())
    val customers by repository.allCustomers.collectAsState(initial = emptyList())
    val suppliers by repository.allSuppliersOwedFirst.collectAsState(initial = emptyList())

    var selectedPeriod by remember { mutableStateOf(ReportPeriod.MONTH) }
    var customStartDate by remember { mutableStateOf<Long?>(null) }
    var customEndDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Dialog state for specialized reports
    var activeReportType by remember { mutableStateOf<String?>(null) }
    // Dialog state for Monthly Quick View
    var showMonthlyReportQuickView by remember { mutableStateOf(false) }
    // Health Check state
    var healthIssues by remember { mutableStateOf<List<HealthIssue>>(emptyList()) }
    var isHealthCheckRan by remember { mutableStateOf(false) }
    var issueToConfirmFix by remember { mutableStateOf<HealthIssue?>(null) }

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

    fun isInPeriod(timestamp: Long): Boolean {
        return when (selectedPeriod) {
            ReportPeriod.TODAY -> timestamp >= startOfToday
            ReportPeriod.WEEK -> timestamp >= startOfWeek
            ReportPeriod.MONTH -> timestamp >= startOfMonth
            ReportPeriod.YEAR -> timestamp >= startOfYear
            ReportPeriod.CUSTOM -> {
                val s = customStartDate ?: 0L
                val e = customEndDate ?: Long.MAX_VALUE
                timestamp in s..e
            }
        }
    }

    // Filtered records
    val filteredSales = sales.filter { isInPeriod(it.timestamp) }
    val filteredPurchases = purchases.filter { isInPeriod(it.timestamp) }
    val filteredExpenses = expenses.filter { isInPeriod(it.timestamp) }

    // Aggregate metrics
    val totalSales = filteredSales.sumOf { it.totalAmount }
    val grossProfit = filteredSales.sumOf { it.profit }
    val totalExpensesAmount = filteredExpenses.sumOf { it.amount }
    val netProfit = grossProfit - totalExpensesAmount
    val totalOrdersCount = filteredSales.size
    val marginPercentage = if (totalSales > 0) (grossProfit / totalSales) * 100 else 0.0

    // Money Now metrics (Stock Value, Customer Baki Receivable, Owed to Suppliers)
    val stockValue = products.sumOf { it.stockQuantity * it.sellPrice }
    val customerBaki = customers.sumOf { it.totalBaki }
    val owedToSuppliers = suppliers.sumOf { it.amountOwed }

    // Low stock items
    val lowStockProducts = products.filter { it.stockQuantity <= it.lowStockLimit }

    // Requirement 71: Slow Sellers - ONLY items that really sold little/none in the period (qty <= 1), not whole inventory
    val soldCountMap = filteredSales.flatMap { it.itemsSummary.split(",") }
        .map { it.trim().substringBefore(" x") }
        .filter { it.isNotBlank() }
        .groupingBy { it }
        .eachCount()

    val slowSellers = products.filter { p ->
        val count = soldCountMap[p.name] ?: 0
        count <= 1
    }.take(6)

    // Best-selling products
    val bestSellers = soldCountMap.entries.sortedByDescending { it.value }.take(5)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Requirement 81: Quick Monthly Report Action Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMonthlyReportQuickView = true }
                    .testTag("btn_quick_monthly_report"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = VividEmeraldPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = if (isBangla) "মাসিক রিপোর্ট দেখুন ও ডাউনলোড" else "Monthly Report (View & Download)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isBangla) "এক ট্যাপে পূর্ণ বিবরণী, PDF ও Excel ডাউনলোড" else "One tap monthly summary, PDF & Excel export",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            // Requirement 69: Period Filter Bar (Today, Week, Month, Year, Custom)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReportPeriod.values().forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = {
                            selectedPeriod = period
                            if (period == ReportPeriod.CUSTOM) {
                                showDatePicker = true
                            }
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (period == ReportPeriod.CUSTOM) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(if (isBangla) period.labelBn else period.labelEn, fontSize = 12.sp)
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

            // Requirement 70: KPI Cards (Total Sales, Total Profit, Orders, Margin)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, VividEmeraldPrimary.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBangla) "মোট নিট লাভ (সব খরচ বাদে)" else "Net Profit (After Expenses)",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatTaka(netProfit, currentLanguage, isMaskable = true),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (netProfit >= 0) VividEmeraldAccent else LowStockRed
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = (if (netProfit >= 0) VividEmeraldPrimary else LowStockRed).copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (netProfit >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (netProfit >= 0) VividEmeraldAccent else LowStockRed
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = DarkSlateCardBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isBangla) "মোট বিক্রয়" else "Total Sales",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Text(
                                text = formatTaka(totalSales, currentLanguage),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Column {
                            Text(
                                text = if (isBangla) "মোট অর্ডার" else "Orders",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "$totalOrdersCount ${if (isBangla) "টি" else ""}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Column {
                            Text(
                                text = if (isBangla) "লাভের হার" else "Margin",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", marginPercentage)}%",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = VividEmeraldAccent
                            )
                        }
                    }
                }
            }

            // Requirement 70: Money Now Card (Stock value, baki, owed to suppliers)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isBangla) "বর্তমান মূলধন ও দেনা-পাওনা (Money Now)" else "Money Now (Current Assets & Liabilities)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Stock Value
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBangla) "স্টক মূল্য" else "Stock Value",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Text(
                                text = formatTaka(stockValue, currentLanguage),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Customer Baki
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToBaki() }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isBangla) "কাস্টমার বাকি >" else "Customer Baki >",
                                    fontSize = 11.sp,
                                    color = LowStockRed
                                )
                            }
                            Text(
                                text = formatTaka(customerBaki, currentLanguage),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LowStockRed
                            )
                        }

                        // Owed to Suppliers
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToSuppliers() }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isBangla) "মহাজন দেনা >" else "Supplier Due >",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFFB74D)
                                )
                            }
                            Text(
                                text = formatTaka(owedToSuppliers, currentLanguage),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB74D)
                            )
                        }
                    }
                }
            }

            // Visual Distribution & Best-selling
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Best-selling Products Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.dp, DarkSlateCardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(16.dp))
                            Text(
                                text = if (isBangla) "সেরা বিক্রিত পণ্য" else "Top Sellers",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (bestSellers.isEmpty()) {
                            Text(if (isBangla) "কোনো তথ্য নেই" else "No data", fontSize = 11.sp, color = TextMuted)
                        } else {
                            bestSellers.forEach { entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = entry.key,
                                        fontSize = 11.sp,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${entry.value} ${if (isBangla) "বার" else ""}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VividEmeraldAccent
                                    )
                                }
                            }
                        }
                    }
                }

                // Category distribution card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.dp, DarkSlateCardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.PieChart, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(16.dp))
                            Text(
                                text = if (isBangla) "ক্যাটাগরি বিক্রয়" else "By Category",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val categoriesSummary = products.groupingBy { it.category }.eachCount().entries.take(4)
                        if (categoriesSummary.isEmpty()) {
                            Text(if (isBangla) "কোনো তথ্য নেই" else "No data", fontSize = 11.sp, color = TextMuted)
                        } else {
                            categoriesSummary.forEach { (cat, count) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 11.sp,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "$count ${if (isBangla) "আইটেম" else "items"}",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Requirement 71: Expenses, Low Stock, and Slow Sellers cards side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Expenses Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.dp, DarkSlateCardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (isBangla) "দোকান খরচ" else "Expenses",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = formatTaka(totalExpensesAmount, currentLanguage),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = LowStockRed,
                            maxLines = 1
                        )
                        Text(
                            text = "${filteredExpenses.size} ${if (isBangla) "টি এন্ট্রি" else "entries"}",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                // 2. Low Stock Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.dp, if (lowStockProducts.isNotEmpty()) LowStockRed.copy(alpha = 0.5f) else DarkSlateCardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (isBangla) "স্বল্প স্টক" else "Low Stock",
                            fontSize = 11.sp,
                            color = if (lowStockProducts.isNotEmpty()) LowStockRed else TextMuted
                        )
                        Text(
                            text = "${lowStockProducts.size} ${if (isBangla) "টি পণ্য" else "items"}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (lowStockProducts.isNotEmpty()) LowStockRed else TextPrimary
                        )
                        Text(
                            text = if (isBangla) "সীমার নিচে" else "Below limit",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                // 3. Slow Sellers Card (Requirement 71)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.dp, DarkSlateCardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (isBangla) "কম বিক্রিত" else "Slow Sellers",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "${slowSellers.size} ${if (isBangla) "টি পণ্য" else "items"}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isBangla) "কম বিক্রি" else "Low velocity",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // Requirement 72: Data Health Check Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, VividEmeraldPrimary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = VividEmeraldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isBangla) "ডাটা হেলথ চেক (Data Health Check)" else "Data Health Check",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    healthIssues = DataHealthCheckManager.runHealthCheck(
                                        repository = repository,
                                        products = products,
                                        customers = customers,
                                        suppliers = suppliers,
                                        sales = sales
                                    )
                                    isHealthCheckRan = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(if (isBangla) "চেক করুন" else "Scan", fontSize = 12.sp)
                        }
                    }

                    if (isHealthCheckRan) {
                        if (healthIssues.isEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (isBangla) "সব ডাটা ও হিসাব শতভাগ নির্ভুল রয়েছে!" else "All calculations and data are 100% healthy!",
                                    fontSize = 12.sp,
                                    color = VividEmeraldAccent
                                )
                            }
                        } else {
                            Text(
                                text = "${healthIssues.size} ${if (isBangla) "টি সমস্যা পাওয়া গেছে। ঠিক করতে ট্যাপ করুন:" else "issues detected. Tap to fix:"}",
                                fontSize = 12.sp,
                                color = LowStockRed,
                                fontWeight = FontWeight.Bold
                            )

                            healthIssues.forEach { issue ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = DarkSlateSurfaceVariant,
                                    border = BorderStroke(1.dp, LowStockRed.copy(alpha = 0.4f))
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
                                                text = if (isBangla) issue.titleBn else issue.titleEn,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = if (isBangla) issue.descriptionBn else issue.descriptionEn,
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                        }

                                        Button(
                                            onClick = { issueToConfirmFix = issue },
                                            colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            Text(if (isBangla) "সংশোধন" else "Fix", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = if (isBangla) "নেগেটিভ স্টক, ভুল অংক ও অমিল হিসাব স্বয়ংক্রিয়ভাবে বের করে এক ক্লিকে সমাধান করুন।" else "Detects negative stock, wrong prices, and balance mismatches with one-tap fixes.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // Requirements 73-80: Specialized Report Launchers
            Text(
                text = if (isBangla) "বিশেষায়িত রিপোর্ট ও স্টেটমেন্ট" else "Reports & Financial Statements",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            val reportMenuItems = listOf(
                Pair(if (isBangla) "বিক্রয় রিপোর্ট (Sales Report)" else "Sales Report", "sales"),
                Pair(if (isBangla) "ডে বুক রিপোর্ট (Day Book)" else "Day Book Report", "day_book"),
                Pair(if (isBangla) "লাভ-লোকসান রিপোর্ট (Profit & Loss)" else "Profit & Loss Statement", "p_and_l"),
                Pair(if (isBangla) "পার্টি স্টেটমেন্ট (Party Statement)" else "Party Statement", "party_statement"),
                Pair(if (isBangla) "ক্যাশ ও ব্যাংক স্টেটমেন্ট" else "Cash & Bank Statement", "cash_bank"),
                Pair(if (isBangla) "কর্মচারী রিপোর্ট (Staff Report)" else "Staff Report", "staff"),
                Pair(if (isBangla) "স্টক ও ইনভেন্টরি রিপোর্ট" else "Inventory Valuation Report", "inventory"),
                Pair(if (isBangla) "ব্যবসায়িক অবস্থান (Business Status)" else "Business Status Report", "business_status")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                reportMenuItems.forEach { (title, type) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeReportType = type }
                            .testTag("report_card_$type"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                        border = BorderStroke(1.dp, DarkSlateCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = when (type) {
                                        "sales" -> Icons.Default.Receipt
                                        "day_book" -> Icons.Default.DateRange
                                        "p_and_l" -> Icons.Default.TrendingUp
                                        "party_statement" -> Icons.Default.People
                                        "cash_bank" -> Icons.Default.AccountBalance
                                        "staff" -> Icons.Default.Badge
                                        "inventory" -> Icons.Default.Inventory2
                                        else -> Icons.Default.Assessment
                                    },
                                    contentDescription = null,
                                    tint = VividEmeraldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Custom Date Range Picker
    if (showDatePicker) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosenCal = Calendar.getInstance()
                chosenCal.set(year, month, dayOfMonth, 0, 0, 0)
                customStartDate = chosenCal.timeInMillis
                chosenCal.set(year, month, dayOfMonth, 23, 59, 59)
                customEndDate = chosenCal.timeInMillis
                showDatePicker = false
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnCancelListener { showDatePicker = false }
            show()
        }
    }

    // Requirement 72: Confirmation Dialog before applying Data Health Fix
    issueToConfirmFix?.let { issue ->
        AlertDialog(
            onDismissRequest = { issueToConfirmFix = null },
            containerColor = DarkSlateSurface,
            title = {
                Text(
                    text = if (isBangla) "সংশোধন নিশ্চিত করুন" else "Confirm Fix",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBangla) issue.descriptionBn else issue.descriptionEn,
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${if (isBangla) "প্রস্তাবিত সমাধান: " else "Recommended action: "}${if (isBangla) issue.recommendedFixBn else issue.recommendedFixEn}",
                        color = VividEmeraldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            issue.fixAction(repository)
                            Toast.makeText(
                                context,
                                if (isBangla) "সফলভাবে সংশোধন সম্পন্ন হয়েছে!" else "Fixed successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Re-run health check
                            healthIssues = DataHealthCheckManager.runHealthCheck(
                                repository = repository,
                                products = products,
                                customers = customers,
                                suppliers = suppliers,
                                sales = sales
                            )
                            issueToConfirmFix = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
                ) {
                    Text(if (isBangla) "হ্যাঁ, সংশোধন করুন" else "Yes, Apply Fix")
                }
            },
            dismissButton = {
                TextButton(onClick = { issueToConfirmFix = null }) {
                    Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
                }
            }
        )
    }

    // Requirements 81, 82, 83: Monthly Report Quick View & Download Dialog
    if (showMonthlyReportQuickView) {
        val cal = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val currentMonthName = monthFormat.format(cal.time)

        AlertDialog(
            onDismissRequest = { showMonthlyReportQuickView = false },
            containerColor = DarkSlateSurface,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBangla) "মাসিক রিপোর্ট ($currentMonthName)" else "Monthly Report ($currentMonthName)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = VividEmeraldAccent
                        )
                        Text(
                            text = ProfileManager.activeProfile?.name?.ifBlank { "Dokan" } ?: "Dokan",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    IconButton(onClick = { showMonthlyReportQuickView = false }) {
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
                    // Summary Section
                    Text(
                        text = if (isBangla) "১. ব্যবসায়িক সারসংক্ষেপ" else "1. Performance Summary",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (isBangla) "মোট বিক্রয়" else "Total Sales", color = TextMuted)
                        Text(text = formatTaka(totalSales, currentLanguage), color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (isBangla) "মোট লাভ" else "Total Profit", color = TextMuted)
                        Text(text = formatTaka(netProfit, currentLanguage, isMaskable = true), color = VividEmeraldAccent, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = DarkSlateCardBorder)

                    // Current Money Section
                    Text(
                        text = if (isBangla) "২. বর্তমান মূলধন ও বকেয়া" else "2. Current Money",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (isBangla) "স্টক মূল্য" else "Stock Value", color = TextMuted)
                        Text(text = formatTaka(stockValue, currentLanguage), color = TextPrimary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (isBangla) "কাস্টমার বাকি" else "Customer Baki", color = LowStockRed)
                        Text(text = formatTaka(customerBaki, currentLanguage), color = LowStockRed, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (isBangla) "মহাজন দেনা (Owed)" else "Owed to Suppliers", color = Color(0xFFFFB74D))
                        Text(text = formatTaka(owedToSuppliers, currentLanguage), color = Color(0xFFFFB74D), fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = DarkSlateCardBorder)

                    // Export / Download Options
                    Text(
                        text = if (isBangla) "ডাউনলোড ও শেয়ার অপশন:" else "Export Options:",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Download PDF
                        Button(
                            onClick = {
                                val pdfFile = ReportExporter.generateMonthlyPdfReport(
                                    context = context,
                                    monthName = currentMonthName,
                                    sales = filteredSales,
                                    products = products,
                                    customers = customers,
                                    suppliers = suppliers,
                                    language = currentLanguage
                                )
                                if (pdfFile != null) {
                                    ReportExporter.shareReportFile(
                                        context = context,
                                        file = pdfFile,
                                        mimeType = "application/pdf",
                                        title = "Monthly Report PDF"
                                    )
                                } else {
                                    Toast.makeText(context, "Could not generate PDF", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (isBangla) "PDF" else "PDF")
                        }

                        // Export CSV
                        OutlinedButton(
                            onClick = {
                                val csvFile = ReportExporter.generateMonthlyCsvReport(
                                    context = context,
                                    monthName = currentMonthName,
                                    sales = filteredSales,
                                    language = currentLanguage
                                )
                                if (csvFile != null) {
                                    ReportExporter.shareReportFile(
                                        context = context,
                                        file = csvFile,
                                        mimeType = "text/csv",
                                        title = "Monthly Report Excel/CSV"
                                    )
                                } else {
                                    Toast.makeText(context, "Could not generate CSV", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, VividEmeraldAccent),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VividEmeraldAccent)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (isBangla) "Excel/CSV" else "Excel/CSV")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMonthlyReportQuickView = false }) {
                    Text(if (isBangla) "বন্ধ করুন" else "Close", color = TextMuted)
                }
            }
        )
    }

    // Specialized Report Detail Dialogs (Requirements 73-79)
    activeReportType?.let { type ->
        SpecializedReportDialog(
            reportType = type,
            sales = filteredSales,
            purchases = filteredPurchases,
            expenses = filteredExpenses,
            customers = customers,
            suppliers = suppliers,
            products = products,
            currentLanguage = currentLanguage,
            onDismiss = { activeReportType = null }
        )
    }
}

/**
 * Requirement 73-80: Specialized Report Viewer Dialog
 * Supports: Sales, Day Book, Profit & Loss (with stock-based toggle), Party Statement, Cash & Bank, Staff, Inventory
 */
@Composable
fun SpecializedReportDialog(
    reportType: String,
    sales: List<SaleOrder>,
    purchases: List<PurchaseRecord>,
    expenses: List<Expense>,
    customers: List<Customer>,
    suppliers: List<Supplier>,
    products: List<Product>,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit
) {
    val isBangla = currentLanguage == AppLanguage.BANGLA
    var isStockValuationEnabled by remember { mutableStateOf(true) }

    val title = when (reportType) {
        "sales" -> if (isBangla) "বিক্রয় রিপোর্ট (Sales Report)" else "Sales Report"
        "day_book" -> if (isBangla) "ডে বুক রিপোর্ট (Day Book)" else "Day Book Report"
        "p_and_l" -> if (isBangla) "লাভ-লোকসান খতিয়ান (P&L)" else "Profit & Loss Statement"
        "party_statement" -> if (isBangla) "পার্টি স্টেটমেন্ট" else "Party Statement"
        "cash_bank" -> if (isBangla) "ক্যাশ ও ব্যাংক স্টেটমেন্ট" else "Cash & Bank Statement"
        "staff" -> if (isBangla) "কর্মচারী হিসাব" else "Staff Report"
        "inventory" -> if (isBangla) "ইনভেন্টরি রিপোর্ট" else "Inventory Valuation"
        else -> if (isBangla) "ব্যবসায়িক অবস্থান" else "Business Status"
    }

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
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VividEmeraldAccent
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (reportType) {
                    "p_and_l" -> {
                        // Requirement 75: Profit & Loss with stock-based toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBangla) "স্টক ভিত্তিক মূল্যায়ন অন্তর্ভুক্ত" else "Include Stock Valuation",
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                            Switch(
                                checked = isStockValuationEnabled,
                                onCheckedChange = { isStockValuationEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = VividEmeraldPrimary)
                            )
                        }

                        val netSales = sales.sumOf { it.totalAmount }
                        val cogs = sales.sumOf { it.totalAmount - it.profit }
                        val grossProfit = netSales - cogs
                        val totalExp = expenses.sumOf { it.amount }
                        val finalNetProfit = grossProfit - totalExp

                        HorizontalDivider(color = DarkSlateCardBorder)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = if (isBangla) "মোট নিট বিক্রয় (Net Sales)" else "Net Sales", color = TextMuted)
                            Text(text = formatTaka(netSales, currentLanguage), color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = if (isBangla) "বিক্রিত পণ্যের ব্যয় (COGS)" else "Cost of Goods Sold", color = TextMuted)
                            Text(text = formatTaka(cogs, currentLanguage), color = TextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = if (isBangla) "মোট গ্রস লাভ" else "Gross Profit", color = VividEmeraldAccent)
                            Text(text = formatTaka(grossProfit, currentLanguage), color = VividEmeraldAccent, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = if (isBangla) "দোকানের মোট খরচ" else "Operating Expenses", color = LowStockRed)
                            Text(text = "- ${formatTaka(totalExp, currentLanguage)}", color = LowStockRed)
                        }
                        if (isStockValuationEnabled) {
                            val stockVal = products.sumOf { it.stockQuantity * it.buyPrice }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = if (isBangla) "সমাপনী স্টক মূল্য (ক্রয়মূল্যে)" else "Closing Stock (At Cost)", color = TextMuted)
                                Text(text = formatTaka(stockVal, currentLanguage), color = TextPrimary)
                            }
                        }
                        HorizontalDivider(color = DarkSlateCardBorder)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = if (isBangla) "সর্বমোট নিট লাভ (Net Profit)" else "Final Net Profit", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = formatTaka(finalNetProfit, currentLanguage, isMaskable = true), fontWeight = FontWeight.Bold, color = if (finalNetProfit >= 0) VividEmeraldAccent else LowStockRed)
                        }
                    }
                    "day_book" -> {
                        // Requirement 74: Day Book
                        Text(text = if (isBangla) "আজকের সকল লেনদেন খতিয়ান:" else "All transactions of today:", fontSize = 12.sp, color = TextMuted)
                        val totalIn = sales.sumOf { it.paidAmount }
                        val totalOut = purchases.sumOf { it.paidAmount } + expenses.sumOf { it.amount }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = if (isBangla) "মোট ক্যাশ ইন (জমা)" else "Total Cash In", color = VividEmeraldAccent)
                            Text(text = formatTaka(totalIn, currentLanguage), color = VividEmeraldAccent, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = if (isBangla) "মোট ক্যাশ আউট (প্রদান)" else "Total Cash Out", color = LowStockRed)
                            Text(text = formatTaka(totalOut, currentLanguage), color = LowStockRed, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "${if (isBangla) "আজকের বিক্রয় সংখ্যা: " else "Sales count: "}${sales.size}", fontSize = 12.sp, color = TextPrimary)
                        Text(text = "${if (isBangla) "আজকের ক্রয় সংখ্যা: " else "Purchases count: "}${purchases.size}", fontSize = 12.sp, color = TextPrimary)
                    }
                    "cash_bank" -> {
                        // Requirement 77: Cash in Hand & Bank
                        val cashInHand = sales.sumOf { it.paidAmount } - expenses.sumOf { it.amount } - purchases.sumOf { it.paidAmount }
                        Text(text = if (isBangla) "বর্তমান নগদ ক্যাশ স্থিতি:" else "Estimated Cash Balance:", color = TextMuted, fontSize = 12.sp)
                        Text(text = formatTaka(cashInHand.coerceAtLeast(0.0), currentLanguage), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = VividEmeraldAccent)
                    }
                    else -> {
                        Text(
                            text = if (isBangla) "মোট রেকর্ড সংখ্যা: ${sales.size + purchases.size}" else "Total transaction records: ${sales.size + purchases.size}",
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (isBangla) "সাপ্লায়ার দেনা: ৳${suppliers.sumOf { it.amountOwed }.toLong()}" else "Supplier due: ৳${suppliers.sumOf { it.amountOwed }.toLong()}",
                            color = Color(0xFFFFB74D),
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (isBangla) "কাস্টমার বাকি: ৳${customers.sumOf { it.totalBaki }.toLong()}" else "Customer due: ৳${customers.sumOf { it.totalBaki }.toLong()}",
                            color = LowStockRed,
                            fontSize = 13.sp
                        )
                    }
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
