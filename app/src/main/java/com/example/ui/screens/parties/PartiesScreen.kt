package com.example.ui.screens.parties

import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.PartyRateManager
import com.example.data.local.ProfileManager
import com.example.data.local.entity.Customer
import com.example.data.local.entity.Product
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
import kotlinx.coroutines.launch

@Composable
fun PartiesScreen(
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

    val customers by repository.allCustomers.collectAsState(initial = emptyList())
    val suppliers by repository.allSuppliersOwedFirst.collectAsState(initial = emptyList())
    val products by repository.allProducts.collectAsState(initial = emptyList())

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Customers, 1: Suppliers
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    var showAddPartyDialog by remember { mutableStateOf(false) }
    var customerForSpecialRate by remember { mutableStateOf<Customer?>(null) }
    var customerForPaymentIn by remember { mutableStateOf<Customer?>(null) }
    var supplierForPaymentOut by remember { mutableStateOf<Supplier?>(null) }

    val customerCategories = listOf("All", "Regular", "Wholesale", "VIP", "Retail")
    val supplierCategories = listOf("All", "Hardware", "Paint", "Cement", "Sanitary", "Electrical", "General")

    val filteredCustomers = customers.filter { c ->
        val matchesQuery = searchQuery.isBlank() ||
                c.name.contains(searchQuery, ignoreCase = true) ||
                c.phone.contains(searchQuery)
        val matchesCategory = selectedCategoryFilter == null || selectedCategoryFilter == "All" ||
                c.category.equals(selectedCategoryFilter, ignoreCase = true)
        matchesQuery && matchesCategory
    }

    val filteredSuppliers = suppliers.filter { s ->
        val matchesQuery = searchQuery.isBlank() ||
                s.name.contains(searchQuery, ignoreCase = true) ||
                s.company.contains(searchQuery, ignoreCase = true) ||
                s.phone.contains(searchQuery)
        val matchesCategory = selectedCategoryFilter == null || selectedCategoryFilter == "All" ||
                s.category.equals(selectedCategoryFilter, ignoreCase = true)
        matchesQuery && matchesCategory
    }

    val totalCustomerDue = customers.sumOf { it.totalBaki }
    val totalSupplierDena = suppliers.sumOf { it.amountOwed }

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
            // Header Stats Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTabIndex = 0 }
                    ) {
                        Text(
                            text = if (isBangla) "কাস্টমারদের মোট বাকি" else "Total Customer Due",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = formatTaka(totalCustomerDue, currentLanguage, isMaskable = true),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = LowStockRed
                        )
                        Text(
                            text = "${customers.size} ${if (isBangla) "জন গ্রাহক" else "Customers"}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(DarkSlateCardBorder)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                            .clickable { selectedTabIndex = 1 }
                    ) {
                        Text(
                            text = if (isBangla) "মহাজনদের মোট দেনা" else "Total Supplier Due",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = formatTaka(totalSupplierDena, currentLanguage, isMaskable = true),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFA000)
                        )
                        Text(
                            text = "${suppliers.size} ${if (isBangla) "জন মহাজন" else "Suppliers"}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = DarkSlateSurface,
                contentColor = VividEmeraldAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = VividEmeraldPrimary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = {
                        selectedTabIndex = 0
                        selectedCategoryFilter = null
                    },
                    text = {
                        Text(
                            text = "${if (isBangla) "কাস্টমার" else "Customers"} (${customers.size})",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                )

                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = {
                        selectedTabIndex = 1
                        selectedCategoryFilter = null
                    },
                    text = {
                        Text(
                            text = "${if (isBangla) "মহাজন / সাপ্লায়ার" else "Suppliers"} (${suppliers.size})",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("parties_search_input"),
                placeholder = {
                    Text(
                        if (selectedTabIndex == 0)
                            (if (isBangla) "কাস্টমারের নাম বা ফোন নম্বর খুঁজুন..." else "Search customer name or phone...")
                        else
                            (if (isBangla) "মহাজন বা কোম্পানির নাম খুঁজুন..." else "Search supplier or company..."),
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

            // Category Filter Chips
            val currentCategories = if (selectedTabIndex == 0) customerCategories else supplierCategories
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                currentCategories.forEach { cat ->
                    val isSelected = (selectedCategoryFilter == null && cat == "All") || selectedCategoryFilter == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategoryFilter = if (cat == "All") null else cat
                        },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VividEmeraldPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = DarkSlateSurface,
                            labelColor = TextMuted
                        )
                    )
                }
            }

            // Parties List Content
            if (selectedTabIndex == 0) {
                // Customers List
                if (filteredCustomers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBangla) "কোন কাস্টমার পাওয়া যায়নি" else "No customers found",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 72.dp)
                    ) {
                        items(filteredCustomers, key = { it.id }) { customer ->
                            CustomerPartyCard(
                                customer = customer,
                                currentLanguage = currentLanguage,
                                onSpecialRateClick = { customerForSpecialRate = customer },
                                onPaymentInClick = { customerForPaymentIn = customer },
                                onCallClick = {
                                    if (customer.phone.isNotBlank()) {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                        context.startActivity(intent)
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                // Suppliers List
                if (filteredSuppliers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBangla) "কোন মহাজন পাওয়া যায়নি" else "No suppliers found",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 72.dp)
                    ) {
                        items(filteredSuppliers, key = { it.id }) { supplier ->
                            SupplierPartyCard(
                                supplier = supplier,
                                currentLanguage = currentLanguage,
                                onPayClick = { supplierForPaymentOut = supplier },
                                onCallClick = {
                                    if (supplier.phone.isNotBlank()) {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${supplier.phone}"))
                                        context.startActivity(intent)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Add Party FAB
        FloatingActionButton(
            onClick = { showAddPartyDialog = true },
            containerColor = VividEmeraldPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_party")
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Add Party", modifier = Modifier.size(28.dp))
        }
    }

    // Add Party Dialog (with Phone Contact Picker)
    if (showAddPartyDialog) {
        AddPartyDialog(
            isBangla = isBangla,
            initialIsCustomer = selectedTabIndex == 0,
            onDismiss = { showAddPartyDialog = false },
            onSaveCustomer = { name, phone, address, cat, due ->
                scope.launch {
                    repository.insertCustomer(
                        Customer(
                            name = name,
                            phone = phone,
                            address = address,
                            category = cat,
                            totalBaki = due
                        )
                    )
                    showAddPartyDialog = false
                    Toast.makeText(context, if (isBangla) "কাস্টমার যুক্ত হয়েছে!" else "Customer added!", Toast.LENGTH_SHORT).show()
                }
            },
            onSaveSupplier = { name, phone, company, cat, due ->
                scope.launch {
                    repository.insertSupplier(
                        Supplier(
                            name = name,
                            phone = phone,
                            company = company,
                            category = cat,
                            amountOwed = due
                        )
                    )
                    showAddPartyDialog = false
                    Toast.makeText(context, if (isBangla) "মহাজন যুক্ত হয়েছে!" else "Supplier added!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Party-Wise Item Rate Dialog
    customerForSpecialRate?.let { customer ->
        PartyWiseRateDialog(
            customer = customer,
            products = products,
            isBangla = isBangla,
            currentLanguage = currentLanguage,
            onDismiss = { customerForSpecialRate = null },
            onSaveRate = { prodId, customRate ->
                PartyRateManager.setPartyRate(context, customer.id, prodId, customRate)
                customerForSpecialRate = null
                Toast.makeText(context, if (isBangla) "বিশেষ রেট সংরক্ষিত হয়েছে!" else "Custom rate saved!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Payment In Dialog for Customer
    customerForPaymentIn?.let { customer ->
        com.example.ui.screens.baki.PaymentInDialog(
            customer = customer,
            isBangla = isBangla,
            currentLanguage = currentLanguage,
            onDismiss = { customerForPaymentIn = null },
            onConfirm = { amount, method, note ->
                scope.launch {
                    repository.recordCustomerPayment(
                        customerId = customer.id,
                        amount = amount,
                        note = "$method: $note"
                    )
                    customerForPaymentIn = null
                    Toast.makeText(context, if (isBangla) "পেমেন্ট জমা নেওয়া হয়েছে!" else "Payment recorded!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Payment Out Dialog for Supplier
    supplierForPaymentOut?.let { supplier ->
        com.example.ui.screens.business.RecordSupplierPaymentDialog(
            supplier = supplier,
            isBangla = isBangla,
            currentLanguage = currentLanguage,
            onDismiss = { supplierForPaymentOut = null },
            onConfirm = { amount, method, note ->
                scope.launch {
                    repository.recordSupplierPayment(
                        supplierId = supplier.id,
                        amount = amount,
                        paymentMethod = method,
                        note = note
                    )
                    supplierForPaymentOut = null
                    Toast.makeText(context, if (isBangla) "পেমেন্ট সম্পন্ন হয়েছে!" else "Payment recorded!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun CustomerPartyCard(
    customer: Customer,
    currentLanguage: AppLanguage,
    onSpecialRateClick: () -> Unit,
    onPaymentInClick: () -> Unit,
    onCallClick: () -> Unit
) {
    val isBangla = currentLanguage == AppLanguage.BANGLA

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("party_customer_${customer.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        border = BorderStroke(1.dp, if (customer.totalBaki > 0) LowStockRed.copy(alpha = 0.4f) else DarkSlateCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = customer.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DarkSlateSurfaceVariant
                        ) {
                            Text(
                                text = customer.category,
                                fontSize = 10.sp,
                                color = VividEmeraldAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (customer.phone.isNotBlank()) {
                        Text(text = customer.phone, fontSize = 12.sp, color = TextMuted)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatTaka(customer.totalBaki, currentLanguage, isMaskable = true),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (customer.totalBaki > 0) LowStockRed else VividEmeraldAccent
                    )
                    Text(
                        text = if (customer.totalBaki > 0) (if (isBangla) "বাকি আছে" else "Due") else (if (isBangla) "পরিশোধিত" else "Clear"),
                        fontSize = 10.sp,
                        color = if (customer.totalBaki > 0) LowStockRed else VividEmeraldAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (customer.phone.isNotBlank()) {
                    IconButton(onClick = onCallClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = VividEmeraldAccent, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Spacer(modifier = Modifier.width(32.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onSpecialRateClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, VividEmeraldAccent.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("btn_special_rate_${customer.id}")
                    ) {
                        Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(12.dp), tint = VividEmeraldAccent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBangla) "বিশেষ দর" else "Custom Rate", fontSize = 11.sp, color = VividEmeraldAccent)
                    }

                    Button(
                        onClick = onPaymentInClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_pay_in_${customer.id}")
                    ) {
                        Text(if (isBangla) "জমা নিন" else "Receive", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SupplierPartyCard(
    supplier: Supplier,
    currentLanguage: AppLanguage,
    onPayClick: () -> Unit,
    onCallClick: () -> Unit
) {
    val isBangla = currentLanguage == AppLanguage.BANGLA

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("party_supplier_${supplier.id}"),
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
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = supplier.company.ifBlank { supplier.name },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DarkSlateSurfaceVariant
                        ) {
                            Text(
                                text = supplier.category,
                                fontSize = 10.sp,
                                color = Color(0xFFFFA000),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (supplier.company.isNotBlank() && supplier.company != supplier.name) {
                        Text(text = "${if (isBangla) "ব্যক্তি:" else "Contact:"} ${supplier.name}", fontSize = 11.sp, color = TextMuted)
                    }
                    if (supplier.phone.isNotBlank()) {
                        Text(text = supplier.phone, fontSize = 12.sp, color = TextMuted)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatTaka(supplier.amountOwed, currentLanguage, isMaskable = true),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (supplier.amountOwed > 0) Color(0xFFFFA000) else VividEmeraldAccent
                    )
                    Text(
                        text = if (isBangla) "দেনা" else "Payable",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (supplier.phone.isNotBlank()) {
                    IconButton(onClick = onCallClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = VividEmeraldAccent, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Spacer(modifier = Modifier.width(32.dp))
                }

                Button(
                    onClick = onPayClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_pay_supplier_${supplier.id}")
                ) {
                    Text(if (isBangla) "পেমেন্ট পরিশোধ" else "Pay Supplier", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AddPartyDialog(
    isBangla: Boolean,
    initialIsCustomer: Boolean,
    onDismiss: () -> Unit,
    onSaveCustomer: (name: String, phone: String, address: String, category: String, openingDue: Double) -> Unit,
    onSaveSupplier: (name: String, phone: String, company: String, category: String, openingDue: Double) -> Unit
) {
    val context = LocalContext.current
    var isCustomer by remember { mutableStateOf(initialIsCustomer) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var companyOrAddress by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(if (isCustomer) "Regular" else "Hardware") }
    var openingDueStr by remember { mutableStateOf("") }

    // Contact Picker Launcher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            name = c.getString(nameIndex) ?: ""
                        }
                        val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
                        if (idIndex != -1) {
                            val contactId = c.getString(idIndex)
                            val phoneCursor = context.contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                                arrayOf(contactId),
                                null
                            )
                            phoneCursor?.use { pc ->
                                if (pc.moveToFirst()) {
                                    val numberIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    if (numberIndex != -1) {
                                        phone = pc.getString(numberIndex)?.replace("\\s+".toRegex(), "") ?: ""
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not read contact: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
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
                    text = if (isCustomer) (if (isBangla) "নতুন কাস্টমার যোগ" else "Add Customer")
                    else (if (isBangla) "নতুন মহাজন যোগ" else "Add Supplier"),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Contact Import Button
                OutlinedButton(
                    onClick = { contactPickerLauncher.launch(null) },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, VividEmeraldAccent),
                    modifier = Modifier.testTag("btn_import_contact")
                ) {
                    Icon(Icons.Default.ContactPhone, contentDescription = null, modifier = Modifier.size(14.dp), tint = VividEmeraldAccent)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "কন্টাক্ট" else "Contacts", fontSize = 11.sp, color = VividEmeraldAccent)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Party Type Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isCustomer,
                        onClick = {
                            isCustomer = true
                            category = "Regular"
                        },
                        label = { Text(if (isBangla) "কাস্টমার" else "Customer") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VividEmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = !isCustomer,
                        onClick = {
                            isCustomer = false
                            category = "Hardware"
                        },
                        label = { Text(if (isBangla) "মহাজন / সাপ্লায়ার" else "Supplier") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VividEmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isCustomer) (if (isBangla) "গ্রাহকের নাম *" else "Customer Name *") else (if (isBangla) "মহাজন / প্রতিনিধির নাম" else "Contact Person")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_party_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isBangla) "মোবাইল নম্বর" else "Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_party_phone"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = companyOrAddress,
                    onValueChange = { companyOrAddress = it },
                    label = { Text(if (isCustomer) (if (isBangla) "ঠিকানা" else "Address") else (if (isBangla) "কোম্পানি / ব্র্যান্ডের নাম *" else "Company Name *")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_party_company_address"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text(if (isBangla) "ক্যাটাগরি" else "Category") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = openingDueStr,
                        onValueChange = { openingDueStr = it },
                        label = { Text(if (isBangla) "পূর্বের বাকি/দেনা" else "Opening Due") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val due = openingDueStr.toDoubleOrNull() ?: 0.0
                    if (isCustomer) {
                        if (name.isNotBlank()) {
                            onSaveCustomer(name.trim(), phone.trim(), companyOrAddress.trim(), category.trim(), due)
                        }
                    } else {
                        val finalCompany = companyOrAddress.trim().ifBlank { name.trim() }
                        val finalName = name.trim().ifBlank { finalCompany }
                        if (finalCompany.isNotBlank()) {
                            onSaveSupplier(finalName, phone.trim(), finalCompany, category.trim(), due)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_save_party")
            ) {
                Text(if (isBangla) "যুক্ত করুন" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun PartyWiseRateDialog(
    customer: Customer,
    products: List<Product>,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSaveRate: (productId: Long, customRate: Double) -> Unit
) {
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var isProductDropdownOpen by remember { mutableStateOf(false) }
    var customPriceStr by remember {
        mutableStateOf(
            selectedProduct?.let { prod ->
                val existing = PartyRateManager.getPartyRate(customer.id, prod.id)
                (existing ?: prod.sellPrice).toString()
            } ?: ""
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Column {
                Text(
                    text = if (isBangla) "কাস্টমার বিশেষ রেট নির্ধারণ" else "Party-wise Item Rate",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = customer.name,
                    fontSize = 13.sp,
                    color = VividEmeraldAccent
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isBangla)
                        "এই কাস্টমারের কাছে পণ্য বিক্রি করার সময় এই বিশেষ দর স্বয়ংক্রিয়ভাবে প্রযোজ্য হবে।"
                    else
                        "This custom price will automatically apply whenever selling to this customer.",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                // Product Picker
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedProduct?.localizedName(isBangla) ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBangla) "পণ্য নির্বাচন করুন" else "Select Product") },
                        trailingIcon = {
                            IconButton(onClick = { isProductDropdownOpen = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isProductDropdownOpen = true },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    DropdownMenu(
                        expanded = isProductDropdownOpen,
                        onDismissRequest = { isProductDropdownOpen = false }
                    ) {
                        products.forEach { prod ->
                            DropdownMenuItem(
                                text = {
                                    Text("${prod.localizedName(isBangla)} (সাধারণ মূল্য: ${formatTaka(prod.sellPrice, currentLanguage)})")
                                },
                                onClick = {
                                    selectedProduct = prod
                                    val existing = PartyRateManager.getPartyRate(customer.id, prod.id)
                                    customPriceStr = (existing ?: prod.sellPrice).toString()
                                    isProductDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                selectedProduct?.let { prod ->
                    Text(
                        text = "${if (isBangla) "সাধারণ বিক্রয় মূল্য:" else "Standard Price:"} ${formatTaka(prod.sellPrice, currentLanguage)} (ক্রয়মূল্য: ${formatTaka(prod.buyPrice, currentLanguage)})",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                OutlinedTextField(
                    value = customPriceStr,
                    onValueChange = { customPriceStr = it },
                    label = { Text(if (isBangla) "কাস্টমারের বিশেষ বিক্রয় মূল্য *" else "Customer Custom Rate *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_custom_party_rate"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val prod = selectedProduct
                    val rate = customPriceStr.toDoubleOrNull()
                    if (prod != null && rate != null && rate > 0) {
                        onSaveRate(prod.id, rate)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_save_party_rate")
            ) {
                Text(if (isBangla) "সংরক্ষণ করুন" else "Save Rate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}
