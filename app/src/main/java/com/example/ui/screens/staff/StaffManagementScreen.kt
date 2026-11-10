package com.example.ui.screens.staff

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.ProfileManager
import com.example.data.local.StaffSessionManager
import com.example.data.local.entity.StaffMember
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StaffManagementScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val scope = rememberCoroutineScope()
    val repository = remember { AppDatabase.getRepository(context) }
    val staffList by repository.allStaffMembers.collectAsState(initial = emptyList())

    var showAddStaffDialog by remember { mutableStateOf(false) }
    var editingStaffPermissions by remember { mutableStateOf<StaffMember?>(null) }
    var qrStaffTarget by remember { mutableStateOf<StaffMember?>(null) }
    var showJoinStaffDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Multi-Phone Live Sync & QR Invite Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                border = BorderStroke(1.dp, VividEmeraldAccent.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(VividEmeraldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = VividEmeraldAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isBangla) "টিম ও মাল্টি-ফোন সিঙ্ক" else "Team & Multi-Phone Sync",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (isBangla) "দোকানের কর্মী তালিকা ও ৫ মিনিটের এককালীন QR সিঙ্ক" else "Staff list & 5-minute one-time QR sync",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = {
                                    val target = staffList.firstOrNull()
                                    if (target != null) {
                                        qrStaffTarget = target
                                    } else {
                                        Toast.makeText(
                                            context,
                                            if (isBangla) "প্রথমে একজন কর্মী যোগ করুন" else "Please add a staff member first",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_open_qr_sync")
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = if (isBangla) "সিঙ্ক QR" else "Sync QR", fontSize = 12.sp, maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = { showJoinStaffDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                border = BorderStroke(1.dp, VividEmeraldAccent.copy(alpha = 0.6f)),
                                modifier = Modifier.testTag("btn_join_as_staff")
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = if (isBangla) "যুক্ত হোন" else "Join", fontSize = 12.sp, color = VividEmeraldAccent, maxLines = 1)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSlateSurfaceVariant.copy(alpha = 0.6f))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBangla)
                                    "স্টাফ অ্যাকাউন্ট কখনই অ্যাডমিন নয়। আপনি যেকোনো সময় এক ক্লিকে কর্মী পজ (Pause) বা রিমুভ করতে পারেন।"
                                else
                                    "Staff accounts are never admin. You can pause or remove any staff member anytime with immediate effect.",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Staff List Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) "টিম সদস্য (${staffList.size})" else "Team Members (${staffList.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VividEmeraldAccent
                )
                Text(
                    text = if (isBangla) "আনলিমিটেড কর্মী যোগ সম্ভব" else "Unlimited staff allowed",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            if (staffList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isBangla) "কোনো কর্মচারী যোগ করা হয়নি" else "No staff members added yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBangla)
                                "পার্টনার, ম্যানেজার, হিসাবরক্ষক, সেলস বা স্টক কর্মী যোগ করুন এবং ৫ মিনিটের QR দিয়ে কানেক্ট করুন।"
                            else
                                "Add Partner, Manager, Accountant, Sales, Stock, or Entry staff and connect using 5-minute QR codes.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(staffList, key = { it.id }) { staff ->
                        StaffCard(
                            staff = staff,
                            isBangla = isBangla,
                            onManagePermissions = { editingStaffPermissions = staff },
                            onTogglePause = {
                                scope.launch {
                                    val updated = staff.copy(isActive = !staff.isActive)
                                    repository.updateStaff(updated)
                                    val msg = if (updated.isActive) {
                                        if (isBangla) "${staff.name} এখন সক্রিয়" else "${staff.name} resumed"
                                    } else {
                                        if (isBangla) "${staff.name} সাময়িক স্থগিত (Paused)" else "${staff.name} paused"
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            onShowQr = { qrStaffTarget = staff },
                            onDelete = {
                                scope.launch {
                                    repository.deleteStaff(staff.id)
                                    Toast.makeText(
                                        context,
                                        if (isBangla) "স্টাফ সদস্য মুছে ফেলা হয়েছে" else "Staff removed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Add Staff FAB
        FloatingActionButton(
            onClick = { showAddStaffDialog = true },
            containerColor = VividEmeraldPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_staff")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBangla) "নতুন কর্মচারী যোগ" else "Add Staff",
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }

    // Dialog: Add Staff
    if (showAddStaffDialog) {
        AddStaffDialog(
            isBangla = isBangla,
            onDismiss = { showAddStaffDialog = false },
            onSave = { name, phone, role, permissionLevel, allowedFeatures ->
                scope.launch {
                    val newStaff = StaffMember(
                        name = name,
                        phone = phone,
                        role = role,
                        permissionLevel = permissionLevel,
                        allowedFeatures = allowedFeatures,
                        isActive = true
                    )
                    repository.insertStaff(newStaff)
                    showAddStaffDialog = false
                    Toast.makeText(
                        context,
                        if (isBangla) "কর্মচারী সফলভাবে যোগ করা হয়েছে!" else "Staff member added!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    // Dialog: Manage Permissions
    editingStaffPermissions?.let { staff ->
        ManagePermissionsDialog(
            staff = staff,
            isBangla = isBangla,
            onDismiss = { editingStaffPermissions = null },
            onSave = { newLevel, newAllowedFeatures ->
                scope.launch {
                    val updated = staff.copy(
                        permissionLevel = newLevel,
                        allowedFeatures = newAllowedFeatures
                    )
                    repository.updateStaff(updated)
                    editingStaffPermissions = null
                    Toast.makeText(
                        context,
                        if (isBangla) "পারমিশন আপডেট সফল হয়েছে" else "Permissions updated",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    // Dialog: One-Time Expiring QR Code (5 Minutes)
    qrStaffTarget?.let { staff ->
        ExpiringQrSyncModalDialog(
            staff = staff,
            isBangla = isBangla,
            onDismiss = { qrStaffTarget = null }
        )
    }

    // Dialog: Join Store as Staff (for second phone)
    if (showJoinStaffDialog) {
        JoinStaffModalDialog(
            isBangla = isBangla,
            onDismiss = { showJoinStaffDialog = false },
            onJoinSuccess = {
                showJoinStaffDialog = false
                Toast.makeText(
                    context,
                    if (isBangla) "দোকানে সফলভাবে যুক্ত হয়েছেন!" else "Joined store successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                onBack()
            }
        )
    }
}

@Composable
fun StaffCard(
    staff: StaffMember,
    isBangla: Boolean,
    onManagePermissions: () -> Unit,
    onTogglePause: () -> Unit,
    onShowQr: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("staff_card_${staff.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        border = BorderStroke(
            1.dp,
            if (staff.isActive) DarkSlateCardBorder else LowStockRed.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (staff.isActive) DarkSlateSurfaceVariant else LowStockRed.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = staff.name.take(1).uppercase(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (staff.isActive) VividEmeraldAccent else LowStockRed
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = staff.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(VividEmeraldPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = staff.role,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VividEmeraldAccent
                                )
                            }
                            if (!staff.isActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(LowStockRed.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isBangla) "স্থগিত (Paused)" else "Paused",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LowStockRed
                                    )
                                }
                            }
                        }
                        if (staff.phone.isNotBlank()) {
                            Text(
                                text = "Phone: ${staff.phone}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onShowQr, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = VividEmeraldAccent, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DarkSlateCardBorder)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${if (isBangla) "অ্যাক্সেস লেভেল:" else "Access:"} ${staff.permissionLevel}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VividEmeraldAccent
                    )
                    Text(
                        text = "${if (isBangla) "অনুমোদিত:" else "Allowed:"} ${staff.allowedFeatures}",
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onTogglePause,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, if (staff.isActive) LowStockRed.copy(alpha = 0.6f) else VividEmeraldAccent)
                    ) {
                        Text(
                            text = if (staff.isActive) (if (isBangla) "পজ করুন" else "Pause") else (if (isBangla) "চালু করুন" else "Resume"),
                            fontSize = 11.sp,
                            color = if (staff.isActive) LowStockRed else VividEmeraldAccent,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = onManagePermissions,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSlateSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_manage_permissions_${staff.id}")
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isBangla) "পারমিশন" else "Permissions", fontSize = 11.sp, color = TextPrimary, maxLines = 1)
                    }
                }
            }
        }
    }
}

// Add Staff Dialog
@Composable
fun AddStaffDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, role: String, permissionLevel: String, allowedFeatures: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Sales") }
    var selectedLevel by remember { mutableStateOf("Limited") }

    val roles = listOf("Partner", "Manager", "Accountant", "Sales", "Stock", "Entry")
    val levels = listOf("Full", "Half", "Limited", "Selected")

    val allFeatureOptions = listOf(
        "sales" to if (isBangla) "বিক্রয় ও চালান (Sales)" else "Sales",
        "stock" to if (isBangla) "পণ্য স্টক (Stock)" else "Stock",
        "baki" to if (isBangla) "বাকি খাতা (Baki)" else "Customer Baki",
        "purchases" to if (isBangla) "মহাজন ও ক্রয় (Purchases)" else "Purchases",
        "expenses" to if (isBangla) "দোকান খরচ (Expenses)" else "Expenses",
        "reports" to if (isBangla) "রিপোর্ট (Reports)" else "Reports"
    )

    var activeFeatures by remember { mutableStateOf(setOf("sales", "stock")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "নতুন কর্মচারী যোগ করুন" else "Add Staff Member",
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
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isBangla) "কর্মচারীর নাম *" else "Staff Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_staff_name"),
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
                    label = { Text(if (isBangla) "মোবাইল নম্বর" else "Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_staff_phone"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Role Selector: Partner, Manager, Accountant, Sales, Stock, Entry
                Text(
                    text = if (isBangla) "পদবী / ভূমিকা (Role):" else "Role in Shop:",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    roles.take(3).forEach { role ->
                        val isSelected = selectedRole == role
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) VividEmeraldPrimary else DarkSlateSurfaceVariant)
                                .clickable {
                                    selectedRole = role
                                    when (role) {
                                        "Partner", "Manager" -> {
                                            selectedLevel = "Full"
                                            activeFeatures = setOf("sales", "stock", "baki", "purchases", "expenses", "reports")
                                        }
                                        "Accountant" -> {
                                            selectedLevel = "Half"
                                            activeFeatures = setOf("sales", "baki", "purchases", "expenses", "reports")
                                        }
                                    }
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = role,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextMuted,
                                maxLines = 1
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    roles.drop(3).forEach { role ->
                        val isSelected = selectedRole == role
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) VividEmeraldPrimary else DarkSlateSurfaceVariant)
                                .clickable {
                                    selectedRole = role
                                    when (role) {
                                        "Sales" -> {
                                            selectedLevel = "Limited"
                                            activeFeatures = setOf("sales", "stock")
                                        }
                                        "Stock" -> {
                                            selectedLevel = "Limited"
                                            activeFeatures = setOf("stock")
                                        }
                                        "Entry" -> {
                                            selectedLevel = "Limited"
                                            activeFeatures = setOf("sales", "stock")
                                        }
                                    }
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = role,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextMuted,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Initial Permission Level: Full, Half, Limited, Selected
                Text(
                    text = if (isBangla) "পারমিশন স্তর (Permission Level):" else "Permission Level:",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    levels.forEach { level ->
                        val isSelected = selectedLevel == level
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) VividEmeraldAccent.copy(alpha = 0.3f) else DarkSlateSurfaceVariant)
                                .clickable {
                                    selectedLevel = level
                                    when (level) {
                                        "Full" -> activeFeatures = setOf("sales", "stock", "baki", "purchases", "expenses", "reports")
                                        "Half" -> activeFeatures = setOf("sales", "stock", "baki")
                                        "Limited" -> activeFeatures = setOf("sales", "stock")
                                    }
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = level,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) VividEmeraldAccent else TextMuted,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Tickable Feature List
                Text(
                    text = if (isBangla) "অনুমোদিত ফিচার (Tickable Features):" else "Allowed Features:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                allFeatureOptions.forEach { (featKey, featLabel) ->
                    val isChecked = activeFeatures.contains(featKey)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedLevel = "Selected"
                                activeFeatures = if (isChecked) {
                                    activeFeatures.toMutableSet().apply { remove(featKey) }
                                } else {
                                    activeFeatures.toMutableSet().apply { add(featKey) }
                                }
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checkState ->
                                selectedLevel = "Selected"
                                activeFeatures = if (checkState) {
                                    activeFeatures.toMutableSet().apply { add(featKey) }
                                } else {
                                    activeFeatures.toMutableSet().apply { remove(featKey) }
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = VividEmeraldPrimary,
                                uncheckedColor = TextMuted
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = featLabel,
                            fontSize = 13.sp,
                            color = if (isChecked) TextPrimary else TextMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSlateSurfaceVariant)
                        .padding(8.dp)
                ) {
                    Text(
                        text = if (isBangla)
                            "স্টাফ অ্যাকাউন্ট কখনই অ্যাডমিন হবে না। সেটিংস, পিন পরিবর্তন বা ডাটা ডিলিটের কোনো সুযোগ নেই।"
                        else
                            "Staff accounts are never admin. Admin features are completely hidden and locked.",
                        fontSize = 10.sp,
                        color = VividEmeraldAccent
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val finalFeatures = activeFeatures.joinToString(",")
                        onSave(name.trim(), phone.trim(), selectedRole, selectedLevel, finalFeatures)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_save_staff")
            ) {
                Text(if (isBangla) "সংরক্ষণ করুন" else "Save Staff", maxLines = 1)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted, maxLines = 1)
            }
        }
    )
}

// Manage Permissions Dialog
@Composable
fun ManagePermissionsDialog(
    staff: StaffMember,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (level: String, allowedFeatures: String) -> Unit
) {
    var selectedLevel by remember { mutableStateOf(staff.permissionLevel) }
    val allFeatureOptions = listOf(
        "sales" to if (isBangla) "বিক্রয় ও চালান (Sales)" else "Sales",
        "stock" to if (isBangla) "পণ্য স্টক (Stock)" else "Stock",
        "baki" to if (isBangla) "বাকি খাতা (Baki)" else "Customer Baki",
        "purchases" to if (isBangla) "মহাজন ও ক্রয় (Purchases)" else "Purchases",
        "expenses" to if (isBangla) "দোকান খরচ (Expenses)" else "Expenses",
        "reports" to if (isBangla) "রিপোর্ট (Reports)" else "Reports"
    )

    val currentSelected = remember(staff.allowedFeatures) {
        staff.allowedFeatures.split(",").map { it.trim().lowercase() }.filter { it.isNotBlank() }.toMutableSet()
    }
    var activeFeatures by remember { mutableStateOf(currentSelected) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = "${staff.name} - ${if (isBangla) "পারমিশন পরিচালনা" else "Manage Permissions"}",
                fontSize = 17.sp,
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSlateSurfaceVariant)
                        .padding(8.dp)
                ) {
                    Text(
                        text = if (isBangla)
                            "স্টাফ একাউন্ট 'Full' পারমিশন থাকলেও কখনই সিস্টেম অ্যাডমিন হতে পারবে না। স্পর্শকাতর ডাটা ডিলিট বা পিন পরিবর্তনের ক্ষমতা শুধুমাত্র প্রধান অ্যাডমিনের।"
                        else
                            "Staff accounts are never admin even with Full access. Only the primary admin can delete store data or change PIN.",
                        fontSize = 11.sp,
                        color = VividEmeraldAccent
                    )
                }

                // Level Radio Selector
                Text(
                    text = if (isBangla) "পারমিশন স্তর নির্বাচন করুন:" else "Select Permission Level:",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Full", "Half", "Limited", "Selected").forEach { lvl ->
                        val isSelected = selectedLevel == lvl
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) VividEmeraldPrimary else DarkSlateSurfaceVariant)
                                .clickable {
                                    selectedLevel = lvl
                                    when (lvl) {
                                        "Full" -> activeFeatures = mutableSetOf("sales", "stock", "baki", "purchases", "expenses", "reports")
                                        "Half" -> activeFeatures = mutableSetOf("sales", "stock", "baki")
                                        "Limited" -> activeFeatures = mutableSetOf("sales", "stock")
                                    }
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lvl,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextMuted,
                                maxLines = 1
                            )
                        }
                    }
                }

                HorizontalDivider(color = DarkSlateCardBorder)

                // Feature Tickable Grid
                Text(
                    text = if (isBangla) "ফিচার ভিত্তিক অ্যাক্সেস গ্রিড:" else "Tickable Feature Grid:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                allFeatureOptions.forEach { (featKey, featLabel) ->
                    val isChecked = activeFeatures.contains(featKey)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedLevel = "Selected"
                                activeFeatures = if (isChecked) {
                                    activeFeatures.toMutableSet().apply { remove(featKey) }
                                } else {
                                    activeFeatures.toMutableSet().apply { add(featKey) }
                                }
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checkState ->
                                selectedLevel = "Selected"
                                activeFeatures = if (checkState) {
                                    activeFeatures.toMutableSet().apply { add(featKey) }
                                } else {
                                    activeFeatures.toMutableSet().apply { remove(featKey) }
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = VividEmeraldPrimary,
                                uncheckedColor = TextMuted
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = featLabel,
                            fontSize = 13.sp,
                            color = if (isChecked) TextPrimary else TextMuted
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(selectedLevel, activeFeatures.joinToString(","))
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_confirm_save_permissions")
            ) {
                Text(if (isBangla) "সংরক্ষণ করুন" else "Save Permissions", maxLines = 1)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted, maxLines = 1)
            }
        }
    )
}

// Expiring 5-Minute QR Sync Modal Dialog (Owner's phone)
@Composable
fun ExpiringQrSyncModalDialog(
    staff: StaffMember,
    isBangla: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val activeProfile = ProfileManager.activeProfile
    val storeId = activeProfile.id

    val qrToken = remember(staff.id) {
        StaffSessionManager.generateExpiringQrPayload(
            storeId = storeId,
            storeName = activeProfile.name,
            staff = staff
        )
    }

    var secondsLeft by remember { mutableStateOf(300) } // 5 minutes = 300 seconds

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        }
    }

    val minutes = secondsLeft / 60
    val secs = secondsLeft % 60
    val timeFormatted = String.format("%02d:%02d", minutes, secs)
    val isExpired = secondsLeft <= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) "এককালীন সিঙ্ক QR কোড" else "One-Time Sync QR Code",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isExpired) LowStockRed.copy(alpha = 0.2f) else VividEmeraldPrimary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (isExpired) (if (isBangla) "মেয়াদ শেষ" else "Expired") else timeFormatted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isExpired) LowStockRed else VividEmeraldAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isBangla)
                        "${staff.name} (${staff.role})-এর জন্য তৈরি। স্টাফ তার ফোনে Dokan অ্যাপ খুলে 'যুক্ত হোন' বাটন দিয়ে এই QR কোডটি স্ক্যান করবেন।"
                    else
                        "For ${staff.name} (${staff.role}). Scan this from the staff phone within 5 minutes to join the store.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                // Large QR Visual Box
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isExpired) Color.LightGray else Color.White)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpired) Icons.Default.LockClock else Icons.Default.QrCode,
                        contentDescription = "Sync QR",
                        tint = if (isExpired) Color.Gray else Color.Black,
                        modifier = Modifier.size(130.dp)
                    )
                }

                if (isExpired) {
                    Text(
                        text = if (isBangla) "এই QR কোডের ৫ মিনিটের মেয়াদ শেষ হয়েছে। উইন্ডোটি বন্ধ করে আবার খুলুন।" else "This QR code has expired. Close and reopen to generate a new 5-minute code.",
                        fontSize = 11.sp,
                        color = LowStockRed,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSlateSurfaceVariant)
                            .clickable {
                                clipboardManager.setText(AnnotatedString(qrToken))
                                Toast.makeText(context, if (isBangla) "টোকেন কপি হয়েছে" else "Token copied", Toast.LENGTH_SHORT).show()
                            }
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (isBangla) "সিঙ্ক কোড (ক্লিপবোর্ডে কপি করতে ট্যাপ করুন):" else "Sync Token (Tap to copy):",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = qrToken.take(32) + "...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = VividEmeraldAccent
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
            ) {
                Text(if (isBangla) "বন্ধ করুন" else "Close", maxLines = 1)
            }
        }
    )
}

