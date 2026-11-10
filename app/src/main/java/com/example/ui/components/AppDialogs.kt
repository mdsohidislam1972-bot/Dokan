package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BusinessProfile
import com.example.data.local.NotificationManager
import com.example.data.local.NotificationType
import com.example.data.local.PinLockManager
import com.example.data.local.ProfileManager
import com.example.ui.theme.*

@Composable
fun EditProfileDialog(
    profile: BusinessProfile,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (BusinessProfile) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var phone by remember { mutableStateOf(profile.phone) }
    var address by remember { mutableStateOf(profile.address) }
    var registrationNumber by remember { mutableStateOf(profile.registrationNumber) }
    var bankAccount by remember { mutableStateOf(profile.bankAccount) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "দোকানের প্রোফাইল সম্পাদনা" else "Edit Store Profile",
                style = AppTypographyScale.sectionHeading,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isBangla) "দোকান / ভেন্ডরের নাম" else "Store / Vendor Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = VividEmeraldAccent,
                        unfocusedBorderColor = DarkSlateCardBorder
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isBangla) "মোবাইল নম্বর" else "Contact Phone") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("profile_phone_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = VividEmeraldAccent,
                        unfocusedBorderColor = DarkSlateCardBorder
                    )
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(if (isBangla) "দোকানের ঠিকানা" else "Store Location / Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_address_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = VividEmeraldAccent,
                        unfocusedBorderColor = DarkSlateCardBorder
                    )
                )

                OutlinedTextField(
                    value = registrationNumber,
                    onValueChange = { registrationNumber = it },
                    label = { Text(if (isBangla) "ট্রেড লাইসেন্স / ভ্যাট রেজিঃ" else "Trade License / Tax ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_reg_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = VividEmeraldAccent,
                        unfocusedBorderColor = DarkSlateCardBorder
                    )
                )

                OutlinedTextField(
                    value = bankAccount,
                    onValueChange = { bankAccount = it },
                    label = { Text(if (isBangla) "ব্যাংক / বিকাশ মার্চেন্ট নম্বর" else "Bank / bKash Account") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_bank_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = VividEmeraldAccent,
                        unfocusedBorderColor = DarkSlateCardBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        profile.copy(
                            name = name.ifBlank { profile.name },
                            phone = phone,
                            address = address,
                            registrationNumber = registrationNumber,
                            bankAccount = bankAccount
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text(if (isBangla) "সংরক্ষণ করুন" else "Save Changes", color = TextPrimary)
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
fun PinVerifyDialog(
    title: String,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onPinSuccess: () -> Unit
) {
    var pinText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = title,
                style = AppTypographyScale.sectionHeading,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isBangla) "আপনার ৪-৬ সংখ্যার সিকিউরিটি পিন কোড দিন" else "Enter your 4-6 digit security PIN",
                    style = AppTypographyScale.normalText,
                    color = TextMuted
                )

                OutlinedTextField(
                    value = pinText,
                    onValueChange = {
                        if (it.length <= 6) {
                            pinText = it
                            isError = false
                        }
                    },
                    isError = isError,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("pin_verify_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = VividEmeraldAccent,
                        unfocusedBorderColor = DarkSlateCardBorder
                    )
                )

                if (isError) {
                    Text(
                        text = if (isBangla) "ভুল পিন কোড! পুনরায় চেষ্টা করুন।" else "Incorrect PIN code! Please try again.",
                        color = LowStockRed,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (PinLockManager.verifyPin(pinText)) {
                        onPinSuccess()
                    } else {
                        isError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("submit_pin_button")
            ) {
                Text(if (isBangla) "যাচাই করুন" else "Verify", color = TextPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSwitcherBottomSheet(
    isBangla: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val profiles = ProfileManager.profiles
    val activeProfile = ProfileManager.activeProfile
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSlateSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) "দোকান ও শাখা পরিবর্তন" else "Switch Store / Vendor Shop",
                    style = AppTypographyScale.sectionHeading,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Text(
                text = if (isBangla) "আপনার সক্রিয় দোকান অথবা ভেন্ডর নির্বাচন করুন:" else "Select your active store branch or vendor:",
                style = AppTypographyScale.hintText,
                color = TextMuted
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(profiles) { profile ->
                    val isActive = profile.id == activeProfile?.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                ProfileManager.switchProfile(context, profile.id)
                                onDismiss()
                            }
                            .testTag("store_profile_${profile.id}"),
                        color = if (isActive) DarkSlateSurfaceVariant else DarkSlateBackground,
                        shape = RoundedCornerShape(12.dp),
                        border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, VividEmeraldAccent) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isActive) VividEmeraldPrimary else DarkSlateCardBorder),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = TextPrimary
                                    )
                                }
                                Column {
                                    Text(
                                        text = profile.name,
                                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                                        color = if (isActive) VividEmeraldAccent else TextPrimary
                                    )
                                    Text(
                                        text = "${profile.businessType} • ${profile.phone.ifBlank { "No phone" }}",
                                        style = AppTypographyScale.hintText,
                                        color = TextMuted
                                    )
                                }
                            }

                            if (isActive) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = VividEmeraldAccent
                                )
                            }
                        }
                    }
                }
            }

            var showCreateDialog by remember { mutableStateOf(false) }
            var newName by remember { mutableStateOf("") }
            var newPhone by remember { mutableStateOf("") }
            var newType by remember { mutableStateOf("") }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_add_store_profile")
            ) {
                Icon(Icons.Default.AddBusiness, contentDescription = null, tint = TextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBangla) "নতুন দোকান বা প্রোফাইল যোগ করুন" else "Add New Store Profile",
                    color = TextPrimary,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold
                )
            }

            if (showCreateDialog) {
                AlertDialog(
                    onDismissRequest = { showCreateDialog = false },
                    containerColor = DarkSlateSurface,
                    title = {
                        Text(
                            text = if (isBangla) "নতুন ব্যবসার প্রোফাইল" else "Add Store Profile",
                            style = AppTypographyScale.sectionHeading,
                            color = TextPrimary
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                label = { Text(if (isBangla) "দোকানের নাম" else "Store Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("new_profile_name")
                            )
                            OutlinedTextField(
                                value = newPhone,
                                onValueChange = { newPhone = it },
                                label = { Text(if (isBangla) "মোবাইল নম্বর" else "Phone Number") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("new_profile_phone")
                            )
                            OutlinedTextField(
                                value = newType,
                                onValueChange = { newType = it },
                                label = { Text(if (isBangla) "ব্যবসার ধরণ" else "Business Type") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("new_profile_type")
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    ProfileManager.createProfile(
                                        context = context,
                                        name = newName,
                                        phone = newPhone,
                                        address = "",
                                        businessType = newType.ifBlank { "Retail Store" }
                                    )
                                    showCreateDialog = false
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                            modifier = Modifier.testTag("btn_confirm_add_profile")
                        ) {
                            Text(if (isBangla) "সংরক্ষণ করুন" else "Save Profile", maxLines = 1)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCreateDialog = false }) {
                            Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted, maxLines = 1)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsBottomSheet(
    isBangla: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val notifications = NotificationManager.notifications
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSlateSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = VividEmeraldAccent)
                    Text(
                        text = if (isBangla) "বিজ্ঞপ্তি ও সতর্কতা" else "Notifications & Alerts",
                        style = AppTypographyScale.sectionHeading,
                        color = TextPrimary
                    )
                }

                if (notifications.isNotEmpty()) {
                    TextButton(onClick = { NotificationManager.markAllAsRead(context) }) {
                        Text(
                            text = if (isBangla) "সব পঠিত" else "Mark all read",
                            fontSize = 12.sp,
                            color = VividEmeraldAccent
                        )
                    }
                }
            }

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBangla) "কোনো নতুন বিজ্ঞপ্তি নেই" else "No new notifications",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notifications) { notif ->
                        val isOverdue = notif.type == NotificationType.OVERDUE_PAYMENT
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    NotificationManager.markAsRead(context, notif.id)
                                },
                            color = if (notif.isRead) DarkSlateBackground else DarkSlateSurfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isOverdue) LowStockRed.copy(alpha = 0.2f) else VividEmeraldPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isOverdue) Icons.Default.Warning else Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = if (isOverdue) LowStockRed else VividEmeraldAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isBangla) notif.titleBn else notif.titleEn,
                                        style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isBangla) notif.messageBn else notif.messageEn,
                                        style = AppTypographyScale.hintText,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
