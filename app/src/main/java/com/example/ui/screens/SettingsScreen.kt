package com.example.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.backup.BackupManager
import com.example.data.local.AppDatabase
import com.example.data.local.AppSettings
import com.example.data.local.CurrencyPosition
import com.example.data.local.FontSizeOption
import com.example.data.local.NumberFormatOption
import com.example.data.local.PinLockManager
import com.example.data.local.ProfileManager
import com.example.data.local.ThemeModeOption
import com.example.data.local.ViewMode
import com.example.data.repository.HardwareRepository
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.LanguageController
import com.example.ui.theme.DarkSlateBackground
import com.example.ui.theme.DarkSlateCardBorder
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.DarkSlateSurfaceVariant
import com.example.ui.theme.LowStockRed
import android.app.Activity
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.LinearProgressIndicator
import com.example.data.firebase.FirebaseManager
import com.example.data.firebase.FirestoreSyncManager
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VividEmeraldAccent
import com.example.ui.theme.VividEmeraldPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    onNavigateToBusinessProfile: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val scope = rememberCoroutineScope()
    val repository = remember { AppDatabase.getRepository(context) }
    val backupManager = remember { BackupManager(repository) }
    val activeProfile = ProfileManager.activeProfile

    // Dialog flags
    var showPinDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showPrivacyExplanationDialog by remember { mutableStateOf(false) }
    var showTransactionSettingsDialog by remember { mutableStateOf(false) }
    var showInvoiceSettingsDialog by remember { mutableStateOf(false) }
    var showItemSettingsDialog by remember { mutableStateOf(false) }

    // Firebase & Migration states
    val firebaseUser by FirebaseManager.currentUser.collectAsState()
    var isCloudSyncActive by remember { mutableStateOf(FirestoreSyncManager.isCloudSyncActive(context)) }
    var isMigrating by remember { mutableStateOf(false) }
    var migrationStatusText by remember { mutableStateOf("") }
    var showRollbackDialog by remember { mutableStateOf(false) }
    var isMigrationDone by remember { mutableStateOf(FirestoreSyncManager.isMigrationCompleted(context)) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Store Profile Card
            Text(
                text = if (isBangla) "দোকানের প্রোফাইল ও তথ্য" else "Store Profile",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = VividEmeraldAccent
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToBusinessProfile() }
                    .testTag("card_store_profile"),
                shape = RoundedCornerShape(14.dp),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = VividEmeraldPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Store,
                                    contentDescription = null,
                                    tint = VividEmeraldAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = activeProfile.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${activeProfile.phone.ifBlank { "017XXXXXXXX" }} • ${activeProfile.businessType}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Edit Profile",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 2. Language, Theme & Font Size (Item 109)
            Text(
                text = if (isBangla) "ভাষা ও ডিসপ্লে (Language & Display)" else "Language & Display",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = VividEmeraldAccent
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Language Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(if (isBangla) "অ্যাপের ভাষা (Language)" else "App Language", fontSize = 14.sp, color = TextPrimary)
                                Text(if (isBangla) "বর্তমানে: বাংলা" else "Currently: English", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                        Button(
                            onClick = { LanguageController.toggleLanguage() },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSlateSurfaceVariant),
                            border = BorderStroke(1.dp, VividEmeraldPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_toggle_language")
                        ) {
                            Text(if (isBangla) "English" else "বাংলা", color = VividEmeraldAccent, fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(color = DarkSlateCardBorder)

                    // Font Size Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FormatSize, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(if (isBangla) "ফন্ট সাইজ" else "Font Size", fontSize = 14.sp, color = TextPrimary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FontSizeOption.values().forEach { opt ->
                                val isSelected = AppSettings.fontSize == opt
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) VividEmeraldPrimary else DarkSlateSurfaceVariant)
                                        .clickable { AppSettings.setFontSize(context, opt) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (isBangla) opt.labelBn else opt.labelEn,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.White else TextMuted
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = DarkSlateCardBorder)

                    // Theme Mode (Dark / Light / System)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(if (isBangla) "থিম মোড" else "Theme Mode", fontSize = 14.sp, color = TextPrimary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ThemeModeOption.values().forEach { mode ->
                                val isSelected = AppSettings.themeMode == mode
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) VividEmeraldPrimary else DarkSlateSurfaceVariant)
                                        .clickable { AppSettings.setThemeMode(context, mode) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (isBangla) mode.labelBn else mode.labelEn,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color.White else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Currency, Country, Date, Time & Number Format (Items 110, 111)
            Text(
                text = if (isBangla) "মুদ্রা, সংখ্যা ও তারিখ ফরম্যাট" else "Currency, Numbers & Formats",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = VividEmeraldAccent
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Currency Choice (Taka, Rupee, Dollar)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isBangla) "মুদ্রা প্রতীক (Currency)" else "Currency Symbol", fontSize = 13.sp, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("৳" to "Taka", "₹" to "Rupee", "$" to "Dollar").forEach { (sym, label) ->
                                val isSelected = AppSettings.currencySymbol == sym
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) VividEmeraldPrimary else DarkSlateSurfaceVariant)
                                        .clickable { AppSettings.setCurrency(context, sym) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$sym $label",
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.White else TextMuted,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = DarkSlateCardBorder)

                    // Currency Position (Prefix / Suffix)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isBangla) "মুদ্রার অবস্থান" else "Currency Position", fontSize = 13.sp, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CurrencyPosition.values().forEach { pos ->
                                val isSelected = AppSettings.currencyPosition == pos
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) VividEmeraldPrimary else DarkSlateSurfaceVariant)
                                        .clickable { AppSettings.setCurrencyPosition(context, pos) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (isBangla) pos.labelBn else pos.labelEn,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color.White else TextMuted
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = DarkSlateCardBorder)

                    // Number Format (South Asian 10,00,000 vs Standard 1,000,000)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(if (isBangla) "সংখ্যা ফরম্যাট" else "Number Format", fontSize = 13.sp, color = TextPrimary)
                            Text(if (isBangla) "লাখ/কোটি নাকি মিলিয়ন" else "Lakh/Crore vs Million", fontSize = 11.sp, color = TextMuted)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            NumberFormatOption.values().forEach { numOpt ->
                                val isSelected = AppSettings.numberFormat == numOpt
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) VividEmeraldPrimary else DarkSlateSurfaceVariant)
                                        .clickable { AppSettings.setNumberFormat(context, numOpt) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (numOpt == NumberFormatOption.SOUTH_ASIAN) "10,00,000" else "1,000,000",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else TextMuted
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = DarkSlateCardBorder)

                    // Date & Time Formats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isBangla) "তারিখ ফরম্যাট" else "Date Format", fontSize = 13.sp, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("dd/MM/yyyy", "yyyy-MM-dd", "dd MMM yyyy").forEach { fmt ->
                                val isSelected = AppSettings.dateFormat == fmt
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) VividEmeraldPrimary else DarkSlateSurfaceVariant)
                                        .clickable { AppSettings.setDateFormat(context, fmt) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = fmt,
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color.White else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Privacy Mode & Privacy Extras (Items 112, 113)
            Text(
                text = if (isBangla) "গোপনীয়তা মোড (Privacy Mode)" else "Privacy Mode & Protection",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = VividEmeraldAccent
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Main Privacy Mode Toggle + Explanation Popup Trigger
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                if (AppSettings.isPrivacyModeEnabled) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = VividEmeraldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (isBangla) "গোপনীয়তা মোড চালু" else "Enable Privacy Mode", fontSize = 14.sp, color = TextPrimary)
                                    IconButton(onClick = { showPrivacyExplanationDialog = true }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.HelpOutline, contentDescription = "Explain", tint = TextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text(if (isBangla) "হোম স্ক্রিনের আয় ও ক্রয়ের দাম লুকায়" else "Hides sensitive totals & buying prices", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                        Switch(
                            checked = AppSettings.isPrivacyModeEnabled,
                            onCheckedChange = { AppSettings.togglePrivacyMode(context) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VividEmeraldPrimary),
                            modifier = Modifier.testTag("switch_privacy_mode")
                        )
                    }

                    HorizontalDivider(color = DarkSlateCardBorder)

                    // Extra: Hide profit & balances until tapped
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isBangla) "ট্যাপ না করা পর্যন্ত লাভ লুকান" else "Hide profit until tapped", fontSize = 13.sp, color = TextPrimary)
                            Text(if (isBangla) "ট্যাপ করলে সাময়িক দৃশ্যমান হবে" else "Tap to reveal temporarily", fontSize = 11.sp, color = TextMuted)
                        }
                        Switch(
                            checked = AppSettings.hideProfitUntilTapped,
                            onCheckedChange = {
                                AppSettings.setPrivacyExtras(context, it, AppSettings.hideCustomerCredit, AppSettings.limitStaffVisibility)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VividEmeraldPrimary)
                        )
                    }

                    // Extra: Hide customer credit amounts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isBangla) "কাস্টমার বাকি খাতার টাকা লুকান" else "Hide customer credit amounts", fontSize = 13.sp, color = TextPrimary)
                            Text(if (isBangla) "অন্যান্য গ্রাহকের সামনে টাকার অংক গোপন" else "Mask credit balance from bystanders", fontSize = 11.sp, color = TextMuted)
                        }
                        Switch(
                            checked = AppSettings.hideCustomerCredit,
                            onCheckedChange = {
                                AppSettings.setPrivacyExtras(context, AppSettings.hideProfitUntilTapped, it, AppSettings.limitStaffVisibility)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VividEmeraldPrimary)
                        )
                    }

                    // Extra: Limit staff visibility
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isBangla) "কর্মচারীদের দৃশ্যমানতা সীমাবদ্ধ করুন" else "Limit what staff can see", fontSize = 13.sp, color = TextPrimary)
                            Text(if (isBangla) "স্টাফ ডিভাইসে লাভ ও মূল ব্যালেন্স ব্লক" else "Hide net margins on staff phones", fontSize = 11.sp, color = TextMuted)
                        }
                        Switch(
                            checked = AppSettings.limitStaffVisibility,
                            onCheckedChange = {
                                AppSettings.setPrivacyExtras(context, AppSettings.hideProfitUntilTapped, AppSettings.hideCustomerCredit, it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VividEmeraldPrimary)
                        )
                    }
                }
            }

            // 5. App Lock & PIN Protection (Item 114)
            Text(
                text = if (isBangla) "অ্যাপ লক ও পিন নিরাপত্তা (PIN Protection)" else "PIN & App Security",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = VividEmeraldAccent
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (PinLockManager.isPinSet) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (PinLockManager.isPinSet) VividEmeraldAccent else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(if (isBangla) "সিকিউরিটি পিন" else "Security PIN", fontSize = 14.sp, color = TextPrimary)
                                Text(
                                    text = if (PinLockManager.isPinSet) (if (isBangla) "পিন লক সক্রিয়" else "PIN active") else (if (isBangla) "পিন বন্ধ" else "PIN off"),
                                    fontSize = 11.sp,
                                    color = if (PinLockManager.isPinSet) VividEmeraldAccent else TextMuted
                                )
                            }
                        }

                        Button(
                            onClick = { showPinDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                            modifier = Modifier.testTag("btn_setup_pin")
                        ) {
                            Text(if (PinLockManager.isPinSet) (if (isBangla) "পরিবর্তন" else "Change") else (if (isBangla) "পিন দিন" else "Set PIN"), fontSize = 12.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSlateSurfaceVariant.copy(alpha = 0.5f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (isBangla)
                                "পিন শুধুমাত্র স্পর্শকাতর কাজের জন্য (যেমন ডাটা মুছে ফেলা, ব্যাকআপ রিস্টোর, পারমিশন পরিবর্তন, গোপন সংখ্যা উন্মোচন)। সাধারণ অ্যাপ ওপেন করার সময় পিন চাওয়া হবে না।"
                            else
                                "PIN guards sensitive actions only (deleting data, restoring backups, permissions, revealing numbers). Not required to launch the app.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // 6. Transaction Settings, Invoice Settings, Item Settings & Categories (Items 115, 116, 117, 119, 120)
            Text(
                text = if (isBangla) "চালান, লেনদেন ও ইনভেন্টরি সেটিংস" else "Transactions, Invoices & Items",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = VividEmeraldAccent
            )

            // Transaction Settings Button Card (Item 115)
            SettingsNavCard(
                title = if (isBangla) "লেনদেন সেটিংস (Transaction Settings)" else "Transaction Settings",
                subtitle = if (isBangla) "চালান প্রিফিক্স, অতিরিক্ত চার্জ, রাউন্ড অফ ও ছবি সেভ" else "Invoice prefix, charges, round off, photo options",
                icon = Icons.Default.Receipt,
                testTag = "card_trans_settings",
                onClick = { showTransactionSettingsDialog = true }
            )

            // Invoice Settings Button Card (Items 116, 117)
            SettingsNavCard(
                title = if (isBangla) "ইনভয়েস ও প্রিন্ট সেটিংস (Invoice Settings)" else "Invoice & Print Settings",
                subtitle = if (isBangla) "প্রিন্ট স্টাইল, A4 মাপ, লোগো, স্বাক্ষর, শর্ত ও ব্যাংক QR" else "Print style, A4 size, logo, signature, QR, branding",
                icon = Icons.Default.Description,
                testTag = "card_invoice_settings",
                onClick = { showInvoiceSettingsDialog = true }
            )

            // Item Settings Button Card (Item 119)
            SettingsNavCard(
                title = if (isBangla) "পণ্য ও আইটেম সেটিংস (Item Settings)" else "Item & Inventory Settings",
                subtitle = if (isBangla) "মাপের একক, বারকোড, পাইকারি দর, MRP, লোকেশন ও স্টক ওয়ার্নিং" else "Units, barcode, wholesale, MRP, shelf location, stock alert",
                icon = Icons.Default.Inventory,
                testTag = "card_item_settings",
                onClick = { showItemSettingsDialog = true }
            )

            // Category Management Hub (Item 120)
            SettingsNavCard(
                title = if (isBangla) "ক্যাটাগরি ম্যানেজমেন্ট হাব (Manage Categories)" else "Manage All Categories",
                subtitle = if (isBangla) "পার্টি, পণ্য, খরচ ও আয়ের ক্যাটাগরি এক জায়গায়" else "Party, item, expense & income categories in one place",
                icon = Icons.Default.Category,
                testTag = "card_manage_categories",
                onClick = { onNavigateToCategories() }
            )

            // 7. Backup & Data Restore Section
            Text(
                text = if (isBangla) "ডাটা ব্যাকআপ ও ফায়ারবেস ক্লাউড" else "Backup & Firebase Cloud",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = VividEmeraldAccent
            )

            // Firebase Live Cloud & Migration Card
            Card(
                modifier = Modifier.fillMaxWidth().testTag("card_firebase_cloud"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, if (firebaseUser != null && isCloudSyncActive) VividEmeraldAccent.copy(alpha = 0.5f) else DarkSlateCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (firebaseUser != null && isCloudSyncActive) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (firebaseUser != null && isCloudSyncActive) VividEmeraldAccent else LowStockRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isBangla) "ফায়ারবেস ক্লাউড সিঙ্ক" else "Firebase Cloud Sync",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "stores/${FirestoreSyncManager.getActiveStoreId()}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (firebaseUser != null && isCloudSyncActive) VividEmeraldPrimary.copy(alpha = 0.2f) else DarkSlateSurfaceVariant
                        ) {
                            Text(
                                text = if (firebaseUser != null && isCloudSyncActive) (if (isBangla) "সংযুক্ত" else "Connected") else (if (isBangla) "অফলাইন" else "Offline"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (firebaseUser != null && isCloudSyncActive) VividEmeraldAccent else TextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = DarkSlateCardBorder)

                    // User identity row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (firebaseUser != null) (firebaseUser?.displayName ?: "Store Owner") else (if (isBangla) "গুগল অ্যাকাউন্ট সংযুক্ত নেই" else "No Google Account"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = firebaseUser?.email ?: (if (isBangla) "অফলাইন লোকাল ডাটাবেজ সচল" else "Operating in local Room mode"),
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        if (firebaseUser == null) {
                            Button(
                                onClick = {
                                    val activity = context as? Activity
                                    if (activity != null) {
                                        FirebaseManager.signInWithGoogle(
                                            activity = activity,
                                            scope = scope,
                                            onSuccess = { user ->
                                                Toast.makeText(context, if (isBangla) "লগইন সফল: ${user.email}" else "Signed in as ${user.email}", Toast.LENGTH_SHORT).show()
                                                scope.launch { FirestoreSyncManager.syncStoreProfile(context) }
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                                modifier = Modifier.testTag("btn_google_signin")
                            ) {
                                Text(if (isBangla) "গুগল সাইন ইন" else "Sign In", fontSize = 12.sp, maxLines = 1)
                            }
                        } else {
                            Button(
                                onClick = {
                                    FirebaseManager.signOut(context, scope) {
                                        Toast.makeText(context, if (isBangla) "সাইন আউট সম্পন্ন" else "Signed out", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSlateSurfaceVariant),
                                border = BorderStroke(1.dp, DarkSlateCardBorder),
                                modifier = Modifier.testTag("btn_google_signout")
                            ) {
                                Text(if (isBangla) "সাইন আউট" else "Sign Out", fontSize = 12.sp, color = TextPrimary, maxLines = 1)
                            }
                        }
                    }

                    // One-Time Migration Button & Progress
                    if (isMigrating) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = migrationStatusText.ifBlank { if (isBangla) "ডাটা মাইগ্রেশন হচ্ছে..." else "Migrating data..." },
                                fontSize = 11.sp,
                                color = VividEmeraldAccent
                            )
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = VividEmeraldPrimary,
                                trackColor = DarkSlateSurfaceVariant
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                if (firebaseUser == null) {
                                    Toast.makeText(context, if (isBangla) "প্রথমে গুগল সাইন ইন করুন" else "Please sign in with Google first", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isMigrating = true
                                scope.launch {
                                    FirestoreSyncManager.performOneTimeMigration(
                                        context = context,
                                        repository = repository,
                                        onProgress = { step, curr, total ->
                                            migrationStatusText = "[$curr/$total] $step"
                                        },
                                        onComplete = { count ->
                                            isMigrating = false
                                            isMigrationDone = true
                                            Toast.makeText(context, if (isBangla) "মাইগ্রেশন সফল! $count টি রেকর্ড ক্লাউডে সিঙ্ক হয়েছে।" else "Migration complete! $count records synced.", Toast.LENGTH_LONG).show()
                                        },
                                        onError = { err ->
                                            isMigrating = false
                                            Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp).testTag("btn_one_time_migration"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isMigrationDone) (if (isBangla) "পুনরায় ক্লাউড সিঙ্ক করুন" else "Re-sync to Firebase") else (if (isBangla) "সব ডাটা ক্লাউডে মাইগ্রেট করুন" else "Migrate All Data to Cloud"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    // Rollback Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showRollbackDialog = true },
                            modifier = Modifier.fillMaxWidth().height(38.dp).testTag("btn_rollback_local"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSlateSurfaceVariant),
                            border = BorderStroke(1.dp, DarkSlateCardBorder)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBangla) "লোকাল ডাটাবেজে রোলব্যাক" else "Rollback to Local Database",
                                fontSize = 12.sp,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }
                    }

                    // Offline & Storage Info note
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSlateSurfaceVariant.copy(alpha = 0.5f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (isBangla)
                                "অফলাইন ক্যাশ চালু: ইন্টারনেট ছাড়াও অ্যাপ চলবে এবং পুনরায় সংযুক্ত হলে ফায়ারবেস ক্লাউডে স্বয়ংক্রিয়ভাবে সিঙ্ক হবে। ছবির ফাইল ফায়ারবেস স্টোরেজে সংরক্ষিত হয়।"
                            else
                                "Offline cache active: app functions seamlessly without internet and syncs automatically when reconnected. Media files stored in Firebase Storage.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val json = backupManager.generateBackupJson(context)
                                    val shareIntent = backupManager.createGoogleDriveShareIntent(context, json)
                                    context.startActivity(shareIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_backup_drive"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isBangla) "গুগল ড্রাইভ বা ফাইল শেয়ার করুন" else "Share Backup / Google Drive", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val json = backupManager.generateBackupJson(context)
                                    backupManager.saveBackupToLocalFile(context, json)
                                    Toast.makeText(context, if (isBangla) "ডিভাইসে ব্যাকআপ সংরক্ষিত হয়েছে!" else "Backup saved to phone!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_backup_local"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSlateSurfaceVariant),
                        border = BorderStroke(1.dp, DarkSlateCardBorder)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isBangla) "মোবাইলে ব্যাকআপ ফাইল সেভ করুন" else "Save Backup Locally", color = TextPrimary, fontSize = 13.sp)
                    }
                }
            }

            // 8. Danger Zone Section (PIN Protected Reset)
            Text(
                text = if (isBangla) "বিপদজনক জোন (Danger Zone)" else "Danger Zone",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = LowStockRed
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, LowStockRed.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (isBangla) "সকল ডাটা রিসেট করুন" else "Reset All Data", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LowStockRed)
                        Text(if (isBangla) "সকল পণ্য, বিক্রয় এবং বাকি হিসাব মুছে যাবে" else "Permanently erase products and orders", fontSize = 11.sp, color = TextMuted)
                    }

                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = LowStockRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_reset_all_data")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBangla) "রিসেট" else "Reset")
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // Dialog: Privacy Mode Explanation Popup (Item 112)
    if (showPrivacyExplanationDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyExplanationDialog = false },
            containerColor = DarkSlateSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = VividEmeraldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBangla) "গোপনীয়তা মোড কী এবং কেন?" else "What is Privacy Mode?",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBangla)
                            "দোকানে যখন কাস্টমার বা আগন্তুকরা আপনার ফোন স্ক্রিন দেখতে পান, তখন তাদের সামনে ব্যবসার মূল লাভ, মোট মূলধন এবং মালামালের ক্রয়মূল্য আড়াল রাখতে গোপনীয়তা মোড ডিজাইন করা হয়েছে।"
                        else
                            "Privacy mode hides confidential totals, net profit margins, and supplier purchase prices so customers standing across the counter cannot see sensitive numbers.",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (isBangla)
                            "• হোম স্ক্রিনের দৈনিক লাভ ও রাজস্ব সংখ্যা '***' দিয়ে মাস্ক করা হয়।\n• বিক্রয় স্ক্রিনে পণ্যের ক্রয়মূল্য লুকিয়ে রাখা হয়।\n• নির্দিষ্ট সংখ্যায় ট্যাপ করলে সাময়িকভাবে কয়েক সেকেন্ডের জন্য দৃশ্যমান হবে।"
                        else
                            "• Home screen stats are masked with '***'.\n• Stock buying price is hidden.\n• Tap any masked amount to reveal it momentarily.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyExplanationDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
                ) {
                    Text(if (isBangla) "বুঝেছি" else "Got It")
                }
            }
        )
    }

    // Dialog: Transaction Settings (Item 115)
    if (showTransactionSettingsDialog) {
        TransactionSettingsDialog(
            isBangla = isBangla,
            onDismiss = { showTransactionSettingsDialog = false },
            onSave = { prefix, charges, roundOff, lang, saveImg, cropImg ->
                AppSettings.setTransactionSettings(context, prefix, charges, roundOff, lang)
                AppSettings.setTransactionImageOptions(context, saveImg, cropImg)
                showTransactionSettingsDialog = false
                Toast.makeText(context, if (isBangla) "লেনদেন সেটিংস সংরক্ষিত হয়েছে!" else "Transaction settings saved!", Toast.LENGTH_SHORT).show()
            },
            onReset = {
                AppSettings.resetTransactionSettings(context)
                showTransactionSettingsDialog = false
                Toast.makeText(context, if (isBangla) "ডিফল্ট লেনদেন সেটিংস রিসেট হয়েছে" else "Transaction settings reset to default", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Invoice Settings (Items 116, 117)
    if (showInvoiceSettingsDialog) {
        InvoiceSettingsDialog(
            isBangla = isBangla,
            onDismiss = { showInvoiceSettingsDialog = false },
            onSave = { printStyle, isA4, showLogo, showBank, showReg, showPartyBal, showUnit, showNotes, showSig, terms, bankAcc, bankQr, showPhone, hideBranding ->
                AppSettings.setInvoiceSettings(
                    context, printStyle, isA4, showLogo, showBank, showReg,
                    showPartyBal, showUnit, showNotes, showSig, terms, bankAcc, bankQr
                )
                AppSettings.setInvoiceExtraOptions(context, showPhone, hideBranding)
                showInvoiceSettingsDialog = false
                Toast.makeText(context, if (isBangla) "ইনভয়েস সেটিংস সংরক্ষিত হয়েছে!" else "Invoice settings saved!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Item Settings (Item 119)
    if (showItemSettingsDialog) {
        ItemSettingsDialog(
            isBangla = isBangla,
            onDismiss = { showItemSettingsDialog = false },
            onSave = { wholesale, mrp, location, partyRate, lowStock, preventOos, qtyDecimals ->
                AppSettings.setItemFieldWholesale(context, wholesale)
                AppSettings.setItemFieldMrp(context, mrp)
                AppSettings.setItemFieldLocation(context, location)
                AppSettings.setPartyWiseRate(context, partyRate)
                AppSettings.setLowStockWarning(context, lowStock)
                AppSettings.setPreventOutOfStockSale(context, preventOos)
                AppSettings.setQuantityDecimals(context, qtyDecimals)
                showItemSettingsDialog = false
                Toast.makeText(context, if (isBangla) "পণ্য সেটিংস সংরক্ষিত হয়েছে!" else "Item settings saved!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // PIN Dialog
    if (showPinDialog) {
        SetupPinDialog(
            isBangla = isBangla,
            onDismiss = { showPinDialog = false },
            onSetPin = { newPin ->
                PinLockManager.setPin(newPin)
                showPinDialog = false
                Toast.makeText(context, if (isBangla) "পিন সফলভাবে সেট হয়েছে!" else "PIN set successfully!", Toast.LENGTH_SHORT).show()
            },
            onRemovePin = {
                PinLockManager.removePin()
                showPinDialog = false
                Toast.makeText(context, if (isBangla) "পিন অপসারণ করা হয়েছে!" else "PIN removed!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Rollback to Local Confirmation Dialog
    if (showRollbackDialog) {
        AlertDialog(
            onDismissRequest = { showRollbackDialog = false },
            containerColor = DarkSlateSurface,
            title = {
                Text(
                    text = if (isBangla) "লোকাল ডাটাবেজে রোলব্যাক করতে চান?" else "Rollback to Local Database?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = if (isBangla)
                        "আপনার ফোনে সংরক্ষিত রুম SQLite ডাটাবেজ অক্ষত রয়েছে। রোলব্যাক করলে অ্যাপ সম্পূর্ণ অফলাইন লোকাল মোডে চলবে এবং কোনো ডাটা নষ্ট হবে না।"
                    else
                        "Your local Room SQLite database on the phone is 100% intact. Rolling back runs the app in local mode without losing any records.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        FirestoreSyncManager.rollbackToLocal(context)
                        isCloudSyncActive = false
                        showRollbackDialog = false
                        Toast.makeText(context, if (isBangla) "লোকাল মোডে রোলব্যাক সম্পন্ন হয়েছে" else "Rolled back to local mode", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
                ) {
                    Text(if (isBangla) "রোলব্যাক নিশ্চিত করুন" else "Confirm Rollback", maxLines = 1)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRollbackDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted, maxLines = 1)
                }
            }
        )
    }

    // Reset Data Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = DarkSlateSurface,
            title = {
                Text(
                    text = if (isBangla) "আপনি কি নিশ্চিত?" else "Reset All Data?",
                    color = LowStockRed,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isBangla)
                        "দোকানের সকল পণ্য, বিক্রয় ইতিহাস, কাস্টমার এবং মহাজনের বাকি ডাটাবেজ থেকে সম্পূর্ণ মুছে যাবে। এটি আর ফিরিয়ে আনা যাবে না।"
                    else
                        "All products, sales orders, customers, and supplier dues will be permanently deleted.",
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.clearAllData()
                            showResetDialog = false
                            Toast.makeText(context, if (isBangla) "সকল ডাটা মুছে ফেলা হয়েছে!" else "All data reset!", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LowStockRed),
                    modifier = Modifier.testTag("btn_confirm_reset")
                ) {
                    Text(if (isBangla) "হ্যাঁ, মুছে ফেলুন" else "Yes, Reset All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
fun SettingsNavCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
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
                Surface(
                    shape = CircleShape,
                    color = VividEmeraldPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
                }
            }

            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Dialog: Transaction Settings (Item 115)
@Composable
fun TransactionSettingsDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (prefix: String, charges: Double, roundOff: Boolean, lang: AppLanguage, saveImages: Boolean, cropImages: Boolean) -> Unit,
    onReset: () -> Unit
) {
    var prefix by remember { mutableStateOf(AppSettings.invoicePrefix) }
    var chargesText by remember { mutableStateOf(AppSettings.additionalCharges.toString()) }
    var isRoundOff by remember { mutableStateOf(AppSettings.isRoundOffEnabled) }
    var saveImages by remember { mutableStateOf(AppSettings.saveTransactionImages) }
    var cropImages by remember { mutableStateOf(AppSettings.imageCropping) }
    var selectedLang by remember { mutableStateOf(AppSettings.reminderMessageLanguage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "লেনদেন সেটিংস" else "Transaction Settings",
                fontSize = 18.sp,
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
                OutlinedTextField(
                    value = prefix,
                    onValueChange = { prefix = it },
                    label = { Text(if (isBangla) "চালান প্রিফিক্স (যেমন: INV-)" else "Invoice Prefix") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_invoice_prefix"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = chargesText,
                    onValueChange = { chargesText = it },
                    label = { Text(if (isBangla) "অতিরিক্ত চার্জ / ডেলিভারি ফি (৳)" else "Additional Charges (৳)") },
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isBangla) "স্বয়ংক্রিয় রাউন্ড অফ (Round Off)" else "Round Off Decimals", fontSize = 13.sp, color = TextPrimary)
                    Switch(
                        checked = isRoundOff,
                        onCheckedChange = { isRoundOff = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VividEmeraldPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isBangla) "চালানের ছবি সংরক্ষণ (Save Images)" else "Save Transaction Images", fontSize = 13.sp, color = TextPrimary)
                    Switch(
                        checked = saveImages,
                        onCheckedChange = { saveImages = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VividEmeraldPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isBangla) "ছবি ক্রপিং সুবিধা (Image Cropping)" else "Enable Image Cropping", fontSize = 13.sp, color = TextPrimary)
                    Switch(
                        checked = cropImages,
                        onCheckedChange = { cropImages = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VividEmeraldPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isBangla) "তাগাদা বার্তার ভাষা" else "Reminder Message Language", fontSize = 13.sp, color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(AppLanguage.BANGLA to "বাংলা", AppLanguage.ENGLISH to "English").forEach { (lang, label) ->
                            val isSel = selectedLang == lang
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSel) VividEmeraldPrimary else DarkSlateSurfaceVariant)
                                    .clickable { selectedLang = lang }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(label, fontSize = 11.sp, color = if (isSel) Color.White else TextMuted)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onReset,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSlateSurfaceVariant)
                ) {
                    Text(if (isBangla) "রিসেট" else "Reset", color = LowStockRed)
                }
                Button(
                    onClick = {
                        val ch = chargesText.toDoubleOrNull() ?: 0.0
                        onSave(prefix.trim().ifBlank { "INV-" }, ch, isRoundOff, selectedLang, saveImages, cropImages)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                    modifier = Modifier.testTag("btn_save_transaction_settings")
                ) {
                    Text(if (isBangla) "সংরক্ষণ" else "Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}

// Dialog: Invoice Settings (Items 116, 117)
@Composable
fun InvoiceSettingsDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        printStyle: String, isA4: Boolean, showLogo: Boolean, showBank: Boolean,
        showReg: Boolean, showPartyBal: Boolean, showUnit: Boolean, showNotes: Boolean,
        showSig: Boolean, terms: String, bankAcc: String, bankQr: Boolean,
        showPhone: Boolean, hideBranding: Boolean
    ) -> Unit
) {
    var printStyle by remember { mutableStateOf(AppSettings.invoicePrintStyle) }
    var isA4 by remember { mutableStateOf(AppSettings.isA4SizeInvoice) }
    var showLogo by remember { mutableStateOf(AppSettings.showInvoiceLogo) }
    var showBank by remember { mutableStateOf(AppSettings.showInvoiceBankAccount) }
    var showReg by remember { mutableStateOf(AppSettings.showInvoiceRegNumber) }
    var showPartyBal by remember { mutableStateOf(AppSettings.showInvoicePartyBalance) }
    var showUnit by remember { mutableStateOf(AppSettings.showInvoiceItemUnit) }
    var showNotes by remember { mutableStateOf(AppSettings.showInvoiceNotes) }
    var showSig by remember { mutableStateOf(AppSettings.showInvoiceSignature) }
    var terms by remember { mutableStateOf(AppSettings.invoiceTerms) }
    var bankAcc by remember { mutableStateOf(AppSettings.bankAccountNumber) }
    var bankQr by remember { mutableStateOf(AppSettings.isBankQrEnabled) }
    var showPhone by remember { mutableStateOf(AppSettings.showInvoicePhoneNumber) }
    var hideBranding by remember { mutableStateOf(AppSettings.hideInvoiceAppBranding) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "ইনভয়েস ও প্রিন্ট সেটিংস" else "Invoice & Print Settings",
                fontSize = 18.sp,
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
                // Thermal vs A4 paper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isBangla) "A4 সাইজ পেপার প্রিন্ট" else "A4 Size Paper Invoice", fontSize = 13.sp, color = TextPrimary)
                    Switch(
                        checked = isA4,
                        onCheckedChange = { isA4 = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VividEmeraldPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isBangla) "ব্যাংক পেমেন্ট QR প্রদর্শন" else "Show Bank Payment QR", fontSize = 13.sp, color = TextPrimary)
                    Switch(
                        checked = bankQr,
                        onCheckedChange = { bankQr = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VividEmeraldPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isBangla) "ডিজিটাল স্বাক্ষর প্রদর্শন" else "Show Signature Line", fontSize = 13.sp, color = TextPrimary)
                    Switch(
                        checked = showSig,
                        onCheckedChange = { showSig = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VividEmeraldPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isBangla) "অ্যাপ ব্র্যান্ডিং লুকান" else "Hide App Branding", fontSize = 13.sp, color = TextPrimary)
                    Switch(
                        checked = hideBranding,
                        onCheckedChange = { hideBranding = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VividEmeraldPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isBangla) "পার্টি পূর্ববর্তী বাকি ব্যালেন্স" else "Show Party Balance", fontSize = 13.sp, color = TextPrimary)
                    Switch(
                        checked = showPartyBal,
                        onCheckedChange = { showPartyBal = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VividEmeraldPrimary)
                    )
                }

                OutlinedTextField(
                    value = bankAcc,
                    onValueChange = { bankAcc = it },
                    label = { Text(if (isBangla) "চালানে প্রদর্শিত ব্যাংক/বিকাশ নং" else "Bank / bKash for Invoice") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = terms,
                    onValueChange = { terms = it },
                    label = { Text(if (isBangla) "ইনভয়েসের শর্তাবলী ও ধন্যবাদ বার্তা" else "Terms & Conditions") },
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
                    onSave(printStyle, isA4, showLogo, showBank, showReg, showPartyBal, showUnit, showNotes, showSig, terms, bankAcc, bankQr, showPhone, hideBranding)
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_save_invoice_settings")
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

// Dialog: Item Settings (Item 119)
@Composable
fun ItemSettingsDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (wholesale: Boolean, mrp: Boolean, location: Boolean, partyRate: Boolean, lowStock: Boolean, preventOos: Boolean, qtyDecimals: Boolean) -> Unit
) {
    var wholesale by remember { mutableStateOf(AppSettings.isWholesalePriceEnabled) }
    var mrp by remember { mutableStateOf(AppSettings.isMrpEnabled) }
    var location by remember { mutableStateOf(AppSettings.isItemLocationEnabled) }
    var partyRate by remember { mutableStateOf(AppSettings.isPartyWiseRateEnabled) }
    var lowStock by remember { mutableStateOf(AppSettings.isLowStockWarningEnabled) }
    var preventOos by remember { mutableStateOf(AppSettings.isPreventOutOfStockSaleEnabled) }
    var qtyDecimals by remember { mutableStateOf(AppSettings.isQuantityDecimalsEnabled) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "পণ্য ও আইটেম সেটিংস" else "Item & Inventory Settings",
                fontSize = 18.sp,
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isBangla) "পাইকারি দর (Wholesale Price)" else "Wholesale Price Field", fontSize = 13.sp, color = TextPrimary)
                    Switch(checked = wholesale, onCheckedChange = { wholesale = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VividEmeraldPrimary))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isBangla) "সর্বোচ্চ খুচরা মূল্য (MRP)" else "Maximum Retail Price (MRP)", fontSize = 13.sp, color = TextPrimary)
                    Switch(checked = mrp, onCheckedChange = { mrp = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VividEmeraldPrimary))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isBangla) "দোকানে পণ্য রাখার স্থান (Location)" else "Item Location / Shelf", fontSize = 13.sp, color = TextPrimary)
                    Switch(checked = location, onCheckedChange = { location = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VividEmeraldPrimary))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isBangla) "পার্টি ভিত্তিক স্পেশাল দর" else "Party-wise Special Rate", fontSize = 13.sp, color = TextPrimary)
                    Switch(checked = partyRate, onCheckedChange = { partyRate = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VividEmeraldPrimary))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isBangla) "কম স্টকের সতর্কতা (Low Stock Alert)" else "Low Stock Warning", fontSize = 13.sp, color = TextPrimary)
                    Switch(checked = lowStock, onCheckedChange = { lowStock = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VividEmeraldPrimary))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isBangla) "স্টক শূন্য হলে বিক্রি বন্ধ রাখুন" else "Prevent Out-of-Stock Sale", fontSize = 13.sp, color = TextPrimary)
                    Switch(checked = preventOos, onCheckedChange = { preventOos = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VividEmeraldPrimary))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isBangla) "দশমিক পরিমাণ (Quantity Decimals)" else "Quantity Decimals (e.g. 1.5 kg)", fontSize = 13.sp, color = TextPrimary)
                    Switch(checked = qtyDecimals, onCheckedChange = { qtyDecimals = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = VividEmeraldPrimary))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(wholesale, mrp, location, partyRate, lowStock, preventOos, qtyDecimals)
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_save_item_settings")
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

@Composable
fun SetupPinDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSetPin: (String) -> Unit,
    onRemovePin: () -> Unit
) {
    var pin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "সিকিউরিটি পিন সেট করুন" else "Security PIN Setup",
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
                    text = if (isBangla) "৪ থেকে ৬ ডিজিটের গোপন পিন নম্বর লিখুন:" else "Enter a 4-6 digit security PIN:",
                    fontSize = 13.sp,
                    color = TextMuted
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    label = { Text(if (isBangla) "পিন কোড" else "PIN Code") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_pin_code"),
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
                    if (pin.length in 4..6) {
                        onSetPin(pin)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_confirm_pin")
            ) {
                Text(if (isBangla) "পিন সংরক্ষণ করুন" else "Save PIN")
            }
        },
        dismissButton = {
            Row {
                if (PinLockManager.isPinSet) {
                    TextButton(onClick = onRemovePin) {
                        Text(if (isBangla) "পিন বন্ধ করুন" else "Remove PIN", color = LowStockRed)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
                }
            }
        }
    )
}
