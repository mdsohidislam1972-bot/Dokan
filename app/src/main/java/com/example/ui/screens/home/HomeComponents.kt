package com.example.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BusinessProfile
import com.example.data.local.entity.Customer
import com.example.data.local.entity.Product
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.formatTaka
import com.example.ui.localization.toBengaliNumerals
import com.example.ui.theme.*

@Composable
fun ProfileCompletionCard(
    activeProfile: BusinessProfile?,
    isBangla: Boolean,
    onCompleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completion = activeProfile?.getCompletionPercentage() ?: 40
    if (completion >= 100) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onCompleteClick() }
            .testTag("card_profile_completion"),
        color = DarkSlateSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, VividEmeraldPrimary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(VividEmeraldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = VividEmeraldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = if (isBangla) "দোকানের প্রোফাইল সম্পূর্ণ করুন" else "Complete Store Profile",
                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                Text(
                    text = if (isBangla) "${toBengaliNumerals(completion)}%" else "$completion%",
                    color = VividEmeraldAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            LinearProgressIndicator(
                progress = { completion / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = VividEmeraldAccent,
                trackColor = DarkSlateBackground
            )

            Text(
                text = if (isBangla) "ট্রেড লাইসেন্স ও ব্যাংক তথ্য দিলে গ্রাহক মেমোতে তথ্য প্রদর্শন হবে" else "Add trade license & bank info to display on customer invoices",
                style = AppTypographyScale.hintText,
                color = TextMuted
            )
        }
    }
}

@Composable
fun TotalBalanceCard(
    balance: Double,
    currentLanguage: AppLanguage,
    isBangla: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("card_total_balance"),
        color = DarkSlateSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, DarkSlateCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(VividEmeraldPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = VividEmeraldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = if (isBangla) "মোট ক্যাশ ও ব্যাংক ব্যালেন্স" else "Total Cash & Bank Balance",
                        style = AppTypographyScale.hintText,
                        color = TextMuted
                    )
                    Text(
                        text = formatTaka(balance, currentLanguage, isMaskable = true),
                        style = AppTypographyScale.screenTitle,
                        color = VividEmeraldAccent
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted
            )
        }
    }
}

@Composable
fun PeriodFiguresSection(
    figures: PeriodFigures,
    currentLanguage: AppLanguage,
    isBangla: Boolean,
    onSalesClick: () -> Unit,
    onPurchasesClick: () -> Unit,
    onExpensesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Sales
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onSalesClick() }
                .testTag("period_sales_card"),
            color = DarkSlateSurface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkSlateCardBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isBangla) "মোট বিক্রি" else "Total Sales",
                    style = AppTypographyScale.hintText,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTaka(figures.totalSales, currentLanguage, isMaskable = true),
                    style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        }

        // Purchases
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onPurchasesClick() }
                .testTag("period_purchases_card"),
            color = DarkSlateSurface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkSlateCardBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isBangla) "মোট ক্রয়" else "Purchases",
                    style = AppTypographyScale.hintText,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTaka(figures.totalPurchases, currentLanguage, isMaskable = true),
                    style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        }

        // Expenses
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onExpensesClick() }
                .testTag("period_expenses_card"),
            color = DarkSlateSurface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkSlateCardBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isBangla) "দোকান খরচ" else "Expenses",
                    style = AppTypographyScale.hintText,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTaka(figures.totalExpenses, currentLanguage, isMaskable = true),
                    style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                    color = LowStockRed
                )
            }
        }
    }
}

@Composable
fun CashflowChartCard(
    chartState: CashflowState,
    currentLanguage: AppLanguage,
    isBangla: Boolean,
    onChartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onChartClick() }
            .testTag("card_cashflow_chart"),
        color = DarkSlateSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, DarkSlateCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = VividEmeraldAccent)
                    Text(
                        text = if (isBangla) "৭ দিনের ক্যাশফ্লো (টাকা জমা ও প্রদান)" else "7-Day Cashflow In/Out",
                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isBangla) "টাকা জমা (In)" else "Cash In",
                        style = AppTypographyScale.hintText,
                        color = TextMuted
                    )
                    Text(
                        text = formatTaka(chartState.totalCashIn, currentLanguage),
                        fontWeight = FontWeight.Bold,
                        color = VividEmeraldAccent
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isBangla) "টাকা খরচ/প্রদান (Out)" else "Cash Out",
                        style = AppTypographyScale.hintText,
                        color = TextMuted
                    )
                    Text(
                        text = formatTaka(chartState.totalCashOut, currentLanguage),
                        fontWeight = FontWeight.Bold,
                        color = LowStockRed
                    )
                }
            }
        }
    }
}

