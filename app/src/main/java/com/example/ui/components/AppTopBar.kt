package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.data.local.NotificationManager
import com.example.ui.theme.LowStockRed
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.AppTypographyScale
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.local.AppSettings
import com.example.data.local.ViewMode
import com.example.ui.theme.DarkSlateBackground
import com.example.ui.theme.DarkSlateCardBorder
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.DarkSlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VividEmeraldAccent
import com.example.ui.theme.VividEmeraldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentViewMode = AppSettings.viewMode
    val isSimple = currentViewMode == ViewMode.SIMPLE
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier.testTag("app_top_bar"),
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!canNavigateBack) {
                    // Crossed tools emblem matching reference image
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(DarkSlateSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Handyman,
                            contentDescription = "Hardware Icon",
                            tint = VividEmeraldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = title,
                    style = AppTypographyScale.screenTitle.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        },
        actions = {
            // View Mode Toggle Button with active state badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkSlateSurfaceVariant,
                border = BorderStroke(1.dp, if (isSimple) VividEmeraldAccent.copy(alpha = 0.6f) else DarkSlateCardBorder),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        val nextMode = if (isSimple) ViewMode.DETAILED else ViewMode.SIMPLE
                        AppSettings.setViewMode(nextMode, context)
                        val msg = if (nextMode == ViewMode.SIMPLE) {
                            if (currentLanguage == AppLanguage.BANGLA) "সহজ ভিউ চালু হয়েছে" else "Switched to Simple view"
                        } else {
                            if (currentLanguage == AppLanguage.BANGLA) "বিস্তারিত ভিউ চালু হয়েছে" else "Switched to Detailed view"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                    .testTag("top_bar_view_mode_toggle")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSimple) VividEmeraldPrimary else Color.Transparent
                    ) {
                        Text(
                            text = if (currentLanguage == AppLanguage.BANGLA) "সহজ" else "Simple",
                            style = AppTypographyScale.hintText.copy(
                                fontWeight = if (isSimple) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            ),
                            color = if (isSimple) Color.White else TextMuted,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (!isSimple) VividEmeraldPrimary else Color.Transparent
                    ) {
                        Text(
                            text = if (currentLanguage == AppLanguage.BANGLA) "বিস্তারিত" else "Detail",
                            style = AppTypographyScale.hintText.copy(
                                fontWeight = if (!isSimple) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            ),
                            color = if (!isSimple) Color.White else TextMuted,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // "বাংলা | EN" button matching reference image
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkSlateSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        val nextLang = if (currentLanguage == AppLanguage.BANGLA) AppLanguage.ENGLISH else AppLanguage.BANGLA
                        onLanguageChange(nextLang)
                    }
                    .testTag("quick_lang_badge")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "বাং",
                        style = AppTypographyScale.hintText.copy(
                            fontWeight = if (currentLanguage == AppLanguage.BANGLA) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        ),
                        color = if (currentLanguage == AppLanguage.BANGLA) VividEmeraldAccent else TextMuted
                    )
                    Text(
                        text = "|",
                        style = AppTypographyScale.hintText.copy(fontSize = 11.sp),
                        color = TextMuted
                    )
                    Text(
                        text = "EN",
                        style = AppTypographyScale.hintText.copy(
                            fontWeight = if (currentLanguage == AppLanguage.ENGLISH) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        ),
                        color = if (currentLanguage == AppLanguage.ENGLISH) VividEmeraldAccent else TextMuted
                    )
                }
            }

            if (!com.example.data.local.StaffSessionManager.isStaffSession) {
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("settings_button").size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextMuted,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            // Privacy Mode toggle button
            val isPrivacy = AppSettings.isPrivacyModeEnabled
            IconButton(
                onClick = { showPrivacyDialog = true },
                modifier = Modifier.size(34.dp).testTag("top_bar_privacy_toggle")
            ) {
                Icon(
                    imageVector = if (isPrivacy) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Privacy Mode",
                    tint = if (isPrivacy) VividEmeraldAccent else TextMuted,
                    modifier = Modifier.size(19.dp)
                )
            }

            // Notifications Bell button with unread badge
            val unreadCount = NotificationManager.unreadCount
            IconButton(
                onClick = { showNotificationsSheet = true },
                modifier = Modifier.size(34.dp).testTag("top_bar_notifications_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = if (unreadCount > 0) VividEmeraldAccent else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(LowStockRed)
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkSlateBackground
        )
    )

    if (showNotificationsSheet) {
        NotificationsBottomSheet(
            isBangla = currentLanguage == AppLanguage.BANGLA,
            onDismiss = { showNotificationsSheet = false }
        )
    }

    if (showPrivacyDialog) {
        PrivacyModeExplanationDialog(
            isBangla = currentLanguage == AppLanguage.BANGLA,
            onDismiss = { showPrivacyDialog = false },
            onToggle = {}
        )
    }
}

@Composable
fun LanguageSettingsDialog(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = AppStrings.settings(currentLanguage),
                style = AppTypographyScale.screenTitle,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = AppStrings.selectLanguage(currentLanguage),
                    style = AppTypographyScale.sectionHeading,
                    color = TextMuted
                )

                // Language options
                AppLanguage.values().forEach { lang ->
                    val isSelected = currentLanguage == lang
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onLanguageSelected(lang)
                            }
                            .testTag("lang_option_${lang.code}"),
                        color = if (isSelected) DarkSlateSurfaceVariant else DarkSlateBackground,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = lang.nativeName,
                                    style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Medium),
                                    color = if (isSelected) VividEmeraldAccent else TextPrimary
                                )
                                Text(
                                    text = lang.displayName,
                                    style = AppTypographyScale.hintText,
                                    color = TextMuted
                                )
                            }
                            Icon(
                                imageVector = if (isSelected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSelected) VividEmeraldAccent else DarkSlateCardBorder,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // App status hints
                Surface(
                    color = DarkSlateBackground,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = AppStrings.currentStoreMode(currentLanguage),
                            style = AppTypographyScale.hintText,
                            color = TextMuted
                        )
                        Text(
                            text = AppStrings.activeCurrency(currentLanguage),
                            style = AppTypographyScale.hintText,
                            color = VividEmeraldAccent
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VividEmeraldPrimary
                ),
                modifier = Modifier
                    .height(44.dp)
                    .testTag("dialog_close_button")
            ) {
                Text(
                    text = AppStrings.close(currentLanguage),
                    style = AppTypographyScale.buttonText,
                    color = TextPrimary
                )
            }
        }
    )
}

@Composable
fun PrivacyModeExplanationDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onToggle: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "গোপনীয়তা মোড" else "Privacy Mode",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = if (isBangla)
                    "গোপনীয়তা মোড চালু থাকলে স্ক্রিনে সংবেদনশীল আর্থিক তথ্য (মোট লাভ, মোট নগদ ও বিক্রয়) সাময়িকভাবে লুকানো থাকবে।"
                else
                    "When Privacy Mode is enabled, sensitive financial figures like profit, net balance, and sales totals are hidden from customer view.",
                color = TextMuted
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("privacy_dialog_ok")
            ) {
                Text(if (isBangla) "ঠিক আছে" else "Got It")
            }
        }
    )
}
