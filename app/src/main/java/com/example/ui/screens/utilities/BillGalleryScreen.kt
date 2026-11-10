package com.example.ui.screens.utilities

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.AppDatabase
import com.example.data.local.entity.PurchaseRecord
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.formatTaka
import com.example.ui.theme.DarkSlateBackground
import com.example.ui.theme.DarkSlateCardBorder
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.DarkSlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VividEmeraldAccent
import com.example.ui.theme.VividEmeraldPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GalleryBillItem(
    val id: String,
    val title: String,
    val supplierName: String,
    val invoiceNo: String,
    val amount: Double,
    val date: Long,
    val imageUri: String
)

@Composable
fun BillGalleryScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val scope = rememberCoroutineScope()
    val repository = remember { AppDatabase.getRepository(context) }

    val purchases by repository.allPurchases.collectAsState(initial = emptyList())
    var selectedBillForView by remember { mutableStateOf<GalleryBillItem?>(null) }
    var showAddManualBillDialog by remember { mutableStateOf(false) }

    // Combine purchase bills with uploaded bills
    val purchaseBills = purchases.map { p ->
        GalleryBillItem(
            id = "purch_${p.id}",
            title = if (!p.billPhotoUri.isNullOrBlank()) "Purchase Bill Photo" else "Purchase Invoice",
            supplierName = p.supplierName,
            invoiceNo = "PUR-${p.id}",
            amount = p.totalCost,
            date = p.timestamp,
            imageUri = p.billPhotoUri ?: ""
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            text = if (isBangla) "বিলের ছবি গ্যালারি (Bill Gallery)" else "Bill Gallery",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isBangla) "ক্রয় চালান ও রসিদের ছবি স্বয়ংক্রিয়ভাবে সংরক্ষিত" else "All purchase bills and invoices organized",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(VividEmeraldPrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${purchaseBills.size} ${if (isBangla) "টি বিল" else "Bills"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VividEmeraldAccent
                        )
                    }
                }
            }

            if (purchaseBills.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = TextMuted, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isBangla) "গ্যালারিতে এখনো কোনো বিল সংরক্ষিত নেই" else "No bills in gallery yet",
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                        Text(
                            text = if (isBangla) "ক্রয় চালানের ছবি তুললে তা স্বয়ংক্রিয়ভাবে এখানে আসবে" else "Purchases with receipt photos will appear here automatically",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(purchaseBills, key = { it.id }) { item ->
                        BillThumbnailCard(
                            bill = item,
                            currentLanguage = currentLanguage,
                            onClick = { selectedBillForView = item }
                        )
                    }
                }
            }
        }

        // Add Bill FAB
        FloatingActionButton(
            onClick = { showAddManualBillDialog = true },
            containerColor = VividEmeraldPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_bill_photo")
        ) {
            Icon(Icons.Default.AddAPhoto, contentDescription = "Add Bill")
        }
    }

    // Full Screen Bill Viewer Dialog
    if (selectedBillForView != null) {
        Dialog(onDismissRequest = { selectedBillForView = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedBillForView!!.supplierName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        IconButton(onClick = { selectedBillForView = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSlateSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedBillForView!!.imageUri.isNotBlank()) {
                            AsyncImage(
                                model = selectedBillForView!!.imageUri,
                                contentDescription = "Bill Full Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Receipt, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(54.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(selectedBillForView!!.invoiceNo, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text(formatTaka(selectedBillForView!!.amount, currentLanguage), color = VividEmeraldAccent)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(if (isBangla) "চালান নম্বর:" else "Invoice No:", fontSize = 11.sp, color = TextMuted)
                            Text(selectedBillForView!!.invoiceNo, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(if (isBangla) "বিলের পরিমাণ:" else "Amount:", fontSize = 11.sp, color = TextMuted)
                            Text(formatTaka(selectedBillForView!!.amount, currentLanguage), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VividEmeraldAccent)
                        }
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Bill - ${selectedBillForView!!.supplierName}")
                                putExtra(Intent.EXTRA_TEXT, "Dokan Purchase Bill:\nSupplier: ${selectedBillForView!!.supplierName}\nInvoice: ${selectedBillForView!!.invoiceNo}\nAmount: ${selectedBillForView!!.amount} BDT")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Bill"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBangla) "শেয়ার করুন" else "Share Bill")
                    }
                }
            }
        }
    }

    if (showAddManualBillDialog) {
        AddManualBillDialog(
            isBangla = isBangla,
            onDismiss = { showAddManualBillDialog = false },
            onSave = { supplier, invoice, amount ->
                scope.launch {
                    repository.insertPurchases(
                        listOf(
                            PurchaseRecord(
                                productId = 0L,
                                productName = "Manual Bill Receipt",
                                supplierName = supplier,
                                totalCost = amount,
                                paidAmount = amount,
                                dueAmount = 0.0,
                                note = "Bill / Invoice: $invoice",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    )
                    Toast.makeText(context, if (isBangla) "বিল সফলভাবে যোগ করা হয়েছে!" else "Bill added to gallery!", Toast.LENGTH_SHORT).show()
                    showAddManualBillDialog = false
                }
            }
        )
    }
}

@Composable
private fun BillThumbnailCard(
    bill: GalleryBillItem,
    currentLanguage: AppLanguage,
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(bill.date))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("card_bill_${bill.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        border = BorderStroke(1.dp, DarkSlateCardBorder)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(DarkSlateSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (bill.imageUri.isNotBlank()) {
                    AsyncImage(
                        model = bill.imageUri,
                        contentDescription = "Bill image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Image, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                }
            }

            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = bill.supplierName.ifBlank { "Supplier Bill" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatTaka(bill.amount, currentLanguage),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = VividEmeraldAccent
                )
                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun AddManualBillDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, Double) -> Unit
) {
    var supplierName by remember { mutableStateOf("") }
    var invoiceNumber by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "নতুন বিল রসিদ যোগ করুন" else "Add New Bill Receipt",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = supplierName,
                    onValueChange = { supplierName = it },
                    label = { Text(if (isBangla) "সাপ্লায়ার / মহাজনের নাম *" else "Supplier Name *") },
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
                    value = invoiceNumber,
                    onValueChange = { invoiceNumber = it },
                    label = { Text(if (isBangla) "চালান বা ইনভয়েস নম্বর" else "Invoice / Bill Number") },
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
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(if (isBangla) "টাকার পরিমাণ (৳) *" else "Total Amount (৳) *") },
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
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (supplierName.isNotBlank() && amount > 0) {
                        onSave(supplierName.trim(), invoiceNumber.trim().ifBlank { "BILL-${System.currentTimeMillis() % 10000}" }, amount)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
            ) {
                Text(if (isBangla) "সংরক্ষণ" else "Save Bill")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}