@Composable
fun ExploreAppRow(
    isBangla: Boolean,
    onQuickEntryClick: () -> Unit,
    onQuickPosClick: () -> Unit,
    onViewReportsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onQuickEntryClick,
            modifier = Modifier.weight(1f).height(40.dp).testTag("btn_explore_quick_party"),
            colors = ButtonDefaults.buttonColors(containerColor = DarkSlateSurfaceVariant),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Text(if (isBangla) "⚡ দ্রুত এন্ট্রি" else "⚡ Quick Entry", fontSize = 12.sp, color = TextPrimary)
        }

        Button(
            onClick = onQuickPosClick,
            modifier = Modifier.weight(1f).height(40.dp).testTag("btn_explore_pos"),
            colors = ButtonDefaults.buttonColors(containerColor = DarkSlateSurfaceVariant),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Text(if (isBangla) "🛒 পিওএস বিক্রি" else "🛒 POS Sell", fontSize = 12.sp, color = TextPrimary)
        }

        Button(
            onClick = onViewReportsClick,
            modifier = Modifier.weight(1f).height(40.dp).testTag("btn_explore_reports"),
            colors = ButtonDefaults.buttonColors(containerColor = DarkSlateSurfaceVariant),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Text(if (isBangla) "📊 রিপোর্ট খাতা" else "📊 Reports", fontSize = 12.sp, color = TextPrimary)
        }
    }
}

