package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.AppSettings
import com.example.data.local.UnitManager
import com.example.data.local.entity.Category
import com.example.data.local.entity.Product
import com.example.data.repository.HardwareRepository
import com.example.ui.components.HardwareThumbnailIcon
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
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun StockScreen(
    currentLanguage: AppLanguage,
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

    val products by repository.allProducts.collectAsState(initial = emptyList())
    val dbCategories by repository.allCategories.collectAsState(initial = emptyList())

    var isGridView by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var onlyLowStock by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productForAddStock by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var showCategoryManagerDialog by remember { mutableStateOf(false) }
    var showBarcodeScanDialog by remember { mutableStateOf(false) }
    var showFileImportDialog by remember { mutableStateOf(false) }
    var prefilledBarcodeForAdd by remember { mutableStateOf("") }

    // File Picker for CSV Import
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val lines = reader.readLines()
                reader.close()

                if (lines.isNotEmpty()) {
                    scope.launch {
                        var importedCount = 0
                        var skippedCount = 0
                        // Simple CSV parser: Name, Category, BuyPrice, SellPrice, Qty, Unit, Barcode
                        for (i in 1 until lines.size) {
                            val line = lines[i].trim()
                            if (line.isBlank()) continue
                            val parts = line.split(",").map { it.trim().removeSurrounding("\"") }
                            if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                                val name = parts[0]
                                val cat = if (parts.size > 1 && parts[1].isNotBlank()) parts[1] else "General"
                                val buy = if (parts.size > 2) parts[2].toDoubleOrNull() ?: 0.0 else 0.0
                                val sell = if (parts.size > 3) parts[3].toDoubleOrNull() ?: (buy * 1.2) else (buy * 1.2)
                                val qty = if (parts.size > 4) parts[4].toIntOrNull() ?: 10 else 10
                                val unit = if (parts.size > 5 && parts[5].isNotBlank()) parts[5] else "pcs"
                                val bar = if (parts.size > 6) parts[6] else ""

                                // Skip duplicate by name or barcode
                                val exists = products.any { p ->
                                    p.nameBn.equals(name, ignoreCase = true) ||
                                            p.nameEn.equals(name, ignoreCase = true) ||
                                            (bar.isNotBlank() && p.barcode == bar)
                                }
                                if (exists) {
                                    skippedCount++
                                } else {
                                    repository.insertProduct(
                                        Product(
                                            nameBn = name,
                                            nameEn = name,
                                            category = cat,
                                            buyPrice = buy,
                                            sellPrice = sell,
                                            stockQuantity = qty,
                                            unitBn = unit,
                                            unitEn = unit,
                                            barcode = bar
                                        )
                                    )
                                    importedCount++
                                }
                            }
                        }
                        Toast.makeText(
                            context,
                            if (isBangla) "$importedCount টি পণ্য ইম্পোর্ট সম্পন্ন, $skippedCount টি ডুপ্লিকেট বাদ"
                            else "Imported $importedCount items, skipped $skippedCount duplicates",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val filteredProducts = products.filter { product ->
        val matchesQuery = searchQuery.isBlank() ||
                product.nameBn.contains(searchQuery, ignoreCase = true) ||
                product.nameEn.contains(searchQuery, ignoreCase = true) ||
                product.category.contains(searchQuery, ignoreCase = true) ||
                product.barcode.contains(searchQuery, ignoreCase = true) ||
                product.location.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategory == null || product.category.equals(selectedCategory, ignoreCase = true)
        val matchesLowStock = !onlyLowStock || product.isLowStock
        matchesQuery && matchesCategory && matchesLowStock
    }

    val totalStockUnits = products.sumOf { it.stockQuantity }
    val lowStockCount = products.count { it.isLowStock }
    val totalInventoryValue = products.sumOf { it.buyPrice * it.stockQuantity }

    val categories = remember(products, dbCategories) {
        val fromProds = products.map { it.category }
        val fromDb = dbCategories.map { cat: com.example.data.local.entity.Category -> cat.nameBn.ifBlank { cat.nameEn } }
        (fromProds + fromDb).distinct().filter { it.isNotBlank() }
    }

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
            // Metrics Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Products Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.dp, DarkSlateCardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (isBangla) "মোট পণ্য" else "Total Items",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "${products.size} (${totalStockUnits})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                // Low Stock Alert Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onlyLowStock = !onlyLowStock },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (onlyLowStock) LowStockRed.copy(alpha = 0.2f) else DarkSlateSurface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (onlyLowStock) LowStockRed else DarkSlateCardBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (isBangla) "কম স্টক অ্যালার্ট" else "Low Stock",
                            fontSize = 11.sp,
                            color = if (lowStockCount > 0) LowStockRed else TextMuted
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (lowStockCount > 0) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = LowStockRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "$lowStockCount",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (lowStockCount > 0) LowStockRed else TextPrimary
                            )
                        }
                    }
                }

                // Stock Value Card
                Card(
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                    border = BorderStroke(1.dp, DarkSlateCardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (isBangla) "মোট স্টক মূল্য" else "Stock Value",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = formatTaka(totalInventoryValue, currentLanguage, isMaskable = true),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = VividEmeraldAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Search Bar & Utility Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stock_search_input"),
                    placeholder = {
                        Text(
                            if (isBangla) "নাম, বারকোড বা তাক খুঁজুন..." else "Search name, barcode, shelf...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                            IconButton(onClick = { showBarcodeScanDialog = true }) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode", tint = VividEmeraldAccent)
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

                // Grid / List View Toggle Button
                IconButton(
                    onClick = { isGridView = !isGridView },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSlateSurface)
                        .testTag("btn_toggle_stock_view")
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "Toggle Grid/List",
                        tint = VividEmeraldAccent
                    )
                }

                // File Import Button
                IconButton(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSlateSurface)
                        .testTag("btn_import_stock_file")
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = "Import CSV",
                        tint = VividEmeraldAccent
                    )
                }
            }

            // Category Horizontal Chips + Add Category Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Manage Categories Button
                OutlinedButton(
                    onClick = { showCategoryManagerDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, VividEmeraldAccent),
                    modifier = Modifier.testTag("btn_manage_categories")
                ) {
                    Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(14.dp), tint = VividEmeraldAccent)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "+ ক্যাটাগরি" else "+ Category", fontSize = 11.sp, color = VividEmeraldAccent)
                }

                FilterChip(
                    selected = selectedCategory == null && !onlyLowStock,
                    onClick = {
                        selectedCategory = null
                        onlyLowStock = false
                    },
                    label = { Text(if (isBangla) "সকল পণ্য" else "All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VividEmeraldPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = DarkSlateSurface,
                        labelColor = TextMuted
                    )
                )

                FilterChip(
                    selected = onlyLowStock,
                    onClick = { onlyLowStock = !onlyLowStock },
                    label = {
                        Text(if (isBangla) "কম স্টক ($lowStockCount)" else "Low Stock ($lowStockCount)")
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LowStockRed,
                        selectedLabelColor = Color.White,
                        containerColor = DarkSlateSurface,
                        labelColor = if (lowStockCount > 0) LowStockRed else TextMuted
                    )
                )

                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = if (selectedCategory == cat) null else cat
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

            // Products Content (Grid vs List)
            if (filteredProducts.isEmpty()) {
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
                            Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (isBangla) "কোন পণ্য পাওয়া যায়নি" else "No products found",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            } else if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 72.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductStockGridCard(
                            product = product,
                            currentLanguage = currentLanguage,
                            onAddStockClick = { productForAddStock = product },
                            onEditClick = { productToEdit = product }
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
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductStockCard(
                            product = product,
                            currentLanguage = currentLanguage,
                            onAddStockClick = { productForAddStock = product },
                            onEditClick = { productToEdit = product },
                            onDeleteClick = { productToDelete = product }
                        )
                    }
                }
            }
        }

        // Add Product FAB
        FloatingActionButton(
            onClick = {
                prefilledBarcodeForAdd = ""
                showAddDialog = true
            },
            containerColor = VividEmeraldPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_product")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Product", modifier = Modifier.size(28.dp))
        }
    }

    // Add Product Dialog
    if (showAddDialog) {
        ProductFormDialog(
            isBangla = isBangla,
            product = null,
            initialBarcode = prefilledBarcodeForAdd,
            categories = categories,
            onDismiss = { showAddDialog = false },
            onSave = { newProd ->
                scope.launch {
                    repository.insertProduct(newProd)
                    showAddDialog = false
                    Toast.makeText(context, if (isBangla) "পণ্য যুক্ত হয়েছে!" else "Product Added!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Edit Product Dialog
    productToEdit?.let { prod ->
        ProductFormDialog(
            isBangla = isBangla,
            product = prod,
            initialBarcode = prod.barcode,
            categories = categories,
            onDismiss = { productToEdit = null },
            onSave = { updated ->
                scope.launch {
                    repository.updateProduct(updated)
                    productToEdit = null
                    Toast.makeText(context, if (isBangla) "পণ্য আপডেট হয়েছে!" else "Product Updated!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Quick Add Stock Dialog
    productForAddStock?.let { prod ->
        QuickAddStockDialog(
            product = prod,
            isBangla = isBangla,
            currentLanguage = currentLanguage,
            onDismiss = { productForAddStock = null },
            onConfirm = { addedQty, buyPrice ->
                scope.launch {
                    repository.addStock(
                        productId = prod.id,
                        productName = prod.localizedName(isBangla),
                        addedQty = addedQty,
                        unitBuyingPrice = buyPrice,
                        supplierName = "Direct Stock In"
                    )
                    productForAddStock = null
                    Toast.makeText(context, if (isBangla) "স্টক বৃদ্ধি পেয়েছে!" else "Stock Updated!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Delete Confirmation Dialog
    productToDelete?.let { prod ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            containerColor = DarkSlateSurface,
            title = {
                Text(
                    text = if (isBangla) "পণ্য মুছে ফেলতে চান?" else "Delete Product?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isBangla)
                        "'${prod.localizedName(isBangla)}' মুছে ফেললে এটি স্টকে দেখাবে না।"
                    else
                        "Are you sure you want to delete '${prod.nameEn}'? It will be removed from stock.",
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.softDeleteProduct(prod.id)
                            productToDelete = null
                            Toast.makeText(context, if (isBangla) "পণ্য মুছে ফেলা হয়েছে" else "Product Deleted", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LowStockRed)
                ) {
                    Text(if (isBangla) "মুছে ফেলুন" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
                }
            }
        )
    }

    // Category Manager Dialog
    if (showCategoryManagerDialog) {
        CategoryManagerDialog(
            existingCategories = categories,
            isBangla = isBangla,
            onDismiss = { showCategoryManagerDialog = false },
            onAddCategory = { newCatName ->
                scope.launch {
                    repository.insertCategory(
                        com.example.data.local.entity.Category(
                            nameEn = newCatName,
                            nameBn = newCatName,
                            emoji = "📦",
                            sortOrder = 99,
                            isVisible = true
                        )
                    )
                    Toast.makeText(context, if (isBangla) "ক্যাটাগরি যুক্ত হয়েছে!" else "Category Added!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Barcode Lookup / Scan Dialog
    if (showBarcodeScanDialog) {
        BarcodeScanLookupDialog(
            isBangla = isBangla,
            onDismiss = { showBarcodeScanDialog = false },
            onBarcodeEntered = { scannedCode ->
                showBarcodeScanDialog = false
                val match = products.find { it.barcode.equals(scannedCode, ignoreCase = true) }
                if (match != null) {
                    searchQuery = match.barcode
                    Toast.makeText(context, "${match.localizedName(isBangla)} found!", Toast.LENGTH_SHORT).show()
                } else {
                    prefilledBarcodeForAdd = scannedCode
                    showAddDialog = true
                    Toast.makeText(context, "New barcode: $scannedCode. Enter product details.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun ProductStockCard(
    product: Product,
    currentLanguage: AppLanguage,
    onAddStockClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isBangla = currentLanguage == AppLanguage.BANGLA

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_stock_item_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        border = BorderStroke(1.dp, if (product.isLowStock) LowStockRed.copy(alpha = 0.5f) else DarkSlateCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HardwareThumbnailIcon(
                    photoKey = product.photoKey,
                    category = product.category,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.localizedName(isBangla),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${product.category} • ${product.localizedUnit(isBangla)}",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        if (product.location.isNotBlank()) {
                            Text(
                                text = " • 📍 ${product.location}",
                                fontSize = 11.sp,
                                color = VividEmeraldAccent
                            )
                        }
                    }
                    if (product.barcode.isNotBlank()) {
                        Text(
                            text = "Barcode: ${product.barcode}",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (product.isLowStock) LowStockRed.copy(alpha = 0.15f) else VividEmeraldPrimary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (product.isLowStock) LowStockRed else VividEmeraldPrimary)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (product.isLowStock) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = LowStockRed,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "${product.stockQuantity} ${product.localizedUnit(isBangla)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (product.isLowStock) LowStockRed else VividEmeraldAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column {
                        Text(if (isBangla) "বিক্রয় মূল্য" else "Sell Price", fontSize = 10.sp, color = TextMuted)
                        Text(
                            text = formatTaka(product.sellPrice, currentLanguage),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Column {
                        Text(if (isBangla) "ক্রয় মূল্য" else "Buy Price", fontSize = 10.sp, color = TextMuted)
                        Text(
                            text = formatTaka(product.buyPrice, currentLanguage, isMaskable = true),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                    }
                    if (product.wholesalePrice > 0) {
                        Column {
                            Text(if (isBangla) "পাইকারি" else "Wholesale", fontSize = 10.sp, color = TextMuted)
                            Text(
                                text = formatTaka(product.wholesalePrice, currentLanguage),
                                fontSize = 12.sp,
                                color = VividEmeraldAccent
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onAddStockClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_add_stock_${product.id}")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBangla) "স্টক +" else "+ Stock", fontSize = 12.sp)
                    }

                    IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }

                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProductStockGridCard(
    product: Product,
    currentLanguage: AppLanguage,
    onAddStockClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val isBangla = currentLanguage == AppLanguage.BANGLA

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_grid_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        border = BorderStroke(1.dp, if (product.isLowStock) LowStockRed.copy(alpha = 0.5f) else DarkSlateCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                HardwareThumbnailIcon(
                    photoKey = product.photoKey,
                    category = product.category,
                    modifier = Modifier.size(48.dp)
                )

                if (product.isLowStock) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd),
                        shape = CircleShape,
                        color = LowStockRed
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Text(
                text = product.localizedName(isBangla),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${product.stockQuantity} ${product.localizedUnit(isBangla)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (product.isLowStock) LowStockRed else VividEmeraldAccent
            )

            Text(
                text = formatTaka(product.sellPrice, currentLanguage),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextMuted, modifier = Modifier.size(14.dp))
                }

                Button(
                    onClick = onAddStockClick,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("+ Stock", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun ProductFormDialog(
    isBangla: Boolean,
    product: Product?,
    initialBarcode: String = "",
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var nameBn by remember { mutableStateOf(product?.nameBn ?: "") }
    var nameEn by remember { mutableStateOf(product?.nameEn ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var category by remember { mutableStateOf(product?.category ?: if (categories.isNotEmpty()) categories[0] else "Hardware") }
    var buyPriceStr by remember { mutableStateOf(product?.buyPrice?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var sellPriceStr by remember { mutableStateOf(product?.sellPrice?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var stockStr by remember { mutableStateOf(product?.stockQuantity?.toString() ?: "10") }
    var lowStockStr by remember { mutableStateOf(product?.lowStockThreshold?.toString() ?: "5") }
    var unit by remember { mutableStateOf(product?.unitBn ?: "pcs") }
    var location by remember { mutableStateOf(product?.location ?: "") }
    var wholesalePriceStr by remember { mutableStateOf(product?.wholesalePrice?.takeIf { it > 0 }?.toString() ?: "") }
    var mrpStr by remember { mutableStateOf(product?.mrp?.takeIf { it > 0 }?.toString() ?: "") }
    var barcode by remember { mutableStateOf(product?.barcode?.ifBlank { initialBarcode } ?: initialBarcode) }
    var photoUri by remember { mutableStateOf(product?.photoKey ?: "") }

    val allUnits = remember { UnitManager.getAllUnits() }
    var isUnitDropdownOpen by remember { mutableStateOf(false) }
    var isCategoryDropdownOpen by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (product == null) {
                    if (isBangla) "নতুন পণ্য যোগ করুন" else "Add New Product"
                } else {
                    if (isBangla) "পণ্য এডিট করুন" else "Edit Product"
                },
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
                // Photo Picker Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSlateSurfaceVariant)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri.isNotBlank()) {
                            HardwareThumbnailIcon(photoKey = photoUri, category = category, modifier = Modifier.fillMaxSize())
                        } else {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Photo", tint = VividEmeraldAccent)
                        }
                    }

                    Column {
                        Text(if (isBangla) "পণ্যের ছবি" else "Product Photo", fontSize = 13.sp, color = TextPrimary)
                        Text(if (isBangla) "ছবি তুলতে বা গ্যালারি থেকে যোগ করতে ট্যাপ করুন" else "Tap to choose image", fontSize = 11.sp, color = TextMuted)
                    }
                }

                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text(if (isBangla) "পণ্যের নাম (বাংলা) *" else "Name (Bangla) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_prod_name_bn"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text(if (isBangla) "পণ্যের নাম (English)" else "Name (English)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_prod_name_en"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Category & Unit Picker Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text(if (isBangla) "ক্যাটাগরি" else "Category") },
                            trailingIcon = {
                                IconButton(onClick = { isCategoryDropdownOpen = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VividEmeraldPrimary,
                                unfocusedBorderColor = DarkSlateCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        DropdownMenu(
                            expanded = isCategoryDropdownOpen,
                            onDismissRequest = { isCategoryDropdownOpen = false }
                        ) {
                            categories.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c) },
                                    onClick = {
                                        category = c
                                        isCategoryDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text(if (isBangla) "একক (Unit)" else "Unit") },
                            trailingIcon = {
                                IconButton(onClick = { isUnitDropdownOpen = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VividEmeraldPrimary,
                                unfocusedBorderColor = DarkSlateCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        DropdownMenu(
                            expanded = isUnitDropdownOpen,
                            onDismissRequest = { isUnitDropdownOpen = false }
                        ) {
                            allUnits.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u) },
                                    onClick = {
                                        unit = u
                                        isUnitDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Buy & Sell Price Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = buyPriceStr,
                        onValueChange = { buyPriceStr = it },
                        label = { Text(if (isBangla) "ক্রয় মূল্য *" else "Buy Price *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_prod_buy_price"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = sellPriceStr,
                        onValueChange = { sellPriceStr = it },
                        label = { Text(if (isBangla) "বিক্রয় মূল্য *" else "Sell Price *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_prod_sell_price"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                // Wholesale Price & MRP Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = wholesalePriceStr,
                        onValueChange = { wholesalePriceStr = it },
                        label = { Text(if (isBangla) "পাইকারি দর" else "Wholesale Price") },
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

                    OutlinedTextField(
                        value = mrpStr,
                        onValueChange = { mrpStr = it },
                        label = { Text(if (isBangla) "এমআরপি (MRP)" else "MRP") },
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

                // Stock Qty & Low Stock Alert Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text(if (isBangla) "বর্তমান স্টক" else "Stock Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_prod_stock"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = lowStockStr,
                        onValueChange = { lowStockStr = it },
                        label = { Text(if (isBangla) "কম স্টক সীমা (default 5)" else "Low Stock Limit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                // Shelf Location & Barcode Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text(if (isBangla) "তাক / লোকেশন" else "Shelf Location") },
                        placeholder = { Text("e.g. Rack A-2") },
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
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text(if (isBangla) "বারকোড (Barcode)" else "Barcode") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_prod_barcode"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (isBangla) "বিবরণ (ঐচ্ছিক)" else "Description (Optional)") },
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
                    val buy = buyPriceStr.toDoubleOrNull() ?: 0.0
                    val sell = sellPriceStr.toDoubleOrNull() ?: 0.0
                    val qty = stockStr.toIntOrNull() ?: 0
                    val alert = lowStockStr.toIntOrNull() ?: 5
                    val wholesale = wholesalePriceStr.toDoubleOrNull() ?: 0.0
                    val mrpVal = mrpStr.toDoubleOrNull() ?: 0.0
                    val finalEn = nameEn.ifBlank { nameBn }
                    val finalBn = nameBn.ifBlank { nameEn }

                    if (finalBn.isNotBlank()) {
                        val result = product?.copy(
                            nameBn = finalBn,
                            nameEn = finalEn,
                            category = category.trim().ifBlank { "General" },
                            description = description.trim(),
                            photoKey = photoUri,
                            buyPrice = buy,
                            sellPrice = sell,
                            stockQuantity = qty,
                            lowStockThreshold = alert,
                            unitBn = unit,
                            unitEn = unit,
                            barcode = barcode.trim(),
                            location = location.trim(),
                            wholesalePrice = wholesale,
                            mrp = mrpVal
                        ) ?: Product(
                            nameBn = finalBn,
                            nameEn = finalEn,
                            category = category.trim().ifBlank { "General" },
                            description = description.trim(),
                            photoKey = photoUri,
                            buyPrice = buy,
                            sellPrice = sell,
                            stockQuantity = qty,
                            lowStockThreshold = alert,
                            unitBn = unit,
                            unitEn = unit,
                            barcode = barcode.trim(),
                            location = location.trim(),
                            wholesalePrice = wholesale,
                            mrp = mrpVal
                        )
                        onSave(result)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_save_product")
            ) {
                Text(if (isBangla) "সংরক্ষণ করুন" else "Save")
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
fun CategoryManagerDialog(
    existingCategories: List<String>,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onAddCategory: (String) -> Unit
) {
    var newCategoryName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "ক্যাটাগরি ব্যবস্থাপনা" else "Manage Categories",
                fontSize = 17.sp,
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
                    text = if (isBangla) "বিদ্যমান ক্যাটাগরি সমূহ:" else "Existing Categories:",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    existingCategories.forEach { c ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkSlateSurfaceVariant
                        ) {
                            Text(
                                text = c,
                                fontSize = 12.sp,
                                color = VividEmeraldAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = DarkSlateCardBorder, thickness = 1.dp)

                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text(if (isBangla) "নতুন ক্যাটাগরির নাম" else "New Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_new_category"),
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
                    if (newCategoryName.isNotBlank()) {
                        onAddCategory(newCategoryName.trim())
                        newCategoryName = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_save_new_category")
            ) {
                Text(if (isBangla) "ক্যাটাগরি যোগ করুন" else "Add Category")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বন্ধ করুন" else "Close", color = TextMuted)
            }
        }
    )
}

@Composable
fun BarcodeScanLookupDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onBarcodeEntered: (String) -> Unit
) {
    var barcodeInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = VividEmeraldAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBangla) "বারকোড স্ক্যান বা ইনপুট" else "Scan or Enter Barcode",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
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
                        "ক্যামেরা স্ক্যানার দিয়ে স্ক্যান করুন অথবা পণ্যের বারকোড নম্বর লিখুন:"
                    else
                        "Scan with a handheld scanner or enter the product barcode number:",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                OutlinedTextField(
                    value = barcodeInput,
                    onValueChange = { barcodeInput = it },
                    label = { Text(if (isBangla) "বারকোড নম্বর" else "Barcode Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_scan_barcode"),
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
                    if (barcodeInput.isNotBlank()) {
                        onBarcodeEntered(barcodeInput.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_confirm_barcode_scan")
            ) {
                Text(if (isBangla) "খুঁজুন / যোগ করুন" else "Search / Add")
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
fun QuickAddStockDialog(
    product: Product,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, newBuyPrice: Double) -> Unit
) {
    var quantityStr by remember { mutableStateOf("10") }
    var buyPriceStr by remember {
        mutableStateOf(if (product.buyPrice % 1 == 0.0) product.buyPrice.toLong().toString() else product.buyPrice.toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "স্টক বৃদ্ধি করুন" else "Add Stock",
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
                    text = "${product.localizedName(isBangla)} (বর্তমান স্টক: ${product.stockQuantity} ${product.localizedUnit(isBangla)})",
                    fontSize = 13.sp,
                    color = VividEmeraldAccent
                )

                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text(if (isBangla) "যোগ করার পরিমাণ *" else "Quantity to Add *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_quick_add_qty"),
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
                    label = { Text(if (isBangla) "ক্রয় মূল্য (প্রতি একক)" else "Buy Price (per unit)") },
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
                    val addQty = quantityStr.toIntOrNull() ?: 0
                    val buy = buyPriceStr.toDoubleOrNull() ?: product.buyPrice
                    if (addQty > 0) {
                        onConfirm(addQty, buy)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_confirm_add_stock")
            ) {
                Text(if (isBangla) "স্টক যোগ করুন" else "Add to Stock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}
