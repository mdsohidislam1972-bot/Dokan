package com.example.ui.screens.utilities

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun HelpAndAboutScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    onLogOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Help & Support, 1: About App, 2: Web App QR

    var showReportDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSlateSurface,
            contentColor = VividEmeraldAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = VividEmeraldAccent
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(if (isBangla) "সাহায্য ও সাপোর্ট" else "Help & Support", fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(if (isBangla) "অ্যাপ পরিচিতি" else "About Dokan", fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(if (isBangla) "ডেস্কটপ ওয়েব" else "Desktop Web", fontSize = 13.sp) }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // 129: HELP AND SUPPORT
                    HelpSupportSection(
                        isBangla = isBangla,
                        onOpenReport = { showReportDialog = true }
                    )
                }
                1 -> {
                    // 130: ABOUT APP + 131: LOG OUT
                    AboutAppSection(
                        isBangla = isBangla,
                        onShowLogout = { showLogoutDialog = true }
                    )
                }
                2 -> {
                    // 128: DESKTOP WEB APP WITH QR LOGIN
                    DesktopWebQrSection(isBangla = isBangla)
                }
            }
        }
    }

    if (showReportDialog) {
        ReportIssueDialog(
            isBangla = isBangla,
            onDismiss = { showReportDialog = false },
            onSubmit = { title, desc ->
                showReportDialog = false
                Toast.makeText(context, if (isBangla) "আপনার সমস্যাটি সফলভাবে জমা হয়েছে!" else "Issue submitted successfully! We'll review it.", Toast.LENGTH_LONG).show()
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = DarkSlateSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = LowStockRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isBangla) "লগ আউট নিশ্চিতকরণ" else "Confirm Log Out", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Text(
                    text = if (isBangla)
                        "আপনি কি নিশ্চিত যে এই ডিভাইস থেকে লগ আউট করতে চান? আপনার অনলাইন ডাটা ক্লাউডে সুরক্ষিত থাকবে।"
                    else
                        "Are you sure you want to log out from this device? Your online records remain safe in Firebase cloud.",
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogOut()
                        Toast.makeText(context, if (isBangla) "লগ আউট সম্পন্ন হয়েছে" else "Logged out successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LowStockRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_confirm_logout")
                ) {
                    Text(if (isBangla) "লগ আউট" else "Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
                }
            }
        )
    }
}

