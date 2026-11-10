package com.example.ui.screens.sell

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.Category
import com.example.data.local.entity.Customer
import com.example.data.local.entity.Product
import com.example.data.local.entity.SaleOrder
import com.example.data.repository.HardwareRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SellViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = HardwareRepository(
        database.productDao(),
        database.saleOrderDao(),
        database.categoryDao(),
        database.purchaseDao(),
        database.customerDao(),
        database.supplierDao(),
        database.supplierPaymentDao()
    )

    // Search Query & Category Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = combine(_searchQuery, _selectedCategory) { query, cat ->
        Pair(query, cat)
    }.flatMapLatest { (query, cat) ->
        repository.filterProducts(query, cat)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selection State: exactly one product selected at a time
    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    // Sell Form Bottom Sheet State
    private val _isSellSheetOpen = MutableStateFlow(false)
    val isSellSheetOpen: StateFlow<Boolean> = _isSellSheetOpen.asStateFlow()

    // Form inputs for the selected product sale
    private val _quantityText = MutableStateFlow("1")
    val quantityText: StateFlow<String> = _quantityText.asStateFlow()

    private val _sellingPriceText = MutableStateFlow("")
    val sellingPriceText: StateFlow<String> = _sellingPriceText.asStateFlow()

    private val _discountText = MutableStateFlow("")
    val discountText: StateFlow<String> = _discountText.asStateFlow()

    private val _isCreditSale = MutableStateFlow(false)
    val isCreditSale: StateFlow<Boolean> = _isCreditSale.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _paidNowText = MutableStateFlow("")
    val paidNowText: StateFlow<String> = _paidNowText.asStateFlow()

    private val _dueDateDays = MutableStateFlow<Int?>(null)
    val dueDateDays: StateFlow<Int?> = _dueDateDays.asStateFlow()

    private val _showNewCustomerDialog = MutableStateFlow(false)
    val showNewCustomerDialog: StateFlow<Boolean> = _showNewCustomerDialog.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _completedSaleOrder = MutableStateFlow<SaleOrder?>(null)
    val completedSaleOrder: StateFlow<SaleOrder?> = _completedSaleOrder.asStateFlow()

    // Multi-item Cart Support (Requirement 34 & 40)
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Notes or remarks on sale (Requirement 36)
    private val _notesText = MutableStateFlow("")
    val notesText: StateFlow<String> = _notesText.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialHardwareDataIfNeeded()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    /**
     * Finds and selects a product by barcode. If found and not out of stock, selects it or adds to cart.
     */
    fun findProductByBarcode(barcode: String): Product? {
        val trimmed = barcode.trim()
        if (trimmed.isBlank()) return null
        val found = products.value.find { it.barcode.equals(trimmed, ignoreCase = true) }
        if (found != null) {
            selectProduct(found)
        }
        return found
    }

    /**
     * Taps an item card to select it.
     * Items with zero stock cannot be selected if prevent out-of-stock is enabled.
     * Tapping another item switches selection. Tapping current item toggles it off.
     */
    fun selectProduct(product: Product): Boolean {
        if (product.stockQuantity <= 0 && com.example.data.local.AppSettings.isPreventOutOfStockSaleEnabled) {
            _errorMessage.value = "Product is out of stock!"
            return false
        }
        if (_selectedProduct.value?.id == product.id) {
            _selectedProduct.value = null
        } else {
            _selectedProduct.value = product
            // Check party rate if customer is already selected
            val customer = _selectedCustomer.value
            if (customer != null) {
                val partyRate = com.example.data.local.PartyRateManager.getPartyRate(customer.id, product.id)
                if (partyRate != null) {
                    _sellingPriceText.value = if (partyRate % 1.0 == 0.0) partyRate.toLong().toString() else partyRate.toString()
                }
            }
        }
        return true
    }

    fun clearSelection() {
        _selectedProduct.value = null
    }

    /**
     * Opens the sell form popup/bottom sheet for the selected item,
     * pre-filling quantity (1), selling price from stock, and clearing discount/credit.
     */
    fun openSellForm() {
        val product = _selectedProduct.value ?: return
        if (product.stockQuantity <= 0) return

        _quantityText.value = "1"
        _sellingPriceText.value = if (product.sellPrice % 1.0 == 0.0) {
            product.sellPrice.toLong().toString()
        } else {
            product.sellPrice.toString()
        }
        _discountText.value = ""
        _isCreditSale.value = false
        _selectedCustomer.value = null
        _paidNowText.value = ""
        _dueDateDays.value = null
        _errorMessage.value = null
        _isSellSheetOpen.value = true
    }

    fun closeSellForm() {
        _isSellSheetOpen.value = false
        _errorMessage.value = null
    }

    fun setQuantityText(text: String) {
        val sanitized = text.filter { it.isDigit() }
        _quantityText.value = sanitized
        _errorMessage.value = null
    }

    fun incrementQuantity() {
        val current = _quantityText.value.toIntOrNull() ?: 0
        val max = _selectedProduct.value?.stockQuantity ?: 9999
        if (current < max) {
            _quantityText.value = (current + 1).toString()
        }
        _errorMessage.value = null
    }

    fun decrementQuantity() {
        val current = _quantityText.value.toIntOrNull() ?: 1
        if (current > 1) {
            _quantityText.value = (current - 1).toString()
        }
        _errorMessage.value = null
    }

    fun setSellingPriceText(text: String) {
        val sanitized = text.filter { it.isDigit() || it == '.' }
        _sellingPriceText.value = sanitized
    }

    fun setDiscountText(text: String) {
        val sanitized = text.filter { it.isDigit() || it == '.' }
        _discountText.value = sanitized
    }

    fun setCreditSale(isCredit: Boolean) {
        _isCreditSale.value = isCredit
        if (!isCredit) {
            _selectedCustomer.value = null
            _paidNowText.value = ""
            _dueDateDays.value = null
        }
    }

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
        _errorMessage.value = null

        // Item 50: Party-wise item rate - automatically applied when selling to them
        val product = _selectedProduct.value
        if (customer != null && product != null) {
            val partyRate = com.example.data.local.PartyRateManager.getPartyRate(customer.id, product.id)
            if (partyRate != null) {
                _sellingPriceText.value = if (partyRate % 1.0 == 0.0) partyRate.toLong().toString() else partyRate.toString()
            }
        }
    }

    fun setPaidNowText(text: String) {
        val sanitized = text.filter { it.isDigit() || it == '.' }
        _paidNowText.value = sanitized
    }

    fun setDueDateDays(days: Int?) {
        _dueDateDays.value = days
    }

    fun openNewCustomerDialog() {
        _showNewCustomerDialog.value = true
    }

    fun closeNewCustomerDialog() {
        _showNewCustomerDialog.value = false
    }

    fun addNewCustomer(name: String, phone: String, address: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val customer = Customer(
                name = name.trim(),
                phone = phone.trim(),
                address = address.trim(),
                totalBaki = 0.0,
                lastTransactionTime = System.currentTimeMillis()
            )
            val newId = repository.insertCustomer(customer)
            _selectedCustomer.value = customer.copy(id = newId)
            closeNewCustomerDialog()
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun dismissSuccessDialog() {
        _completedSaleOrder.value = null
    }

    /**
     * Confirms the sale:
     * - Validates quantity > 0 and <= stockQuantity (blocks sale with warning if exceeded)
     * - Reduces inventory stock
     * - Creates SaleOrder in database
     * - Updates customer baki if credit sale
     * - Clears selection and closes the form
     */
    fun confirmSale(isBangla: Boolean, onSuccess: (SaleOrder) -> Unit = {}) {
        val product = _selectedProduct.value
        if (product == null) {
            _errorMessage.value = if (isBangla) "কোনো পণ্য নির্বাচিত করা হয়নি!" else "No product selected!"
            return
        }

        val qty = _quantityText.value.toIntOrNull() ?: 0
        if (qty <= 0) {
            _errorMessage.value = if (isBangla) "পরিমাণ কমপক্ষে ১ হতে হবে!" else "Quantity must be at least 1!"
            return
        }

        if (qty > product.stockQuantity) {
            val unit = product.localizedUnit(isBangla)
            _errorMessage.value = if (isBangla)
                "মজুদের চেয়ে বেশি পরিমাণ! বর্তমানে মজুদ আছে মাত্র ${product.stockQuantity} $unit"
            else
                "Quantity exceeds available stock! Max available: ${product.stockQuantity} $unit"
            return
        }

        val unitPrice = _sellingPriceText.value.toDoubleOrNull() ?: product.sellPrice
        val subtotal = qty * unitPrice
        val rawDiscount = _discountText.value.toDoubleOrNull() ?: 0.0
        // Requirement 32: discount must never be bigger than price, total never below zero
        val discount = rawDiscount.coerceIn(0.0, subtotal)
        val totalAmount = (subtotal - discount).coerceAtLeast(0.0)

        val isCredit = _isCreditSale.value
        val customer = _selectedCustomer.value

        if (isCredit && customer == null) {
            _errorMessage.value = if (isBangla)
                "বাকিতে বিক্রির জন্য গ্রাহক নির্বাচন করুন অথবা নতুন গ্রাহক যোগ করুন!"
            else
                "Please select or add a customer for credit (Baki) sale!"
            return
        }

        val paidAmount: Double
        val dueAmount: Double

        if (isCredit) {
            val enteredPaid = _paidNowText.value.toDoubleOrNull() ?: 0.0
            paidAmount = enteredPaid.coerceIn(0.0, totalAmount)
            dueAmount = (totalAmount - paidAmount).coerceAtLeast(0.0)
        } else {
            paidAmount = totalAmount
            dueAmount = 0.0
        }

        val now = System.currentTimeMillis()
        val dueDateTimestamp = _dueDateDays.value?.let { days ->
            now + (days.toLong() * 24 * 60 * 60 * 1000L)
        }

        val invoiceNum = "INV-${(now % 900000) + 100000}"
        val note = _notesText.value.trim()
        val summaryText = buildString {
            append("${product.localizedName(isBangla)} x$qty")
            if (note.isNotBlank()) {
                append(" (${if (isBangla) "নোট" else "Note"}: $note)")
            }
        }

        val order = SaleOrder(
            invoiceNumber = invoiceNum,
            timestamp = now,
            customerId = customer?.id,
            customerName = customer?.name ?: (if (isBangla) "ক্যাশ গ্রাহক" else "Walk-in Customer"),
            customerPhone = customer?.phone ?: "",
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            dueAmount = dueAmount,
            totalCost = product.buyPrice * qty,
            totalItemsCount = qty,
            summaryItems = summaryText,
            dueDate = dueDateTimestamp,
            isCredit = isCredit && dueAmount > 0
        )

        viewModelScope.launch {
            repository.completeSale(order, listOf(Pair(product.id, qty)))
            _completedSaleOrder.value = order
            _isSellSheetOpen.value = false
            _selectedProduct.value = null
            _notesText.value = ""
            onSuccess(order)
        }
    }

    // Cart Operations (Requirements 34 & 40)
    fun addToCart(product: Product, quantity: Int = 1) {
        if (product.stockQuantity <= 0) return
        val current = _cartItems.value.toMutableList()
        val idx = current.indexOfFirst { it.product.id == product.id }
        if (idx >= 0) {
            val existing = current[idx]
            val newQty = (existing.quantity + quantity).coerceAtMost(product.stockQuantity)
            current[idx] = existing.copy(quantity = newQty)
        } else {
            current.add(CartItem(product = product, quantity = quantity.coerceAtMost(product.stockQuantity), unitPrice = product.sellPrice))
        }
        _cartItems.value = current
    }

    fun updateCartItemQuantity(productId: Long, delta: Int) {
        val current = _cartItems.value.toMutableList()
        val idx = current.indexOfFirst { it.product.id == productId }
        if (idx >= 0) {
            val item = current[idx]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                current.removeAt(idx)
            } else {
                val max = item.product.stockQuantity
                current[idx] = item.copy(quantity = newQty.coerceAtMost(max))
            }
            _cartItems.value = current
        }
    }

    fun removeFromCart(productId: Long) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun setNotesText(text: String) {
        _notesText.value = text
    }

    fun confirmCartSale(
        isBangla: Boolean,
        customer: Customer?,
        rawDiscount: Double,
        isCredit: Boolean,
        paidNow: Double,
        notes: String,
        onSuccess: (SaleOrder) -> Unit = {}
    ) {
        val items = _cartItems.value
        if (items.isEmpty()) return

        val subtotal = items.sumOf { it.total }
        val discount = rawDiscount.coerceIn(0.0, subtotal)
        val totalAmount = (subtotal - discount).coerceAtLeast(0.0)

        val paidAmount: Double
        val dueAmount: Double
        if (isCredit) {
            paidAmount = paidNow.coerceIn(0.0, totalAmount)
            dueAmount = (totalAmount - paidAmount).coerceAtLeast(0.0)
        } else {
            paidAmount = totalAmount
            dueAmount = 0.0
        }

        val now = System.currentTimeMillis()
        val invoiceNum = "INV-${(now % 900000) + 100000}"
        val summary = items.joinToString(", ") { "${it.product.localizedName(isBangla)} x${it.quantity}" } +
                (if (notes.isNotBlank()) " (${if (isBangla) "নোট" else "Note"}: ${notes.trim()})" else "")

        val totalCost = items.sumOf { it.product.buyPrice * it.quantity }
        val totalItemsCount = items.sumOf { it.quantity }

        val order = SaleOrder(
            invoiceNumber = invoiceNum,
            timestamp = now,
            customerId = customer?.id,
            customerName = customer?.name ?: (if (isBangla) "ক্যাশ গ্রাহক" else "Walk-in Customer"),
            customerPhone = customer?.phone ?: "",
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            dueAmount = dueAmount,
            totalCost = totalCost,
            totalItemsCount = totalItemsCount,
            summaryItems = summary,
            isCredit = isCredit && dueAmount > 0
        )

        viewModelScope.launch {
            repository.completeSale(order, items.map { Pair(it.product.id, it.quantity) })
            _completedSaleOrder.value = order
            _cartItems.value = emptyList()
            onSuccess(order)
        }
    }
}

/**
 * Data class representing an item added to the multi-product sales cart.
 */
data class CartItem(
    val product: Product,
    val quantity: Int,
    val unitPrice: Double
) {
    val total: Double get() = quantity * unitPrice
}

