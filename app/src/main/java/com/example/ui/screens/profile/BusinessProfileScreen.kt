package com.example.ui.screens.profile

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.PinLockManager
import com.example.data.local.ProfileManager
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.DarkSlateBackground
import com.example.ui.theme.DarkSlateCardBorder
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.DarkSlateSurfaceVariant
import com.example.ui.theme.LowStockRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VividEmeraldAccent
import com.example.ui.theme.VividEmeraldPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val scope = rememberCoroutineScope()
    val activeProfile = ProfileManager.activeProfile

    // Profile state fields
    var storeName by remember { mutableStateOf(activeProfile.name) }
    var ownerName by remember { mutableStateOf(activeProfile.ownerName) }
    var phone by remember { mutableStateOf(activeProfile.phone) }
    var email by remember { mutableStateOf(activeProfile.email) }
    var selectedCategory by remember { mutableStateOf(activeProfile.category) }
    var selectedBusinessType by remember { mutableStateOf(activeProfile.businessType) }
    var division by remember { mutableStateOf(activeProfile.division) }
    var district by remember { mutableStateOf(activeProfile.district) }
    var streetAddress by remember { mutableStateOf(activeProfile.streetAddress.ifBlank { activeProfile.address }) }
    var regNumber by remember { mutableStateOf(activeProfile.registrationNumber) }
    var bankAccount by remember { mutableStateOf(activeProfile.bankAccount) }
    var isGoogleLinked by remember { mutableStateOf(activeProfile.googleAccountLinked) }
    var googleEmail by remember { mutableStateOf(activeProfile.googleEmail.ifBlank { "user@gmail.com" }) }

    // Dropdown expanded states
    var categoryExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var divisionExpanded by remember { mutableStateOf(false) }

    // Danger area dialogs
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showPinPromptDialog by remember { mutableStateOf(false) }
    var pinEntered by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val businessCategories = listOf(
        "Hardware & Sanitary",
        "Electronics & Electric",
        "Grocery & Super Shop",
        "Pharmacy & Medicine",
        "Paints & Tiles",
        "Stationary & Books",
        "Fashion & Clothing",
        "Agriculture & Seeds",
        "Motorcycle & Auto Parts",
        "Other Commercial Service"
    )

    val businessTypes = listOf(
        "Retailer" to if (isBangla) "খুচরা বিক্রেতা (Retailer)" else "Retailer",
        "Wholesaler" to if (isBangla) "পাইকারি বিক্রেতা (Wholesaler)" else "Wholesaler",
        "Distributor" to if (isBangla) "ডিস্ট্রিবিউটর (Distributor)" else "Distributor",
        "Manufacturer" to if (isBangla) "উৎপাদনকারী (Manufacturer)" else "Manufacturer",
        "Service" to if (isBangla) "সেবামূলক প্রতিষ্ঠান (Service Provider)" else "Service"
    )

    val bangladeshDivisions = listOf(
        "Dhaka", "Chittagong", "Rajshahi", "Khulna", "Barisal", "Sylhet", "Rangpur", "Mymensingh"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Banner / Logo Placeholder
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
            border = BorderStroke(1.dp, DarkSlateCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(VividEmeraldPrimary.copy(alpha = 0.2f))
                        .clickable {
                            Toast.makeText(context, if (isBangla) "লোগো নির্বাচন সম্পন্ন" else "Logo selected", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Upload Logo",
                        tint = VividEmeraldAccent,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = storeName.ifBlank { "Dokan Business" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "$selectedCategory • $selectedBusinessType",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        // Section 1: My Account & Google Account Link (Item 99)
        Text(
            text = if (isBangla) "আমার অ্যাকাউন্ট ও প্রোফাইল" else "My Account & Profile",
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
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text(if (isBangla) "মালিকের নাম *" else "Owner Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_owner_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isBangla) "মোবাইল নম্বর *" else "Mobile Number *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_profile_phone"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(if (isBangla) "ইমেইল অ্যাড্রেস" else "Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_profile_email"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Google Account Linking Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSlateSurfaceVariant)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = if (isGoogleLinked) VividEmeraldAccent else TextMuted
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isBangla) "গুগল অ্যাকাউন্ট সংযোগ" else "Link Google Account",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isGoogleLinked) googleEmail else (if (isBangla) "ক্লাউড ব্যাকআপ ও নিরাপত্তার জন্য" else "For Firebase cloud sync & restore"),
                                fontSize = 11.sp,
                                color = if (isGoogleLinked) VividEmeraldAccent else TextMuted
                            )
                        }
                    }

                    Button(
                        onClick = {
                            isGoogleLinked = !isGoogleLinked
                            Toast.makeText(
                                context,
                                if (isGoogleLinked)
                                    (if (isBangla) "গুগল অ্যাকাউন্ট সংযুক্ত হয়েছে!" else "Google account linked!")
                                else
                                    (if (isBangla) "গুগল অ্যাকাউন্ট সংযোগ বিচ্ছিন্ন করা হয়েছে" else "Google account unlinked"),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isGoogleLinked) DarkSlateCardBorder else VividEmeraldPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isGoogleLinked) (if (isBangla) "সংযুক্ত ✓" else "Linked ✓") else (if (isBangla) "সংযোগ করুন" else "Link"),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Section 2: Business Profile, Category & Type (Item 100)
        Text(
            text = if (isBangla) "দোকানের সাধারণ তথ্য ও ধরন" else "Business Profile & Category",
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
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text(if (isBangla) "প্রতিষ্ঠানের নাম *" else "Business / Store Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_profile_store_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBangla) "ব্যবসার ক্যাটাগরি (Category)" else "Business Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.background(DarkSlateSurface)
                    ) {
                        businessCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = TextPrimary) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Type Dropdown (Retailer, Wholesaler, Distributor, Manufacturer, Service)
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = businessTypes.find { it.first == selectedBusinessType }?.second ?: selectedBusinessType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBangla) "ব্যবসার ধরন (Type)" else "Business Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                        modifier = Modifier.background(DarkSlateSurface)
                    ) {
                        businessTypes.forEach { (typeKey, typeLabel) ->
                            DropdownMenuItem(
                                text = { Text(typeLabel, color = TextPrimary) },
                                onClick = {
                                    selectedBusinessType = typeKey
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Detailed Address (Item 101)
        Text(
            text = if (isBangla) "ঠিকানা ও অবস্থান" else "Store Location & Address",
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
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Division selector
                    ExposedDropdownMenuBox(
                        expanded = divisionExpanded,
                        onExpandedChange = { divisionExpanded = !divisionExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = division,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isBangla) "বিভাগ" else "Division") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = divisionExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VividEmeraldPrimary,
                                unfocusedBorderColor = DarkSlateCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = divisionExpanded,
                            onDismissRequest = { divisionExpanded = false },
                            modifier = Modifier.background(DarkSlateSurface)
                        ) {
                            bangladeshDivisions.forEach { div ->
                                DropdownMenuItem(
                                    text = { Text(div, color = TextPrimary) },
                                    onClick = {
                                        division = div
                                        divisionExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text(if (isBangla) "জেলা" else "District") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                OutlinedTextField(
                    value = streetAddress,
                    onValueChange = { streetAddress = it },
                    label = { Text(if (isBangla) "রাস্তা / বাজার / এলাকার ঠিকানা" else "Street Address / Market") },
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
        }

        // Section 4: Financial Information (Item 102)
        Text(
            text = if (isBangla) "আর্থিক ও রেজিস্ট্রেশন তথ্য" else "Financial & Registration Info",
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
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = regNumber,
                    onValueChange = { regNumber = it },
                    label = { Text(if (isBangla) "ট্রেড লাইসেন্স বা ভ্যাট/টিআইএন নম্বর" else "Trade License / Tax Reg No.") },
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
                    value = bankAccount,
                    onValueChange = { bankAccount = it },
                    label = { Text(if (isBangla) "প্রধান ব্যাংক অ্যাকাউন্ট বা বিকাশ নম্বর" else "Primary Bank Account / bKash") },
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
        }

        // Save Changes Button
        Button(
            onClick = {
                val updated = activeProfile.copy(
                    name = storeName.trim().ifBlank { "My Hardware Store" },
                    ownerName = ownerName.trim(),
                    phone = phone.trim(),
                    email = email.trim(),
                    category = selectedCategory,
                    businessType = selectedBusinessType,
                    division = division,
                    district = district.trim(),
                    streetAddress = streetAddress.trim(),
                    address = "$streetAddress, $district, $division".trim(),
                    registrationNumber = regNumber.trim(),
                    bankAccount = bankAccount.trim(),
                    googleAccountLinked = isGoogleLinked,
                    googleEmail = googleEmail
                )
                ProfileManager.updateProfile(context, updated)
                Toast.makeText(context, if (isBangla) "প্রোফাইল তথ্য সংরক্ষিত হয়েছে!" else "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_save_business_profile")
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isBangla) "প্রোফাইল পরিবর্তন সংরক্ষণ করুন" else "Save Profile Changes", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Section 5: DANGER AREA (PIN Protected) (Item 103)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
            border = BorderStroke(1.dp, LowStockRed.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Danger Area",
                        tint = LowStockRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBangla) "বিপজ্জনক জোন (Danger Area - PIN সুরক্ষিত)" else "Danger Area (PIN Protected)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LowStockRed
                    )
                }

                Text(
                    text = if (isBangla)
                        "দোকানের প্রোফাইল সম্পূর্ণ মুছে ফেলা বা প্রাথমিক রিসেট করার মতো ধ্বংসাত্মক কাজ এখানে সংরক্ষিত। এই কাজ করার জন্য সিকিউরিটি পিন প্রয়োজন।"
                    else
                        "Destructive actions such as deleting this store profile or resetting database records are strictly guarded by your security PIN.",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Button(
                    onClick = {
                        if (PinLockManager.isPinSet) {
                            showPinPromptDialog = true
                        } else {
                            showDeleteConfirmDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LowStockRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_danger_delete_business")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBangla) "এই দোকান প্রোফাইল মুছে ফেলুন" else "Delete This Business Profile")
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    // PIN Prompt Dialog for Danger Action
    if (showPinPromptDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinPromptDialog = false
                pinEntered = ""
                pinError = false
            },
            containerColor = DarkSlateSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = LowStockRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBangla) "সিকিউরিটি পিন নিশ্চিত করুন" else "Verify Security PIN",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBangla) "ধ্বংসাত্মক কাজের জন্য আপনার সিকিউরিটি পিন লিখুন:" else "Enter your PIN to perform this destructive action:",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    OutlinedTextField(
                        value = pinEntered,
                        onValueChange = {
                            pinEntered = it
                            pinError = false
                        },
                        isError = pinError,
                        label = { Text("PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_danger_pin"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    if (pinError) {
                        Text(
                            text = if (isBangla) "ভুল পিন কোড!" else "Incorrect PIN code!",
                            color = LowStockRed,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (PinLockManager.verifyPin(pinEntered)) {
                            showPinPromptDialog = false
                            showDeleteConfirmDialog = true
                            pinEntered = ""
                        } else {
                            pinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LowStockRed)
                ) {
                    Text(if (isBangla) "যাচাই করুন" else "Verify")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinPromptDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
                }
            }
        )
    }

    // Confirmation Dialog to Delete Business Profile
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = DarkSlateSurface,
            title = {
                Text(
                    text = if (isBangla) "দোকান মুছে ফেলা নিশ্চিত করুন?" else "Confirm Delete Business?",
                    color = LowStockRed,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isBangla)
                        "আপনি কি নিশ্চিত যে '${activeProfile.name}' প্রোফাইলটি মুছে ফেলতে চান? যদি একাধিক প্রোফাইল থাকে তবে এটি মুছে যাবে।"
                    else
                        "Are you sure you want to delete '${activeProfile.name}'?",
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val deleted = ProfileManager.deleteProfile(context, activeProfile.id)
                        showDeleteConfirmDialog = false
                        if (deleted) {
                            Toast.makeText(context, if (isBangla) "প্রোফাইল মুছে ফেলা হয়েছে!" else "Business profile deleted!", Toast.LENGTH_LONG).show()
                            onBack()
                        } else {
                            Toast.makeText(context, if (isBangla) "একমাত্র প্রোফাইল মুছে ফেলা যাবে না।" else "Cannot delete the only existing profile.", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LowStockRed)
                ) {
                    Text(if (isBangla) "হ্যাঁ, মুছে ফেলুন" else "Yes, Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
                }
            }
        )
    }
}
