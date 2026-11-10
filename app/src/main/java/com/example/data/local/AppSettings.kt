package com.example.data.local

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.LanguageController

enum class ViewMode(val titleEn: String, val titleBn: String, val descEn: String, val descBn: String) {
    SIMPLE(
        titleEn = "Simple View",
        titleBn = "সহজ ভিউ",
        descEn = "Clean layout with larger buttons and essential details for fast-paced shop counter sales.",
        descBn = "দ্রুত বিক্রির জন্য পরিষ্কার ও বড় বাটনযুক্ত সহজ স্ক্রিন।"
    ),
    DETAILED(
        titleEn = "Detailed View",
        titleBn = "বিস্তারিত ভিউ",
        descEn = "Comprehensive view showing buying prices, profit margins, stock levels, timestamps and notes.",
        descBn = "ক্রয়মূল্য, আনুমানিক লাভ, নির্দিষ্ট স্টক অ্যালার্ট ও বিস্তারিত তথ্যসহ স্ক্রিন।"
    )
}

enum class FontSizeOption(val scale: Float, val labelEn: String, val labelBn: String) {
    SMALL(0.88f, "Small", "ছোট"),
    NORMAL(1.0f, "Normal", "স্বাভাবিক"),
    LARGE(1.15f, "Large", "বড়")
}

enum class ThemeModeOption(val labelEn: String, val labelBn: String) {
    SYSTEM("System Default", "সিস্টেম অনুযায়ী"),
    DARK("Dark Mode", "ডার্ক মোড"),
    LIGHT("Light Mode", "লাইট মোড")
}

enum class CurrencyPosition(val labelEn: String, val labelBn: String) {
    PREFIX("Prefix (৳ 100)", "আগে (৳ ১০০)"),
    SUFFIX("Suffix (100 ৳)", "পরে (১০০ ৳)")
}

enum class NumberFormatOption(val labelEn: String, val labelBn: String) {
    SOUTH_ASIAN("10,00,000 (Lakh/Crore)", "১০,০০,০০০ (লাখ/কোটি)"),
    STANDARD("1,000,000 (Million)", "১,০০০,০০০ (মিলিয়ন)")
}

object AppSettings {
    private const val PREFS_NAME = "hardware_store_prefs"

    // 1. Core View Mode & Language
    var viewMode by mutableStateOf(ViewMode.DETAILED)
        private set

    // 2. Preferences
    var fontSize by mutableStateOf(FontSizeOption.NORMAL)
        private set
    var themeMode by mutableStateOf(ThemeModeOption.DARK)
        private set
    var currencySymbol by mutableStateOf("৳")
        private set
    var currencyPosition by mutableStateOf(CurrencyPosition.PREFIX)
        private set
    var numberFormat by mutableStateOf(NumberFormatOption.SOUTH_ASIAN)
        private set
    var country by mutableStateOf("Bangladesh")
        private set
    var dateFormat by mutableStateOf("dd/MM/yyyy")
        private set
    var timeFormat by mutableStateOf("12h")
        private set

    // 3. Privacy Mode & Extras
    var isPrivacyModeEnabled by mutableStateOf(false)
        private set
    var hideProfitUntilTapped by mutableStateOf(true)
        private set
    var hideCustomerCredit by mutableStateOf(false)
        private set
    var limitStaffVisibility by mutableStateOf(true)
        private set
    var showPrivacyExplanationPopup by mutableStateOf(false)

    // 4. Transaction Settings
    var invoicePrefix by mutableStateOf("INV-")
        private set
    var additionalCharges by mutableDoubleStateOf(0.0)
        private set
    var isRoundOffEnabled by mutableStateOf(false)
        private set
    var saveTransactionImages by mutableStateOf(true)
        private set
    var imageCropping by mutableStateOf(true)
        private set
    var reminderMessageLanguage by mutableStateOf(AppLanguage.BANGLA)
        private set
    var showInvoicePhoneNumber by mutableStateOf(true)
        private set
    var hideInvoiceAppBranding by mutableStateOf(false)
        private set
    var defaultItemUnit by mutableStateOf("টি")
        private set
    var barcodeScannerEnabled by mutableStateOf(true)
        private set

