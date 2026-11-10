package com.example.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.local.AppSettings
import java.text.NumberFormat
import java.util.Locale

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String
) {
    ENGLISH("en", "English", "English"),
    BANGLA("bn", "Bangla", "বাংলা")
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.BANGLA }

object LanguageController {
    var currentLanguage: AppLanguage by mutableStateOf(AppLanguage.BANGLA)
        private set

    fun setLanguage(language: AppLanguage) {
        currentLanguage = language
    }

    fun toggleLanguage() {
        val next = if (currentLanguage == AppLanguage.BANGLA) AppLanguage.ENGLISH else AppLanguage.BANGLA
        setLanguage(next)
    }
}

@Composable
fun ProvideAppLanguage(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppLanguage provides LanguageController.currentLanguage) {
        content()
    }
}

fun toBengaliNumerals(input: Any): String {
    val str = input.toString()
    val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val sb = StringBuilder()
    for (ch in str) {
        if (ch in '0'..'9') {
            sb.append(bengaliDigits[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

fun formatTaka(
    amount: Double,
    language: AppLanguage = LanguageController.currentLanguage,
    isMaskable: Boolean = false
): String {
    if (isMaskable && AppSettings.isPrivacyModeEnabled) {
        return "৳ •••••"
    }

    val isBangla = language == AppLanguage.BANGLA
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
        maximumFractionDigits = 2
    }
    val formattedNumber = formatter.format(amount)

    return if (isBangla) {
        "৳ ${toBengaliNumerals(formattedNumber)}"
    } else {
        "৳ $formattedNumber"
    }
}

object AppStrings {
    fun settings(language: AppLanguage): String =
        if (language == AppLanguage.BANGLA) "সেটিংস ও ডাটা" else "Settings & Preferences"

    fun selectLanguage(language: AppLanguage): String =
        if (language == AppLanguage.BANGLA) "অ্যাপের ভাষা নির্বাচন করুন" else "Select Application Language"

    fun currentStoreMode(language: AppLanguage): String =
        if (language == AppLanguage.BANGLA) "দোকানের মোড: মাল্টি-ভেন্ডর মার্কেটপ্লেস" else "Store Mode: Multi-Vendor Marketplace"

    fun activeCurrency(language: AppLanguage): String =
        if (language == AppLanguage.BANGLA) "মুদ্রা: বাংলাদেশী টাকা (৳ BDT)" else "Currency: Bangladeshi Taka (৳ BDT)"

    fun close(language: AppLanguage): String =
        if (language == AppLanguage.BANGLA) "বন্ধ করুন" else "Close"
}
