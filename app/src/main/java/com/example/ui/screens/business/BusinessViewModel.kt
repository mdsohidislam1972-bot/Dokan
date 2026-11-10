package com.example.ui.screens.business

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.Expense
import com.example.data.local.entity.PurchaseRecord
import com.example.data.local.entity.SaleOrder
import com.example.data.local.entity.Supplier
import com.example.data.repository.HardwareRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BusinessFinancialSummary(
    val totalRevenue: Double = 0.0,
    val grossProfit: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val customerReceivableDue: Double = 0.0,
    val supplierPayableDena: Double = 0.0
)

class BusinessViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    val repository = HardwareRepository(
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

    val suppliers: StateFlow<List<Supplier>> = repository.allSuppliersOwedFirst
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<SaleOrder>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchases: StateFlow<List<PurchaseRecord>> = repository.allPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val financialSummary: StateFlow<BusinessFinancialSummary> = combine(
        repository.allSales,
        repository.allExpenses,
        repository.allCustomers,
        repository.allSuppliersOwedFirst
    ) { salesList, expenseList, customerList, supplierList ->
        val revenue = salesList.sumOf { it.totalAmount }
        val gross = salesList.sumOf { it.profit }
        val exp = expenseList.sumOf { it.amount }
        val net = gross - exp
        val custDue = customerList.sumOf { it.totalBaki }
        val suppDena = supplierList.sumOf { it.amountOwed }

        BusinessFinancialSummary(
            totalRevenue = revenue,
            grossProfit = gross,
            totalExpenses = exp,
            netProfit = net,
            customerReceivableDue = custDue,
            supplierPayableDena = suppDena
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BusinessFinancialSummary())

    fun addSupplier(name: String, phone: String, company: String, openingDue: Double) {
        viewModelScope.launch {
            val supplier = Supplier(
                name = name.trim(),
                phone = phone.trim(),
                company = company.trim(),
                amountOwed = openingDue
            )
            repository.insertSupplier(supplier)
        }
    }

    fun recordSupplierPayment(
        supplierId: Long,
        amount: Double,
        method: String,
        note: String
    ) {
        viewModelScope.launch {
            repository.recordSupplierPayment(
                supplierId = supplierId,
                amount = amount,
                paymentMethod = method,
                note = note
            )
        }
    }

    fun addExpense(title: String, amount: Double, category: String, note: String) {
        viewModelScope.launch {
            val expense = Expense(
                title = title.trim(),
                amount = amount,
                category = category.trim().ifBlank { "General" },
                note = note.trim()
            )
            repository.insertExpense(expense)
        }
    }

    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch {
            repository.deleteExpense(expenseId)
        }
    }
}