    // 5. Invoice Settings
    var invoicePrintStyle by mutableStateOf("Thermal")
        private set
    var isA4SizeInvoice by mutableStateOf(false)
        private set
    var showInvoiceLogo by mutableStateOf(true)
        private set
    var showInvoiceBankAccount by mutableStateOf(true)
        private set
    var showInvoiceRegNumber by mutableStateOf(true)
        private set
    var showInvoicePartyBalance by mutableStateOf(true)
        private set
    var showInvoiceItemUnit by mutableStateOf(true)
        private set
    var showInvoiceNotes by mutableStateOf(true)
        private set
    var showInvoiceSignature by mutableStateOf(true)
        private set
    var invoiceTerms by mutableStateOf("Thank you for your business! / আমাদের সাথে ব্যবসা করার জন্য ধন্যবাদ!")
        private set
    var bankAccountNumber by mutableStateOf("")
        private set
    var isBankQrEnabled by mutableStateOf(false)
        private set

    // 6. Item & Party Settings
    var isWholesalePriceEnabled by mutableStateOf(false)
        private set
    var isMrpEnabled by mutableStateOf(false)
        private set
    var isItemLocationEnabled by mutableStateOf(false)
        private set
    var isPartyWiseRateEnabled by mutableStateOf(false)
        private set
    var isLowStockWarningEnabled by mutableStateOf(true)
        private set
    var isPreventOutOfStockSaleEnabled by mutableStateOf(true)
        private set
    var isQuantityDecimalsEnabled by mutableStateOf(false)
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // View Mode
        val modeStr = prefs.getString("view_mode", ViewMode.DETAILED.name)
        viewMode = try {
            ViewMode.valueOf(modeStr ?: ViewMode.DETAILED.name)
        } catch (e: Exception) {
            ViewMode.DETAILED
        }

        // Language
        val langCode = prefs.getString("selected_language", null)
        if (langCode != null) {
            val lang = if (langCode == "en") AppLanguage.ENGLISH else AppLanguage.BANGLA
            LanguageController.setLanguage(lang)
        }

        // Preferences
        val fontStr = prefs.getString("pref_font_size", FontSizeOption.NORMAL.name)
        fontSize = try { FontSizeOption.valueOf(fontStr ?: FontSizeOption.NORMAL.name) } catch (e: Exception) { FontSizeOption.NORMAL }

        val themeStr = prefs.getString("pref_theme_mode", ThemeModeOption.DARK.name)
        themeMode = try { ThemeModeOption.valueOf(themeStr ?: ThemeModeOption.DARK.name) } catch (e: Exception) { ThemeModeOption.DARK }

        currencySymbol = prefs.getString("pref_currency_symbol", "৳") ?: "৳"

        val posStr = prefs.getString("pref_currency_position", CurrencyPosition.PREFIX.name)
        currencyPosition = try { CurrencyPosition.valueOf(posStr ?: CurrencyPosition.PREFIX.name) } catch (e: Exception) { CurrencyPosition.PREFIX }

        val numStr = prefs.getString("pref_number_format", NumberFormatOption.SOUTH_ASIAN.name)
        numberFormat = try { NumberFormatOption.valueOf(numStr ?: NumberFormatOption.SOUTH_ASIAN.name) } catch (e: Exception) { NumberFormatOption.SOUTH_ASIAN }

        country = prefs.getString("pref_country", "Bangladesh") ?: "Bangladesh"
        dateFormat = prefs.getString("pref_date_format", "dd/MM/yyyy") ?: "dd/MM/yyyy"
        timeFormat = prefs.getString("pref_time_format", "12h") ?: "12h"

        // Privacy Mode
        isPrivacyModeEnabled = prefs.getBoolean("pref_privacy_mode", false)

        // Transaction Settings
        invoicePrefix = prefs.getString("pref_inv_prefix", "INV-") ?: "INV-"
        additionalCharges = prefs.getFloat("pref_add_charges", 0f).toDouble()
        isRoundOffEnabled = prefs.getBoolean("pref_round_off", false)
        val remLangCode = prefs.getString("pref_reminder_lang", "bn")
        reminderMessageLanguage = if (remLangCode == "en") AppLanguage.ENGLISH else AppLanguage.BANGLA

        // Invoice Settings
        invoicePrintStyle = prefs.getString("pref_inv_print_style", "Thermal") ?: "Thermal"
        isA4SizeInvoice = prefs.getBoolean("pref_inv_a4", false)
        showInvoiceLogo = prefs.getBoolean("pref_inv_show_logo", true)
        showInvoiceBankAccount = prefs.getBoolean("pref_inv_show_bank", true)
        showInvoiceRegNumber = prefs.getBoolean("pref_inv_show_reg", true)
        showInvoicePartyBalance = prefs.getBoolean("pref_inv_show_party_bal", true)
        showInvoiceItemUnit = prefs.getBoolean("pref_inv_show_unit", true)
        showInvoiceNotes = prefs.getBoolean("pref_inv_show_notes", true)
        showInvoiceSignature = prefs.getBoolean("pref_inv_show_sig", true)
        invoiceTerms = prefs.getString("pref_inv_terms", "Thank you for your business!") ?: "Thank you for your business!"
        bankAccountNumber = prefs.getString("pref_bank_account", "") ?: ""
        isBankQrEnabled = prefs.getBoolean("pref_bank_qr", false)