@Composable
fun ShortcutsGridSection(
    shortcuts: List<AppShortcut>,
    isBangla: Boolean,
    onShortcutClick: (AppShortcut) -> Unit,
    onToggleShortcut: (AppShortcut) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isBangla) "শর্টকাট ফিচারসমূহ" else "Quick Shortcuts",
                style = AppTypographyScale.sectionHeading,
                color = TextPrimary
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(shortcuts.filter { it.isEnabled }) { item ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onShortcutClick(item) }
                        .testTag("shortcut_${item.id}"),
                    color = DarkSlateSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DarkSlateCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(VividEmeraldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(20.dp))
                        }
                        Text(
                            text = item.localizedTitle(isBangla),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PromotionalBannerCard(
    title: String,
    subtitle: String,
    isBangla: Boolean,
    onSaveBanner: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { showEditDialog = true }
            .testTag("card_promotional_banner"),
        color = DarkSlateSurfaceVariant,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, VividEmeraldPrimary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(VividEmeraldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = TextPrimary)
                }
                Column {
                    Text(
                        text = title,
                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                        color = VividEmeraldAccent
                    )
                    Text(
                        text = subtitle,
                        style = AppTypographyScale.hintText,
                        color = TextMuted
                    )
                }
            }

            IconButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Banner", tint = TextMuted)
            }
        }
    }

    if (showEditDialog) {
        var editTitle by remember { mutableStateOf(title) }
        var editSubtitle by remember { mutableStateOf(subtitle) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = DarkSlateSurface,
            title = {
                Text(if (isBangla) "ব্যানার বার্তা পরিবর্তন" else "Edit Banner Notice", color = TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editSubtitle,
                        onValueChange = { editSubtitle = it },
                        label = { Text("Subtitle") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveBanner(editTitle, editSubtitle)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun QuickAddPartyDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSaveCustomer: (name: String, phone: String, address: String, initialBaki: Double) -> Unit,
    onSaveSupplier: (name: String, company: String, phone: String, amountOwed: Double) -> Unit
) {
    var isSupplier by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var addressOrCompany by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) {
                    if (isSupplier) "নতুন মহাজন / সাপ্লায়ার যোগ" else "নতুন গ্রাহক / পার্টি যোগ"
                } else {
                    if (isSupplier) "Add New Supplier" else "Add New Customer"
                },
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = !isSupplier,
                        onClick = { isSupplier = false },
                        label = { Text(if (isBangla) "গ্রাহক (Customer)" else "Customer") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = isSupplier,
                        onClick = { isSupplier = true },
                        label = { Text(if (isBangla) "মহাজন (Supplier)" else "Supplier") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isBangla) "নাম" else "Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isBangla) "মোবাইল নম্বর" else "Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = addressOrCompany,
                    onValueChange = { addressOrCompany = it },
                    label = { Text(if (isSupplier) (if (isBangla) "কোম্পানির নাম" else "Company") else (if (isBangla) "ঠিকানা" else "Address")) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(if (isSupplier) (if (isBangla) "পূর্বের দেনা (টাকা)" else "Opening Due") else (if (isBangla) "পূর্বের বাকি (টাকা)" else "Opening Baki")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (isSupplier) {
                        onSaveSupplier(name, addressOrCompany, phone, amt)
                    } else {
                        onSaveCustomer(name, phone, addressOrCompany, amt)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
            ) {
                Text(if (isBangla) "সংরক্ষণ" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (isBangla) "বাতিল" else "Cancel") }
        }
    )
}

@Composable
fun QuickPaymentInDialog(
    customers: List<Customer>,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (custId: Long, amt: Double, note: String) -> Unit
) {
    var selectedCustId by remember { mutableStateOf(customers.firstOrNull { it.totalBaki > 0 }?.id ?: customers.firstOrNull()?.id ?: 0L) }
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = { Text(if (isBangla) "বাকির টাকা জমা (Payment In)" else "Receive Customer Payment", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (isBangla) "গ্রাহক নির্বাচন করুন:" else "Select Customer:", color = TextMuted, fontSize = 12.sp)

                var dropdownExpanded by remember { mutableStateOf(false) }
                val selectedCust = customers.find { it.id == selectedCustId }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { dropdownExpanded = true },
                    color = DarkSlateBackground
                ) {
                    Text(
                        text = selectedCust?.let { "${it.name} (বাকি: ৳${it.totalBaki.toLong()})" } ?: "Select",
                        modifier = Modifier.padding(12.dp),
                        color = TextPrimary
                    )
                }

                DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                    customers.forEach { c ->
                        DropdownMenuItem(
                            text = { Text("${c.name} - ৳${c.totalBaki.toLong()}") },
                            onClick = {
                                selectedCustId = c.id
                                dropdownExpanded = false
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(if (isBangla) "জমার পরিমাণ (টাকা)" else "Paid Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBangla) "নোট / ট্রানজেকশন আইডি" else "Note / Trx ID") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (selectedCustId > 0 && amt > 0) {
                        onConfirm(selectedCustId, amt, note)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
            ) {
                Text(if (isBangla) "জমা সম্পন্ন" else "Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (isBangla) "বাতিল" else "Cancel") }
        }
    )
}

@Composable
fun QuickSalesReturnDialog(
    products: List<Product>,
    customers: List<Customer>,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (prodId: Long, qty: Int, refundAmt: Double, custId: Long?, note: String) -> Unit
) {
    var selectedProdId by remember { mutableStateOf(products.firstOrNull()?.id ?: 0L) }
    var qtyStr by remember { mutableStateOf("1") }
    var refundStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = { Text(if (isBangla) "পণ্য বিক্রি ফেরত (Sales Return)" else "Process Sales Return", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val selectedProd = products.find { it.id == selectedProdId }

                Text(if (isBangla) "পণ্য: ${selectedProd?.localizedName(isBangla) ?: "None"}" else "Product: ${selectedProd?.nameEn ?: "None"}", color = TextPrimary)

                OutlinedTextField(
                    value = qtyStr,
                    onValueChange = { qtyStr = it },
                    label = { Text(if (isBangla) "ফেরতের সংখ্যা" else "Return Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = refundStr,
                    onValueChange = { refundStr = it },
                    label = { Text(if (isBangla) "ফেরত মূল্য (টাকা)" else "Refund Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBangla) "ফেরতের কারণ" else "Reason / Note") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = qtyStr.toIntOrNull() ?: 1
                    val refund = refundStr.toDoubleOrNull() ?: 0.0
                    onConfirm(selectedProdId, qty, refund, null, note)
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
            ) {
                Text(if (isBangla) "ফেরত নিন" else "Confirm Return")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (isBangla) "বাতিল" else "Cancel") }
        }
    )
}

@Composable
fun QuickQuotationDialog(
    products: List<Product>,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onConvertToSale: (prod: Product, qty: Int, price: Double, buyerName: String) -> Unit
) {
    var selectedProdId by remember { mutableStateOf(products.firstOrNull()?.id ?: 0L) }
    var qtyStr by remember { mutableStateOf("1") }
    var buyerName by remember { mutableStateOf("") }
    val selectedProd = products.find { it.id == selectedProdId }
    var priceStr by remember { mutableStateOf(selectedProd?.sellPrice?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = { Text(if (isBangla) "কোটেশন / মূল্য অনুমান" else "Create Quotation", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = { Text(if (isBangla) "ক্রেতার নাম" else "Buyer Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(if (isBangla) "পণ্য: ${selectedProd?.localizedName(isBangla) ?: ""}" else "Product: ${selectedProd?.nameEn ?: ""}", color = TextPrimary)

                OutlinedTextField(
                    value = qtyStr,
                    onValueChange = { qtyStr = it },
                    label = { Text(if (isBangla) "পরিমাণ" else "Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text(if (isBangla) "দর (প্রতি ইউনিট)" else "Unit Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedProd != null) {
                        val qty = qtyStr.toIntOrNull() ?: 1
                        val price = priceStr.toDoubleOrNull() ?: selectedProd.sellPrice
                        onConvertToSale(selectedProd, qty, price, buyerName.ifBlank { "Quotation Customer" })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
            ) {
                Text(if (isBangla) "বিক্রিতে রূপান্তর" else "Convert to Sale")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (isBangla) "বাতিল" else "Cancel") }
        }
    )
}
