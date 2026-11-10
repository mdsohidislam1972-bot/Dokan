package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.Expense
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.formatTaka
import com.example.ui.screens.business.BusinessViewModel
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
import java.util.Date
import java.util.Locale

@Composable
fun BusinessScreen(
    currentLanguage: AppLanguage,
    onNavigateToBaki: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val viewModel: BusinessViewModel = viewModel()
    val summary by viewModel.financialSummary.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Detailed Reports & Health Check Launcher Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToReports)
                    .testTag("banner_open_reports_hub"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = VividEmeraldPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBangla) "পূর্ণাঙ্গ ব্যবসায়িক রিপোর্ট ও হেলথ চেক >" else "Full Reports, Statements & Health Check >",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (isBangla) "মাসিক PDF ডাউনলোড, লাভ-লোকসান ও হিসাব যাচাই" else "Monthly PDF export, P&L, Day Book & Data Health",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Hero Profit Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_net_profit_summary"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, VividEmeraldPrimary.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBangla) "মোট নিট লাভ (সব খরচ বাদে)" else "Net Profit (After Expenses)",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatTaka(summary.netProfit, currentLanguage, isMaskable = true),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.netProfit >= 0) VividEmeraldAccent else LowStockRed
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = (if (summary.netProfit >= 0) VividEmeraldPrimary else LowStockRed).copy(alpha = 0.15f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (summary.netProfit >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (summary.netProfit >= 0) VividEmeraldAccent else LowStockRed,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = DarkSlateCardBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(if (isBangla) "মোট বিক্রয়" else "Total Sales", fontSize = 11.sp, color = TextMuted)
                            Text(formatTaka(summary.totalRevenue, currentLanguage), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                        Column {
                            Text(if (isBangla) "মোট খরচ" else "Total Expenses", fontSize = 11.sp, color = TextMuted)
                            Text(formatTaka(summary.totalExpenses, currentLanguage, isMaskable = true), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LowStockRed)
                        }
                        Column {
                            Text(if (isBangla) "মোট ক্রয় লাভ" else "Gross Margin", fontSize = 11.sp, color = TextMuted)
                            Text(formatTaka(summary.grossProfit, currentLanguage, isMaskable = true), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                    }
                }
            }

            // Customer Baki & Supplier Dena Quick Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Customer Baki (Receivable)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onNavigateToBaki)
                        .testTag("card_customer_baki_quick"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.dp, DarkSlateCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBangla) "কাস্টমারদের বাকি" else "Customer Baki",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = VividEmeraldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatTaka(summary.customerReceivableDue, currentLanguage, isMaskable = true),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = LowStockRed
                        )
                        Text(
                            text = if (isBangla) "খাতা খুলুন →" else "Open Ledger →",
                            fontSize = 11.sp,
                            color = VividEmeraldAccent,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Supplier Dena (Payable)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("card_supplier_dena_quick"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.dp, DarkSlateCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isBangla) "মহাজন দেনা" else "Supplier Due",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatTaka(summary.supplierPayableDena, currentLanguage, isMaskable = true),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFA000)
                        )
                        Text(
                            text = if (isBangla) "প্রদেয় দেনা" else "Payable Balance",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Expense Tracker Header & Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) "দোকানের খরচ সমূহ (Expenses)" else "Store Expenses",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Button(
                    onClick = { showAddExpenseDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_add_expense")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "খরচ লিখুন" else "Add Expense", fontSize = 12.sp)
                }
            }

            // Expenses List
            if (expenses.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.dp, DarkSlateCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.MoneyOff,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = if (isBangla) "কোন খরচের হিসাব লিপিবদ্ধ নেই" else "No expenses recorded yet",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                expenses.forEach { expense ->
                    ExpenseItemCard(
                        expense = expense,
                        currentLanguage = currentLanguage,
                        onDelete = { expenseToDelete = expense }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Add Expense Dialog
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            isBangla = isBangla,
            onDismiss = { showAddExpenseDialog = false },
            onSave = { title, amount, category, note ->
                viewModel.addExpense(title, amount, category, note)
                showAddExpenseDialog = false
                Toast.makeText(context, if (isBangla) "খরচ যুক্ত হয়েছে!" else "Expense recorded!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Confirmation Dialog
    expenseToDelete?.let { exp ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            containerColor = DarkSlateSurface,
            title = {
                Text(
                    text = if (isBangla) "খরচ মুছুন?" else "Delete Expense?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isBangla)
                        "'${exp.title}' (${formatTaka(exp.amount, currentLanguage)}) মুছে ফেলতে চান?"
                    else
                        "Are you sure you want to delete '${exp.title}'?",
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExpense(exp.id)
                        expenseToDelete = null
                        Toast.makeText(context, if (isBangla) "খরচ মুছে ফেলা হয়েছে" else "Expense deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LowStockRed)
                ) {
                    Text(if (isBangla) "মুছে ফেলুন" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
fun ExpenseItemCard(
    expense: Expense,
    currentLanguage: AppLanguage,
    onDelete: () -> Unit
) {
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(expense.timestamp) { dateFormat.format(Date(expense.timestamp)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = expense.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${expense.category} • $formattedTime",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                if (expense.note.isNotBlank()) {
                    Text(
                        text = expense.note,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "- ${formatTaka(expense.amount, currentLanguage)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = LowStockRed
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Expense",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, category: String, note: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(if (isBangla) "দোকান খরচ" else "Shop Expense") }
    var note by remember { mutableStateOf("") }

    val categories = listOf(
        if (isBangla) "দোকান ভাড়া" else "Rent",
        if (isBangla) "বিদ্যুৎ বিল" else "Electricity",
        if (isBangla) "কর্মচারী বেতন" else "Salary",
        if (isBangla) "চা ও নাস্তা" else "Tea & Snacks",
        if (isBangla) "যাতায়াত / পরিবহন" else "Transport",
        if (isBangla) "অন্যান্য" else "Miscellaneous"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "দোকানের খরচ লিখুন" else "Record Store Expense",
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
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isBangla) "খরচের খাত / শিরোনাম *" else "Expense Title *") },
                    placeholder = { Text(if (isBangla) "যেমনঃ বিদ্যুৎ বিল বা চা নাস্তা" else "e.g. Electric bill") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_expense_title"),
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
                    label = { Text(if (isBangla) "টাকার পরিমাণ *" else "Amount *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_expense_amount"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(if (isBangla) "ক্যাটাগরি" else "Category") },
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
                    if (title.isNotBlank() && amt > 0) {
                        onSave(title, amt, category, note)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_save_expense")
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
