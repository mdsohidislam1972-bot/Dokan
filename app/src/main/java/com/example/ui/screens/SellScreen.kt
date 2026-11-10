package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.provider.ContactsContract
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.ui.screens.sell.CartCheckoutBottomSheet
import com.example.ui.components.HardwareThumbnailIcon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppSettings
import com.example.data.local.ViewMode
import com.example.data.local.entity.Category
import com.example.data.local.entity.Customer
import com.example.data.local.entity.Product
import com.example.data.local.entity.SaleOrder
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.formatTaka
import com.example.ui.localization.toBengaliNumerals
import com.example.ui.screens.sell.SellViewModel
import com.example.ui.theme.AppTypographyScale
import com.example.ui.theme.DarkSlateBackground
import com.example.ui.theme.DarkSlateCardBorder
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.DarkSlateSurfaceVariant
import com.example.ui.theme.LowStockRed
import com.example.ui.theme.LowStockRedContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VividEmeraldAccent
import com.example.ui.theme.VividEmeraldPrimary

/**
 * Redesigned Sell Screen:
 * 1. Shows all stock items in a 3-column grid view matching Stock screen style.
 * 2. Search bar at top with category filter chips.
 * 3. Single tap selects an item with clear emerald border & checkmark badge; tapping another switches selection.
 * 4. Items with zero stock show "Out of stock" and cannot be selected.
 * 5. Fixed bottom bar with disabled "Sell" button until an item is selected, showing selected item name.
 * 6. Tapping "Sell" opens a comprehensive sell form bottom sheet with:
 *    - Quantity (with unit, steppers)
 *    - Selling price (pre-filled, editable)
 *    - Automatically calculated total
 *    - Optional discount (Detailed view)
 *    - Paid in full vs Baki (with customer selector, new customer creation, and part payment)
 *    - Stock validation warning blocking sale if quantity exceeds available stock
 *    - Confirm Sale & Cancel buttons
 * 7. On Confirm Sale: saves order, reduces inventory stock, updates baki balance if credit, shows success dialog,
 *    and clears selection.
 * 8. Fully adaptive to both Simple and Detailed views.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellScreen(
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier,
    viewModel: SellViewModel = viewModel()
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val isSimpleView = AppSettings.viewMode == ViewMode.SIMPLE

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val isSellSheetOpen by viewModel.isSellSheetOpen.collectAsStateWithLifecycle()
    val completedSaleOrder by viewModel.completedSaleOrder.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    var isCartSheetOpen by remember { mutableStateOf(false) }
    var showBarcodeScanDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
            .testTag("screen_sell")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp) // Leave space for the fixed bottom sell bar
        ) {
            // 1. Search Bar & Category Filter Header
            SellTopSearchSection(
                searchQuery = searchQuery,
                onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                selectedCategory = selectedCategory,
                categories = categories,
                onCategorySelected = { viewModel.onCategorySelected(it) },
                onBarcodeScanClick = { showBarcodeScanDialog = true },
                isBangla = isBangla
            )

            // 2. Stock Items Grid View (Same style as Stock screen)
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (isBangla) "কোনো পণ্য পাওয়া যায়নি" else "No products found",
                            style = AppTypographyScale.sectionHeading,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        val isSelected = selectedProduct?.id == product.id
                        val isOutOfStock = product.stockQuantity <= 0

                        SellProductGridCard(
                            product = product,
                            isSelected = isSelected,
                            isOutOfStock = isOutOfStock,
                            isBangla = isBangla,
                            currentLanguage = currentLanguage,
                            isSimpleView = isSimpleView,
                            onCardClick = {
                                if (isOutOfStock) {
                                    Toast.makeText(
                                        context,
                                        if (isBangla) "পণ্যটি স্টকে নেই!" else "Item is out of stock!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    viewModel.selectProduct(product)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Floating Cart Summary Banner (Requirements 34 & 40)
        AnimatedVisibility(
            visible = cartItems.isNotEmpty(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isCartSheetOpen = true },
                shape = RoundedCornerShape(12.dp),
                color = VividEmeraldPrimary,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "${if (isBangla) "কার্ট: " else "Cart: "}${cartItems.size} ${if (isBangla) "পণ্য" else "items"} • ৳${cartItems.sumOf { it.total }.toLong()}",
                            style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Text(
                        text = if (isBangla) "কার্ট দেখুন >" else "View Cart >",
                        style = AppTypographyScale.buttonText.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        // 3. Fixed Bottom Bar with Selected Item Summary & Big "Sell" Button
        FixedBottomSellBar(
            selectedProduct = selectedProduct,
            isBangla = isBangla,
            currentLanguage = currentLanguage,
            onSellClick = { viewModel.openSellForm() },
            onAddToCartClick = {
                selectedProduct?.let { prod ->
                    viewModel.addToCart(prod, 1)
                    Toast.makeText(
                        context,
                        if (isBangla) "${prod.localizedName(true)} কার্টে যোগ করা হয়েছে" else "Added to cart",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // 4. Sell Form Modal Bottom Sheet
        if (isSellSheetOpen && selectedProduct != null) {
            SellFormBottomSheet(
                product = selectedProduct!!,
                viewModel = viewModel,
                isBangla = isBangla,
                currentLanguage = currentLanguage,
                isSimpleView = isSimpleView,
                onDismiss = { viewModel.closeSellForm() }
            )
        }

        // 4b. Cart Checkout Bottom Sheet (Requirement 34, 40)
        if (isCartSheetOpen) {
            CartCheckoutBottomSheet(
                viewModel = viewModel,
                isBangla = isBangla,
                currentLanguage = currentLanguage,
                onDismiss = { isCartSheetOpen = false }
            )
        }

        // 5. Sale Success Confirmation Dialog
        if (completedSaleOrder != null) {
            SaleSuccessDialog(
                order = completedSaleOrder!!,
                isBangla = isBangla,
                currentLanguage = currentLanguage,
                onDismiss = { viewModel.dismissSuccessDialog() }
            )
        }

        // 6. Barcode Scanner / Lookup Dialog
        if (showBarcodeScanDialog) {
            var barcodeInput by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showBarcodeScanDialog = false },
                containerColor = DarkSlateSurface,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = VividEmeraldAccent
                        )
                        Text(
                            text = if (isBangla) "বারকোড স্ক্যান / খুঁজুন" else "Barcode Scan / Search",
                            style = AppTypographyScale.sectionHeading,
                            color = TextPrimary
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (isBangla) "পণ্যের বারকোড নম্বর লিখুন বা ক্যামেরা স্ক্যান করুন:" else "Enter or scan product barcode:",
                            style = AppTypographyScale.hintText,
                            color = TextMuted
                        )
                        OutlinedTextField(
                            value = barcodeInput,
                            onValueChange = { barcodeInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sell_barcode_input"),
                            placeholder = { Text(if (isBangla) "বারকোড লিখুন..." else "e.g. 894112345678", color = TextMuted) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSlateSurfaceVariant,
                                unfocusedContainerColor = DarkSlateSurfaceVariant,
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
                            val product = viewModel.findProductByBarcode(barcodeInput)
                            if (product != null) {
                                Toast.makeText(
                                    context,
                                    if (isBangla) "${product.localizedName(true)} নির্বাচিত হয়েছে" else "${product.name} selected",
                                    Toast.LENGTH_SHORT
                                ).show()
                                showBarcodeScanDialog = false
                            } else {
                                Toast.makeText(
                                    context,
                                    if (isBangla) "বারকোডটি পাওয়া যায়নি!" else "No item matched this barcode!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
                    ) {
                        Text(if (isBangla) "খুঁজুন" else "Search")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBarcodeScanDialog = false }) {
                        Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
                    }
                }
            )
        }
    }
}

/**
 * Top Search & Category Filter Section
 */
