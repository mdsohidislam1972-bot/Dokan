package com.example.ui.screens.baki

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.ProfileManager
import com.example.data.local.entity.Customer
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
fun BakiScreen(
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

    val customers by repository.allCustomers.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var onlyWithDue by remember { mutableStateOf(false) }

    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var customerForPaymentIn by remember { mutableStateOf<Customer?>(null) }
    var customerForAddDue by remember { mutableStateOf<Customer?>(null) }

    val filteredCustomers = customers.filter { c ->
        val matchesQuery = searchQuery.isBlank() ||
                c.name.contains(searchQuery, ignoreCase = true) ||
                c.phone.contains(searchQuery)
        val matchesDue = !onlyWithDue || c.totalBaki > 0
        matchesQuery && matchesDue
    }

    val totalCustomerDue = customers.sumOf { it.totalBaki }
    val customersWithDueCount = customers.count { it.totalBaki > 0 }

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
            // Header Due Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, LowStockRed.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBangla) "কাস্টমারদের মোট বাকি (পাওনা)" else "Total Customer Due (Receivable)",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatTaka(totalCustomerDue, currentLanguage, isMaskable = true),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = LowStockRed
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${customers.size} ${if (isBangla) "জন গ্রাহক" else "Customers"}",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "$customersWithDueCount ${if (isBangla) "জনের বাকি আছে" else "with balance"}",
                            fontSize = 11.sp,
                            color = LowStockRed
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
                    .testTag("baki_search_input"),
                placeholder = {
                    Text(
                        if (isBangla) "গ্রাহকের নাম বা মোবাইল নম্বর খুঁজুন..." else "Search customer name or phone...",
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

            Spacer(modifier = Modifier.height(10.dp))

            // Customer List
            if (filteredCustomers.isEmpty()) {
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
                            Icons.Default.People,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (isBangla) "কোন কাস্টমার পাওয়া যায়নি" else "No customers found",
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
                    contentPadding = PaddingValues(bottom = 72.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        CustomerBakiCard(
                            customer = customer,
                            currentLanguage = currentLanguage,
                            onReceivePaymentClick = { customerForPaymentIn = customer },
                            onAddDueClick = { customerForAddDue = customer },
                            onCallClick = {
                                if (customer.phone.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                    context.startActivity(intent)
                                }
                            },
                            onRemindClick = {
                                val storeName = ProfileManager.activeProfile?.name ?: (if (isBangla) "আমাদের দোকান" else "Our Store")
                                val reminderText = if (isBangla) {
                                    "শ্রদ্ধেয় ${customer.name}, $storeName এ আপনার মোট বাকি পাওনা ${formatTaka(customer.totalBaki, currentLanguage)}। অনুগ্রহ করে দ্রুত পরিশোধ করুন। ধন্যবাদ।"
                                } else {
                                    "Dear ${customer.name}, your total outstanding balance at $storeName is ${formatTaka(customer.totalBaki, currentLanguage)}. Please settle at your earliest convenience. Thank you."
                                }
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, reminderText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Send Reminder / তাগাদা পাঠান"))
                            }
                        )
                    }
                }
            }
        }

        // Add Customer FAB
        FloatingActionButton(
            onClick = { showAddCustomerDialog = true },
            containerColor = VividEmeraldPrimary,
            contentColor = androidx.compose.ui.graphics.Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_customer")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Customer", modifier = Modifier.size(28.dp))
        }
    }

    // Add Customer Dialog
    if (showAddCustomerDialog) {
        AddCustomerDialog(
            isBangla = isBangla,
            onDismiss = { showAddCustomerDialog = false },
            onSave = { name, phone, address, initialDue ->
                scope.launch {
                    val customer = Customer(
                        name = name.trim(),
                        phone = phone.trim(),
                        address = address.trim(),
                        totalBaki = initialDue
                    )
                    repository.insertCustomer(customer)
                    showAddCustomerDialog = false
                    Toast.makeText(context, if (isBangla) "কাস্টমার যুক্ত হয়েছে!" else "Customer Added!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Payment In Dialog
    customerForPaymentIn?.let { c ->
        PaymentInDialog(
            customer = c,
            isBangla = isBangla,
            currentLanguage = currentLanguage,
            onDismiss = { customerForPaymentIn = null },
            onConfirm = { amount, method, note ->
                scope.launch {
                    val noteText = if (note.isNotBlank()) "$method: $note" else method
                    repository.recordCustomerPayment(
                        customerId = c.id,
                        amount = amount,
                        note = noteText
                    )
                    customerForPaymentIn = null
                    Toast.makeText(context, if (isBangla) "জমা নেওয়া হয়েছে!" else "Payment Received!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Add Due Dialog
    customerForAddDue?.let { c ->
        AddCustomerDueDialog(
            customer = c,
            isBangla = isBangla,
            currentLanguage = currentLanguage,
            onDismiss = { customerForAddDue = null },
            onConfirm = { addedDue ->
                scope.launch {
                    val newBalance = c.totalBaki + addedDue
                    repository.updateCustomerBalance(c.id, newBalance)
                    customerForAddDue = null
                    Toast.makeText(context, if (isBangla) "বাকি যোগ হয়েছে!" else "Due Added!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun CustomerBakiCard(
    customer: Customer,
    currentLanguage: AppLanguage,
    onReceivePaymentClick: () -> Unit,
    onAddDueClick: () -> Unit,
    onCallClick: () -> Unit,
    onRemindClick: () -> Unit
) {
    val isBangla = currentLanguage == AppLanguage.BANGLA

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("customer_baki_item_${customer.id}"),
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
                    Text(
                        text = customer.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (customer.phone.isNotBlank()) {
                        Text(
                            text = customer.phone,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    if (customer.address.isNotBlank()) {
                        Text(
                            text = customer.address,
                            fontSize = 11.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatTaka(customer.totalBaki, currentLanguage, isMaskable = true),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (customer.totalBaki > 0) LowStockRed else VividEmeraldAccent
                    )
                    Text(
                        text = if (customer.totalBaki > 0) (if (isBangla) "বাকি আছে" else "Due Balance") else (if (isBangla) "পরিশোধিত" else "Clear"),
                        fontSize = 11.sp,
                        color = if (customer.totalBaki > 0) LowStockRed else VividEmeraldAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call & Reminder
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (customer.phone.isNotBlank()) {
                        IconButton(
                            onClick = onCallClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = VividEmeraldAccent, modifier = Modifier.size(16.dp))
                        }
                    }

                    if (customer.totalBaki > 0) {
                        IconButton(
                            onClick = onRemindClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Remind", tint = androidx.compose.ui.graphics.Color(0xFFFFA000), modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Payment In and Add Due buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onAddDueClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, LowStockRed.copy(alpha = 0.5f))
                    ) {
                        Text(if (isBangla) "+ বাকি দিন" else "+ Due", fontSize = 11.sp, color = LowStockRed)
                    }

                    Button(
                        onClick = onReceivePaymentClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_receive_payment_${customer.id}")
                    ) {
                        Text(if (isBangla) "জমা নিন" else "Receive", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AddCustomerDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, address: String, initialDue: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var dueStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "নতুন কাস্টমার যুক্ত করুন" else "Add New Customer",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isBangla) "গ্রাহকের নাম *" else "Customer Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_customer_name"),
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
                    modifier = Modifier.fillMaxWidth().testTag("input_customer_phone"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(if (isBangla) "ঠিকানা (ঐচ্ছিক)" else "Address (Optional)") },
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
                    value = dueStr,
                    onValueChange = { dueStr = it },
                    label = { Text(if (isBangla) "পূর্বের বাকি (ঐচ্ছিক)" else "Previous Due (Optional)") },
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
        },
        confirmButton = {
            Button(
                onClick = {
                    val initialDue = dueStr.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        onSave(name, phone, address, initialDue)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_save_customer")
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
fun PaymentInDialog(
    customer: Customer,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, method: String, note: String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var method by remember { mutableStateOf(if (isBangla) "নগদ (Cash)" else "Cash") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "বাকি টাকা জমা নিন" else "Receive Payment",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${customer.name} (মোট বাকি: ${formatTaka(customer.totalBaki, currentLanguage)})",
                    fontSize = 13.sp,
                    color = LowStockRed
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(if (isBangla) "জমার পরিমাণ *" else "Received Amount *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_customer_pay_amount"),
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
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onConfirm(amt, method, note)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_confirm_customer_payment")
            ) {
                Text(if (isBangla) "জমা সম্পন্ন করুন" else "Confirm Receipt")
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
fun AddCustomerDueDialog(
    customer: Customer,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (addedDue: Double) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "বাকি যোগ করুন" else "Add Customer Due",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${customer.name} (বর্তমান বাকি: ${formatTaka(customer.totalBaki, currentLanguage)})",
                    fontSize = 13.sp,
                    color = TextMuted
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(if (isBangla) "যোগ করার পরিমাণ *" else "Due Amount *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_add_due_amount"),
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
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onConfirm(amt)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LowStockRed),
                modifier = Modifier.testTag("btn_confirm_add_due")
            ) {
                Text(if (isBangla) "বাকি যোগ করুন" else "Add Due")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}
