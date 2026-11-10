package com.example.ui.screens.marketplace

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.local.BusinessProfile
import com.example.data.local.ProfileManager
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.formatTaka
import com.example.ui.localization.toBengaliNumerals
import com.example.ui.theme.*

data class MarketplaceVendor(
    val id: String,
    val storeName: String,
    val ownerName: String,
    val category: String,
    val phone: String,
    val address: String,
    val rating: Double,
    val reviewCount: Int,
    val commissionRate: Double, // e.g. 8.5%
    val totalSales: Double,
    val commissionPaid: Double,
    val commissionDue: Double,
    val isVerified: Boolean = true,
    val bannerColorHex: String = "#10B981"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    onNavigateToSell: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA

    // Preloaded multi-vendor directory matching Bangladeshi and Dokan marketplace ecosystem
    var vendorsList by remember {
        mutableStateOf(
            listOf(
                MarketplaceVendor(
                    id = "vendor_meghna",
                    storeName = "মেঘনা হার্ডওয়্যার ও মেটাল স্টোর (Meghna Hardware)",
                    ownerName = "মোঃ রফিকুল ইসলাম",
                    category = "Hardware & Tools",
                    phone = "01712-345678",
                    address = "দোকান #১২, নবাবপুর রোড, ঢাকা",
                    rating = 4.9,
                    reviewCount = 142,
                    commissionRate = 8.0,
                    totalSales = 185000.0,
                    commissionPaid = 12000.0,
                    commissionDue = 2800.0,
                    bannerColorHex = "#10B981"
                ),
                MarketplaceVendor(
                    id = "vendor_national",
                    storeName = "ন্যাশনাল পলিমার অ্যান্ড পাইপস (National Polymer)",
                    ownerName = "ইঞ্জিঃ সারোয়ার হোসেন",
                    category = "Pipes & Fittings",
                    phone = "01819-876543",
                    address = "প্লট #৪৫, তেজগাঁও শিল্প এলাকা, ঢাকা",
                    rating = 4.8,
                    reviewCount = 98,
                    commissionRate = 7.5,
                    totalSales = 240000.0,
                    commissionPaid = 15000.0,
                    commissionDue = 3000.0,
                    bannerColorHex = "#3B82F6"
                ),
                MarketplaceVendor(
                    id = "vendor_asian_paints",
                    storeName = "এশিয়ান পেইন্টস অফিসিয়াল স্টোর (Asian Paints Outlet)",
                    ownerName = "আলমগীর কবির",
                    category = "Paint & Chemicals",
                    phone = "01912-987654",
                    address = "রোড #৭, মহাখালী বাণিজ্যিক এলাকা, ঢাকা",
                    rating = 4.9,
                    reviewCount = 210,
                    commissionRate = 10.0,
                    totalSales = 310000.0,
                    commissionPaid = 25000.0,
                    commissionDue = 6000.0,
                    bannerColorHex = "#EC4899"
                ),
                MarketplaceVendor(
                    id = "vendor_superstar",
                    storeName = "সুপার স্টার ইলেকট্রিক হাব (SSG Hub)",
                    ownerName = "হাজী দেলোয়ার হোসেন",
                    category = "Electrical Fittings",
                    phone = "01611-223388",
                    address = "সুন্দরবন স্কয়ার মার্কেট, গুলিস্তান, ঢাকা",
                    rating = 4.7,
                    reviewCount = 76,
                    commissionRate = 9.0,
                    totalSales = 145000.0,
                    commissionPaid = 10000.0,
                    commissionDue = 3050.0,
                    bannerColorHex = "#F59E0B"
                ),
                MarketplaceVendor(
                    id = "vendor_bengal_locks",
                    storeName = "বেঙ্গল সিকিউরিটি লকস অ্যান্ড ফিটিংস (Bengal Locks)",
                    ownerName = "জহিরুল হক",
                    category = "Locks & Fittings",
                    phone = "01533-445566",
                    address = "চকবাজার পাইকারি মার্কেট, ঢাকা",
                    rating = 4.8,
                    reviewCount = 64,
                    commissionRate = 8.5,
                    totalSales = 98000.0,
                    commissionPaid = 7000.0,
                    commissionDue = 1330.0,
                    bannerColorHex = "#8B5CF6"
                )
            )
        )
    }

    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var activeTab by remember { mutableIntStateOf(0) } // 0: Stores Directory, 1: Commission & Payouts, 2: Marketplace Settings
    var showRegisterVendorDialog by remember { mutableStateOf(false) }
    var selectedVendorForDetails by remember { mutableStateOf<MarketplaceVendor?>(null) }

    val categories = listOf("All", "Hardware & Tools", "Pipes & Fittings", "Paint & Chemicals", "Electrical Fittings", "Locks & Fittings")

    val filteredVendors = vendorsList.filter { vendor ->
        val matchesCategory = selectedCategory == null || selectedCategory == "All" || vendor.category.equals(selectedCategory, ignoreCase = true)
        val matchesQuery = searchQuery.isBlank() || vendor.storeName.contains(searchQuery, ignoreCase = true) ||
                vendor.ownerName.contains(searchQuery, ignoreCase = true) ||
                vendor.address.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    val totalGMV = vendorsList.sumOf { it.totalSales }
    val totalCommissionEarned = vendorsList.sumOf { it.commissionPaid + it.commissionDue }
    val totalPendingPayouts = vendorsList.sumOf { it.commissionDue }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
            .testTag("screen_marketplace")
    ) {
        // Sticky Top Bar
        Surface(
            color = DarkSlateSurface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_marketplace")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (isBangla) "দোকান মাল্টি-ভেন্ডর মার্কেটপ্লেস" else "Dokan Marketplace Hub",
                                style = AppTypographyScale.sectionHeading,
                                color = TextPrimary
                            )
                            Surface(
                                color = VividEmeraldPrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "PRO",
                                    color = VividEmeraldAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isBangla) "ভেন্ডর স্টোর, কমিশন হিসাব ও মার্কেটপ্লেস নেটওয়ার্ক" else "Vendor Stores, Commissions & Store Directory",
                            style = AppTypographyScale.hintText,
                            color = TextMuted
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { showRegisterVendorDialog = true },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = VividEmeraldPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_add_vendor")
                ) {
                    Icon(Icons.Default.AddBusiness, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "নতুন ভেন্ডর" else "Add Vendor", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Marketplace Metric Overview Cards (Tailwind style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp)),
                color = DarkSlateSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(if (isBangla) "মোট ভেন্ডর" else "Total Vendors", style = AppTypographyScale.hintText, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isBangla) "${toBengaliNumerals(vendorsList.size)} টি স্টোর" else "${vendorsList.size} Stores",
                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                        color = VividEmeraldAccent
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp)),
                color = DarkSlateSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(if (isBangla) "মার্কেটপ্লেস বিক্রি" else "Total GMV", style = AppTypographyScale.hintText, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatTaka(totalGMV, currentLanguage),
                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp)),
                color = DarkSlateSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(if (isBangla) "কমিশন পাওনা" else "Commission Due", style = AppTypographyScale.hintText, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatTaka(totalPendingPayouts, currentLanguage),
                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                        color = VividEmeraldAccent
                    )
                }
            }
        }

        // Navigation Tabs (Stores / Commissions / Settings)
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = DarkSlateSurface,
            contentColor = VividEmeraldAccent
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text(if (isBangla) "ভেন্ডর স্টোরসমূহ (${vendorsList.size})" else "Vendor Stores (${vendorsList.size})", fontSize = 13.sp) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text(if (isBangla) "কমিশন ও সেটেলমেন্ট" else "Commissions & Payouts", fontSize = 13.sp) }
            )
        }

        if (activeTab == 0) {
            // Search Bar & Filter Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isBangla) "ভেন্ডর বা দোকানের নাম দিয়ে খুঁজুন..." else "Search vendor store, owner or location...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_vendor"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = VividEmeraldAccent,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedContainerColor = DarkSlateSurface,
                        unfocusedContainerColor = DarkSlateSurface
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = (cat == "All" && selectedCategory == null) || selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = if (cat == "All") null else cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VividEmeraldPrimary,
                                selectedLabelColor = TextPrimary,
                                containerColor = DarkSlateSurface,
                                labelColor = TextMuted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) VividEmeraldAccent else DarkSlateCardBorder
                            )
                        )
                    }
                }
            }

            // Vendors List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredVendors) { vendor ->
                    VendorCard(
                        vendor = vendor,
                        isBangla = isBangla,
                        currentLanguage = currentLanguage,
                        onViewDetails = { selectedVendorForDetails = vendor },
                        onActivateAsStore = {
                            ProfileManager.createProfile(
                                context = context,
                                name = vendor.storeName,
                                phone = vendor.phone,
                                address = vendor.address,
                                businessType = vendor.category
                            )
                            Toast.makeText(
                                context,
                                if (isBangla) "'${vendor.storeName}' সক্রিয় স্টোর হিসেবে সেট হয়েছে!" else "Switched active store to '${vendor.storeName}'!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        } else {
            // Commissions & Settlements Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        color = DarkSlateSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = VividEmeraldAccent)
                            Text(
                                text = if (isBangla) "দোকান মার্কেটপ্লেসে স্বয়ংক্রিয় কমিশন প্রতিটি অর্ডারে হিসাব হয় এবং পে-আউট ক্লিয়ার করা যায়।" else "Dokan multi-vendor commissions are automatically calculated per completed sale order and settled here.",
                                style = AppTypographyScale.hintText,
                                color = TextPrimary
                            )
                        }
                    }
                }

                items(vendorsList) { vendor ->
                    Surface(
                        color = DarkSlateSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DarkSlateCardBorder),
                        modifier = Modifier.fillMaxWidth().testTag("settlement_row_${vendor.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(vendor.storeName, style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                    Text("${if (isBangla) "ভেন্ডর মালিক: " else "Owner: "}${vendor.ownerName} • ${vendor.commissionRate}% Commission", style = AppTypographyScale.hintText, color = TextMuted)
                                }

                                Surface(
                                    color = if (vendor.commissionDue > 0) VividEmeraldPrimary.copy(alpha = 0.2f) else DarkSlateBackground,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (vendor.commissionDue > 0) (if (isBangla) "বকেয়া কমিশন" else "Due") else (if (isBangla) "পরিশোধিত" else "Settled"),
                                        color = if (vendor.commissionDue > 0) VividEmeraldAccent else TextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = DarkSlateCardBorder)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(if (isBangla) "মোট বিক্রি" else "Total Sold", style = AppTypographyScale.hintText, color = TextMuted)
                                    Text(formatTaka(vendor.totalSales, currentLanguage), fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                                }

                                Column {
                                    Text(if (isBangla) "পরিশোধিত" else "Paid Commission", style = AppTypographyScale.hintText, color = TextMuted)
                                    Text(formatTaka(vendor.commissionPaid, currentLanguage), fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                                }

                                Column {
                                    Text(if (isBangla) "পাওনা কমিশন" else "Unpaid Commission", style = AppTypographyScale.hintText, color = TextMuted)
                                    Text(formatTaka(vendor.commissionDue, currentLanguage), fontWeight = FontWeight.Bold, color = VividEmeraldAccent, fontSize = 13.sp)
                                }
                            }

                            if (vendor.commissionDue > 0) {
                                Button(
                                    onClick = {
                                        vendorsList = vendorsList.map {
                                            if (it.id == vendor.id) {
                                                it.copy(
                                                    commissionPaid = it.commissionPaid + it.commissionDue,
                                                    commissionDue = 0.0
                                                )
                                            } else it
                                        }
                                        Toast.makeText(
                                            context,
                                            if (isBangla) "'${vendor.storeName}'-এর কমিশন সেটেলমেন্ট সম্পন্ন হয়েছে!" else "Settlement completed for '${vendor.storeName}'!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(40.dp).testTag("btn_settle_${vendor.id}"),
                                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (isBangla) "কমিশন সেটেল করুন (${formatTaka(vendor.commissionDue, currentLanguage)})" else "Record Settlement (${formatTaka(vendor.commissionDue, currentLanguage)})", color = TextPrimary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // Register Vendor Dialog
    if (showRegisterVendorDialog) {
        RegisterVendorDialog(
            isBangla = isBangla,
            onDismiss = { showRegisterVendorDialog = false },
            onRegister = { newVendor ->
                vendorsList = listOf(newVendor) + vendorsList
                showRegisterVendorDialog = false
                Toast.makeText(
                    context,
                    if (isBangla) "নতুন ভেন্ডর সফলভাবে মার্কেটপ্লেসে নিবন্ধিত হয়েছে!" else "New vendor registered successfully in Dokan Marketplace!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // Vendor Storefront Details Dialog
    if (selectedVendorForDetails != null) {
        VendorStorefrontDialog(
            vendor = selectedVendorForDetails!!,
            isBangla = isBangla,
            currentLanguage = currentLanguage,
            onDismiss = { selectedVendorForDetails = null },
            onQuickSell = {
                selectedVendorForDetails = null
                onNavigateToSell()
            }
        )
    }
}

@Composable
fun VendorCard(
    vendor: MarketplaceVendor,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onViewDetails: () -> Unit,
    onActivateAsStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onViewDetails() }
            .testTag("vendor_card_${vendor.id}"),
        color = DarkSlateSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, DarkSlateCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = vendor.storeName,
                                style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            if (vendor.isVerified) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Vendor",
                                    tint = VividEmeraldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = "${vendor.category} • ${vendor.ownerName}",
                            style = AppTypographyScale.hintText,
                            color = TextMuted
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                    Text(
                        text = "${vendor.rating} (${if (isBangla) toBengaliNumerals(vendor.reviewCount) else vendor.reviewCount})",
                        style = AppTypographyScale.hintText,
                        color = TextPrimary
                    )
                }

                Text("•", color = TextMuted, fontSize = 12.sp)

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Percent, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(14.dp))
                    Text(
                        text = "${if (isBangla) toBengaliNumerals(vendor.commissionRate) else vendor.commissionRate}% কমিশন",
                        style = AppTypographyScale.hintText,
                        color = VividEmeraldAccent
                    )
                }

                Text("•", color = TextMuted, fontSize = 12.sp)

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Text(
                        text = vendor.address.take(24) + "...",
                        style = AppTypographyScale.hintText,
                        color = TextMuted
                    )
                }
            }

            HorizontalDivider(color = DarkSlateCardBorder)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onActivateAsStore,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, VividEmeraldPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_switch_store_${vendor.id}")
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "এই দোকানে সুইচ করুন" else "Switch Active Store", color = VividEmeraldAccent, fontSize = 11.sp)
                }

                Button(
                    onClick = onViewDetails,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_view_storefront_${vendor.id}")
                ) {
                    Text(if (isBangla) "দোকান ভিজিট করুন" else "View Storefront", color = TextPrimary, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun RegisterVendorDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onRegister: (MarketplaceVendor) -> Unit
) {
    var storeName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Hardware & Tools") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var commissionStr by remember { mutableStateOf("8.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "নতুন ভেন্ডর স্টোর নিবন্ধন" else "Register Vendor Store",
                style = AppTypographyScale.sectionHeading,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text(if (isBangla) "দোকান / ভেন্ডরের নাম" else "Store / Vendor Name") },
                    modifier = Modifier.fillMaxWidth().testTag("input_vendor_store_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text(if (isBangla) "মালিকের নাম" else "Owner Name") },
                    modifier = Modifier.fillMaxWidth().testTag("input_vendor_owner_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isBangla) "মোবাইল নম্বর" else "Contact Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("input_vendor_phone"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(if (isBangla) "দোকানের ঠিকানা / মার্কেট" else "Store Address / Market") },
                    modifier = Modifier.fillMaxWidth().testTag("input_vendor_address"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = commissionStr,
                    onValueChange = { commissionStr = it },
                    label = { Text(if (isBangla) "মার্কেটপ্লেস কমিশন (%)" else "Marketplace Commission (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_vendor_commission"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (storeName.isNotBlank() && ownerName.isNotBlank()) {
                        val newVendor = MarketplaceVendor(
                            id = "vendor_${System.currentTimeMillis() % 10000}",
                            storeName = storeName.trim(),
                            ownerName = ownerName.trim(),
                            category = category,
                            phone = phone.trim(),
                            address = address.trim(),
                            rating = 5.0,
                            reviewCount = 1,
                            commissionRate = commissionStr.toDoubleOrNull() ?: 8.0,
                            totalSales = 0.0,
                            commissionPaid = 0.0,
                            commissionDue = 0.0
                        )
                        onRegister(newVendor)
                    }
                },
                enabled = storeName.isNotBlank() && ownerName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_confirm_register_vendor")
            ) {
                Text(if (isBangla) "নিবন্ধন সম্পন্ন" else "Register Vendor", color = TextPrimary)
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
fun VendorStorefrontDialog(
    vendor: MarketplaceVendor,
    isBangla: Boolean,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onQuickSell: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(VividEmeraldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = TextPrimary)
                }
                Column {
                    Text(vendor.storeName, style = AppTypographyScale.sectionHeading, color = TextPrimary)
                    Text(vendor.category, style = AppTypographyScale.hintText, color = VividEmeraldAccent)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = DarkSlateBackground,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${if (isBangla) "মালিক: " else "Owner: "}${vendor.ownerName}", color = TextPrimary, fontSize = 13.sp)
                        Text("${if (isBangla) "যোগাযোগ: " else "Phone: "}${vendor.phone}", color = TextPrimary, fontSize = 13.sp)
                        Text("${if (isBangla) "ঠিকানা: " else "Location: "}${vendor.address}", color = TextMuted, fontSize = 12.sp)
                        Text("${if (isBangla) "রেটিং: " else "Rating: "}${vendor.rating} ★ (${vendor.reviewCount} Reviews)", color = Color(0xFFFBBF24), fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(if (isBangla) "মোট বিক্রয় ভলিউম" else "Total GMV Sold", style = AppTypographyScale.hintText, color = TextMuted)
                        Text(formatTaka(vendor.totalSales, currentLanguage), fontWeight = FontWeight.Bold, color = VividEmeraldAccent)
                    }

                    Column {
                        Text(if (isBangla) "মার্কেটপ্লেস কমিশন" else "Commission Rate", style = AppTypographyScale.hintText, color = TextMuted)
                        Text("${vendor.commissionRate}%", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onQuickSell,
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
            ) {
                Text(if (isBangla) "এই ভেন্ডরের বিক্রি করুন" else "Sell for this Vendor", color = TextPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বন্ধ করুন" else "Close", color = TextMuted)
            }
        }
    )
}
