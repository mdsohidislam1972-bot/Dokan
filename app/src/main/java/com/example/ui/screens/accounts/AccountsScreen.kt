package com.example.ui.screens.accounts

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BankAccount
import com.example.data.local.entity.Expense
import com.example.data.local.entity.OtherIncome
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val scope = rememberCoroutineScope()
    val repository = remember { AppDatabase.getRepository(context) }

    val accounts by repository.allBankAccounts.collectAsState(initial = emptyList())
    val expenses by repository.allExpenses.collectAsState(initial = emptyList())
    val otherIncomes by repository.allOtherIncomes.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Accounts, 1: Expenses, 2: Other Income

    // Dialog state
    var showNewAccountDialog by remember { mutableStateOf(false) }
    var adjustingAccount by remember { mutableStateOf<BankAccount?>(null) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddIncomeDialog by remember { mutableStateOf(false) }

    val totalBalance = accounts.sumOf { it.currentBalance }
    val totalExpenses = expenses.sumOf { it.amount }
    val totalOtherIncome = otherIncomes.sumOf { it.amount }

    var showMergeLogDialog by remember { mutableStateOf(false) }
    var mergeLogDetail by remember { mutableStateOf(repository.getLatestMergeLog()) }

    // Run safe idempotency check & duplicate merge on screen entry
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val log = repository.ensureDefaultCashDrawerAndMergeDuplicates()
            if (log != null) {
                mergeLogDetail = log
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Overall Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBangla) "মোট ব্যালেন্স (ক্যাশ ও ব্যাংক)" else "Overall Total Balance",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatTaka(totalBalance, currentLanguage),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = VividEmeraldAccent
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = DarkSlateCardBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = LowStockRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isBangla) "মোট খরচ" else "Total Expense",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                            Text(
                                text = formatTaka(totalExpenses, currentLanguage),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LowStockRed
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = VividEmeraldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isBangla) "অন্যান্য আয়" else "Other Income",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                            Text(
                                text = formatTaka(totalOtherIncome, currentLanguage),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = VividEmeraldAccent
                            )
                        }
                    }
                }
            }

            if (mergeLogDetail != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurfaceVariant),
                    border = BorderStroke(1.dp, VividEmeraldPrimary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = VividEmeraldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBangla) "অডিট: ডুপ্লিকেট ক্যাশ মার্জ সম্পন্ন" else "Audit: Duplicate cash merged",
                                fontSize = 12.sp,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }
                        TextButton(
                            onClick = { showMergeLogDialog = true },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isBangla) "লগ দেখুন" else "View Log",
                                fontSize = 11.sp,
                                color = VividEmeraldAccent,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSlateSurface,
                contentColor = VividEmeraldAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = VividEmeraldAccent
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = if (isBangla) "ক্যাশ ও ব্যাংক (${accounts.size})" else "Accounts (${accounts.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) VividEmeraldAccent else TextMuted
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = if (isBangla) "খরচ সমূহ (${expenses.size})" else "Expenses (${expenses.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) VividEmeraldAccent else TextMuted
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = if (isBangla) "অন্যান্য আয় (${otherIncomes.size})" else "Other Income (${otherIncomes.size})",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 2) VividEmeraldAccent else TextMuted
                        )
                    }
                )
            }

            // Content per tab
            when (selectedTab) {
                0 -> {
                    // Accounts List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 0.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(accounts, key = { it.id }) { acc ->
                            AccountCard(
                                account = acc,
                                isBangla = isBangla,
                                currentLanguage = currentLanguage,
                                onAdjust = { adjustingAccount = acc },
                                onSetDefault = {
                                    scope.launch {
                                        repository.setDefaultAccount(acc.id)
                                        Toast.makeText(context, if (isBangla) "${acc.name} প্রধান অ্যাকাউন্ট হিসেবে সেট করা হয়েছে" else "${acc.name} set as default", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onDelete = {
                                    scope.launch {
                                        repository.deleteAccount(acc)
                                        Toast.makeText(context, if (isBangla) "অ্যাকাউন্ট মুছে ফেলা হয়েছে" else "Account deleted", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
                1 -> {
                    // Expenses List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(expenses, key = { it.id }) { exp ->
                            ExpenseCard(
                                expense = exp,
                                isBangla = isBangla,
                                currentLanguage = currentLanguage,
                                onDelete = {
                                    scope.launch {
                                        repository.deleteExpense(exp.id)
                                        Toast.makeText(context, if (isBangla) "খরচ মুছে ফেলা হয়েছে" else "Expense deleted", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
                2 -> {
                    // Other Income List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(otherIncomes, key = { it.id }) { inc ->
                            OtherIncomeCard(
                                income = inc,
                                isBangla = isBangla,
                                currentLanguage = currentLanguage,
                                onDelete = {
                                    scope.launch {
                                        repository.deleteOtherIncome(inc.id)
                                        Toast.makeText(context, if (isBangla) "আয় রেকর্ড মুছে ফেলা হয়েছে" else "Income record deleted", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }
        }

        // Action FAB depending on tab
        FloatingActionButton(
            onClick = {
                when (selectedTab) {
                    0 -> showNewAccountDialog = true
                    1 -> showAddExpenseDialog = true
                    2 -> showAddIncomeDialog = true
                }
            },
            containerColor = VividEmeraldPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_account_or_entry")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (selectedTab) {
                        0 -> if (isBangla) "নতুন অ্যাকাউন্ট" else "New Account"
                        1 -> if (isBangla) "খরচ যোগ" else "Add Expense"
                        else -> if (isBangla) "আয় যোগ" else "Add Income"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Dialog: New Account
    if (showNewAccountDialog) {
        NewAccountDialog(
            isBangla = isBangla,
            onDismiss = { showNewAccountDialog = false },
            onSave = { name, type, accNumber, balance, note ->
                scope.launch {
                    repository.insertAccount(
                        BankAccount(
                            name = name,
                            accountType = type,
                            accountNumber = accNumber,
                            currentBalance = balance,
                            note = note
                        )
                    )
                    showNewAccountDialog = false
                    Toast.makeText(context, if (isBangla) "নতুন অ্যাকাউন্ট যোগ হয়েছে!" else "Account created!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Dialog: Adjust Balance
    adjustingAccount?.let { acc ->
        AdjustBalanceDialog(
            account = acc,
            isBangla = isBangla,
            currentLanguage = currentLanguage,
            onDismiss = { adjustingAccount = null },
            onSaveAdjustment = { newBalance, reasonNote ->
                scope.launch {
                    val updatedNote = if (acc.note.isNotBlank()) "${acc.note} | Adj: $reasonNote" else "Adj: $reasonNote"
                    repository.updateAccount(
                        acc.copy(
                            currentBalance = newBalance,
                            note = updatedNote,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                    adjustingAccount = null
                    Toast.makeText(context, if (isBangla) "ব্যালেন্স সমন্বয় সফল হয়েছে!" else "Balance adjusted!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Dialog: Add Expense
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            isBangla = isBangla,
            accounts = accounts,
            onDismiss = { showAddExpenseDialog = false },
            onSave = { title, amount, category, note, selectedAccId ->
                scope.launch {
                    repository.addExpense(
                        title = title,
                        amount = amount,
                        category = category,
                        note = note
                    )
                    // If an account is selected, deduct from it
                    if (selectedAccId != null) {
                        val acc = accounts.find { it.id == selectedAccId }
                        if (acc != null) {
                            repository.updateAccount(
                                acc.copy(
                                    currentBalance = acc.currentBalance - amount,
                                    lastUpdated = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                    showAddExpenseDialog = false
                    Toast.makeText(context, if (isBangla) "খরচ রেকর্ড করা হয়েছে!" else "Expense recorded!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Dialog: Add Other Income
    if (showAddIncomeDialog) {
        AddOtherIncomeDialog(
            isBangla = isBangla,
            accounts = accounts,
            onDismiss = { showAddIncomeDialog = false },
            onSave = { title, amount, category, note, selectedAccId ->
                scope.launch {
                    repository.insertOtherIncome(
                        OtherIncome(
                            title = title,
                            amount = amount,
                            category = category,
                            accountId = selectedAccId,
                            note = note
                        )
                    )
                    // If an account is selected, credit to it
                    if (selectedAccId != null) {
                        val acc = accounts.find { it.id == selectedAccId }
                        if (acc != null) {
                            repository.updateAccount(
                                acc.copy(
                                    currentBalance = acc.currentBalance + amount,
                                    lastUpdated = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                    showAddIncomeDialog = false
                    Toast.makeText(context, if (isBangla) "অন্যান্য আয় রেকর্ড করা হয়েছে!" else "Other income recorded!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Dialog: Merge Audit Log
    if (showMergeLogDialog && mergeLogDetail != null) {
        AlertDialog(
            onDismissRequest = { showMergeLogDialog = false },
            containerColor = DarkSlateSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBangla) "ক্যাশ অ্যাকাউন্ট মার্জ অডিট লগ" else "Cash Account Merge Audit Log",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = mergeLogDetail ?: "",
                        fontSize = 13.sp,
                        color = TextMuted,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBangla) "✓ শুধুমাত্র একটি প্রধান (DEFAULT) ক্যাশ অ্যাকাউন্ট কার্যকর রয়েছে।" else "✓ Strictly one DEFAULT cash drawer is now active.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VividEmeraldAccent
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showMergeLogDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
                ) {
                    Text(if (isBangla) "ঠিক আছে" else "Close", maxLines = 1)
                }
            }
        )
    }
}

@Composable
fun AccountCard(
    account: BankAccount,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onAdjust: () -> Unit,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    val isWallet = account.accountType.equals("WALLET", ignoreCase = true)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("account_card_${account.id}"),
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isWallet) VividEmeraldPrimary.copy(alpha = 0.15f) else Color(0xFF3B82F6).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isWallet) Icons.Default.AccountBalanceWallet else Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = if (isWallet) VividEmeraldAccent else Color(0xFF60A5FA),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = account.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (account.isDefault) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(VividEmeraldPrimary.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isBangla) "প্রধান" else "DEFAULT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VividEmeraldAccent
                                    )
                                }
                            }
                        }
                        if (account.accountNumber.isNotBlank()) {
                            Text(
                                text = "${if (isWallet) "Wallet ID: " else "A/C: "}${account.accountNumber}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Balance
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatTaka(account.currentBalance, currentLanguage),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (account.currentBalance >= 0) VividEmeraldAccent else LowStockRed
                    )
                    Text(
                        text = if (isWallet) (if (isBangla) "মোবাইল/ক্যাশ" else "Wallet") else (if (isBangla) "ব্যাংক" else "Bank"),
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            if (account.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = account.note,
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DarkSlateCardBorder)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onAdjust,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSlateSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_adjust_balance_${account.id}")
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBangla) "ব্যালেন্স সমন্বয় (Adjust)" else "Adjust Balance",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        maxLines = 1
                    )
                }

                if (!account.isDefault) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = onSetDefault,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VividEmeraldAccent),
                            border = BorderStroke(1.dp, VividEmeraldPrimary.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("btn_set_default_${account.id}")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBangla) "প্রধান করুন" else "Make Default",
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp).testTag("btn_delete_account_${account.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = LowStockRed.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseCard(
    expense: Expense,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onDelete: () -> Unit
) {
    val dateStr = remember(expense.timestamp) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(expense.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(LowStockRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoneyOff,
                        contentDescription = null,
                        tint = LowStockRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = expense.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${expense.category} • $dateStr",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    if (expense.note.isNotBlank()) {
                        Text(
                            text = "Note: ${expense.note}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "-${formatTaka(expense.amount, currentLanguage)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LowStockRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun OtherIncomeCard(
    income: OtherIncome,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onDelete: () -> Unit
) {
    val dateStr = remember(income.timestamp) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(income.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(VividEmeraldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Paid,
                        contentDescription = null,
                        tint = VividEmeraldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = income.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${income.category} • $dateStr",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    if (income.note.isNotBlank()) {
                        Text(
                            text = "Note: ${income.note}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "+${formatTaka(income.amount, currentLanguage)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VividEmeraldAccent
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// Dialog: New Account
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAccountDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, accNumber: String, balance: Double, note: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("WALLET") } // "WALLET" or "BANK"
    var accNumber by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val accountTypes = listOf(
        "WALLET" to if (isBangla) "ক্যাশ বা মোবাইল ওয়ালেট (bKash/Nagad/Cash)" else "Cash / Mobile Wallet",
        "BANK" to if (isBangla) "বাণিজ্যিক ব্যাংক অ্যাকাউন্ট (Bank A/C)" else "Bank Account"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "নতুন অ্যাকাউন্ট তৈরি করুন" else "Create New Account",
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
                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accountTypes.forEach { (typeKey, label) ->
                        val isSelected = selectedType == typeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) VividEmeraldPrimary else DarkSlateSurfaceVariant)
                                .clickable { selectedType = typeKey }
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (typeKey == "WALLET") (if (isBangla) "ওয়ালেট/ক্যাশ" else "Wallet") else (if (isBangla) "ব্যাংক" else "Bank"),
                                color = if (isSelected) Color.White else TextMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isBangla) "অ্যাকাউন্টের নাম *" else "Account Name *") },
                    placeholder = { Text(if (selectedType == "WALLET") "e.g. বিকাশ মার্চেন্ট" else "e.g. ডাচ বাংলা ব্যাংক") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_account_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = accNumber,
                    onValueChange = { accNumber = it },
                    label = { Text(if (selectedType == "WALLET") (if (isBangla) "মোবাইল/আইডি নম্বর" else "Wallet / Mobile No.") else (if (isBangla) "হিসাব নম্বর (A/C No.)" else "Account Number")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_account_number"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text(if (isBangla) "প্রাথমিক জমা ব্যালেন্স (৳)" else "Opening Balance (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_opening_balance"),
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
                    label = { Text(if (isBangla) "মন্তব্য / নোট (ঐচ্ছিক)" else "Note (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_account_note"),
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
                    if (name.isNotBlank()) {
                        val balance = balanceText.toDoubleOrNull() ?: 0.0
                        onSave(name.trim(), selectedType, accNumber.trim(), balance, note.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_save_account")
            ) {
                Text(if (isBangla) "সংরক্ষণ করুন" else "Save Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}

// Dialog: Adjust Balance
@Composable
fun AdjustBalanceDialog(
    account: BankAccount,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSaveAdjustment: (newBalance: Double, reason: String) -> Unit
) {
    var newBalanceText by remember { mutableStateOf(account.currentBalance.toString()) }
    var reasonNote by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "ব্যালেন্স সমন্বয় (Adjust Balance)" else "Adjust Balance",
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
                    text = "${account.name} (${if (isBangla) "বর্তমান ব্যালেন্স:" else "Current:"} ${formatTaka(account.currentBalance, currentLanguage)})",
                    fontSize = 13.sp,
                    color = VividEmeraldAccent,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = newBalanceText,
                    onValueChange = { newBalanceText = it },
                    label = { Text(if (isBangla) "সঠিক নতুন ব্যালেন্স (৳) *" else "Correct New Balance (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_adjusted_balance"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = reasonNote,
                    onValueChange = { reasonNote = it },
                    label = { Text(if (isBangla) "সমন্বয়ের কারণ / নোট *" else "Adjustment Reason / Note *") },
                    placeholder = { Text(if (isBangla) "যেমন: ক্যাশ গণনা গরমিল, ব্যাংক চার্জ কর্তন" else "e.g. Cash mismatch, Bank fee") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_adjust_reason"),
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
                    val bal = newBalanceText.toDoubleOrNull()
                    if (bal != null) {
                        onSaveAdjustment(bal, reasonNote.ifBlank { "Manual Balance Adjustment" })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_confirm_adjust")
            ) {
                Text(if (isBangla) "সমন্বয় নিশ্চিত করুন" else "Confirm Adjustment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}

// Dialog: Add Expense
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    isBangla: Boolean,
    accounts: List<BankAccount>,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, category: String, note: String, accountId: Long?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Shop Maintenance") }
    var note by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }

    val categories = listOf(
        "Shop Maintenance" to if (isBangla) "দোকান মেরামত ও রক্ষণাবেক্ষণ" else "Shop Maintenance",
        "Rent" to if (isBangla) "দোকান ভাড়া" else "Shop Rent",
        "Electricity & Utility" to if (isBangla) "বিদ্যুৎ ও বিল" else "Electricity & Utility",
        "Staff Salary" to if (isBangla) "কর্মচারী বেতন" else "Staff Salary",
        "Transport & Labor" to if (isBangla) "যাতায়াত ও কুলি খরচ" else "Transport & Labor",
        "Tea & Snacks" to if (isBangla) "চা ও আপ্যায়ন" else "Tea & Snacks",
        "Marketing" to if (isBangla) "বিজ্ঞাপন ও প্রচারণা" else "Marketing",
        "Other" to if (isBangla) "অন্যান্য বিবিধ খরচ" else "Other Expense"
    )

    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "নতুন খরচ যোগ করুন" else "Record New Expense",
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
                    label = { Text(if (isBangla) "খরচের বিবরণ *" else "Expense Title *") },
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
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (isBangla) "খরচের পরিমাণ (৳) *" else "Amount (৳) *") },
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

                // Category selector
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.first == selectedCategory }?.second ?: selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBangla) "ক্যাটাগরি" else "Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.background(DarkSlateSurface)
                    ) {
                        categories.forEach { (catKey, catLabel) ->
                            DropdownMenuItem(
                                text = { Text(catLabel, color = TextPrimary) },
                                onClick = {
                                    selectedCategory = catKey
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Account to deduct from
                if (accounts.isNotEmpty()) {
                    Text(
                        text = if (isBangla) "যে অ্যাকাউন্ট থেকে কর্তন হবে:" else "Deduct from Account:",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        accounts.take(3).forEach { acc ->
                            val isSelected = selectedAccountId == acc.id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) VividEmeraldPrimary else DarkSlateSurfaceVariant)
                                    .clickable { selectedAccountId = acc.id }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = acc.name.take(10),
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White else TextMuted,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBangla) "নোট / মন্তব্য" else "Note") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_expense_note"),
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
                    val amount = amountText.toDoubleOrNull()
                    if (title.isNotBlank() && amount != null && amount > 0) {
                        onSave(title.trim(), amount, selectedCategory, note.trim(), selectedAccountId)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LowStockRed),
                modifier = Modifier.testTag("btn_confirm_add_expense")
            ) {
                Text(if (isBangla) "খরচ নিশ্চিত করুন" else "Save Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}

// Dialog: Add Other Income
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOtherIncomeDialog(
    isBangla: Boolean,
    accounts: List<BankAccount>,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, category: String, note: String, accountId: Long?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Scrap Sale") }
    var note by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }

    val categories = listOf(
        "Scrap Sale" to if (isBangla) "ভাঙারি বা পুরানো মালামাল বিক্রি" else "Scrap / Waste Sale",
        "Commission" to if (isBangla) "কমিশন আয়" else "Commission",
        "Cashback" to if (isBangla) "ক্যাশব্যাক / রিওয়ার্ড" else "Cashback / Reward",
        "Delivery" to if (isBangla) "ডেলিভারি চার্জ আয়" else "Delivery Charge",
        "Rent" to if (isBangla) "ভাড়া আয়" else "Rent Income",
        "Interest" to if (isBangla) "ব্যাংক সুদ / মুনাফা" else "Bank Profit / Interest",
        "Other" to if (isBangla) "অন্যান্য বিবিধ আয়" else "Other Income"
    )

    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "অন্যান্য আয় যোগ করুন" else "Record Other Income",
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
                    label = { Text(if (isBangla) "আয়ের শিরোনাম *" else "Income Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_income_title"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (isBangla) "আয়ের পরিমাণ (৳) *" else "Amount (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_income_amount"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Category selector
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.first == selectedCategory }?.second ?: selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBangla) "আয়ের ক্যাটাগরি" else "Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.background(DarkSlateSurface)
                    ) {
                        categories.forEach { (catKey, catLabel) ->
                            DropdownMenuItem(
                                text = { Text(catLabel, color = TextPrimary) },
                                onClick = {
                                    selectedCategory = catKey
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Account to deposit to
                if (accounts.isNotEmpty()) {
                    Text(
                        text = if (isBangla) "যে অ্যাকাউন্টে জমা হবে:" else "Deposit to Account:",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        accounts.take(3).forEach { acc ->
                            val isSelected = selectedAccountId == acc.id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) VividEmeraldPrimary else DarkSlateSurfaceVariant)
                                    .clickable { selectedAccountId = acc.id }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = acc.name.take(10),
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White else TextMuted,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBangla) "নোট / বিবরণ" else "Note") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_income_note"),
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
                    val amount = amountText.toDoubleOrNull()
                    if (title.isNotBlank() && amount != null && amount > 0) {
                        onSave(title.trim(), amount, selectedCategory, note.trim(), selectedAccountId)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_confirm_add_income")
            ) {
                Text(if (isBangla) "আয় সংরক্ষণ করুন" else "Save Income")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}