@Composable
private fun SellTopSearchSection(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    selectedCategory: String?,
    categories: List<Category>,
    onCategorySelected: (String?) -> Unit,
    onBarcodeScanClick: () -> Unit = {},
    isBangla: Boolean
) {
    val context = LocalContext.current
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                onSearchQueryChanged(spoken)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search Input with Voice Search & Barcode Scanner
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("sell_search_input"),
            placeholder = {
                Text(
                    text = if (isBangla) "বিক্রির জন্য পণ্য খুঁজুন..." else "Search product to sell...",
                    style = AppTypographyScale.normalText,
                    color = TextMuted
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = VividEmeraldAccent,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onBarcodeScanClick,
                        modifier = Modifier.testTag("btn_barcode_scan")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Barcode scanner",
                            tint = VividEmeraldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isBangla) "bn-BD" else "en-US")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, if (isBangla) "পণ্যের নাম বলুন..." else "Speak product name...")
                            }
                            try {
                                speechLauncher.launch(intent)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    if (isBangla) "ভয়েস সার্চ চালু করা যায়নি" else "Could not start voice search",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.testTag("btn_voice_search")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice search",
                            tint = VividEmeraldAccent,
                            modifier = Modifier.size(20.dp)
                        )
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

        // Category Filter Chips Row
        if (categories.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "All" chip
                val isAllSelected = selectedCategory == null
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isAllSelected) VividEmeraldPrimary else DarkSlateSurface,
                    border = BorderStroke(1.dp, if (isAllSelected) VividEmeraldAccent else DarkSlateCardBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onCategorySelected(null) }
                ) {
                    Text(
                        text = if (isBangla) "সব পণ্য" else "All",
                        style = AppTypographyScale.hintText.copy(
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isAllSelected) Color.White else TextPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                categories.forEach { cat ->
                    val isCatSelected = selectedCategory == cat.nameEn
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isCatSelected) VividEmeraldPrimary else DarkSlateSurface,
                        border = BorderStroke(1.dp, if (isCatSelected) VividEmeraldAccent else DarkSlateCardBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onCategorySelected(cat.nameEn) }
                    ) {
                        Text(
                            text = "${cat.emoji} ${cat.localizedName(isBangla)}",
                            style = AppTypographyScale.hintText.copy(
                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCatSelected) Color.White else TextPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Grid Card matching Stock screen styling with single-item selection highlight
 * and "Out of stock" indicator.
 */
@Composable
private fun SellProductGridCard(
    product: Product,
    isSelected: Boolean,
    isOutOfStock: Boolean,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    isSimpleView: Boolean,
    onCardClick: () -> Unit
) {
    val isLowStock = product.isLowStock && !isOutOfStock

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = when {
                    isSelected -> VividEmeraldAccent
                    isOutOfStock -> LowStockRed.copy(alpha = 0.4f)
                    isLowStock -> LowStockRed.copy(alpha = 0.6f)
                    else -> DarkSlateCardBorder
                },
                shape = RoundedCornerShape(12.dp)
            )
            .alpha(if (isOutOfStock) 0.55f else 1f)
            .clickable { onCardClick() }
            .testTag("sell_product_item_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkSlateSurfaceVariant else DarkSlateSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Photo Container with Selection checkmark or Out of stock badge (Requirement 31)
            if (isOutOfStock) {
                // Red "Out of Stock" label instead of the picture
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LowStockRed),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (isBangla) "স্টক শেষ" else "Out of Stock",
                            style = AppTypographyScale.hintText.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    HardwareThumbnailIcon(
                        photoKey = product.photoKey,
                        category = product.category,
                        modifier = Modifier.size(36.dp)
                    )

                    // Selection Checkmark Badge
                    if (isSelected) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(4.dp)
                                .size(24.dp),
                            shape = CircleShape,
                            color = VividEmeraldPrimary,
                            border = BorderStroke(1.dp, Color.White)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    if (isLowStock) {
                        // "Low" badge if low stock
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = LowStockRed
                        ) {
                            Text(
                                text = if (isBangla) "স্বল্প" else "Low",
                                style = AppTypographyScale.hintText.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            // Product Name
            Text(
                text = product.localizedName(isBangla),
                style = AppTypographyScale.normalText.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = if (isSimpleView) 14.sp else 13.sp
                ),
                color = if (isSelected) VividEmeraldAccent else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Available Quantity & Selling Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val qtyStr = if (isBangla) toBengaliNumerals(product.stockQuantity.toString()) else product.stockQuantity.toString()
                Text(
                    text = if (isOutOfStock) {
                        if (isBangla) "০ টি" else "0 pcs"
                    } else {
                        "$qtyStr ${product.localizedUnit(isBangla)}"
                    },
                    style = AppTypographyScale.hintText.copy(fontSize = 11.sp),
                    color = if (isOutOfStock || isLowStock) LowStockRed else TextMuted,
                    maxLines = 1
                )

                Text(
                    text = formatTaka(product.sellPrice, currentLanguage),
                    style = AppTypographyScale.hintText.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isSimpleView) 13.sp else 12.sp
                    ),
                    color = if (isOutOfStock) TextMuted else VividEmeraldAccent,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Big Fixed Bottom Sell Bar:
 * - Shows selected item name, available stock, and price
 * - Big "Sell" button that stays disabled until an item is selected
 */
@Composable
private fun FixedBottomSellBar(
    selectedProduct: Product?,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onSellClick: () -> Unit,
    onAddToCartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isItemSelected = selectedProduct != null

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = DarkSlateSurface,
        border = BorderStroke(1.dp, DarkSlateCardBorder),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left: Selected Item Summary
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                if (isItemSelected) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(VividEmeraldAccent)
                        )
                        Text(
                            text = selectedProduct!!.localizedName(isBangla),
                            style = AppTypographyScale.normalText.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    val qtyNum = if (isBangla) toBengaliNumerals(selectedProduct.stockQuantity.toString()) else selectedProduct.stockQuantity.toString()
                    Text(
                        text = if (isBangla) {
                            "মজুদ: $qtyNum ${selectedProduct.localizedUnit(true)} • প্রতি ${selectedProduct.localizedUnit(true)} ৳${toBengaliNumerals(selectedProduct.sellPrice.toLong().toString())}"
                        } else {
                            "Stock: $qtyNum ${selectedProduct.localizedUnit(false)} • Unit: ৳${selectedProduct.sellPrice.toLong()}"
                        },
                        style = AppTypographyScale.hintText.copy(fontSize = 12.sp),
                        color = VividEmeraldAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = if (isBangla) "পণ্য নির্বাচন করুন" else "Select an item to sell",
                        style = AppTypographyScale.normalText.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        ),
                        color = TextMuted
                    )
                    Text(
                        text = if (isBangla) "উপরে তালিকা থেকে যেকোনো একটি পণ্যে ট্যাপ করুন" else "Tap any card above to select it",
                        style = AppTypographyScale.hintText.copy(fontSize = 12.sp),
                        color = TextMuted.copy(alpha = 0.7f)
                    )
                }
            }

            // Right: Actions (Cart & Instant Sell)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isItemSelected) {
                    OutlinedButton(
                        onClick = onAddToCartClick,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("btn_add_to_cart"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, VividEmeraldAccent),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VividEmeraldAccent),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isBangla) "কার্ট" else "Cart",
                            style = AppTypographyScale.buttonText.copy(fontSize = 13.sp)
                        )
                    }
                }

                Button(
                    onClick = onSellClick,
                    enabled = isItemSelected,
                    modifier = Modifier
                        .height(48.dp)
                        .widthIn(min = 100.dp)
                        .testTag("btn_open_sell_form"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VividEmeraldPrimary,
                        disabledContainerColor = DarkSlateSurfaceVariant,
                        disabledContentColor = TextMuted
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isBangla) "বিক্রি" else "Sell",
                        style = AppTypographyScale.buttonText.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

/**
 * Sell Form Bottom Sheet:
 * - Quantity with steppers and unit
 * - Selling price (pre-filled, editable)
 * - Automatically calculated total amount
 * - Optional discount (Detailed view)
 * - Payment Type: Paid in full vs Baki
 * - If Baki: customer selector, add new customer, part payment
 * - Warning blocking sale if quantity exceeds stock
 * - Confirm Sale and Cancel buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellFormBottomSheet(
    product: Product,
    viewModel: SellViewModel,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    isSimpleView: Boolean,
    onDismiss: () -> Unit
) {
    val quantityText by viewModel.quantityText.collectAsStateWithLifecycle()
    val sellingPriceText by viewModel.sellingPriceText.collectAsStateWithLifecycle()
    val discountText by viewModel.discountText.collectAsStateWithLifecycle()
    val isCreditSale by viewModel.isCreditSale.collectAsStateWithLifecycle()
    val selectedCustomer by viewModel.selectedCustomer.collectAsStateWithLifecycle()
    val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val paidNowText by viewModel.paidNowText.collectAsStateWithLifecycle()
    val dueDateDays by viewModel.dueDateDays.collectAsStateWithLifecycle()
    val showNewCustomerDialog by viewModel.showNewCustomerDialog.collectAsStateWithLifecycle()
    val notesText by viewModel.notesText.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        if (uri != null) {
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        val pickedName = if (nameIndex >= 0) it.getString(nameIndex) else ""
                        if (pickedName.isNotBlank()) {
                            viewModel.addNewCustomer(pickedName, "", "")
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, if (isBangla) "কন্টাক্ট পড়া যায়নি" else "Failed to read contact", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val qty = quantityText.toIntOrNull() ?: 0
    val unitPrice = sellingPriceText.toDoubleOrNull() ?: product.sellPrice
    val subtotal = qty * unitPrice
    val rawDiscount = discountText.toDoubleOrNull() ?: 0.0
    // Requirement 32: discount never bigger than price, total never below zero
    val discount = rawDiscount.coerceIn(0.0, subtotal)
    val totalAmount = (subtotal - discount).coerceAtLeast(0.0)
    val isQuantityExceeded = qty > product.stockQuantity
    val isQuantityZeroOrInvalid = qty <= 0

    var isCustomerDropdownExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSlateSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Photo & Product Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        HardwareThumbnailIcon(
                            photoKey = product.photoKey,
                            category = product.category,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.localizedName(isBangla),
                        style = AppTypographyScale.screenTitle.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val qtyNum = if (isBangla) toBengaliNumerals(product.stockQuantity.toString()) else product.stockQuantity.toString()
                    Text(
                        text = if (isBangla) {
                            "বর্তমান মজুদ: $qtyNum ${product.localizedUnit(true)}"
                        } else {
                            "Available stock: $qtyNum ${product.localizedUnit(false)}"
                        },
                        style = AppTypographyScale.hintText,
                        color = VividEmeraldAccent
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextMuted
                    )
                }
            }

            // Invoice number & Date on each sale (Requirement 35)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val now = remember { System.currentTimeMillis() }
                val invoiceNum = remember { "INV-${(now % 900000) + 100000}" }
                val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
                Text(
                    text = "${if (isBangla) "চালান নং: " else "Invoice: "}$invoiceNum",
                    style = AppTypographyScale.hintText.copy(fontWeight = FontWeight.Bold),
                    color = VividEmeraldAccent
                )
                Text(
                    text = "${if (isBangla) "তারিখ: " else "Date: "}${dateFormat.format(Date(now))}",
                    style = AppTypographyScale.hintText,
                    color = TextMuted
                )
            }

            Divider(color = DarkSlateCardBorder)

            // Error warning box if quantity exceeds stock (Requirement 5)
            if (isQuantityExceeded) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = LowStockRedContainer,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, LowStockRed)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = LowStockRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (isBangla) {
                                "মজুদের চেয়ে বেশি পরিমাণ! বর্তমানে মজুদ আছে মাত্র ${product.stockQuantity} ${product.localizedUnit(true)}।"
                            } else {
                                "Quantity exceeds stock! Currently available: ${product.stockQuantity} ${product.localizedUnit(false)}."
                            },
                            style = AppTypographyScale.normalText.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = LowStockRed
                        )
                    }
                }
            }

            // General error message if any
            if (errorMessage != null && !isQuantityExceeded) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = LowStockRedContainer,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, LowStockRed)
                ) {
                    Text(
                        text = errorMessage!!,
                        style = AppTypographyScale.hintText.copy(fontWeight = FontWeight.Bold),
                        color = LowStockRed,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // 1. Quantity Field with [-] and [+] Stepper Buttons
            Text(
                text = if (isBangla) "বিক্রির পরিমাণ (${product.localizedUnit(true)})" else "Quantity (${product.localizedUnit(false)})",
                style = AppTypographyScale.hintText.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Decrement button
                Button(
                    onClick = { viewModel.decrementQuantity() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_qty_minus"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSlateSurfaceVariant),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Minus",
                        tint = TextPrimary
                    )
                }

                // Direct number input
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { viewModel.setQuantityText(it) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("sell_input_quantity"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = AppTypographyScale.sectionHeading.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = if (isQuantityExceeded) LowStockRed else TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSlateSurfaceVariant,
                        unfocusedContainerColor = DarkSlateSurfaceVariant,
                        focusedBorderColor = if (isQuantityExceeded) LowStockRed else VividEmeraldPrimary,
                        unfocusedBorderColor = if (isQuantityExceeded) LowStockRed else DarkSlateCardBorder
                    ),
                    suffix = {
                        Text(
                            text = product.localizedUnit(isBangla),
                            style = AppTypographyScale.hintText.copy(fontWeight = FontWeight.Bold),
                            color = TextMuted
                        )
                    }
                )

                // Increment button
                Button(
                    onClick = { viewModel.incrementQuantity() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_qty_plus"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Plus",
                        tint = Color.White
                    )
                }
            }

            // 2. Selling Price Field (Pre-filled, editable)
            Text(
                text = if (isBangla) "একক বিক্রয় মূল্য (৳)" else "Unit Selling Price (৳)",
                style = AppTypographyScale.hintText.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            OutlinedTextField(
                value = sellingPriceText,
                onValueChange = { viewModel.setSellingPriceText(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("sell_input_price"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                prefix = {
                    Text(
                        text = "৳ ",
                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                        color = VividEmeraldAccent
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSlateSurfaceVariant,
                    unfocusedContainerColor = DarkSlateSurfaceVariant,
                    focusedBorderColor = VividEmeraldPrimary,
                    unfocusedBorderColor = DarkSlateCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                textStyle = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold)
            )

            // 3. Optional Discount Row (Detailed View only per Requirement 8)
            if (!isSimpleView) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "ছাড় / ডিসকাউন্ট (ঐচ্ছিক)" else "Discount (Optional)",
                            style = AppTypographyScale.hintText.copy(fontWeight = FontWeight.Medium),
                            color = TextMuted
                        )
                    }

                    OutlinedTextField(
                        value = discountText,
                        onValueChange = { viewModel.setDiscountText(it) },
                        modifier = Modifier
                            .width(140.dp)
                            .heightIn(min = 44.dp)
                            .testTag("sell_input_discount"),
                        placeholder = { Text("0", color = TextMuted) },
                        prefix = { Text("৳ ", color = VividEmeraldAccent, fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSlateSurfaceVariant,
                            unfocusedContainerColor = DarkSlateSurfaceVariant,
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }

            // 4. Automatically Calculated Total Amount Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurfaceVariant),
                border = BorderStroke(1.dp, VividEmeraldAccent.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isBangla) "সর্বমোট প্রদেয়" else "Total Amount",
                            style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "$qty × ৳${unitPrice.toLong()}" + if (discount > 0) " - ৳${discount.toLong()}" else "",
                            style = AppTypographyScale.hintText,
                            color = TextMuted
                        )
                    }

                    Text(
                        text = formatTaka(totalAmount, currentLanguage),
                        style = AppTypographyScale.moneyNumber.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = VividEmeraldAccent
                        )
                    )
                }
            }

            Divider(color = DarkSlateCardBorder)

            // 5. Payment Type: Paid in full vs Baki
            Text(
                text = if (isBangla) "পরিশোধ পদ্ধতি" else "Payment Method",
                style = AppTypographyScale.hintText.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Cash / Paid in full
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { viewModel.setCreditSale(false) }
                        .testTag("sell_pay_cash_btn"),
                    shape = RoundedCornerShape(10.dp),
                    color = if (!isCreditSale) VividEmeraldPrimary else DarkSlateSurfaceVariant,
                    border = if (!isCreditSale) null else BorderStroke(1.dp, DarkSlateCardBorder)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (!isCreditSale) Color.White else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isBangla) "নগদ পরিশোধ" else "Paid in Full",
                            style = AppTypographyScale.buttonText.copy(
                                fontWeight = if (!isCreditSale) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (!isCreditSale) Color.White else TextMuted
                        )
                    }
                }

                // Baki / Credit
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { viewModel.setCreditSale(true) }
                        .testTag("sell_pay_baki_btn"),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isCreditSale) VividEmeraldPrimary else DarkSlateSurfaceVariant,
                    border = if (isCreditSale) null else BorderStroke(1.dp, DarkSlateCardBorder)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = if (isCreditSale) Color.White else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isBangla) "বাকি (ক্রেডিট)" else "Baki (Credit)",
                            style = AppTypographyScale.buttonText.copy(
                                fontWeight = if (isCreditSale) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isCreditSale) Color.White else TextMuted
                        )
                    }
                }
            }

            // Baki Details: Customer Selection & Part Payment
            AnimatedVisibility(visible = isCreditSale) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Customer Dropdown Box
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, DarkSlateCardBorder, RoundedCornerShape(10.dp))
                                .clickable { isCustomerDropdownExpanded = true }
                                .testTag("sell_customer_selector"),
                            color = DarkSlateSurfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (selectedCustomer != null) VividEmeraldAccent else TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = selectedCustomer?.let { "${it.name} (${it.phone.ifBlank { "No phone" }})" }
                                            ?: if (isBangla) "গ্রাহক নির্বাচন করুন *" else "Select Customer *",
                                        style = AppTypographyScale.normalText.copy(
                                            fontWeight = if (selectedCustomer != null) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (selectedCustomer != null) TextPrimary else TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = "▼",
                                    style = AppTypographyScale.hintText,
                                    color = TextMuted
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isCustomerDropdownExpanded,
                            onDismissRequest = { isCustomerDropdownExpanded = false },
                            modifier = Modifier.background(DarkSlateSurface)
                        ) {
                            allCustomers.forEach { cust ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = cust.name,
                                                style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Medium),
                                                color = TextPrimary
                                            )
                                            if (cust.phone.isNotBlank()) {
                                                Text(
                                                    text = cust.phone,
                                                    style = AppTypographyScale.hintText,
                                                    color = TextMuted
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectCustomer(cust)
                                        isCustomerDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Buttons: Contacts Picker & Add New Customer (Requirement 33)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { contactPickerLauncher.launch(null) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_pick_contact"),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, VividEmeraldAccent),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VividEmeraldAccent)
                        ) {
                            Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isBangla) "কন্টাক্টস থেকে" else "Contacts",
                                style = AppTypographyScale.buttonText.copy(fontSize = 12.sp)
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.openNewCustomerDialog() },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_add_new_customer"),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, VividEmeraldAccent),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VividEmeraldAccent)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isBangla) "+ নতুন গ্রাহক" else "+ New Customer",
                                style = AppTypographyScale.buttonText.copy(fontSize = 12.sp)
                            )
                        }
                    }

                    // Part Payment Field
                    OutlinedTextField(
                        value = paidNowText,
                        onValueChange = { viewModel.setPaidNowText(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("sell_input_paid_now"),
                        label = {
                            Text(
                                text = if (isBangla) "নগদ পরিশোধ / অগ্রিম জমা (৳)" else "Paid Now / Down Payment (৳)",
                                style = AppTypographyScale.hintText
                            )
                        },
                        placeholder = { Text("0", color = TextMuted) },
                        prefix = { Text("৳ ", color = VividEmeraldAccent, fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSlateSurfaceVariant,
                            unfocusedContainerColor = DarkSlateSurfaceVariant,
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Calculated Due Amount
                    val enteredPaid = paidNowText.toDoubleOrNull() ?: 0.0
                    val remainingDue = (totalAmount - enteredPaid).coerceAtLeast(0.0)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBangla) "বাকি থাকবে:" else "Remaining Due:",
                            style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                            color = LowStockRed
                        )
                        Text(
                            text = formatTaka(remainingDue, currentLanguage),
                            style = AppTypographyScale.moneyNumber.copy(
                                fontWeight = FontWeight.Bold,
                                color = LowStockRed
                            )
                        )
                    }
                }
            }

            // Notes / Remarks field on sale (Requirement 36)
            OutlinedTextField(
                value = notesText,
                onValueChange = { viewModel.setNotesText(it) },
                label = { Text(if (isBangla) "মন্তব্য বা বিবরণ (ঐচ্ছিক)" else "Notes / Remarks (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sell_input_notes"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSlateSurfaceVariant,
                    unfocusedContainerColor = DarkSlateSurfaceVariant,
                    focusedBorderColor = VividEmeraldPrimary,
                    unfocusedBorderColor = DarkSlateCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(Modifier.height(8.dp))

            // 6. Action Buttons: Confirm Sale and Cancel
            val canConfirmSale = !isQuantityExceeded && !isQuantityZeroOrInvalid && (!isCreditSale || selectedCustomer != null)

            Button(
                onClick = {
                    viewModel.confirmSale(isBangla)
                },
                enabled = canConfirmSale,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_confirm_sale"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VividEmeraldPrimary,
                    disabledContainerColor = DarkSlateSurfaceVariant,
                    disabledContentColor = TextMuted
                )
            ) {
                Text(
                    text = if (isBangla) "বিক্রি সম্পন্ন করুন (Confirm Sale)" else "Confirm Sale",
                    style = AppTypographyScale.buttonText.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (canConfirmSale) Color.White else TextMuted
                )
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_cancel_sale")
            ) {
                Text(
                    text = if (isBangla) "বাতিল" else "Cancel",
                    style = AppTypographyScale.buttonText,
                    color = TextMuted
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // New Customer Dialog
    if (showNewCustomerDialog) {
        AddNewCustomerDialog(
            isBangla = isBangla,
            onDismiss = { viewModel.closeNewCustomerDialog() },
            onConfirm = { name, phone, address ->
                viewModel.addNewCustomer(name, phone, address)
            }
        )
    }
}

/**
 * Add New Customer Dialog
 */
@Composable
private fun AddNewCustomerDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "নতুন বাকির গ্রাহক যোগ করুন" else "Add New Customer",
                style = AppTypographyScale.screenTitle,
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
                    modifier = Modifier.fillMaxWidth().testTag("input_new_customer_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSlateSurfaceVariant,
                        unfocusedContainerColor = DarkSlateSurfaceVariant,
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
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("input_new_customer_phone"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSlateSurfaceVariant,
                        unfocusedContainerColor = DarkSlateSurfaceVariant,
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(if (isBangla) "ঠিকানা" else "Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSlateSurfaceVariant,
                        unfocusedContainerColor = DarkSlateSurfaceVariant,
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
                onClick = { onConfirm(name, phone, address) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
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

/**
 * Sale Success Confirmation Dialog (Requirement 6)
 */
@Composable
private fun SaleSuccessDialog(
    order: SaleOrder,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        icon = {
            Surface(
                shape = CircleShape,
                color = VividEmeraldPrimary.copy(alpha = 0.2f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = VividEmeraldAccent,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = if (isBangla) "বিক্রি সফল হয়েছে!" else "Sale Confirmed!",
                style = AppTypographyScale.screenTitle.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${if (isBangla) "চালান নম্বর: " else "Invoice: "}${order.invoiceNumber}",
                    style = AppTypographyScale.hintText,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = if (isBangla) "পণ্য:" else "Item:", style = AppTypographyScale.hintText, color = TextMuted)
                            Text(text = order.summaryItems, style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = if (isBangla) "মোট মূল্য:" else "Total Amount:", style = AppTypographyScale.hintText, color = TextMuted)
                            Text(text = formatTaka(order.totalAmount, currentLanguage), style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold), color = VividEmeraldAccent)
                        }

                        if (order.dueAmount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = if (isBangla) "বাকি পাওনা:" else "Baki Due:", style = AppTypographyScale.hintText, color = LowStockRed)
                                Text(text = formatTaka(order.dueAmount, currentLanguage), style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold), color = LowStockRed)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = if (isBangla) "গ্রাহক:" else "Customer:", style = AppTypographyScale.hintText, color = TextMuted)
                                Text(text = order.customerName, style = AppTypographyScale.hintText, color = TextPrimary)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = if (isBangla) "পরিশোধ:" else "Payment:", style = AppTypographyScale.hintText, color = TextMuted)
                                Text(text = if (isBangla) "নগদ পরিশোধিত" else "Paid in full", style = AppTypographyScale.hintText.copy(fontWeight = FontWeight.Bold), color = VividEmeraldAccent)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Shareable receipt button (Requirement 40)
                OutlinedButton(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            val receiptText = buildString {
                                appendLine(if (isBangla) "=== বিক্রয় রসিদ ===" else "=== SALES RECEIPT ===")
                                appendLine("${if (isBangla) "চালান নম্বর: " else "Invoice No: "}${order.invoiceNumber}")
                                appendLine("${if (isBangla) "তারিখ: " else "Date: "}${SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date(order.timestamp))}")
                                appendLine("${if (isBangla) "গ্রাহক: " else "Customer: "}${order.customerName}")
                                appendLine("${if (isBangla) "পণ্য: " else "Items: "}${order.summaryItems}")
                                appendLine("${if (isBangla) "মোট মূল্য: " else "Total: "}৳${order.totalAmount.toLong()}")
                                appendLine("${if (isBangla) "পরিশোধ: " else "Paid: "}৳${order.paidAmount.toLong()}")
                                if (order.dueAmount > 0) {
                                    appendLine("${if (isBangla) "বাকি পাওনা: " else "Due: "}৳${order.dueAmount.toLong()}")
                                }
                                appendLine(if (isBangla) "ধন্যবাদ! আবার আসবেন।" else "Thank you! Please visit again.")
                            }
                            putExtra(Intent.EXTRA_TEXT, receiptText)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, if (isBangla) "রসিদ শেয়ার করুন" else "Share Receipt"))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_share_receipt"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, VividEmeraldAccent),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VividEmeraldAccent)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isBangla) "রসিদ শেয়ার করুন" else "Share Receipt",
                        style = AppTypographyScale.buttonText
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_success_dismiss"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
                ) {
                    Text(
                        text = if (isBangla) "ঠিক আছে (নতুন বিক্রি)" else "OK (New Sale)",
                        style = AppTypographyScale.buttonText
                    )
                }
            }
        }
    )
}
