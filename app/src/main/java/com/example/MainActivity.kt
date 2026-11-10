package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.local.AppSettings
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.LanguageController
import com.example.ui.localization.ProvideAppLanguage
import com.example.ui.navigation.NavTab
import com.example.ui.screens.BusinessScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SellScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StockScreen
import com.example.ui.theme.HardwareManagerTheme

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.business.BusinessViewModel
import com.example.ui.screens.business.SuppliersScreen

import com.example.data.local.ProfileManager
import com.example.ui.screens.setup.FirstRunSetupScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppSettings.init(this)
        com.example.data.local.UnitManager.init(this)
        com.example.data.local.ProfileManager.init(this)
        com.example.data.local.PinLockManager.init(this)
        com.example.data.local.NotificationManager.init(this)
        com.example.data.local.SubscriptionManager.init(this)
        com.example.data.local.StaffSessionManager.init(this)
        com.example.data.firebase.FirebaseManager.init(this)
        enableEdgeToEdge()
        setContent {
            HardwareManagerTheme {
                ProvideAppLanguage {
                    HardwareManagerApp()
                }
            }
        }
    }
}

@Composable
fun HardwareManagerApp() {
    var isFirstRunCompleted by remember { mutableStateOf(ProfileManager.isFirstRunSetupCompleted) }
    val currentLanguage = LanguageController.currentLanguage

    if (!isFirstRunCompleted) {
        FirstRunSetupScreen(
            currentLanguage = currentLanguage,
            onSetupComplete = {
                isFirstRunCompleted = true
            }
        )
        return
    }

    var currentTab by remember { mutableStateOf(NavTab.HOME) }
    var showBakiScreen by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showSuppliersScreen by remember { mutableStateOf(false) }
    var showPartiesScreen by remember { mutableStateOf(false) }
    var showPurchasesScreen by remember { mutableStateOf(false) }
    var showReportsScreen by remember { mutableStateOf(false) }
    var showMarketplaceScreen by remember { mutableStateOf(false) }
    var showAccountsScreen by remember { mutableStateOf(false) }
    var showCardsScreen by remember { mutableStateOf(false) }
    var showRemindersScreen by remember { mutableStateOf(false) }
    var showStaffScreen by remember { mutableStateOf(false) }
    var showBusinessProfileScreen by remember { mutableStateOf(false) }
    var showCategoriesScreen by remember { mutableStateOf(false) }

    // Part 4 Subscreens
    var showCalculatorsScreen by remember { mutableStateOf(false) }
    var showNotebookScreen by remember { mutableStateOf(false) }
    var showBillGalleryScreen by remember { mutableStateOf(false) }
    var showDataImportScreen by remember { mutableStateOf(false) }
    var showHelpAboutScreen by remember { mutableStateOf(false) }

    val isSubScreenOpen = showSettingsScreen || showBakiScreen || showSuppliersScreen ||
            showPartiesScreen || showPurchasesScreen || showReportsScreen || showMarketplaceScreen ||
            showAccountsScreen || showCardsScreen || showRemindersScreen || showStaffScreen ||
            showBusinessProfileScreen || showCategoriesScreen ||
            showCalculatorsScreen || showNotebookScreen || showBillGalleryScreen ||
            showDataImportScreen || showHelpAboutScreen

    fun closeAllSubScreens() {
        showMarketplaceScreen = false
        showSettingsScreen = false
        showBakiScreen = false
        showSuppliersScreen = false
        showPartiesScreen = false
        showPurchasesScreen = false
        showReportsScreen = false
        showAccountsScreen = false
        showCardsScreen = false
        showRemindersScreen = false
        showStaffScreen = false
        showBusinessProfileScreen = false
        showCategoriesScreen = false
        showCalculatorsScreen = false
        showNotebookScreen = false
        showBillGalleryScreen = false
        showDataImportScreen = false
        showHelpAboutScreen = false
    }

    // Android back handler to return from sub-screens or to Home tab
    BackHandler(enabled = isSubScreenOpen || currentTab != NavTab.HOME) {
        when {
            showMarketplaceScreen -> showMarketplaceScreen = false
            showSettingsScreen -> showSettingsScreen = false
            showBakiScreen -> showBakiScreen = false
            showSuppliersScreen -> showSuppliersScreen = false
            showPartiesScreen -> showPartiesScreen = false
            showPurchasesScreen -> showPurchasesScreen = false
            showReportsScreen -> showReportsScreen = false
            showAccountsScreen -> showAccountsScreen = false
            showCardsScreen -> showCardsScreen = false
            showRemindersScreen -> showRemindersScreen = false
            showStaffScreen -> showStaffScreen = false
            showBusinessProfileScreen -> showBusinessProfileScreen = false
            showCategoriesScreen -> showCategoriesScreen = false
            showCalculatorsScreen -> showCalculatorsScreen = false
            showNotebookScreen -> showNotebookScreen = false
            showBillGalleryScreen -> showBillGalleryScreen = false
            showDataImportScreen -> showDataImportScreen = false
            showHelpAboutScreen -> showHelpAboutScreen = false
            else -> currentTab = NavTab.HOME
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = when {
                    showMarketplaceScreen -> {
                        if (currentLanguage == AppLanguage.BANGLA) "দোকান মাল্টি-ভেন্ডর মার্কেটপ্লেস" else "Dokan Marketplace Hub"
                    }
                    showSettingsScreen -> {
                        if (currentLanguage == AppLanguage.BANGLA) "সেটিংস ও ডাটা ব্যাকআপ" else "Settings & Backup"
                    }
                    showBakiScreen -> {
                        if (currentLanguage == AppLanguage.BANGLA) "বাকির খাতা" else "Customer Ledger"
                    }
                    showSuppliersScreen -> {
                        if (currentLanguage == AppLanguage.BANGLA) "মহাজন ও সাপ্লায়ার খাতা" else "Suppliers Ledger"
                    }
                    showPartiesScreen -> {
                        if (currentLanguage == AppLanguage.BANGLA) "পার্টি তালিকা (গ্রাহক ও মহাজন)" else "Parties Ledger"
                    }
                    showPurchasesScreen -> {
                        if (currentLanguage == AppLanguage.BANGLA) "ক্রয় খাতা ও চালান" else "Purchases & Invoices"
                    }
                    showReportsScreen -> {
                        if (currentLanguage == AppLanguage.BANGLA) "ব্যবসায়িক রিপোর্ট ও হেলথ চেক" else "Reports & Health Check"
                    }
                    showAccountsScreen -> {
                        if (currentLanguage == AppLanguage.BANGLA) "ক্যাশ ও ব্যাংক হিসাব" else "Cash & Bank Accounts"
                    }
                    showCardsScreen -> {
                        if (currentLanguage == AppLanguage.BANGLA) "শুভেচ্ছা কার্ড ও বিজনেস কার্ড" else "Cards Hub & Business Card"
                    }
                    showRemindersScreen -> {
                        if (currentLanguage == AppLanguage.BANGLA) "রিমাইন্ডার ও তাগাদা" else "Reminders & Alerts"
                    }
                    showStaffScreen -> {
                        if (currentLanguage == AppLanguage.BANGLA) "কর্মচারী ও মাল্টি-ফোন সিঙ্ক" else "Staff & Multi-Phone Sync"
                    }
                    showBusinessProfileScreen -> {
                        if (currentLanguage == AppLanguage.BANGLA) "দোকানের বিস্তারিত প্রোফাইল" else "Business Profile"
                    }
                    showCategoriesScreen -> {
                        if (currentLanguage == AppLanguage.BANGLA) "সকল ক্যাটাগরি ম্যানেজমেন্ট" else "Manage Categories"
                    }
                    showCalculatorsScreen -> {
                        if (currentLanguage == AppLanguage.BANGLA) "ব্যবসার হিসাব ক্যালকুলেটর" else "Calculators"
                    }
                    showNotebookScreen -> {
                        if (currentLanguage == AppLanguage.BANGLA) "কুইক নোটবুক ও মেমো" else "Notebook & Memos"
                    }
                    showBillGalleryScreen -> {
                        if (currentLanguage == AppLanguage.BANGLA) "বিল ও রসিদ গ্যালারি" else "Bill Gallery"
                    }
                    showDataImportScreen -> {
                        if (currentLanguage == AppLanguage.BANGLA) "ডাটা ইমপোর্ট" else "Import Data"
                    }
                    showHelpAboutScreen -> {
                        if (currentLanguage == AppLanguage.BANGLA) "সাহায্য ও পরিচিতি" else "Help & About"
                    }
                    else -> currentTab.title(currentLanguage)
                },
                currentLanguage = currentLanguage,
                onLanguageChange = { newLang ->
                    LanguageController.setLanguage(newLang)
                },
                canNavigateBack = isSubScreenOpen || currentTab != NavTab.HOME,
                onNavigateBack = {
                    when {
                        showMarketplaceScreen -> showMarketplaceScreen = false
                        showSettingsScreen -> showSettingsScreen = false
                        showBakiScreen -> showBakiScreen = false
                        showSuppliersScreen -> showSuppliersScreen = false
                        showPartiesScreen -> showPartiesScreen = false
                        showPurchasesScreen -> showPurchasesScreen = false
                        showReportsScreen -> showReportsScreen = false
                        showAccountsScreen -> showAccountsScreen = false
                        showCardsScreen -> showCardsScreen = false
                        showRemindersScreen -> showRemindersScreen = false
                        showStaffScreen -> showStaffScreen = false
                        showBusinessProfileScreen -> showBusinessProfileScreen = false
                        showCategoriesScreen -> showCategoriesScreen = false
                        showCalculatorsScreen -> showCalculatorsScreen = false
                        showNotebookScreen -> showNotebookScreen = false
                        showBillGalleryScreen -> showBillGalleryScreen = false
                        showDataImportScreen -> showDataImportScreen = false
                        showHelpAboutScreen -> showHelpAboutScreen = false
                        else -> currentTab = NavTab.HOME
                    }
                },
                onOpenSettings = {
                    closeAllSubScreens()
                    showSettingsScreen = true
                }
            )
        },
        bottomBar = {
            AppBottomBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    closeAllSubScreens()
                    currentTab = tab
                },
                currentLanguage = currentLanguage
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                showMarketplaceScreen -> {
                    com.example.ui.screens.marketplace.MarketplaceScreen(
                        currentLanguage = currentLanguage,
                        onBack = { showMarketplaceScreen = false },
                        onNavigateToSell = {
                            showMarketplaceScreen = false
                            currentTab = NavTab.SELL
                        }
                    )
                }
                showSettingsScreen -> {
                    SettingsScreen(
                        currentLanguage = currentLanguage,
                        onBack = { showSettingsScreen = false },
                        onNavigateToBusinessProfile = {
                            showSettingsScreen = false
                            showBusinessProfileScreen = true
                        },
                        onNavigateToCategories = {
                            showSettingsScreen = false
                            showCategoriesScreen = true
                        }
                    )
                }
                showBakiScreen -> {
                    com.example.ui.screens.baki.BakiScreen(
                        currentLanguage = currentLanguage,
                        onBack = { showBakiScreen = false }
                    )
                }
                showSuppliersScreen -> {
                    val businessVm: BusinessViewModel = viewModel()
                    SuppliersScreen(
                        viewModel = businessVm,
                        currentLanguage = currentLanguage,
                        onBackToDashboard = { showSuppliersScreen = false }
                    )
                }
                showPartiesScreen -> {
                    com.example.ui.screens.parties.PartiesScreen(
                        currentLanguage = currentLanguage,
                        onBack = { showPartiesScreen = false }
                    )
                }
                showPurchasesScreen -> {
                    com.example.ui.screens.purchases.PurchasesScreen(
                        currentLanguage = currentLanguage,
                        onBack = { showPurchasesScreen = false }
                    )
                }
                showReportsScreen -> {
                    com.example.ui.screens.reports.BusinessReportsScreen(
                        currentLanguage = currentLanguage,
                        onNavigateToBaki = {
                            showReportsScreen = false
                            showBakiScreen = true
                        },
                        onNavigateToSuppliers = {
                            showReportsScreen = false
                            showSuppliersScreen = true
                        },
                        onBack = { showReportsScreen = false }
                    )
                }
                showAccountsScreen -> {
                    com.example.ui.screens.accounts.AccountsScreen(
                        currentLanguage = currentLanguage,
                        onBack = { showAccountsScreen = false }
                    )
                }
                showCardsScreen -> {
                    com.example.ui.screens.cards.CardsHubScreen(
                        currentLanguage = currentLanguage,
                        onBack = { showCardsScreen = false }
                    )
                }
                showRemindersScreen -> {
                    com.example.ui.screens.reminders.RemindersScreen(
                        currentLanguage = currentLanguage,
                        onBack = { showRemindersScreen = false }
                    )
                }
                showStaffScreen -> {
                    com.example.ui.screens.staff.StaffManagementScreen(
                        currentLanguage = currentLanguage,
                        onBack = { showStaffScreen = false }
                    )
                }
                showBusinessProfileScreen -> {
                    com.example.ui.screens.profile.BusinessProfileScreen(
                        currentLanguage = currentLanguage,
                        onBack = { showBusinessProfileScreen = false }
                    )
                }
                showCategoriesScreen -> {
                    com.example.ui.screens.categories.ManageCategoriesScreen(
                        currentLanguage = currentLanguage,
                        onBack = { showCategoriesScreen = false }
                    )
                }
                showCalculatorsScreen -> {
                    com.example.ui.screens.utilities.CalculatorsScreen(
                        currentLanguage = currentLanguage,
                        onBack = { showCalculatorsScreen = false }
                    )
                }
                showNotebookScreen -> {
                    com.example.ui.screens.utilities.NotebookScreen(
                        currentLanguage = currentLanguage,
                        onBack = { showNotebookScreen = false }
                    )
                }
                showBillGalleryScreen -> {
                    com.example.ui.screens.utilities.BillGalleryScreen(
                        currentLanguage = currentLanguage,
                        onBack = { showBillGalleryScreen = false }
                    )
                }
                showDataImportScreen -> {
                    com.example.ui.screens.utilities.DataImportScreen(
                        currentLanguage = currentLanguage,
                        onBack = { showDataImportScreen = false }
                    )
                }
                showHelpAboutScreen -> {
                    com.example.ui.screens.utilities.HelpAndAboutScreen(
                        currentLanguage = currentLanguage,
                        onBack = { showHelpAboutScreen = false }
                    )
                }
                else -> {
                    Crossfade(
                        targetState = currentTab,
                        label = "tab_crossfade"
                    ) { tab ->
                        when (tab) {
                            NavTab.HOME -> HomeScreen(
                                currentLanguage = currentLanguage,
                                onNavigateToSell = { currentTab = NavTab.SELL },
                                onNavigateToStock = { currentTab = NavTab.STOCK },
                                onNavigateToBaki = { showBakiScreen = true },
                                onNavigateToSuppliers = { showSuppliersScreen = true },
                                onNavigateToHistory = { currentTab = NavTab.HISTORY },
                                onNavigateToBusiness = { currentTab = NavTab.BUSINESS },
                                onNavigateToSettings = { showSettingsScreen = true },
                                onNavigateToMarketplace = { showMarketplaceScreen = true }
                            )
                            NavTab.SELL -> SellScreen(currentLanguage = currentLanguage)
                            NavTab.STOCK -> StockScreen(currentLanguage = currentLanguage)
                            NavTab.HISTORY -> HistoryScreen(currentLanguage = currentLanguage)
                            NavTab.BUSINESS -> BusinessScreen(
                                currentLanguage = currentLanguage,
                                onNavigateToBaki = { showBakiScreen = true },
                                onNavigateToReports = { showReportsScreen = true }
                            )
                            NavTab.MORE -> com.example.ui.screens.MoreScreen(
                                currentLanguage = currentLanguage,
                                onNavigateToBaki = { showBakiScreen = true },
                                onNavigateToSuppliers = { showSuppliersScreen = true },
                                onNavigateToParties = { showPartiesScreen = true },
                                onNavigateToPurchases = { showPurchasesScreen = true },
                                onNavigateToReports = { showReportsScreen = true },
                                onNavigateToSettings = { showSettingsScreen = true },
                                onNavigateToBusiness = { currentTab = NavTab.BUSINESS },
                                onNavigateToStock = { currentTab = NavTab.STOCK },
                                onNavigateToSell = { currentTab = NavTab.SELL },
                                onNavigateToMarketplace = { showMarketplaceScreen = true },
                                onNavigateToAccounts = { showAccountsScreen = true },
                                onNavigateToCards = { showCardsScreen = true },
                                onNavigateToReminders = { showRemindersScreen = true },
                                onNavigateToStaff = { showStaffScreen = true },
                                onNavigateToBusinessProfile = { showBusinessProfileScreen = true },
                                onNavigateToCategories = { showCategoriesScreen = true },
                                onNavigateToCalculators = { showCalculatorsScreen = true },
                                onNavigateToNotebook = { showNotebookScreen = true },
                                onNavigateToBillGallery = { showBillGalleryScreen = true },
                                onNavigateToDataImport = { showDataImportScreen = true },
                                onNavigateToHelpAbout = { showHelpAboutScreen = true },
                                onOpenProfileSwitcher = { showBusinessProfileScreen = true },
                                onOpenPaymentIn = { showBakiScreen = true },
                                onOpenPaymentOut = { showSuppliersScreen = true },
                                onOpenAddExpense = { showAccountsScreen = true }
                            )
                        }
                    }
                }
            }
        }
    }
}