// -------------------------------------------------------------
// 129: HELP & SUPPORT SECTION
// -------------------------------------------------------------
@Composable
private fun HelpSupportSection(
    isBangla: Boolean,
    onOpenReport: () -> Unit
) {
    val context = LocalContext.current

    // Contact Cards
    Text(
        text = if (isBangla) "জরুরি সাপোর্ট ও যোগাযোগ" else "Direct Support & Contacts",
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+8801700000000"))
                        context.startActivity(intent)
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(if (isBangla) "হটলাইন নম্বর" else "Helpline", fontSize = 14.sp, color = TextPrimary)
                        Text("+880 1700-000000 (10 AM - 10 PM)", fontSize = 12.sp, color = TextMuted)
                    }
                }
                Text(if (isBangla) "কল করুন" else "Call", fontSize = 12.sp, color = VividEmeraldAccent, fontWeight = FontWeight.Bold)
            }

            HorizontalDivider(color = DarkSlateCardBorder)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@dokan.app"))
                        context.startActivity(intent)
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(if (isBangla) "ইমেইল সাপোর্ট" else "Email Support", fontSize = 14.sp, color = TextPrimary)
                        Text("support@dokan.app", fontSize = 12.sp, color = TextMuted)
                    }
                }
                Text(if (isBangla) "মেইল পাঠান" else "Email", fontSize = 12.sp, color = VividEmeraldAccent, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Video Tutorials
    Text(
        text = if (isBangla) "টিউটোরিয়াল ভিডিও ও গাইড" else "Video Tutorials & Guides",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = VividEmeraldAccent
    )

    val tutorials = listOf(
        "কীভাবে দ্রুত সেল এবং ক্যাশ মেমো করবেন?" to "5 mins • POS Billing",
        "কাস্টমার বাকি খাতা ও SMS তাগাদা পাঠানো" to "4 mins • Credit Ledger",
        "স্টক ও মাল ক্রয় চালানের হিসাব রাখা" to "6 mins • Inventory Management",
        "গুগল ড্রাইভ ও ক্লাউড ডাটা ব্যাকআপ" to "3 mins • Cloud Sync"
    )

    tutorials.forEach { (title, duration) ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    Toast.makeText(context, if (isBangla) "টিউটোরিয়াল লোড হচ্ছে..." else "Opening tutorial...", Toast.LENGTH_SHORT).show()
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
            border = BorderStroke(1.dp, DarkSlateCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Text(text = duration, fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }
    }

    // Report an Issue Button
    Button(
        onClick = onOpenReport,
        colors = ButtonDefaults.buttonColors(containerColor = DarkSlateSurfaceVariant),
        border = BorderStroke(1.dp, VividEmeraldPrimary),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth().testTag("btn_report_issue")
    ) {
        Icon(Icons.Default.ReportProblem, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (isBangla) "সমস্যা বা মতামত জানান" else "Report an Issue / Feedback", color = TextPrimary, fontSize = 13.sp)
    }
}

// -------------------------------------------------------------
// 130: ABOUT APP SECTION + 131: LOG OUT
// -------------------------------------------------------------
@Composable
private fun AboutAppSection(
    isBangla: Boolean,
    onShowLogout: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
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
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(VividEmeraldPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("Dokan - Retail POS & Ledger", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Version 2.4.0 (Build 2026.11)", fontSize = 12.sp, color = TextMuted)
            Text(if (isBangla) "মেড উইথ লাভ ফর বাংলাদেশি ব্যবসায়ী" else "Made for retailers & smart business owners", fontSize = 11.sp, color = VividEmeraldAccent)
        }
    }

    // App Actions List (Item 130)
    val actionItems = listOf(
        Triple(Icons.Default.Language, if (isBangla) "অফিসিয়াল ওয়েবসাইট ভিজিট করুন" else "Visit Official Website", "https://dokan.app"),
        Triple(Icons.Default.Star, if (isBangla) "প্লে স্টোরে রেটিং দিন" else "Rate the App on Google Play", "market://details?id=com.aistudio.dokan.posmgr"),
        Triple(Icons.Default.Share, if (isBangla) "অন্যান্য ব্যবসায়ীদের সাথে অ্যাপ শেয়ার করুন" else "Share App with Fellow Merchants", "share"),
        Triple(Icons.Default.CheckCircle, if (isBangla) "নতুন ফিচার ও আপডেট (What's New)" else "What's New in v2.4.0", "whats_new"),
        Triple(Icons.Default.PrivacyTip, if (isBangla) "প্রাইভেসি পলিসি" else "Privacy Policy", "https://dokan.app/privacy"),
        Triple(Icons.Default.Lock, if (isBangla) "ব্যবহারের শর্তাবলী (Terms & Services)" else "Terms of Service", "https://dokan.app/terms")
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        border = BorderStroke(1.dp, DarkSlateCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            actionItems.forEachIndexed { idx, (icon, label, action) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            when (action) {
                                "share" -> {
                                    val sendIntent = Intent().apply {
                                        this.action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Dokan - Best Retail Store POS & Baki Ledger App. Download now!")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Dokan App"))
                                }
                                "whats_new" -> {
                                    Toast.makeText(context, if (isBangla) "v2.4.0: মাল্টি-ফোন সিঙ্ক, AI কার্ড মেকার এবং বিল গ্যালারি যুক্ত হয়েছে!" else "v2.4.0: Multi-phone sync, Gemini AI card maker, and Bill Gallery added!", Toast.LENGTH_LONG).show()
                                }
                                else -> {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(action))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, label, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = label, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                }
                if (idx < actionItems.lastIndex) {
                    HorizontalDivider(color = DarkSlateCardBorder)
                }
            }
        }
    }

    // 131: LOG OUT BUTTON
    Button(
        onClick = onShowLogout,
        colors = ButtonDefaults.buttonColors(containerColor = LowStockRed.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, LowStockRed),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("btn_logout")
    ) {
        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = LowStockRed, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (isBangla) "লগ আউট করুন (Log Out)" else "Log Out", color = LowStockRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

// -------------------------------------------------------------
// 128: DESKTOP WEB APP WITH QR LOGIN
// -------------------------------------------------------------
@Composable
private fun DesktopWebQrSection(isBangla: Boolean) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        border = BorderStroke(1.dp, VividEmeraldAccent.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(VividEmeraldPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Computer, contentDescription = null, tint = VividEmeraldAccent, modifier = Modifier.size(36.dp))
            }

            Text(
                text = if (isBangla) "কম্পিউটার বা ল্যাপটপে দোকান ব্যবহার করুন" else "Use Dokan on Desktop / Laptop Web",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = if (isBangla)
                    "আপনার কম্পিউটারের ব্রাউজারে web.dokan.app খুলুন। স্ক্রিনে প্রদর্শিত QR কোডটি মোবাইল অ্যাপ দিয়ে স্ক্যান করলেই একই ডাটা ও পারমিশন নিয়ে ওয়েব ভার্সন ওপেন হবে।"
                else
                    "Open web.dokan.app on your desktop browser. Scan the displayed QR code with your phone to instantly sync all live store records and permissions.",
                fontSize = 12.sp,
                color = TextMuted
            )

            // Mock Web Pairing QR Code display
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.QrCode, contentDescription = "Desktop Pairing QR", tint = Color.Black, modifier = Modifier.fillMaxSize())
            }

            Button(
                onClick = {
                    Toast.makeText(context, if (isBangla) "ক্যামেরা কিউআর স্ক্যানার চালু হচ্ছে..." else "Opening Camera QR Scanner...", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isBangla) "ওয়েব QR কোড স্ক্যান করুন" else "Scan Web QR Code", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ReportIssueDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(if (isBangla) "সমস্যা বা পরামর্শ পাঠান" else "Report Issue / Suggestion", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text(if (isBangla) "বিষয়ের শিরোনাম" else "Subject / Title") },
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
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (isBangla) "বিস্তারিত বর্ণনা..." else "Describe your issue...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
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
                    if (subject.isNotBlank() || description.isNotBlank()) {
                        onSubmit(subject.trim(), description.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
            ) {
                Text(if (isBangla) "পাঠান" else "Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}
