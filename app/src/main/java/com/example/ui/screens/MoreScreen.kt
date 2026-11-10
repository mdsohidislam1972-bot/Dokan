package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProfileManager
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.AppTypographyScale
import com.example.ui.theme.DarkSlateBackground
import com.example.ui.theme.DarkSlateCardBorder
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.DarkSlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VividEmeraldAccent
import com.example.ui.theme.VividEmeraldPrimary

data class MoreMenuItem(
    val titleEn: String,
    val titleBn: String,
    val subtitleEn: String,
    val subtitleBn: String,
    val icon: ImageVector,
    val badge: String? = null,
    val testTag: String,
    val onClick: () -> Unit
)

@Composable
fun MoreScreen(
    currentLanguage: AppLanguage,
    onNavigateToBaki: () -> Unit = {},
    onNavigateToSuppliers: () -> Unit = {},
    onNavigateToParties: () -> Unit = {},
    onNavigateToPurchases: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToBusiness: () -> Unit = {},
    onNavigateToStock: () -> Unit = {},
    onNavigateToSell: () -> Unit = {},
    onNavigateToMarketplace: () -> Unit = {},
    onNavigateToAccounts: () -> Unit = {},
    onNavigateToCards: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToStaff: () -> Unit = {},
    onNavigateToBusinessProfile: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToCalculators: () -> Unit = {},
    onNavigateToNotebook: () -> Unit = {},
    onNavigateToBillGallery: () -> Unit = {},
    onNavigateToDataImport: () -> Unit = {},
    onNavigateToHelpAbout: () -> Unit = {},
    onOpenProfileSwitcher: () -> Unit = {},
    onOpenPaymentIn: () -> Unit = {},
    onOpenPaymentOut: () -> Unit = {},
    onOpenSalesReturn: () -> Unit = {},
    onOpenQuotation: () -> Unit = {},
    onOpenAddExpense: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val activeProfile = ProfileManager.activeProfile

    val utilitiesItems = listOf(
        MoreMenuItem(
            titleEn = "Business Calculators (EMI & Tax)",
            titleBn = "ব্যবসার হিসাব ও লোন ক্যালকুলেটর",
            subtitleEn = "EMI loan, compound interest and VAT/Tax",
            subtitleBn = "মাসিক কিস্তি (EMI), সুদ ও ভ্যাট/ট্যাক্স হিসাব",
            icon = Icons.Default.Calculate,
            testTag = "more_item_calculators",
            onClick = onNavigateToCalculators
        ),
        MoreMenuItem(
            titleEn = "Quick Notebook & Memos",
            titleBn = "কুইক নোটবুক ও মেমো",
            subtitleEn = "Important business notes, color tags & reminders",
            subtitleBn = "দোকানের জরুরি হিসাব নোট, কালার ট্যাগ ও পিন মেমো",
            icon = Icons.Default.NoteAlt,
            testTag = "more_item_notebook",
            onClick = onNavigateToNotebook
        ),
        MoreMenuItem(
            titleEn = "Bill & Receipt Gallery",
            titleBn = "বিল ও রসিদ গ্যালারি",
            subtitleEn = "Auto-stored purchase bills & paper invoices",
            subtitleBn = "ক্রয় চালান ও রসিদের ছবি সংরক্ষণ ও শেয়ার",
            icon = Icons.Default.Collections,
            testTag = "more_item_bill_gallery",
            onClick = onNavigateToBillGallery
        ),
        MoreMenuItem(
            titleEn = "Import Data (Parties & Stock)",
            titleBn = "ডাটা ইমপোর্ট (পার্টি ও স্টক)",
            subtitleEn = "Quickly import customer list and product inventory",
            subtitleBn = "এক ক্লিকে গ্রাহক খাতা ও পণ্য তালিকা যুক্ত করুন",
            icon = Icons.Default.UploadFile,
            testTag = "more_item_data_import",
            onClick = onNavigateToDataImport
        ),
        MoreMenuItem(
            titleEn = "Help, Support & About",
            titleBn = "সাহায্য, সাপোর্ট ও পরিচিতি",
            subtitleEn = "Tutorial videos, hotline, web login, app info",
            subtitleBn = "ভিডিও গাইড, হটলাইন, ডেস্কটপ ওয়েব সিঙ্ক ও পরিচিতি",
            icon = Icons.Default.HelpOutline,
            testTag = "more_item_help_about",
            onClick = onNavigateToHelpAbout
        )
    )

    val marketplaceItem = listOf(
        MoreMenuItem(
            titleEn = "Dokan Multi-Vendor Marketplace Hub",
            titleBn = "দোকান মাল্টি-ভেন্ডর মার্কেটপ্লেস হাব",
            subtitleEn = "Browse vendor shops, commission rates, and payouts",
            subtitleBn = "ভেন্ডর স্টোর ব্রাউজ, কমিশন হিসাব ও মার্কেটপ্লেস নেটওয়ার্ক",
            icon = Icons.Default.Storefront,
            badge = "HUB",
            testTag = "more_item_marketplace",
            onClick = onNavigateToMarketplace
        )
    )

    val peopleItems = listOf(
        MoreMenuItem(
            titleEn = "Staff & Multi-Phone Sync",
            titleBn = "কর্মচারী ও মাল্টি-ফোন সিঙ্ক",
            subtitleEn = "Team list, Add Staff button & 5-min sync QR",
            subtitleBn = "টিম তালিকা, নতুন কর্মচারী যোগ ও ৫ মিনিটের সিঙ্ক QR",
            icon = Icons.Default.Badge,
            testTag = "more_item_staff",
            onClick = onNavigateToStaff
        ),
        MoreMenuItem(
            titleEn = "Parties (Customers & Suppliers)",
            titleBn = "পার্টি তালিকা (কাস্টমার ও মহাজন)",
            subtitleEn = "Manage customers, suppliers, custom rates & contacts",
            subtitleBn = "গ্রাহক ও সাপ্লায়ার তালিকা, স্পেশাল দর ও ফোন কন্টাক্ট",
            icon = Icons.Default.PersonAdd,
            testTag = "more_item_parties",
            onClick = onNavigateToParties
        )
    )

    val ledgerItems = listOf(
        MoreMenuItem(
            titleEn = "Cash & Bank Accounts",
            titleBn = "ক্যাশ ও ব্যাংক হিসাব",
            subtitleEn = "Overall balance, wallets, banks, adjust, other income",
            subtitleBn = "মোট ব্যালেন্স, ব্যাংক, ওয়ালেট ও অন্যান্য আয়",
            icon = Icons.Default.AccountBalance,
            testTag = "more_item_accounts",
            onClick = onNavigateToAccounts
        ),
        MoreMenuItem(
            titleEn = "Purchases & Stock In (মাল ক্রয়)",
            titleBn = "ক্রয় খাতা ও পাইকারি চালান",
            subtitleEn = "Add purchase, upload bill photo, supplier dues",
            subtitleBn = "নতুন ক্রয় চালান যোগ, বিলের ছবি ও মহাজন দেনা",
            icon = Icons.Default.LocalShipping,
            testTag = "more_item_purchases",
            onClick = onNavigateToPurchases
        ),
        MoreMenuItem(
            titleEn = "Customer Credit Ledger (Baki)",
            titleBn = "কাস্টমার বাকি খাতা",
            subtitleEn = "Track customer dues, credit sales, payment history",
            subtitleBn = "গ্রাহকের বাকি হিসাব, চালান ও জমার খতিয়ান",
            icon = Icons.Default.Description,
            testTag = "more_item_baki",
            onClick = onNavigateToBaki
        ),
        MoreMenuItem(
            titleEn = "Suppliers & Vendor Ledger",
            titleBn = "মহাজন ও সাপ্লায়ার খাতা",
            subtitleEn = "Track bills, supplier dues, payments to vendors",
            subtitleBn = "পাইকারি চালান, দেনা হিসাব ও মহাজন পরিশোধ",
            icon = Icons.Default.LocalShipping,
            testTag = "more_item_suppliers",
            onClick = onNavigateToSuppliers
        ),
        MoreMenuItem(
            titleEn = "Payment In (Customer Payment)",
            titleBn = "পেমেন্ট ইন (টাকা জমা)",
            subtitleEn = "Record payment received from a customer",
            subtitleBn = "গ্রাহকের কাছ থেকে বকেয়া টাকা জমা গ্রহণ",
            icon = Icons.Default.Payment,
            testTag = "more_item_payment_in",
            onClick = onOpenPaymentIn
        ),
        MoreMenuItem(
            titleEn = "Payment Out (Supplier Payment)",
            titleBn = "পেমেন্ট আউট (টাকা প্রদান)",
            subtitleEn = "Record payment paid to suppliers/vendors",
            subtitleBn = "মহাজন বা ডিলারকে পাওনা টাকা পরিশোধ",
            icon = Icons.Default.AccountBalanceWallet,
            testTag = "more_item_payment_out",
            onClick = onOpenPaymentOut
        )
    )

    val operationsItems = listOf(
        MoreMenuItem(
            titleEn = "Greeting Cards & Business Card",
            titleBn = "শুভেচ্ছা কার্ড ও ডিজিটাল ভিজিটিং কার্ড",
            subtitleEn = "Festival greetings, branded frames & custom cards",
            subtitleBn = "দোকানের নামে শুভেচ্ছা বার্তা, ফ্রেম ও কার্ড",
            icon = Icons.Default.CardGiftcard,
            badge = "NEW",
            testTag = "more_item_cards",
            onClick = onNavigateToCards
        ),
        MoreMenuItem(
            titleEn = "Reminders & Alerts",
            titleBn = "রিমাইন্ডার ও তাগাদা অ্যালার্ট",
            subtitleEn = "Scheduled alerts for dues, events and tasks",
            subtitleBn = "পাওনা তাগাদা, ইভেন্ট ও দৈনিক কাজের রিমাইন্ডার",
            icon = Icons.Default.NotificationsActive,
            testTag = "more_item_reminders",
            onClick = onNavigateToReminders
        ),
        MoreMenuItem(
            titleEn = "Business Reports & Data Health Check",
            titleBn = "ব্যবসায়িক রিপোর্ট ও ডাটা হেলথ চেক",
            subtitleEn = "P&L, Day Book, Staff, Monthly PDF & Error Scanner",
            subtitleBn = "লাভ-লোকসান, ডে বুক, মাসিক PDF ডাউনলোড ও ভুল সংশোধন",
            icon = Icons.Default.BarChart,
            testTag = "more_item_reports_hub",
            onClick = onNavigateToReports
        ),
        MoreMenuItem(
            titleEn = "Sales Return (Credit Note)",
            titleBn = "বিক্রয় ফেরত (Sales Return)",
            subtitleEn = "Return sold goods, restore stock, adjust due",
            subtitleBn = "বিক্রিত পণ্য ফেরত নেওয়া ও কাস্টমার বাকি সমন্বয়",
            icon = Icons.Default.AssignmentReturn,
            testTag = "more_item_sales_return",
            onClick = onOpenSalesReturn
        ),
        MoreMenuItem(
            titleEn = "Price Quotation / Estimate",
            titleBn = "দরপত্র / কোটেশন তৈরি",
            subtitleEn = "Create non-binding price quotes for buyers",
            subtitleBn = "স্টক ছাড়া গ্রাহকের জন্য আনুমানিক খরচের ফর্দ",
            icon = Icons.Default.Receipt,
            testTag = "more_item_quotation",
            onClick = onOpenQuotation
        ),
        MoreMenuItem(
            titleEn = "Daily Expense Tracker",
            titleBn = "দৈনিক দোকান খরচ",
            subtitleEn = "Staff salaries, rent, tea, utility bills",
            subtitleBn = "কর্মচারী বেতন, দোকান ভাড়া, বিদ্যুৎ ও চা নাস্তা খরচ",
            icon = Icons.Default.MoneyOff,
            testTag = "more_item_expense",
            onClick = onOpenAddExpense
        )
    )

    val settingsItems = listOf(
        MoreMenuItem(
            titleEn = "Store Profile & Danger Zone",
            titleBn = "দোকানের বিস্তারিত প্রোফাইল",
            subtitleEn = "Logo, trade license, bank accounts & danger area",
            subtitleBn = "লোগো, ঠিকানা, লাইসেন্স, ব্যাংক ও পিন সুরক্ষা",
            icon = Icons.Default.Store,
            testTag = "more_item_business_profile",
            onClick = onNavigateToBusinessProfile
        ),
        MoreMenuItem(
            titleEn = "Manage All Categories",
            titleBn = "সকল ক্যাটাগরি ম্যানেজমেন্ট",
            subtitleEn = "Party, item, expense & income categories in one hub",
            subtitleBn = "গ্রাহক, পণ্য, খরচ ও আয়ের ক্যাটাগরি এক জায়গায়",
            icon = Icons.Default.Category,
            testTag = "more_item_categories",
            onClick = onNavigateToCategories
        ),
        MoreMenuItem(
            titleEn = "Business Profiles Switcher",
            titleBn = "ব্যবসার প্রোফাইল পরিবর্তন",
            subtitleEn = "Manage multiple shops and profiles separately",
            subtitleBn = "এক অ্যাপে একাধিক ব্যবসার প্রোফাইল নিয়ন্ত্রণ",
            icon = Icons.Default.Business,
            badge = activeProfile.name,
            testTag = "more_item_profiles",
            onClick = onOpenProfileSwitcher
        ),
        MoreMenuItem(
            titleEn = "Backup, Restore & Settings",
            titleBn = "ডাটা ব্যাকআপ ও সেটিংস",
            subtitleEn = "Google Drive cloud backup, printer setup, security",
            subtitleBn = "গুগল ড্রাইভ ব্যাকআপ, প্রিন্টার ও গোপন পিন লক",
            icon = Icons.Default.Backup,
            testTag = "more_item_backup",
            onClick = onNavigateToSettings
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("screen_more"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Store Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, VividEmeraldAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .clickable { onOpenProfileSwitcher() }
                .testTag("more_profile_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = VividEmeraldPrimary.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = VividEmeraldAccent,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeProfile.name.ifBlank { if (isBangla) "আমার দোকান" else "My Store" },
                        style = AppTypographyScale.screenTitle.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = activeProfile.businessType.ifBlank { "Hardware & Tools" },
                        style = AppTypographyScale.hintText,
                        color = VividEmeraldAccent
                    )
                    if (activeProfile.phone.isNotBlank()) {
                        Text(
                            text = "📞 ${activeProfile.phone}",
                            style = AppTypographyScale.hintText,
                            color = TextMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSlateSurfaceVariant
                ) {
                    Text(
                        text = if (isBangla) "বদলান ▾" else "Switch ▾",
                        style = AppTypographyScale.hintText.copy(fontWeight = FontWeight.Bold),
                        color = VividEmeraldAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Section: Utilities & Tools
        Text(
            text = if (isBangla) "ইউটিলিটি ও টুলস (Utilities)" else "Utilities & Tools",
            style = AppTypographyScale.sectionHeading.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
            border = BorderStroke(1.dp, DarkSlateCardBorder)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                utilitiesItems.forEachIndexed { index, item ->
                    MoreMenuRow(item = item, isBangla = isBangla)
                    if (index < utilitiesItems.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(DarkSlateCardBorder.copy(alpha = 0.6f))
                        )
                    }
                }
            }
        }

        // Section: Multi-Vendor Marketplace
        Text(
            text = if (isBangla) "মার্কেটপ্লেস নেটওয়ার্ক" else "Marketplace Hub",
            style = AppTypographyScale.sectionHeading.copy(fontWeight = FontWeight.Bold),
            color = VividEmeraldAccent
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
            border = BorderStroke(1.dp, VividEmeraldPrimary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                marketplaceItem.forEach { item ->
                    MoreMenuRow(item = item, isBangla = isBangla)
                }
            }
        }

        // Section: People & Team (Staff, Parties)
        Text(
            text = if (isBangla) "ব্যক্তি ও টিম (People)" else "People & Team",
            style = AppTypographyScale.sectionHeading.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
            border = BorderStroke(1.dp, DarkSlateCardBorder)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                peopleItems.forEachIndexed { index, item ->
                    MoreMenuRow(item = item, isBangla = isBangla)
                    if (index < peopleItems.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(DarkSlateCardBorder.copy(alpha = 0.6f))
                        )
                    }
                }
            }
        }

        // Section: Ledgers & Payments
        Text(
            text = if (isBangla) "খাতা ও লেনদেন (Ledgers & Accounts)" else "Ledgers & Payments",
            style = AppTypographyScale.sectionHeading.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
            border = BorderStroke(1.dp, DarkSlateCardBorder)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                ledgerItems.forEachIndexed { index, item ->
                    MoreMenuRow(item = item, isBangla = isBangla)
                    if (index < ledgerItems.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(DarkSlateCardBorder.copy(alpha = 0.6f))
                        )
                    }
                }
            }
        }

        // Section: Operations & Extra Features
        Text(
            text = if (isBangla) "অপারেশন ও অন্যান্য সুবিধা" else "Operations & Extra Tools",
            style = AppTypographyScale.sectionHeading.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
            border = BorderStroke(1.dp, DarkSlateCardBorder)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                operationsItems.forEachIndexed { index, item ->
                    MoreMenuRow(item = item, isBangla = isBangla)
                    if (index < operationsItems.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(DarkSlateCardBorder.copy(alpha = 0.6f))
                        )
                    }
                }
            }
        }

        // Section: Management & Settings
        Text(
            text = if (isBangla) "ব্যবস্থাপনা ও ব্যাকআপ" else "Management & Settings",
            style = AppTypographyScale.sectionHeading.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
            border = BorderStroke(1.dp, DarkSlateCardBorder)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                settingsItems.forEachIndexed { index, item ->
                    MoreMenuRow(item = item, isBangla = isBangla)
                    if (index < settingsItems.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(DarkSlateCardBorder.copy(alpha = 0.6f))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MoreMenuRow(
    item: MoreMenuItem,
    isBangla: Boolean
) {
    Surface(
        onClick = item.onClick,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(item.testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = DarkSlateSurfaceVariant,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = VividEmeraldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isBangla) item.titleBn else item.titleEn,
                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.badge != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = VividEmeraldPrimary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = item.badge,
                                style = AppTypographyScale.hintText.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = VividEmeraldAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = if (isBangla) item.subtitleBn else item.subtitleEn,
                    style = AppTypographyScale.hintText,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
