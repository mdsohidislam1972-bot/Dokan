package com.example.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProfileManager
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.DarkSlateBackground
import com.example.ui.theme.DarkSlateCardBorder
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.DarkSlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VividEmeraldPrimary

val BUSINESS_TYPES_LIST = listOf(
    "Hardware & Tools / হার্ডওয়্যার",
    "Grocery & Super Shop / মুদি দোকান",
    "Electronics & Electrical / ইলেকট্রনিক্স",
    "Pharmacy & Health / ফার্মেসি",
    "Sanitary & Plumbing / সেনিটারি",
    "Paint & Hardware / রং ও হার্ডওয়্যার",
    "General Store / জেনারেল স্টোর"
)

@Composable
fun FirstRunSetupScreen(
    currentLanguage: AppLanguage,
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA

    var storeName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(BUSINESS_TYPES_LIST[0]) }
    var isTypeMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Icon & Hero Header
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = VividEmeraldPrimary.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, VividEmeraldPrimary)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = "Store Logo",
                        tint = VividEmeraldPrimary,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Text(
                text = if (isBangla) "স্বাগতম দোকান-এ!" else "Welcome to Dokan!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = if (isBangla)
                    "আপনার দোকানের নাম ও তথ্য দিয়ে খুব সহজেই শুরু করুন।"
                else
                    "Set up your store profile to begin fast billing and stock tracking.",
                fontSize = 14.sp,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Input Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Store Name
                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text(if (isBangla) "দোকানের নাম *" else "Store Name *") },
                        placeholder = { Text(if (isBangla) "যেমনঃ ভাই ভাই হার্ডওয়্যার" else "e.g. Bhai Bhai Hardware") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Store, contentDescription = null, tint = VividEmeraldPrimary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_store_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSlateSurfaceVariant,
                            unfocusedContainerColor = DarkSlateSurfaceVariant,
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Phone Number
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(if (isBangla) "মোবাইল নম্বর" else "Phone Number") },
                        placeholder = { Text("017XXXXXXXX") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = TextMuted)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_store_phone"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSlateSurfaceVariant,
                            unfocusedContainerColor = DarkSlateSurfaceVariant,
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Address
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(if (isBangla) "দোকানের ঠিকানা" else "Store Address") },
                        placeholder = { Text(if (isBangla) "যেমনঃ নিউ মার্কেট, ঢাকা" else "e.g. New Market, Dhaka") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_store_address"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSlateSurfaceVariant,
                            unfocusedContainerColor = DarkSlateSurfaceVariant,
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Business Type Selector
                    Column {
                        Text(
                            text = if (isBangla) "ব্যবসার ধরণ" else "Business Category",
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSlateSurfaceVariant)
                                .border(1.dp, DarkSlateCardBorder, RoundedCornerShape(8.dp))
                                .clickable { isTypeMenuExpanded = true }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Business,
                                        contentDescription = null,
                                        tint = VividEmeraldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = selectedType,
                                        color = TextPrimary,
                                        fontSize = 14.sp
                                    )
                                }
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = TextMuted
                                )
                            }

                            DropdownMenu(
                                expanded = isTypeMenuExpanded,
                                onDismissRequest = { isTypeMenuExpanded = false }
                            ) {
                                BUSINESS_TYPES_LIST.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            selectedType = type
                                            isTypeMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Get Started Button
            Button(
                onClick = {
                    val finalName = storeName.trim().ifBlank {
                        if (isBangla) "আমার দোকান" else "My Dokan"
                    }
                    ProfileManager.completeFirstRunSetup(
                        context = context,
                        storeName = finalName,
                        phone = phone.trim(),
                        address = address.trim(),
                        businessType = selectedType
                    )
                    onSetupComplete()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_complete_setup"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBangla) "দোকান চালু করুন" else "Start Using Dokan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
