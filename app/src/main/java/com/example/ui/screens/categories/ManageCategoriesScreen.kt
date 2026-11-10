package com.example.ui.screens.categories

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.People
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.entity.Category
import com.example.ui.localization.AppLanguage
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
fun ManageCategoriesScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val scope = rememberCoroutineScope()
    val repository = remember { AppDatabase.getRepository(context) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Item, 1: Party, 2: Expense, 3: Other Income
    var showAddDialog by remember { mutableStateOf(false) }

    // Item Categories from Room
    val itemCategories by repository.allCategories.collectAsState(initial = emptyList())

    // In-memory persistent custom categories for Party, Expense, and Income
    val partyCategories = remember {
        mutableStateListOf(
            "Regular", "VIP", "Wholesale Buyer", "Retail Customer", "Local Dealer", "Key Supplier"
        )
    }

    val expenseCategories = remember {
        mutableStateListOf(
            "Shop Maintenance", "Rent", "Electricity & Utility", "Staff Salary", "Transport & Labor", "Tea & Snacks", "Marketing", "Other"
        )
    }

    val incomeCategories = remember {
        mutableStateListOf(
            "Scrap Sale", "Commission", "Cashback", "Delivery Charge", "Rent Income", "Interest", "Other"
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab Row
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
                            text = if (isBangla) "পণ্য (${itemCategories.size})" else "Item (${itemCategories.size})",
                            fontSize = 12.sp,
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
                            text = if (isBangla) "পার্টি (${partyCategories.size})" else "Party (${partyCategories.size})",
                            fontSize = 12.sp,
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
                            text = if (isBangla) "খরচ (${expenseCategories.size})" else "Expense (${expenseCategories.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 2) VividEmeraldAccent else TextMuted
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Text(
                            text = if (isBangla) "আয় (${incomeCategories.size})" else "Income (${incomeCategories.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 3) VividEmeraldAccent else TextMuted
                        )
                    }
                )
            }

            // Description Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSlateSurfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = when (selectedTab) {
                        0 -> if (isBangla) "পণ্য ও মালামালের বিভাগ সমূহ পরিচালনা করুন" else "Manage inventory & stock product categories"
                        1 -> if (isBangla) "গ্রাহক ও মহাজনের পার্টি গ্রুপ ও শ্রেণীবিভাগ" else "Manage customer and supplier party categories"
                        2 -> if (isBangla) "দোকানের সকল প্রকার নিয়মিত ও অনিয়মিত খরচ খাত" else "Manage operational shop expense categories"
                        else -> if (isBangla) "বিক্রয় ব্যতীত অন্যান্য সকল আয়ের খাত সমূহ" else "Manage non-sales other income categories"
                    },
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            // List per Tab
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        items(itemCategories, key = { it.id }) { cat ->
                            CategoryItemRow(
                                title = if (isBangla) cat.nameBn.ifBlank { cat.nameEn } else cat.nameEn,
                                subtitle = "Item Category • ${if (cat.isVisible) "Visible" else "Hidden"}",
                                isDeletable = itemCategories.size > 1,
                                onDelete = {
                                    scope.launch {
                                        repository.deleteCategory(cat.id)
                                    }
                                }
                            )
                        }
                    }
                    1 -> {
                        items(partyCategories) { catName ->
                            CategoryItemRow(
                                title = catName,
                                subtitle = "Party Group (Customer / Supplier)",
                                isDeletable = partyCategories.size > 1,
                                onDelete = { partyCategories.remove(catName) }
                            )
                        }
                    }
                    2 -> {
                        items(expenseCategories) { catName ->
                            CategoryItemRow(
                                title = catName,
                                subtitle = "Operating Expense Category",
                                isDeletable = expenseCategories.size > 1,
                                onDelete = { expenseCategories.remove(catName) }
                            )
                        }
                    }
                    3 -> {
                        items(incomeCategories) { catName ->
                            CategoryItemRow(
                                title = catName,
                                subtitle = "Other Income Stream",
                                isDeletable = incomeCategories.size > 1,
                                onDelete = { incomeCategories.remove(catName) }
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }

        // Add Category FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = VividEmeraldPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_category")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBangla) "নতুন ক্যাটাগরি" else "Add Category",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showAddDialog) {
        AddNewCategoryDialog(
            tabIndex = selectedTab,
            isBangla = isBangla,
            onDismiss = { showAddDialog = false },
            onSave = { name ->
                when (selectedTab) {
                    0 -> {
                        scope.launch {
                            repository.addCategory(
                                Category(
                                    nameEn = name,
                                    nameBn = name,
                                    sortOrder = itemCategories.size + 1
                                )
                            )
                        }
                    }
                    1 -> partyCategories.add(name)
                    2 -> expenseCategories.add(name)
                    3 -> incomeCategories.add(name)
                }
                showAddDialog = false
                Toast.makeText(context, if (isBangla) "ক্যাটাগরি যোগ করা হয়েছে!" else "Category added!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun CategoryItemRow(
    title: String,
    subtitle: String,
    isDeletable: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        border = BorderStroke(1.dp, DarkSlateCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(VividEmeraldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = VividEmeraldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            if (isDeletable) {
                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun AddNewCategoryDialog(
    tabIndex: Int,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val tabTitle = when (tabIndex) {
        0 -> if (isBangla) "পণ্য ক্যাটাগরি" else "Item Category"
        1 -> if (isBangla) "পার্টি ক্যাটাগরি" else "Party Category"
        2 -> if (isBangla) "খরচ ক্যাটাগরি" else "Expense Category"
        else -> if (isBangla) "আয় ক্যাটাগরি" else "Income Category"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = "${if (isBangla) "নতুন" else "New"} $tabTitle",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isBangla) "ক্যাটাগরির নাম *" else "Category Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_new_category_name"),
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
                    if (name.isNotBlank()) onSave(name.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_save_new_category")
            ) {
                Text(if (isBangla) "সংরক্ষণ" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}
