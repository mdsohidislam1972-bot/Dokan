package com.example.ui.screens.home

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.Product
import com.example.data.local.entity.SaleOrder
import com.example.data.repository.HardwareRepository
import com.example.ui.localization.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    val repository = AppDatabase.getRepository(application)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureDefaultCashDrawerAndMergeDuplicates()
        }
    }

    val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSales: StateFlow<List<SaleOrder>> = repository.recentSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customerBaki: StateFlow<Double> = repository.allCustomers.map { list ->
        list.sumOf { it.totalBaki }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val supplierDue: StateFlow<Double> = repository.totalAmountOwedToSuppliers.map {
        it ?: 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Read the exact total balance from all Cash & Bank accounts
    val totalBalance: StateFlow<Double> = repository.allBankAccounts.map { list ->
        list.sumOf { it.currentBalance }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayMetrics: StateFlow<HomeSummaryMetrics> = repository.getTodaySales().map { sales ->
        HomeSummaryMetrics(
            totalSales = sales.sumOf { it.totalAmount },
            totalProfit = sales.sumOf { it.profit },
            ordersCount = sales.size,
            itemsSoldCount = sales.sumOf { it.totalItemsCount }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeSummaryMetrics())

    val periodFigures: StateFlow<PeriodFigures> = combine(
        repository.allSales,
        repository.allPurchases,
        repository.allExpenses
    ) { sales, purchases, expenses ->
        PeriodFigures(
            totalSales = sales.sumOf { it.totalAmount },
            totalPurchases = purchases.sumOf { it.totalCost },
            totalExpenses = expenses.sumOf { it.amount }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PeriodFigures())

    val cashflow7Days: StateFlow<CashflowState> = combine(
        repository.allSales,
        repository.allPurchases,
        repository.allExpenses
    ) { sales, purchases, expenses ->
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - (7L * 24 * 60 * 60 * 1000L)
        val recentSalesPaid = sales.filter { it.timestamp >= sevenDaysAgo }.sumOf { it.paidAmount }
        val recentPurchasesPaid = purchases.filter { it.timestamp >= sevenDaysAgo }.sumOf { it.paidAmount }
        val recentExpenses = expenses.filter { it.timestamp >= sevenDaysAgo }.sumOf { it.amount }
        CashflowState(
            days = listOf("শনি", "রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র"),
            totalCashIn = recentSalesPaid,
            totalCashOut = recentPurchasesPaid + recentExpenses
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CashflowState())

    private val _shortcuts = MutableStateFlow(
        listOf(
            AppShortcut("add_party", "Add Party", "নতুন পার্টি", Icons.Default.PersonAdd),
            AppShortcut("sales_invoice", "New Sale", "বিক্রি চালান", Icons.Default.PointOfSale),
            AppShortcut("payment_in", "Payment In", "টাকা জমা", Icons.Default.CallReceived),
            AppShortcut("payment_out", "Payment Out", "টাকা প্রদান", Icons.Default.CallMade),
            AppShortcut("purchase", "Add Purchase", "ক্রয় চালান", Icons.Default.ShoppingBag),
            AppShortcut("add_item", "Add Product", "নতুন পণ্য", Icons.Default.AddBox),
            AppShortcut("expense", "Expense", "খরচের হিসাব", Icons.Default.ReceiptLong),
            AppShortcut("sales_return", "Sales Return", "বিক্রি ফেরত", Icons.Default.AssignmentReturn),
            AppShortcut("quotation", "Quotation", "কোটেশন / বিল", Icons.Default.RequestQuote)
        )
    )
    val shortcuts: StateFlow<List<AppShortcut>> = _shortcuts.asStateFlow()

    var bannerTitle by mutableStateOf("দোকান মাল্টি-ভেন্ডর মার্কেটপ্লেস টিপস")
    var bannerSubtitle by mutableStateOf("দৈনিক বিক্রয় ও ভেন্ডর হিসাব স্বয়ংক্রিয়ভাবে ক্লাউড ব্যাকআপ রাখুন")

    init {
        viewModelScope.launch {
            repository.seedInitialHardwareDataIfNeeded()
        }
    }

    fun getFormattedDate(language: AppLanguage): String {
        val format = if (language == AppLanguage.BANGLA) "EEEE, dd MMMM yyyy" else "EEEE, MMM dd, yyyy"
        val sdf = SimpleDateFormat(format, if (language == AppLanguage.BANGLA) Locale("bn", "BD") else Locale.ENGLISH)
        return sdf.format(Date())
    }

    fun updateBanner(title: String, subtitle: String) {
        bannerTitle = title
        bannerSubtitle = subtitle
    }

    fun toggleShortcut(shortcut: AppShortcut) {
        _shortcuts.value = _shortcuts.value.map {
            if (it.id == shortcut.id) it.copy(isEnabled = !it.isEnabled) else it
        }
    }
}