// Join Staff Modal Dialog (Staff's phone)
@Composable
fun JoinStaffModalDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onJoinSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tokenInput by remember { mutableStateOf("") }
    var isJoining by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "দোকানে স্টাফ হিসেবে যুক্ত হোন" else "Join Store as Staff",
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
                    text = if (isBangla)
                        "মালিকের ফোনের ৫ মিনিটের এককালীন QR কোডের নিচে থাকা কোডটি এখানে পেস্ট বা টাইপ করুন:"
                    else
                        "Scan or paste the one-time 5-minute invite token shown on the shop owner's phone:",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = {
                        tokenInput = it
                        errorMessage = null
                    },
                    label = { Text(if (isBangla) "সিঙ্ক টোকেন / QR ডাটা" else "Sync Token / QR Data") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("input_staff_sync_token"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 12.sp,
                        color = LowStockRed
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tokenInput.isNotBlank()) {
                        isJoining = true
                        errorMessage = null
                        scope.launch {
                            val result = StaffSessionManager.joinStoreWithInvite(context, tokenInput)
                            isJoining = false
                            if (result.isSuccess) {
                                onJoinSuccess()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Failed to join store"
                            }
                        }
                    }
                },
                enabled = !isJoining && tokenInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_confirm_join_staff")
            ) {
                Text(if (isBangla) "যুক্ত হোন" else "Join Store", maxLines = 1)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted, maxLines = 1)
            }
        }
    )
}
