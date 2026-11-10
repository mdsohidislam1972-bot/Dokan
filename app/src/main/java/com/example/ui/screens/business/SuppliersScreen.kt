package com.example.ui.screens.business

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
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.Supplier
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

@Composable
fun SuppliersScreen(
    viewModel: BusinessViewModel,
    currentLanguage: AppLanguage,
    onBackToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val suppliers by viewModel.suppliers.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddSupplierDialog by remember { mutableStateOf(false) }
    var supplierForPayment by remember { mutableStateOf<Supplier?>(null) }

    val filteredSuppliers = suppliers.filter { s ->
        searchQuery.isBlank() ||
                s.name.contains(searchQuery, ignoreCase = true) ||
                s.company.contains(searchQuery, ignoreCase = true) ||
                s.phone.contains(searchQuery)
    }

    val totalPayable = suppliers.sumOf { it.amountOwed }

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
                    Column {
                        Text(
                            text = if (isBangla) "মহাজনদের মোট দেনা" else "Total Supplier Payable",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatTaka(totalPayable, currentLanguage, isMaskable = true),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFA000)
                        )
                    }

                    Text(
                        text = "${suppliers.size} ${if (isBangla) "জন সাপ্লায়ার" else "Suppliers"}",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("supplier_search_input"),
                placeholder = {
                    Text(
                        if (isBangla) "সাপ্লায়ার বা কোম্পানির নাম খুঁজুন..." else "Search supplier or company...",
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

            // Suppliers List
            if (filteredSuppliers.isEmpty()) {
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
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (isBangla) "কোন মহাজন বা সাপ্লায়ার পাওয়া যায়নি" else "No suppliers found",
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
                    items(filteredSuppliers, key = { it.id }) { supplier ->
                        SupplierCardItem(
                            supplier = supplier,
                            currentLanguage = currentLanguage,
                            onPayClick = { supplierForPayment = supplier },
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

        // Add Supplier FAB
        FloatingActionButton(
            onClick = { showAddSupplierDialog = true },
            containerColor = VividEmeraldPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_supplier")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Supplier", modifier = Modifier.size(28.dp))
        }
    }

    // Add Supplier Dialog
    if (showAddSupplierDialog) {
        AddSupplierDialog(
            isBangla = isBangla,
            onDismiss = { showAddSupplierDialog = false },
            onSave = { name, phone, company, openingDue ->
                viewModel.addSupplier(name, phone, company, openingDue)
                showAddSupplierDialog = false
                Toast.makeText(context, if (isBangla) "সাপ্লায়ার যুক্ত হয়েছে!" else "Supplier Added!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Record Supplier Payment Dialog
    supplierForPayment?.let { s ->
        RecordSupplierPaymentDialog(
            supplier = s,
            isBangla = isBangla,
            currentLanguage = currentLanguage,
            onDismiss = { supplierForPayment = null },
            onConfirm = { amount, method, note ->
                viewModel.recordSupplierPayment(s.id, amount, method, note)
                supplierForPayment = null
                Toast.makeText(context, if (isBangla) "পেমেন্ট সম্পন্ন হয়েছে!" else "Payment Recorded!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun SupplierCardItem(
    supplier: Supplier,
    currentLanguage: AppLanguage,
    onPayClick: () -> Unit,
    onCallClick: () -> Unit
) {
    val isBangla = currentLanguage == AppLanguage.BANGLA

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("supplier_item_${supplier.id}"),
        shape = RoundedCornerShape(12.dp),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = supplier.company.ifBlank { supplier.name },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (supplier.company.isNotBlank() && supplier.company != supplier.name) {
                    Text(
                        text = "${if (isBangla) "যোগাযোগ:" else "Contact:"} ${supplier.name}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
                if (supplier.phone.isNotBlank()) {
                    Text(
                        text = supplier.phone,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatTaka(supplier.amountOwed, currentLanguage, isMaskable = true),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (supplier.amountOwed > 0) Color(0xFFFFA000) else VividEmeraldAccent
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (supplier.phone.isNotBlank()) {
                        IconButton(
                            onClick = onCallClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = VividEmeraldAccent, modifier = Modifier.size(16.dp))
                        }
                    }

                    Button(
                        onClick = onPayClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_pay_supplier_${supplier.id}")
                    ) {
                        Text(if (isBangla) "পরিশোধ" else "Pay", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AddSupplierDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, company: String, openingDue: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var openingDueStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "নতুন মহাজন / সাপ্লায়ার যুক্ত করুন" else "Add New Supplier",
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
                    value = company,
                    onValueChange = { company = it },
                    label = { Text(if (isBangla) "কোম্পানি / দোকানের নাম *" else "Company / Brand Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_supplier_company"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isBangla) "মহাজন / প্রতিনিধির নাম" else "Contact Person Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_supplier_name"),
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
                    modifier = Modifier.fillMaxWidth(),
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
                    label = { Text(if (isBangla) "পূর্বের পাওনা / দেনা (ঐচ্ছিক)" else "Opening Balance Due (Optional)") },
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
                    val finalCompany = company.trim().ifBlank { name.trim() }
                    val finalName = name.trim().ifBlank { finalCompany }
                    val due = openingDueStr.toDoubleOrNull() ?: 0.0
                    if (finalCompany.isNotBlank()) {
                        onSave(finalName, phone.trim(), finalCompany, due)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_save_supplier")
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
fun RecordSupplierPaymentDialog(
    supplier: Supplier,
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
                text = if (isBangla) "মহাজনকে পেমেন্ট দিন" else "Supplier Payment",
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
                    text = "${supplier.company} (বর্তমান দেনা: ${formatTaka(supplier.amountOwed, currentLanguage)})",
                    fontSize = 13.sp,
                    color = Color(0xFFFFA000)
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(if (isBangla) "পরিশোধের পরিমাণ *" else "Payment Amount *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_supplier_pay_amount"),
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
                    label = { Text(if (isBangla) "পরিশোধের মাধ্যম" else "Payment Method") },
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
                modifier = Modifier.testTag("btn_confirm_supplier_payment")
            ) {
                Text(if (isBangla) "পেমেন্ট নিশ্চিত করুন" else "Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}