        // Item & Party Settings
        isWholesalePriceEnabled = prefs.getBoolean("pref_item_wholesale", false)
        isMrpEnabled = prefs.getBoolean("pref_item_mrp", false)
        isItemLocationEnabled = prefs.getBoolean("pref_item_location", false)
        isPartyWiseRateEnabled = prefs.getBoolean("pref_party_rate", false)
        isLowStockWarningEnabled = prefs.getBoolean("pref_low_stock_warning", true)
        isPreventOutOfStockSaleEnabled = prefs.getBoolean("pref_prevent_oos_sale", true)
        isQuantityDecimalsEnabled = prefs.getBoolean("pref_qty_decimals", false)

        // Initialize helper managers
        ProfileManager.init(context)
        PinLockManager.init(context)
        NotificationManager.init(context)
    }

    fun setViewMode(mode: ViewMode, context: Context? = null) {
        viewMode = mode
        if (context != null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString("view_mode", mode.name).apply()
        }
    }

    fun setLanguage(context: Context?, language: AppLanguage) {
        LanguageController.setLanguage(language)
        if (context != null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString("selected_language", language.code).apply()
        }
    }

    fun setFontSize(context: Context, option: FontSizeOption) {
        fontSize = option
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("pref_font_size", option.name).apply()
    }

    fun setThemeMode(context: Context, option: ThemeModeOption) {
        themeMode = option
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("pref_theme_mode", option.name).apply()
    }

    fun setCurrency(context: Context, symbol: String) {
        currencySymbol = symbol
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("pref_currency_symbol", symbol).apply()
    }

    fun setCurrencyPosition(context: Context, position: CurrencyPosition) {
        currencyPosition = position
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("pref_currency_position", position.name).apply()
    }

    fun setNumberFormat(context: Context, option: NumberFormatOption) {
        numberFormat = option
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("pref_number_format", option.name).apply()
    }

    fun setCountry(context: Context, value: String) {
        country = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("pref_country", value).apply()
    }

    fun setDateFormat(context: Context, format: String) {
        dateFormat = format
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("pref_date_format", format).apply()
    }

    fun setTimeFormat(context: Context, format: String) {
        timeFormat = format
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("pref_time_format", format).apply()
    }

    fun togglePrivacyMode(context: Context): Boolean {
        isPrivacyModeEnabled = !isPrivacyModeEnabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("pref_privacy_mode", isPrivacyModeEnabled).apply()
        return isPrivacyModeEnabled
    }

    fun setTransactionSettings(
        context: Context,
        prefix: String,
        charges: Double,
        roundOff: Boolean,
        reminderLang: AppLanguage
    ) {
        invoicePrefix = prefix.trim().ifBlank { "INV-" }
        additionalCharges = charges.coerceAtLeast(0.0)
        isRoundOffEnabled = roundOff
        reminderMessageLanguage = reminderLang

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("pref_inv_prefix", invoicePrefix)
            .putFloat("pref_add_charges", additionalCharges.toFloat())
            .putBoolean("pref_round_off", isRoundOffEnabled)
            .putString("pref_reminder_lang", reminderLang.code)
            .apply()
    }

    fun setInvoiceSettings(
        context: Context,
        printStyle: String,
        a4Size: Boolean,
        showLogo: Boolean,
        showBank: Boolean,
        showReg: Boolean,
        showPartyBal: Boolean,
        showUnit: Boolean,
        showNotes: Boolean,
        showSig: Boolean,
        terms: String,
        bankAcc: String,
        bankQr: Boolean
    ) {
        invoicePrintStyle = printStyle
        isA4SizeInvoice = a4Size
        showInvoiceLogo = showLogo
        showInvoiceBankAccount = showBank
        showInvoiceRegNumber = showReg
        showInvoicePartyBalance = showPartyBal
        showInvoiceItemUnit = showUnit
        showInvoiceNotes = showNotes
        showInvoiceSignature = showSig
        invoiceTerms = terms
        bankAccountNumber = bankAcc
        isBankQrEnabled = bankQr

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("pref_inv_print_style", printStyle)
            .putBoolean("pref_inv_a4", a4Size)
            .putBoolean("pref_inv_show_logo", showLogo)
            .putBoolean("pref_inv_show_bank", showBank)
            .putBoolean("pref_inv_show_reg", showReg)
            .putBoolean("pref_inv_show_party_bal", showPartyBal)
            .putBoolean("pref_inv_show_unit", showUnit)
            .putBoolean("pref_inv_show_notes", showNotes)
            .putBoolean("pref_inv_show_sig", showSig)
            .putString("pref_inv_terms", terms)
            .putString("pref_bank_account", bankAcc)
            .putBoolean("pref_bank_qr", bankQr)
            .apply()
    }

    fun setInvoicePrefix(context: Context, prefix: String) {
        invoicePrefix = prefix
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("pref_inv_prefix", prefix).apply()
    }

    fun setAdditionalCharges(context: Context, charges: Double) {
        additionalCharges = charges
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat("pref_add_charges", charges.toFloat()).apply()
    }

    fun setRoundOff(context: Context, enabled: Boolean) {
        isRoundOffEnabled = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("pref_round_off", enabled).apply()
    }

    fun setInvoicePrintStyle(context: Context, style: String) {
        invoicePrintStyle = style
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("pref_inv_print_style", style).apply()
    }

    fun setA4SizeInvoice(context: Context, enabled: Boolean) {
        isA4SizeInvoice = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("pref_inv_a4", enabled).apply()
    }

    fun setShowInvoiceSignature(context: Context, enabled: Boolean) {
        showInvoiceSignature = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("pref_inv_show_sig", enabled).apply()
    }

    fun setBankQrEnabled(context: Context, enabled: Boolean) {
        isBankQrEnabled = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("pref_bank_qr", enabled).apply()
    }

    fun setInvoiceTerms(context: Context, terms: String) {
        invoiceTerms = terms
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("pref_inv_terms", terms).apply()
    }

    fun setItemFieldWholesale(context: Context, enabled: Boolean) {
        isWholesalePriceEnabled = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("pref_item_wholesale", enabled).apply()
    }

    fun setItemFieldMrp(context: Context, enabled: Boolean) {
        isMrpEnabled = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("pref_item_mrp", enabled).apply()
    }

    fun setItemFieldLocation(context: Context, enabled: Boolean) {
        isItemLocationEnabled = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("pref_item_location", enabled).apply()
    }

    fun setPartyWiseRate(context: Context, enabled: Boolean) {
        isPartyWiseRateEnabled = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("pref_party_rate", enabled).apply()
    }

    fun setLowStockWarning(context: Context, enabled: Boolean) {
        isLowStockWarningEnabled = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("pref_low_stock_warning", enabled).apply()
    }

    fun setPreventOutOfStockSale(context: Context, enabled: Boolean) {
        isPreventOutOfStockSaleEnabled = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("pref_prevent_oos_sale", enabled).apply()
    }

    fun setQuantityDecimals(context: Context, enabled: Boolean) {
        isQuantityDecimalsEnabled = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("pref_qty_decimals", enabled).apply()
    }

    fun setPrivacyExtras(
        context: Context,
        hideProfit: Boolean,
        hideCredit: Boolean,
        limitStaff: Boolean
    ) {
        hideProfitUntilTapped = hideProfit
        hideCustomerCredit = hideCredit
        limitStaffVisibility = limitStaff
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("pref_hide_profit_tapped", hideProfit)
            .putBoolean("pref_hide_customer_credit", hideCredit)
            .putBoolean("pref_limit_staff_visibility", limitStaff)
            .apply()
    }

    fun setTransactionImageOptions(
        context: Context,
        saveImages: Boolean,
        cropImages: Boolean
    ) {
        saveTransactionImages = saveImages
        imageCropping = cropImages
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("pref_save_trans_images", saveImages)
            .putBoolean("pref_crop_trans_images", cropImages)
            .apply()
    }

    fun setInvoiceExtraOptions(
        context: Context,
        showPhone: Boolean,
        hideBranding: Boolean
    ) {
        showInvoicePhoneNumber = showPhone
        hideInvoiceAppBranding = hideBranding
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("pref_inv_show_phone", showPhone)
            .putBoolean("pref_inv_hide_branding", hideBranding)
            .apply()
    }

    fun resetTransactionSettings(context: Context) {
        setTransactionSettings(
            context = context,
            prefix = "INV-",
            charges = 0.0,
            roundOff = false,
            reminderLang = AppLanguage.BANGLA
        )
        setTransactionImageOptions(context, saveImages = true, cropImages = true)
    }
}
