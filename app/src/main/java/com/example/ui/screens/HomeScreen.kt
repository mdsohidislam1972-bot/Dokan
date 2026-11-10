package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import com.example.data.local.AppSettings
import com.example.data.local.PinLockManager
import com.example.data.local.ProfileManager
import com.example.data.local.ViewMode
import com.example.ui.components.PinVerifyDialog
import com.example.ui.components.ProfileSwitcherBottomSheet
import com.example.ui.components.isCustomProductPhoto
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.Product
import com.example.data.local.entity.SaleOrder
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.HardwareCard
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.formatTaka
import com.example.ui.localization.toBengaliNumerals
import com.example.ui.screens.home.CashflowChartCard
import com.example.ui.screens.home.ExploreAppRow
import com.example.ui.screens.home.HomeSummaryMetrics
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.home.PeriodFiguresSection
import com.example.ui.screens.home.ProfileCompletionCard
import com.example.ui.screens.home.PromotionalBannerCard
import com.example.ui.screens.home.QuickAddPartyDialog
import com.example.ui.screens.home.QuickPaymentInDialog
import com.example.ui.screens.home.QuickQuotationDialog
import com.example.ui.screens.home.QuickSalesReturnDialog
import com.example.ui.screens.home.ShortcutsGridSection
import com.example.ui.screens.home.TotalBalanceCard
import com.example.ui.theme.AppTypographyScale
import com.example.ui.theme.DarkSlateBackground
import com.example.ui.theme.DarkSlateCardBorder
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.DarkSlateSurfaceVariant
import com.example.ui.theme.LowStockRed
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VividEmeraldAccent
import com.example.ui.theme.VividEmeraldPrimary
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    currentLanguage: AppLanguage,
    onNavigateToSell: () -> Unit = {},
    onNavigateToStock: () -> Unit = {},
    onNavigateToBaki: () -> Unit = {},
    onNavigateToSuppliers: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToBusiness: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToMarketplace: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val todayMetrics by viewModel.todayMetrics.collectAsStateWithLifecycle()
    val customerBaki by viewModel.customerBaki.collectAsStateWithLifecycle()
    val supplierDue by viewModel.supplierDue.collectAsStateWithLifecycle()
    val totalBalance by viewModel.totalBalance.collectAsStateWithLifecycle()
    val periodFigures by viewModel.periodFigures.collectAsStateWithLifecycle()
    val cashflowState by viewModel.cashflow7Days.collectAsStateWithLifecycle()
    val shortcuts by viewModel.shortcuts.collectAsStateWithLifecycle()
    val lowStockList by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val recentSalesList by viewModel.recentSales.collectAsStateWithLifecycle()
    val allCustomers by viewModel.repository.allCustomers.collectAsStateWithLifecycle(emptyList())
    val allProducts by viewModel.repository.allProducts.collectAsStateWithLifecycle(emptyList())
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val todayDateFormatted = viewModel.getFormattedDate(currentLanguage)

    val activeProfile = ProfileManager.activeProfile

    var showProfileSwitcher by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showPinDialogForReveal by remember { mutableStateOf(false) }

    // Quick Action Dialogs for Shortcuts
    var showQuickAddParty by remember { mutableStateOf(false) }
    var showQuickPaymentIn by remember { mutableStateOf(false) }
    var showQuickSalesReturn by remember { mutableStateOf(false) }
    var showQuickQuotation by remember { mutableStateOf(false) }

    if (showProfileSwitcher) {
        ProfileSwitcherBottomSheet(
            isBangla = isBangla,
            onDismiss = { showProfileSwitcher = false }
        )
    }

    if (showEditProfileDialog && activeProfile != null) {
        EditProfileDialog(
            profile = activeProfile,
            isBangla = isBangla,
            onDismiss = { showEditProfileDialog = false },
            onSave = { updated ->
                ProfileManager.updateProfile(context, updated)
                showEditProfileDialog = false
                android.widget.Toast.makeText(
                    context,
                    if (isBangla) "প্রোফাইল তথ্য আপডেট হয়েছে!" else "Profile updated successfully!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    if (showPinDialogForReveal) {
        PinVerifyDialog(
            title = if (isBangla) "গোপন সংখ্যা দেখার জন্য পিন দিন" else "Enter PIN to Reveal Numbers",
            isBangla = isBangla,
            onDismiss = { showPinDialogForReveal = false },
            onPinSuccess = {
                showPinDialogForReveal = false
                AppSettings.togglePrivacyMode(context)
            }
        )
    }

    val onRevealRequest = {
        if (AppSettings.isPrivacyModeEnabled) {
            if (PinLockManager.isPinSet) {
                showPinDialogForReveal = true
            } else {
                AppSettings.togglePrivacyMode(context)
            }
        }
    }

    val staffSession = com.example.data.local.StaffSessionManager.currentSession

    if (staffSession != null) {
        // Staff see a short, simple home screen tailored strictly for their role
        // For example, a Sales person sees Sell, Stock and today's sales, but not profit, supplier due, or settings
        StaffRoleHomeScreenContent(
            session = staffSession,
            todaySales = todayMetrics.totalSales,
            customerBaki = customerBaki,
            todayDateFormatted = todayDateFormatted,
            isBangla = isBangla,
            currentLanguage = currentLanguage,
            onNavigateToSell = onNavigateToSell,
            onNavigateToStock = onNavigateToStock,
            onNavigateToBaki = onNavigateToBaki,
            modifier = modifier
        )
    } else if (AppSettings.viewMode == ViewMode.SIMPLE) {
        SimpleHomeScreenContent(
            todaySales = todayMetrics.totalSales,
            customerBaki = customerBaki,
            todayDateFormatted = todayDateFormatted,
            isBangla = isBangla,
            currentLanguage = currentLanguage,
            onNavigateToSell = onNavigateToSell,
            onNavigateToStock = onNavigateToStock,
            onNavigateToBaki = onNavigateToBaki,
            onSettingsClick = onNavigateToSettings,
            onProfileClick = { showProfileSwitcher = true },
            onRevealRequest = onRevealRequest,
            modifier = modifier
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(DarkSlateBackground)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("screen_home"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 14. Store Name & Profile Switcher Bar with Settings Icon at top
            SearchBarStatusPill(
                dateText = todayDateFormatted,
                isBangla = isBangla,
                onSettingsClick = onNavigateToSettings,
                onProfileClick = { showProfileSwitcher = true }
            )

            // Dokan Multi-Vendor Marketplace Banner Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onNavigateToMarketplace() }
                    .testTag("card_dokan_marketplace"),
                color = DarkSlateSurface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, VividEmeraldPrimary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(VividEmeraldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = if (isBangla) "দোকান মাল্টি-ভেন্ডর মার্কেটপ্লেস" else "Dokan Multi-Vendor Hub",
                                    style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Surface(
                                    color = VividEmeraldPrimary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        color = VividEmeraldAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isBangla) "ভেন্ডর স্টোর ব্রাউজ, কমিশন ও সেটেলমেন্ট" else "Explore 5+ vendor storefronts & payouts",
                                style = AppTypographyScale.hintText,
                                color = TextMuted
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Marketplace",
                        tint = VividEmeraldAccent
                    )
                }
            }

            // 3. "Complete Your Profile" progress card showing completion percentage
            ProfileCompletionCard(
                activeProfile = activeProfile,
                isBangla = isBangla,
                onCompleteClick = { showEditProfileDialog = true }
            )

            // 20. Large Action Buttons: "New sale" & "Add product"
            LargeActionButtons(
                onNavigateToSell = onNavigateToSell,
                onNavigateToStock = onNavigateToStock,
                isBangla = isBangla
            )

            // 15. "Today's Summary" Card with sales, profit, orders, items sold
            TodaysSummaryCard(
                metrics = todayMetrics,
                isBangla = isBangla,
                currentLanguage = currentLanguage,
                onSalesClick = {
                    if (AppSettings.isPrivacyModeEnabled) onRevealRequest() else onNavigateToHistory()
                },
                onProfitClick = {
                    if (AppSettings.isPrivacyModeEnabled) onRevealRequest() else onNavigateToBusiness()
                },
                onOrdersClick = onNavigateToHistory,
                onItemsSoldClick = onNavigateToStock,
                onCardClick = onNavigateToBusiness
            )

            // 19. Total Balance card for cash and bank
            TotalBalanceCard(
                balance = totalBalance,
                currentLanguage = currentLanguage,
                isBangla = isBangla,
                onClick = onNavigateToBusiness
            )

            // 16 & 17. To Receive (Baki) & To Give (Suppliers) Row - both clickable
            BakiAndSuppliersRow(
                customerBaki = customerBaki,
                supplierDue = supplierDue,
                currentLanguage = currentLanguage,
                isBangla = isBangla,
                onBakiClick = onNavigateToBaki,
                onSuppliersClick = onNavigateToSuppliers
            )

            // 18. Sales, Purchase and Expense cards for the period
            PeriodFiguresSection(
                figures = periodFigures,
                currentLanguage = currentLanguage,
                isBangla = isBangla,
                onSalesClick = onNavigateToHistory,
                onPurchasesClick = onNavigateToStock,
                onExpensesClick = onNavigateToBusiness
            )

            // 23. Cashflow chart for the last 7 days with daily switch, money in/out
            CashflowChartCard(
                chartState = cashflowState,
                currentLanguage = currentLanguage,
                isBangla = isBangla,
                onChartClick = onNavigateToBusiness
            )

            // 24. Explore App row: Quick Entry, Quick POS, View Reports
            ExploreAppRow(
                isBangla = isBangla,
                onQuickEntryClick = { showQuickAddParty = true },
                onQuickPosClick = onNavigateToSell,
                onViewReportsClick = onNavigateToBusiness
            )

            // 25. Customizable shortcuts grid with an Edit Menu
            ShortcutsGridSection(
                shortcuts = shortcuts,
                isBangla = isBangla,
                onShortcutClick = { shortcut ->
                    when (shortcut.id) {
                        "add_party" -> showQuickAddParty = true
                        "sales_invoice" -> onNavigateToSell()
                        "payment_in" -> showQuickPaymentIn = true
                        "payment_out" -> onNavigateToSuppliers()
                        "purchase" -> onNavigateToStock()
                        "add_item" -> onNavigateToStock()
                        "expense" -> onNavigateToBusiness()
                        "add_note" -> onNavigateToSettings()
                        "other_income" -> onNavigateToBusiness()
                        "sales_return" -> showQuickSalesReturn = true
                        "purchase_return" -> onNavigateToSuppliers()
                        "quotation" -> showQuickQuotation = true
                        else -> onNavigateToSell()
                    }
                },
                onToggleShortcut = { viewModel.toggleShortcut(it) }
            )

            // 21. "Low Stock" Card - header & items clickable
            LowStockSection(
                lowStockList = lowStockList,
                isBangla = isBangla,
                onStockClick = onNavigateToStock
            )

            // 22. "Recent Sales" Card - header & items clickable
            RecentSalesSection(
                salesList = recentSalesList,
                currentLanguage = currentLanguage,
                isBangla = isBangla,
                onSalesHistoryClick = onNavigateToHistory
            )

            // 26. Promotional banner area for tips or videos that I can edit
            PromotionalBannerCard(
                title = viewModel.bannerTitle,
                subtitle = viewModel.bannerSubtitle,
                isBangla = isBangla,
                onSaveBanner = { title, subtitle ->
                    viewModel.updateBanner(title, subtitle)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Quick Action Dialog Implementations
    if (showQuickAddParty) {
        QuickAddPartyDialog(
            isBangla = isBangla,
            onDismiss = { showQuickAddParty = false },
            onSaveCustomer = { name, phone, address, initialBaki ->
                scope.launch {
                    viewModel.repository.insertCustomer(
                        com.example.data.local.entity.Customer(
                            name = name,
                            phone = phone,
                            address = address,
                            totalBaki = initialBaki,
                            lastTransactionTime = System.currentTimeMillis()
                        )
                    )
                    showQuickAddParty = false
                    android.widget.Toast.makeText(
                        context,
                        if (isBangla) "গ্রাহক সফলভাবে যোগ হয়েছে!" else "Customer added successfully!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onSaveSupplier = { name, company, phone, amountOwed ->
                scope.launch {
                    viewModel.repository.insertSupplier(
                        com.example.data.local.entity.Supplier(
                            name = name,
                            company = company,
                            phone = phone,
                            amountOwed = amountOwed
                        )
                    )
                    showQuickAddParty = false
                    android.widget.Toast.makeText(
                        context,
                        if (isBangla) "মহাজন সফলভাবে যোগ হয়েছে!" else "Supplier added successfully!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    if (showQuickPaymentIn) {
        QuickPaymentInDialog(
            customers = allCustomers,
            isBangla = isBangla,
            currentLanguage = currentLanguage,
            onDismiss = { showQuickPaymentIn = false },
            onConfirm = { custId, amt, note ->
                scope.launch {
                    viewModel.repository.recordCustomerPayment(custId, amt, note)
                    showQuickPaymentIn = false
                    android.widget.Toast.makeText(
                        context,
                        if (isBangla) "টাকা জমা রেকর্ড করা হয়েছে!" else "Payment recorded successfully!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    if (showQuickSalesReturn) {
        QuickSalesReturnDialog(
            products = allProducts,
            customers = allCustomers,
            isBangla = isBangla,
            onDismiss = { showQuickSalesReturn = false },
            onConfirm = { prodId, qty, refundAmt, custId, note ->
                scope.launch {
                    val prod = allProducts.find { it.id == prodId }
                    if (prod != null) {
                        viewModel.repository.updateProductStockQuantity(prodId, prod.stockQuantity + qty)
                    }
                    if (custId != null) {
                        val cust = allCustomers.find { it.id == custId }
                        if (cust != null) {
                            val newBaki = (cust.totalBaki - refundAmt).coerceAtLeast(0.0)
                            viewModel.repository.updateCustomerBalance(custId, newBaki)
                        }
                    }
                    showQuickSalesReturn = false
                    android.widget.Toast.makeText(
                        context,
                        if (isBangla) "মাল ফেরত সম্পন্ন হয়েছে এবং স্টক যোগ হয়েছে!" else "Return processed and stock restored!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    if (showQuickQuotation) {
        QuickQuotationDialog(
            products = allProducts,
            isBangla = isBangla,
            onDismiss = { showQuickQuotation = false },
            onConvertToSale = { prod, qty, price, buyerName ->
                scope.launch {
                    val total = qty * price
                    val now = System.currentTimeMillis()
                    val order = com.example.data.local.entity.SaleOrder(
                        invoiceNumber = "QUO-INV-${(now % 90000) + 10000}",
                        timestamp = now,
                        customerName = buyerName,
                        totalAmount = total,
                        paidAmount = total,
                        dueAmount = 0.0,
                        totalCost = prod.buyPrice * qty,
                        totalItemsCount = qty,
                        summaryItems = "${prod.localizedName(isBangla)} x$qty"
                    )
                    viewModel.repository.completeSale(order, listOf(Pair(prod.id, qty)))
                    showQuickQuotation = false
                    android.widget.Toast.makeText(
                        context,
                        if (isBangla) "কোটেশন বিক্রিতে রূপান্তর সফল হয়েছে!" else "Quotation converted to sale successfully!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}

/**
 * Top pill showing Search icon with Today's Date and backup status
 */
@Composable
private fun SearchBarStatusPill(
    dateText: String,
    isBangla: Boolean,
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Business Profile Switcher Pill
        val activeProfile = ProfileManager.activeProfile
        val storeName = activeProfile?.name ?: if (isBangla) "আমার দোকান" else "My Store"

        Surface(
            modifier = Modifier
                .weight(1.3f)
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, VividEmeraldAccent.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable { onProfileClick() }
                .testTag("home_profile_switcher_pill"),
            color = DarkSlateSurface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = VividEmeraldPrimary.copy(alpha = 0.2f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = "Store",
                            tint = VividEmeraldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = storeName,
                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isBangla) "প্রোফাইল পরিবর্তন ▾" else "Switch Profile ▾",
                        style = AppTypographyScale.hintText.copy(fontSize = 11.sp),
                        color = VividEmeraldAccent,
                        maxLines = 1
                    )
                }
            }
        }

        // Date & Settings Pill
        Surface(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, DarkSlateCardBorder, RoundedCornerShape(24.dp))
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable { onSettingsClick() }
                .testTag("search_status_pill"),
            color = DarkSlateSurface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBangla) "আজকের তারিখ" else "Today",
                        style = AppTypographyScale.hintText.copy(fontSize = 10.sp),
                        color = TextMuted,
                        maxLines = 1
                    )
                    Text(
                        text = dateText,
                        style = AppTypographyScale.hintText.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Large, easy buttons near the top:
 * "New sale" (Sell Product - large vivid emerald card)
 * and "Add product" (dark slate card with emerald accent)
 */
@Composable
private fun LargeActionButtons(
    onNavigateToSell: () -> Unit,
    onNavigateToStock: () -> Unit,
    isBangla: Boolean
) {
    val isSimple = AppSettings.viewMode == ViewMode.SIMPLE
    val minHeight = if (isSimple) 84.dp else 76.dp

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // "New sale" (Sell Product) button in vibrant emerald green
        Card(
            modifier = Modifier
                .weight(1.3f)
                .heightIn(min = minHeight)
                .clip(RoundedCornerShape(16.dp))
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable { onNavigateToSell() }
                .testTag("btn_new_sale"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = VividEmeraldPrimary
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = "Sell Product",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isBangla) "পণ্য বিক্রি" else "Sell Product",
                    style = AppTypographyScale.buttonText.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // "Add product" button
        Card(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = minHeight)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, DarkSlateCardBorder, RoundedCornerShape(16.dp))
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable { onNavigateToStock() }
                .testTag("btn_add_product"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkSlateSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Product",
                    tint = VividEmeraldAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isBangla) "পণ্য যোগ" else "Add Product",
                    style = AppTypographyScale.buttonText.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * "Today's Summary" Card matching reference image:
 * Section heading "Today's Summary" with 4 columns divided by vertical dividers:
 * Total Sales | Profit | Orders | Items Sold
 * Each tile is individually clickable and opens its breakdown, and the card header opens overview.
 */
@Composable
private fun TodaysSummaryCard(
    metrics: HomeSummaryMetrics,
    isBangla: Boolean,
    currentLanguage: AppLanguage = if (isBangla) AppLanguage.BANGLA else AppLanguage.ENGLISH,
    onSalesClick: () -> Unit = {},
    onProfitClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onItemsSoldClick: () -> Unit = {},
    onCardClick: () -> Unit = {}
) {
    HardwareCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today_summary_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable { onCardClick() }
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isBangla) "আজকের সারসংক্ষেপ" else "Today's Summary",
                style = AppTypographyScale.sectionHeading.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isBangla) "বিস্তারিত দেখুন" else "Breakdown",
                    style = AppTypographyScale.hintText,
                    color = VividEmeraldAccent
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = VividEmeraldAccent,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Total Sales -> opens History or reveals
            val salesText = formatTaka(metrics.totalSales, currentLanguage, isMaskable = true)
            SummaryTileColumn(
                label = if (isBangla) "মোট বিক্রি" else "Total Sales",
                value = salesText,
                onClick = onSalesClick,
                modifier = Modifier.weight(1f)
            )

            VerticalLineDivider()

            // 2. Profit -> opens Business Profit Breakdown or reveals
            val profitText = formatTaka(metrics.totalProfit, currentLanguage, isMaskable = true)
            SummaryTileColumn(
                label = if (isBangla) "লাভ" else "Profit",
                value = profitText,
                onClick = onProfitClick,
                modifier = Modifier.weight(1f)
            )

            VerticalLineDivider()

            // 3. Orders -> opens History
            val ordersText = if (isBangla) {
                toBengaliNumerals(metrics.ordersCount.toString())
            } else {
                metrics.ordersCount.toString()
            }
            SummaryTileColumn(
                label = if (isBangla) "অর্ডার" else "Orders",
                value = ordersText,
                onClick = onOrdersClick,
                modifier = Modifier.weight(0.9f)
            )

            VerticalLineDivider()

            // 4. Items Sold -> opens Stock list
            val itemsText = if (isBangla) {
                toBengaliNumerals(metrics.itemsSoldCount.toString())
            } else {
                metrics.itemsSoldCount.toString()
            }
            SummaryTileColumn(
                label = if (isBangla) "বিক্রি সংখ্যা" else "Items Sold",
                value = itemsText,
                onClick = onItemsSoldClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun VerticalLineDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(DarkSlateCardBorder)
    )
}

@Composable
private fun SummaryTileColumn(
    label: String,
    value: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxHeight()
            .pointerHoverIcon(PointerIcon.Hand)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 2.dp)
                .heightIn(min = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = AppTypographyScale.hintText,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = AppTypographyScale.moneyNumber.copy(
                    fontSize = if (value.length > 6) 16.sp else 20.sp,
                    lineHeight = if (value.length > 6) 22.sp else 28.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = VividEmeraldAccent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Customer Baki & Suppliers Row (Side-by-side matching the reference image)
 * Both cards are clickable and open customer baki & suppliers ledger respectively.
 */
@Composable
private fun BakiAndSuppliersRow(
    customerBaki: Double,
    supplierDue: Double,
    currentLanguage: AppLanguage,
    isBangla: Boolean,
    onBakiClick: () -> Unit = {},
    onSuppliersClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left: "Baki" (owed to me) -> Opens Baki Screen
        Card(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 72.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, DarkSlateCardBorder, RoundedCornerShape(16.dp))
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable { onBakiClick() }
                .testTag("customer_baki_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkSlateSurface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "Baki Icon",
                    tint = TextMuted,
                    modifier = Modifier.size(28.dp)
                )

                Column {
                    Text(
                        text = if (isBangla) "বাকি" else "Baki",
                        style = AppTypographyScale.hintText,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val bakiFormatted = formatTaka(customerBaki, currentLanguage, isMaskable = true)
                    Text(
                        text = bakiFormatted,
                        style = AppTypographyScale.moneyNumber.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = VividEmeraldAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isBangla) "পাওনা টাকা" else "owed to me",
                        style = AppTypographyScale.hintText,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Right: "Suppliers" (I owe) -> Opens Suppliers Ledger Screen
        Card(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 72.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, DarkSlateCardBorder, RoundedCornerShape(16.dp))
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable { onSuppliersClick() }
                .testTag("suppliers_due_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkSlateSurface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = "Suppliers Icon",
                    tint = TextMuted,
                    modifier = Modifier.size(28.dp)
                )

                Column {
                    Text(
                        text = if (isBangla) "মহাজন/সাপ্লায়ার" else "Suppliers",
                        style = AppTypographyScale.hintText,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val supplierFormatted = formatTaka(supplierDue, currentLanguage, isMaskable = true)
                    Text(
                        text = supplierFormatted,
                        style = AppTypographyScale.moneyNumber.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = VividEmeraldAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isBangla) "দেনা বাকি" else "I owe",
                        style = AppTypographyScale.hintText,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * "Low Stock" Section matching reference image:
 * Thumbnail on white rounded square, Product Name, Qty left, and Red "Low" pill badge.
 * All rows and headers are clickable to open stock list.
 */
@Composable
private fun LowStockSection(
    lowStockList: List<Product>,
    isBangla: Boolean,
    onStockClick: () -> Unit = {}
) {
    HardwareCard(modifier = Modifier.testTag("low_stock_card")) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable { onStockClick() }
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isBangla) "কম স্টক সতর্কতা" else "Low Stock",
                style = AppTypographyScale.sectionHeading.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isBangla) "স্টক দেখুন" else "View Stock",
                    style = AppTypographyScale.hintText,
                    color = VividEmeraldAccent
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = VividEmeraldAccent,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        if (lowStockList.isEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable { onStockClick() }
                    .padding(vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = VividEmeraldAccent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (isBangla) "সব পণ্যের পর্যাপ্ত স্টক রয়েছে।" else "All products are well stocked.",
                    style = AppTypographyScale.normalText,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                lowStockList.take(5).forEach { product ->
                    Surface(
                        onClick = onStockClick,
                        color = Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: White rounded square with hardware tool/material icon
                            Surface(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCustomProductPhoto(product.photoKey)) {
                                        AsyncImage(
                                            model = product.photoKey,
                                            contentDescription = product.nameEn,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
                                        )
                                    } else {
                                        Icon(
                                            imageVector = getCategoryIcon(product.category),
                                            contentDescription = product.nameEn,
                                            tint = TextDark,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Middle: Product Name & Quantity
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.localizedName(isBangla),
                                    style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val qtyText = if (isBangla) {
                                    "পরিমাণ: ${toBengaliNumerals(product.stockQuantity.toString())} ${product.unitBn}"
                                } else {
                                    "Qty: ${product.stockQuantity} ${product.unitEn}"
                                }
                                Text(
                                    text = qtyText,
                                    style = AppTypographyScale.hintText,
                                    color = TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Right: Red "Low" pill badge matching reference image
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = LowStockRed
                            ) {
                                Text(
                                    text = if (isBangla) "কম" else "Low",
                                    style = AppTypographyScale.hintText.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * "Recent Sales" Section: Last 5 sales on one line each.
 * All rows and headers are clickable to open sales history records.
 */
@Composable
private fun RecentSalesSection(
    salesList: List<SaleOrder>,
    currentLanguage: AppLanguage,
    isBangla: Boolean,
    onSalesHistoryClick: () -> Unit = {}
) {
    HardwareCard(modifier = Modifier.testTag("recent_sales_card")) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable { onSalesHistoryClick() }
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isBangla) "সর্বশেষ বিক্রি" else "Recent Sales",
                style = AppTypographyScale.sectionHeading.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isBangla) "সব রেকর্ড" else "All Records",
                    style = AppTypographyScale.hintText,
                    color = VividEmeraldAccent
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = VividEmeraldAccent,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        if (salesList.isEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable { onSalesHistoryClick() }
                    .padding(vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = VividEmeraldAccent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (isBangla) "আজকে এখনও কোনো বিক্রি রেকর্ড করা হয়নি।" else "No sales recorded yet today.",
                    style = AppTypographyScale.normalText,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                salesList.take(5).forEach { sale ->
                    val timeStr = SimpleDateFormat("hh:mm a", Locale.US).format(Date(sale.timestamp))
                    val timeFormatted = if (isBangla) toBengaliNumerals(timeStr) else timeStr

                    Surface(
                        onClick = onSalesHistoryClick,
                        color = Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: White rounded square icon
                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = "Sale Receipt",
                                        tint = TextDark,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Middle: Customer name & items
                            Column(modifier = Modifier.weight(1f)) {
                                val itemsWord = if (isBangla) "টি পণ্য" else "items"
                                val itemsCountStr = if (isBangla) toBengaliNumerals(sale.totalItemsCount.toString()) else sale.totalItemsCount.toString()
                                Text(
                                    text = "${sale.customerName} ($itemsCountStr $itemsWord)",
                                    style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Medium),
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = timeFormatted,
                                    style = AppTypographyScale.hintText,
                                    color = TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Right: Amount in ৳ & Baki badge if any
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatTaka(sale.totalAmount, currentLanguage),
                                    style = AppTypographyScale.sectionHeading.copy(fontWeight = FontWeight.Bold),
                                    color = VividEmeraldAccent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (sale.dueAmount > 0) {
                                    val bakiStr = if (isBangla) {
                                        "বাকি: " + formatTaka(sale.dueAmount, currentLanguage)
                                    } else {
                                        "Due: " + formatTaka(sale.dueAmount, currentLanguage)
                                    }
                                    Text(
                                        text = bakiStr,
                                        style = AppTypographyScale.hintText,
                                        color = LowStockRed,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper to select an appropriate hardware icon based on category
 */
private fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase(Locale.ROOT)) {
        "paint" -> Icons.Default.FormatPaint
        "fasteners" -> Icons.Default.Construction
        "plumbing" -> Icons.Default.Plumbing
        "electrical" -> Icons.Default.Lightbulb
        else -> Icons.Default.Build
    }
}

/**
 * Simple View Home Screen:
 * - Designed specifically for older or fast-counter shopkeepers.
 * - Big buttons (touch targets 96dp+), clear high-contrast typography.
 * - Only essentials: Today's Total Sales, Sell, Stock, Customer Baki.
 * - Zero complicated numbers: No profit calculations, no buying prices, no margins.
 */
@Composable
fun SimpleHomeScreenContent(
    todaySales: Double,
    customerBaki: Double,
    todayDateFormatted: String,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onNavigateToSell: () -> Unit,
    onNavigateToStock: () -> Unit,
    onNavigateToBaki: () -> Unit,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onRevealRequest: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("screen_home_simple"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Business Profile & Date/Settings Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val activeProfile = ProfileManager.activeProfile
            val storeName = activeProfile?.name ?: if (isBangla) "আমার দোকান" else "My Store"

            Surface(
                modifier = Modifier
                    .weight(1.3f)
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, VividEmeraldAccent.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable { onProfileClick() }
                .testTag("simple_home_profile_pill"),
            color = DarkSlateSurface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = VividEmeraldPrimary.copy(alpha = 0.2f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = "Store",
                            tint = VividEmeraldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = storeName,
                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isBangla) "প্রোফাইল পরিবর্তন ▾" else "Switch Profile ▾",
                        style = AppTypographyScale.hintText.copy(fontSize = 11.sp),
                        color = VividEmeraldAccent,
                        maxLines = 1
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, DarkSlateCardBorder, RoundedCornerShape(24.dp))
                .clickable { onSettingsClick() }
                .testTag("simple_home_date_pill"),
            color = DarkSlateSurface,
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBangla) "আজকের তারিখ" else "Today",
                        style = AppTypographyScale.hintText.copy(fontSize = 10.sp),
                        color = TextMuted
                    )
                    Text(
                        text = todayDateFormatted,
                        style = AppTypographyScale.hintText.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

        // Today's Total Sales Card - Big, clear, without profit or cost breakdown
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRevealRequest() }
                .testTag("simple_today_sales_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, VividEmeraldAccent.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "আজকের মোট বিক্রি" else "Today's Total Sales",
                        style = AppTypographyScale.sectionHeading.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = TextMuted
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VividEmeraldPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isBangla) "সহজ মোড" else "Simple Mode",
                            style = AppTypographyScale.hintText.copy(
                                fontWeight = FontWeight.Bold,
                                color = VividEmeraldAccent,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = formatTaka(todaySales, currentLanguage, isMaskable = true),
                    style = AppTypographyScale.moneyNumber.copy(
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = VividEmeraldAccent
                    )
                )

                Text(
                    text = if (isBangla) "আজকের সর্বমোট ক্যাশ ও বাকি বিক্রির যোগফল" else "Total sum of today's sales (cash & credit)",
                    style = AppTypographyScale.hintText,
                    color = TextMuted
                )
            }
        }

        // Action 1: Big "Sell Products" Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onNavigateToSell() }
                .testTag("btn_simple_sell"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VividEmeraldPrimary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBangla) "পণ্য বিক্রি করুন" else "Sell Products",
                        style = AppTypographyScale.screenTitle.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (isBangla) "ক্যাশ বা বাকিতে নতুন বিক্রি তৈরি করুন" else "Create new cash or credit sale",
                        style = AppTypographyScale.hintText.copy(fontSize = 13.sp),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Action 2: Big "Stock & Products" Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onNavigateToStock() }
                .testTag("btn_simple_stock"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = DarkSlateSurfaceVariant,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = VividEmeraldAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBangla) "স্টক ও মালামাল" else "Stock & Products",
                        style = AppTypographyScale.screenTitle.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (isBangla) "পণ্যের মজুদ তালিকা ও বিক্রয় মূল্য দেখুন" else "View available stock & selling prices",
                        style = AppTypographyScale.hintText.copy(fontSize = 13.sp),
                        color = TextMuted
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Action 3: Big "Customer Baki" Button
        val hasBaki = customerBaki > 0
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onNavigateToBaki() }
                .testTag("btn_simple_baki"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (hasBaki) LowStockRed.copy(alpha = 0.5f) else DarkSlateCardBorder
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (hasBaki) LowStockRed.copy(alpha = 0.15f) else DarkSlateSurfaceVariant,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = if (hasBaki) LowStockRed else VividEmeraldAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBangla) "কাস্টমার বাকি খাতা" else "Customer Baki",
                        style = AppTypographyScale.screenTitle.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (hasBaki) {
                            val bakiStr = formatTaka(customerBaki, currentLanguage, isMaskable = true)
                            if (isBangla) "মোট বাকি পাওনা: $bakiStr" else "Total Due: $bakiStr"
                        } else {
                            if (isBangla) "কোনো বকেয়া বাকি নেই" else "No pending dues"
                        },
                        style = AppTypographyScale.hintText.copy(
                            fontSize = 14.sp,
                            fontWeight = if (hasBaki) FontWeight.Bold else FontWeight.Normal,
                            color = if (hasBaki) LowStockRed else VividEmeraldAccent
                        )
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Dedicated, clean home screen for Staff members tailored to their role.
 * (e.g. Sales person sees Sell, Stock, and today's sales — but not profit, supplier due, or shop settings).
 */
@Composable
fun StaffRoleHomeScreenContent(
    session: com.example.data.local.ActiveStaffSession,
    todaySales: Double,
    customerBaki: Double,
    todayDateFormatted: String,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onNavigateToSell: () -> Unit,
    onNavigateToStock: () -> Unit,
    onNavigateToBaki: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPaused = !session.isActive
    val canSell = session.allowedFeatures.contains("sales") && !isPaused
    val canStock = session.allowedFeatures.contains("stock") && !isPaused
    val canBaki = session.allowedFeatures.contains("baki") && !isPaused

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("screen_staff_home"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Staff Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
            border = BorderStroke(
                1.dp,
                if (isPaused) LowStockRed.copy(alpha = 0.6f) else VividEmeraldAccent.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isPaused) LowStockRed.copy(alpha = 0.15f) else VividEmeraldPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = if (isPaused) LowStockRed else VividEmeraldAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = session.staffName,
                                style = AppTypographyScale.screenTitle.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = TextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = VividEmeraldPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = session.role,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VividEmeraldAccent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${session.storeName} • $todayDateFormatted",
                            style = AppTypographyScale.hintText,
                            color = TextMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPaused) LowStockRed.copy(alpha = 0.2f) else DarkSlateSurfaceVariant
                ) {
                    Text(
                        text = if (isPaused) (if (isBangla) "স্থগিত" else "Paused") else (if (isBangla) "সক্রিয়" else "Active"),
                        style = AppTypographyScale.hintText.copy(fontWeight = FontWeight.Bold),
                        color = if (isPaused) LowStockRed else VividEmeraldAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (isPaused) {
            // Paused Warning Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = LowStockRed.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, LowStockRed.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Pause, contentDescription = null, tint = LowStockRed)
                    Text(
                        text = if (isBangla)
                            "আপনার অ্যাকাউন্টটি মালিক কর্তৃক সাময়িক স্থগিত করা হয়েছে।"
                        else
                            "Your staff account has been paused by the store owner.",
                        fontSize = 12.sp,
                        color = LowStockRed
                    )
                }
            }
        }

        // Today's Sales Counter (Safe: No profit, no supplier dues)
        if (session.allowedFeatures.contains("sales") && !isPaused) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("staff_today_sales_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.5.dp, VividEmeraldAccent.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBangla) "আজকের মোট বিক্রি" else "Today's Total Sales",
                            style = AppTypographyScale.sectionHeading.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = TextMuted
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = VividEmeraldPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isBangla) "স্টাফ ভিউ" else "Staff View",
                                style = AppTypographyScale.hintText.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = VividEmeraldAccent,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = formatTaka(todaySales, currentLanguage),
                        style = AppTypographyScale.moneyNumber.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = VividEmeraldAccent
                        )
                    )

                    Text(
                        text = if (isBangla) "আজকের মোট বিক্রি হিসাব (লাভ বা খরচ গোপন)" else "Today's sales total (cost & profit hidden)",
                        style = AppTypographyScale.hintText,
                        color = TextMuted
                    )
                }
            }
        }

        // Action 1: Sell / POS (If permitted)
        if (canSell) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigateToSell() }
                    .testTag("btn_staff_sell"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VividEmeraldPrimary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "পণ্য বিক্রি করুন (Sell)" else "Sell Products",
                            style = AppTypographyScale.screenTitle.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (isBangla) "ক্যাশ বা বাকিতে দ্রুত চালান তৈরি" else "Quick POS sale & checkout",
                            style = AppTypographyScale.hintText.copy(fontSize = 13.sp),
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Action 2: Stock (If permitted)
        if (canStock) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 92.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigateToStock() }
                    .testTag("btn_staff_stock"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = DarkSlateSurfaceVariant,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = VividEmeraldAccent,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "স্টক ও মালামাল" else "Stock & Inventory",
                            style = AppTypographyScale.screenTitle.copy(
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (isBangla) "পণ্য তালিকা ও বিক্রয় দর চেক করুন" else "Check stock quantity & selling price",
                            style = AppTypographyScale.hintText.copy(fontSize = 13.sp),
                            color = TextMuted
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Action 3: Customer Baki (If permitted)
        if (canBaki) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 92.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigateToBaki() }
                    .testTag("btn_staff_baki"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = DarkSlateSurfaceVariant,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = VividEmeraldAccent,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "কাস্টমার বাকি খাতা" else "Customer Baki",
                            style = AppTypographyScale.screenTitle.copy(
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (isBangla) "গ্রাহকের বকেয়া ও পেমেন্ট রেকর্ড" else "Customer dues & payment history",
                            style = AppTypographyScale.hintText.copy(fontSize = 13.sp),
                            color = TextMuted
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Leave Staff Mode Button
        OutlinedButton(
            onClick = {
                com.example.data.local.StaffSessionManager.leaveStaffSession(context)
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, DarkSlateCardBorder)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isBangla) "স্টাফ সেশন ত্যাগ করুন" else "Switch to Owner / Leave Staff Mode",
                color = TextMuted,
                fontSize = 12.sp,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

