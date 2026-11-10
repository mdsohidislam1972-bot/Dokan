package com.example.ui.screens.purchases

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.AppDatabase
import com.example.data.local.entity.Product
import com.example.data.local.entity.PurchaseRecord
import com.example.data.local.entity.Supplier
import com.example.data.repository.HardwareRepository
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.formatTaka
import com.example.ui.screens.business.AddSupplierDialog
import com.example.ui.screens.business.RecordSupplierPaymentDialog
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PurchasesScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
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

    val purchases by repository.allPurchases.collectAsState(initial = emptyList())
    val products by repository.allProducts.collectAsState(initial = emptyList())
    val suppliers by repository.allSuppliersOwedFirst.collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var showAddPurchaseDialog by remember { mutableStateOf(false) }
    var showAddSupplierDialog by remember { mutableStateOf(false) }
    var showPaymentOutDialog by remember { mutableStateOf(false) }
    var purchaseForReturn by remember { mutableStateOf<PurchaseRecord?>(null) }
    var viewBillPhotoUri by remember { mutableStateOf<String?>(null) }

    val filteredPurchases = purchases.filter { p ->
        searchQuery.isBlank() ||
                p.productName.contains(searchQuery, ignoreCase = true) ||
                p.supplierName.contains(searchQuery, ignoreCase = true) ||
                p.note.contains(searchQuery, ignoreCase = true)
    }

    val totalPurchasedAmount = purchases.filter { it.totalCost > 0 }.sumOf { it.totalCost }
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
            // Supplier & Purchase Top Summary Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Purchases
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.dp, DarkSlateCardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (isBangla) "মোট মালামাল ক্রয়" else "Total Purchases",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = formatTaka(totalPurchasedAmount, currentLanguage),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${purchases.size} ${if (isBangla) "টি চালান" else "Invoices"}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                // Supplier Dena & Quick Add Supplier
                Card(
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.dp, Color(0xFFFFA000).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBangla) "মহাজন দেনা" else "Supplier Due",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            IconButton(
                                onClick = { showAddSupplierDialog = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Supplier", tint = VividEmeraldAccent, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(
                            text = formatTaka(totalSupplierDena, currentLanguage, isMaskable = true),
                            fontSize = 16.sp,
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

            // Quick Actions Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showAddPurchaseDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_open_add_purchase"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBangla) "+ মাল ক্রয় চালান" else "+ Add Purchase", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showPaymentOutDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_open_payment_out"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFA000))
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFFA000))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBangla) "পেমেন্ট আউট (প্রদান)" else "Payment Out", fontSize = 12.sp, color = Color(0xFFFFA000))
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("purchases_search_input"),
                placeholder = {
                    Text(
                        if (isBangla) "পণ্য বা মহাজনের নাম দিয়ে খুঁজুন..." else "Search by product or supplier...",
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

            Spacer(modifier = Modifier.height(8.dp))

            // Purchases List
            if (filteredPurchases.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBangla) "কোন ক্রয়ের রেকর্ড পাওয়া যায়নি" else "No purchase records found",
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
                    items(filteredPurchases, key = { it.id }) { purchase ->
                        PurchaseItemCard(
                            purchase = purchase,
                            currentLanguage = currentLanguage,
                            onReturnClick = { purchaseForReturn = purchase },
                            onViewBillClick = {
                                if (!purchase.billPhotoUri.isNullOrBlank()) {
                                    viewBillPhotoUri = purchase.billPhotoUri
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Purchase Dialog
    if (showAddPurchaseDialog) {
        AddPurchaseDialog(
            products = products,
            suppliers = suppliers,
            isBangla = isBangla,
            currentLanguage = currentLanguage,
            onDismiss = { showAddPurchaseDialog = false },
            onConfirm = { prod, qty, buyPrice, supp, paid, due, note, billUri ->
                scope.launch {
                    val supplierName = supp?.company?.ifBlank { supp.name } ?: "Cash Purchase"
                    repository.addStock(
                        productId = prod.id,
                        productName = prod.localizedName(isBangla),
                        addedQty = qty,
                        unitBuyingPrice = buyPrice,
                        supplierName = supplierName,
                        note = note,
                        supplierId = supp?.id,
                        paidAmount = paid,
                        dueAmount = due,
                        billPhotoUri = billUri
                    )
                    showAddPurchaseDialog = false
                    Toast.makeText(context, if (isBangla) "ক্রয় সম্পন্ন ও স্টক আপডেট হয়েছে!" else "Purchase recorded & stock updated!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Purchase Return Dialog
    purchaseForReturn?.let { p ->
        PurchaseReturnDialog(
            purchase = p,
            isBangla = isBangla,
            currentLanguage = currentLanguage,
            onDismiss = { purchaseForReturn = null },
            onConfirm = { returnQty, reason ->
                scope.launch {
                    repository.returnPurchase(
                        purchaseRecordId = p.id,
                        productId = p.productId,
                        productName = p.productName,
                        returnQty = returnQty,
                        unitBuyingPrice = p.unitBuyingPrice,
                        supplierId = p.supplierId,
                        supplierName = p.supplierName,
                        note = reason
                    )
                    purchaseForReturn = null
                    Toast.makeText(context, if (isBangla) "ক্রয় ফেরত সম্পন্ন হয়েছে!" else "Purchase return recorded!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Add Supplier Dialog
    if (showAddSupplierDialog) {
        AddSupplierDialog(
            isBangla = isBangla,
            onDismiss = { showAddSupplierDialog = false },
            onSave = { name, phone, company, openingDue ->
                scope.launch {
                    repository.insertSupplier(
                        Supplier(
                            name = name,
                            phone = phone,
                            company = company,
                            amountOwed = openingDue
                        )
                    )
                    showAddSupplierDialog = false
                    Toast.makeText(context, if (isBangla) "মহাজন যুক্ত হয়েছে!" else "Supplier Added!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Payment Out Dialog
    if (showPaymentOutDialog) {
        PaymentOutSupplierDialog(
            suppliers = suppliers,
            isBangla = isBangla,
            currentLanguage = currentLanguage,
            onDismiss = { showPaymentOutDialog = false },
            onConfirm = { suppId, amount, method, note ->
                scope.launch {
                    repository.recordSupplierPayment(
                        supplierId = suppId,
                        amount = amount,
                        paymentMethod = method,
                        note = note
                    )
                    showPaymentOutDialog = false
                    Toast.makeText(context, if (isBangla) "মহাজনকে পেমেন্ট প্রদান সম্পন্ন!" else "Payment recorded!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // View Bill Photo Dialog
    viewBillPhotoUri?.let { uriStr ->
        AlertDialog(
            onDismissRequest = { viewBillPhotoUri = null },
            containerColor = DarkSlateSurface,
            title = {
                Text(
                    text = if (isBangla) "ক্রয় চালানের ছবি (Bill Photo)" else "Purchase Bill Photo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = uriStr,
                        contentDescription = "Bill Photo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewBillPhotoUri = null },
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
                ) {
                    Text(if (isBangla) "ঠিক আছে" else "Close")
                }
            }
        )
    }
}

@Composable
fun PurchaseItemCard(
    purchase: PurchaseRecord,
    currentLanguage: AppLanguage,
    onReturnClick: () -> Unit,
    onViewBillClick: () -> Unit
) {
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(purchase.timestamp) { dateFormat.format(Date(purchase.timestamp)) }
    val isReturn = purchase.quantity < 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("purchase_item_${purchase.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        border = BorderStroke(1.dp, if (isReturn) LowStockRed.copy(alpha = 0.5f) else DarkSlateCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = purchase.productName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isReturn) LowStockRed else TextPrimary
                    )
                    Text(
                        text = "${purchase.supplierName.ifBlank { "Cash Purchase" }} • $formattedDate",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Text(
                    text = formatTaka(purchase.totalCost, currentLanguage),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isReturn) LowStockRed else VividEmeraldAccent
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (isReturn) "ফেরত: " else "পরিমাণ: "}${kotlin.math.abs(purchase.quantity)} x ${formatTaka(purchase.unitBuyingPrice, currentLanguage)}",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!purchase.billPhotoUri.isNullOrBlank()) {
                        IconButton(
                            onClick = onViewBillClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "Bill Photo", tint = VividEmeraldAccent, modifier = Modifier.size(18.dp))
                        }
                    }

                    if (!isReturn) {
                        OutlinedButton(
                            onClick = onReturnClick,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            border = BorderStroke(1.dp, LowStockRed.copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("btn_return_purchase_${purchase.id}")
                        ) {
                            Icon(Icons.Default.AssignmentReturn, contentDescription = null, modifier = Modifier.size(12.dp), tint = LowStockRed)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBangla) "ফেরত" else "Return", fontSize = 11.sp, color = LowStockRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddPurchaseDialog(
    products: List<Product>,
    suppliers: List<Supplier>,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (
        product: Product,
        quantity: Int,
        unitBuyingPrice: Double,
        supplier: Supplier?,
        paidAmount: Double,
        dueAmount: Double,
        note: String,
        billPhotoUri: String?
    ) -> Unit
) {
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var selectedSupplier by remember { mutableStateOf<Supplier?>(null) }
    var isCashPurchase by remember { mutableStateOf(true) }

    var isProductDropdownOpen by remember { mutableStateOf(false) }
    var isSupplierDropdownOpen by remember { mutableStateOf(false) }

    var quantityStr by remember { mutableStateOf("10") }
    var buyPriceStr by remember { mutableStateOf(selectedProduct?.buyPrice?.toString() ?: "100") }
    var paidAmountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var billPhotoUri by remember { mutableStateOf<String?>(null) }

    // Photo Picker for Bill
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            billPhotoUri = uri.toString()
        }
    }

    val qty = quantityStr.toIntOrNull() ?: 0
    val buyPrice = buyPriceStr.toDoubleOrNull() ?: 0.0
    val totalCost = qty * buyPrice

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "নতুন মাল ক্রয় চালান যোগ" else "Add New Purchase",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Cash vs Supplier Purchase Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isCashPurchase,
                        onClick = {
                            isCashPurchase = true
                            selectedSupplier = null
                        },
                        label = { Text(if (isBangla) "নগদ ক্রয় (Cash)" else "Cash Purchase") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VividEmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = !isCashPurchase,
                        onClick = { isCashPurchase = false },
                        label = { Text(if (isBangla) "মহাজন / সাপ্লায়ার" else "From Supplier") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VividEmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                // Supplier Dropdown (if not cash purchase)
                if (!isCashPurchase) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedSupplier?.company?.ifBlank { selectedSupplier?.name } ?: (if (isBangla) "মহাজন নির্বাচন করুন" else "Select Supplier"),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isBangla) "মহাজন *" else "Supplier *") },
                            trailingIcon = {
                                IconButton(onClick = { isSupplierDropdownOpen = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isSupplierDropdownOpen = true },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VividEmeraldPrimary,
                                unfocusedBorderColor = DarkSlateCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        DropdownMenu(
                            expanded = isSupplierDropdownOpen,
                            onDismissRequest = { isSupplierDropdownOpen = false }
                        ) {
                            suppliers.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("${s.company.ifBlank { s.name }} (দেনা: ${formatTaka(s.amountOwed, currentLanguage)})") },
                                    onClick = {
                                        selectedSupplier = s
                                        isSupplierDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Product Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedProduct?.localizedName(isBangla) ?: (if (isBangla) "পণ্য নির্বাচন করুন" else "Select Product"),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBangla) "পণ্য *" else "Product *") },
                        trailingIcon = {
                            IconButton(onClick = { isProductDropdownOpen = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isProductDropdownOpen = true }
                            .testTag("select_purchase_product"),
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
                                text = { Text("${prod.localizedName(isBangla)} (স্টক: ${prod.stockQuantity})") },
                                onClick = {
                                    selectedProduct = prod
                                    buyPriceStr = prod.buyPrice.toString()
                                    isProductDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text(if (isBangla) "ক্রয় পরিমাণ *" else "Quantity *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_purchase_qty"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = buyPriceStr,
                        onValueChange = { buyPriceStr = it },
                        label = { Text(if (isBangla) "একক ক্রয় মূল্য *" else "Unit Price *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_purchase_price"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                // Total Cost Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isBangla) "সর্বমোট ক্রয় মূল্য:" else "Total Purchase Cost:", fontSize = 13.sp, color = TextMuted)
                        Text(formatTaka(totalCost, currentLanguage), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VividEmeraldAccent)
                    }
                }

                if (!isCashPurchase) {
                    OutlinedTextField(
                        value = paidAmountStr,
                        onValueChange = { paidAmountStr = it },
                        label = { Text(if (isBangla) "নগদ পরিশোধ (ঐচ্ছিক)" else "Paid Amount") },
                        placeholder = { Text("0.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBangla) "নোট / চালান নম্বর" else "Invoice / Notes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Bill Photo Upload Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, VividEmeraldAccent)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBangla) "চালানের ছবি তুলুন" else "Upload Bill Photo", color = VividEmeraldAccent, fontSize = 12.sp)
                    }

                    if (billPhotoUri != null) {
                        Text("✓ Attached", color = VividEmeraldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val prod = selectedProduct
                    if (prod != null && qty > 0 && buyPrice > 0) {
                        val paid = if (isCashPurchase) totalCost else (paidAmountStr.toDoubleOrNull() ?: 0.0)
                        val due = if (isCashPurchase) 0.0 else (totalCost - paid).coerceAtLeast(0.0)
                        onConfirm(prod, qty, buyPrice, selectedSupplier, paid, due, note, billPhotoUri)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_save_purchase")
            ) {
                Text(if (isBangla) "ক্রয় নিশ্চিত করুন" else "Confirm Purchase")
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
fun PurchaseReturnDialog(
    purchase: PurchaseRecord,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (returnQty: Int, reason: String) -> Unit
) {
    var returnQtyStr by remember { mutableStateOf("1") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "ক্রয়কৃত পণ্য ফেরত দিন" else "Purchase Return",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = LowStockRed
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${purchase.productName} (ক্রয় পরিমাণ: ${purchase.quantity}, একক দর: ${formatTaka(purchase.unitBuyingPrice, currentLanguage)})",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                OutlinedTextField(
                    value = returnQtyStr,
                    onValueChange = { returnQtyStr = it },
                    label = { Text(if (isBangla) "ফেরতের পরিমাণ *" else "Return Quantity *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_return_qty"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LowStockRed,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(if (isBangla) "ফেরতের কারণ" else "Reason for return") },
                    placeholder = { Text(if (isBangla) "যেমনঃ নষ্ট বা মেয়াদোত্তীর্ণ" else "e.g. Damaged goods") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LowStockRed,
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
                    val q = returnQtyStr.toIntOrNull() ?: 0
                    if (q in 1..purchase.quantity) {
                        onConfirm(q, reason.trim().ifBlank { "Damaged / Surplus return" })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LowStockRed),
                modifier = Modifier.testTag("btn_confirm_return")
            ) {
                Text(if (isBangla) "ফেরত নিশ্চিত করুন" else "Confirm Return")
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
fun PaymentOutSupplierDialog(
    suppliers: List<Supplier>,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (supplierId: Long, amount: Double, method: String, note: String) -> Unit
) {
    var selectedSupplier by remember { mutableStateOf(suppliers.firstOrNull()) }
    var isSupplierDropdownOpen by remember { mutableStateOf(false) }
    var amountStr by remember { mutableStateOf("") }
    var method by remember { mutableStateOf(if (isBangla) "নগদ (Cash)" else "Cash") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "মহাজনকে পেমেন্ট দিন (Payment Out)" else "Record Payment Out",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFA000)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Supplier selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedSupplier?.company?.ifBlank { selectedSupplier?.name } ?: (if (isBangla) "মহাজন নির্বাচন করুন" else "Select Supplier"),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBangla) "মহাজন *" else "Supplier *") },
                        trailingIcon = {
                            IconButton(onClick = { isSupplierDropdownOpen = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isSupplierDropdownOpen = true },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    DropdownMenu(
                        expanded = isSupplierDropdownOpen,
                        onDismissRequest = { isSupplierDropdownOpen = false }
                    ) {
                        suppliers.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.company.ifBlank { s.name }} (দেনা: ${formatTaka(s.amountOwed, currentLanguage)})") },
                                onClick = {
                                    selectedSupplier = s
                                    isSupplierDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                selectedSupplier?.let { s ->
                    Text(
                        text = "${if (isBangla) "বর্তমান মোট দেনা:" else "Current Amount Owed:"} ${formatTaka(s.amountOwed, currentLanguage)}",
                        fontSize = 12.sp,
                        color = Color(0xFFFFA000)
                    )
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(if (isBangla) "পরিশোধের পরিমাণ *" else "Payment Amount *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_payment_out_amount"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = method,
                    onValueChange = { method = it },
                    label = { Text(if (isBangla) "মাধ্যম" else "Method") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBangla) "নোট (ঐচ্ছিক)" else "Note (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
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
                    val s = selectedSupplier
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (s != null && amt > 0) {
                        onConfirm(s.id, amt, method, note)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_confirm_payment_out")
            ) {
                Text(if (isBangla) "পেমেন্ট সম্পন্ন করুন" else "Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}
