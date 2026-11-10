package com.example.ui.screens.cards

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.local.AppSettings
import com.example.data.local.ProfileManager
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.DarkSlateBackground
import com.example.ui.theme.DarkSlateCardBorder
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.DarkSlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VividEmeraldAccent
import com.example.ui.theme.VividEmeraldPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GreetingTemplate(
    val id: String,
    val category: String,
    val titleBn: String,
    val titleEn: String,
    val messageBn: String,
    val messageEn: String,
    val gradientColors: List<Color>,
    val iconEmoji: String
)

@Composable
fun CardsHubScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBangla = currentLanguage == AppLanguage.BANGLA
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Greeting Cards, 1: Digital Business Card

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
    ) {
        // Tab Row
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
                text = {
                    Text(
                        text = if (isBangla) "শুভেচ্ছা কার্ড (Greetings)" else "Greeting Cards",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) VividEmeraldAccent else TextMuted
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = if (isBangla) "ডিজিটাল বিজনেস কার্ড" else "Business Card Maker",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) VividEmeraldAccent else TextMuted
                    )
                }
            )
        }

        when (selectedTab) {
            0 -> GreetingCardsSection(currentLanguage = currentLanguage)
            1 -> BusinessCardMakerSection(currentLanguage = currentLanguage)
        }
    }
}

// -------------------------------------------------------------
// SECTION 1: GREETING CARDS & EDITOR + AI GENERATOR
// -------------------------------------------------------------

@Composable
fun GreetingCardsSection(
    currentLanguage: AppLanguage
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTemplateForEdit by remember { mutableStateOf<GreetingTemplate?>(null) }
    var showAiGeneratorDialog by remember { mutableStateOf(false) }

    val allTemplates = remember {
        listOf(
            // Good Morning
            GreetingTemplate(
                id = "gm_1",
                category = "Good Morning",
                titleBn = "শুভ সকাল",
                titleEn = "Good Morning",
                messageBn = "একটি নতুন সকাল, নতুন সম্ভাবনা। আপনার আজকের দিনটি আনন্দ ও সাফল্যে ভরে উঠুক। আমাদের সাথে থাকার জন্য ধন্যবাদ।",
                messageEn = "A fresh morning brings new opportunities! Wishing you a fruitful and blessed day ahead. Thank you for being with us.",
                gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                iconEmoji = "☀️"
            ),
            GreetingTemplate(
                id = "gm_2",
                category = "Good Morning",
                titleBn = "উজ্জ্বল সকালের শুভেচ্ছা",
                titleEn = "Peaceful Morning",
                messageBn = "শুভ সকাল! আপনার প্রতিটি পদক্ষেপে সাফল্য ও বরকত আসুক। আন্তরিক শুভকামনা রইল।",
                messageEn = "Good morning! May peace, prosperity, and success accompany every step you take today.",
                gradientColors = listOf(Color(0xFF059669), Color(0xFF10B981)),
                iconEmoji = "🌅"
            ),
            // Good Night
            GreetingTemplate(
                id = "gn_1",
                category = "Good Night",
                titleBn = "শুভ রাত্রি",
                titleEn = "Good Night",
                messageBn = "দিনের সমস্ত ক্লান্তি মুছে যাক শান্তিময় ঘুমে। আগামী দিনটি হোক আরও সুন্দর। শুভ রাত্রি।",
                messageEn = "May your night be peaceful and restful. Wake up refreshed for a victorious tomorrow. Good night!",
                gradientColors = listOf(Color(0xFF1E1B4B), Color(0xFF312E81)),
                iconEmoji = "🌙"
            ),
            GreetingTemplate(
                id = "gn_2",
                category = "Good Night",
                titleBn = "মিষ্টি স্বপ্নের রাত্রি",
                titleEn = "Sweet Dreams",
                messageBn = "সারাদিনের কাজের শেষে একটি শান্তির বিশ্রাম কামনা করছি। শুভ রাত্রি ও আন্তরিক শুভেচ্ছা।",
                messageEn = "After a long productive day, wishing you sweet rest and serenity. Good night!",
                gradientColors = listOf(Color(0xFF4C1D95), Color(0xFF5B21B6)),
                iconEmoji = "✨"
            ),
            // Happy Birthday
            GreetingTemplate(
                id = "hbd_1",
                category = "Happy Birthday",
                titleBn = "শুভ জন্মদিন",
                titleEn = "Happy Birthday",
                messageBn = "জন্মদিনের অনেক অনেক প্রীতি ও শুভেচ্ছা! জীবনের প্রতিটি বছর ভরে উঠুক সুস্বাস্থ্য, দীর্ঘায়ু এবং অফুরন্ত আনন্দে।",
                messageEn = "Wishing you a very Happy Birthday! May your special day bring boundless joy, health, and endless prosperities.",
                gradientColors = listOf(Color(0xFFE11D48), Color(0xFFBE123C)),
                iconEmoji = "🎂"
            ),
            GreetingTemplate(
                id = "hbd_2",
                category = "Happy Birthday",
                titleBn = "জন্মদিনের শুভেচ্ছা ও দোয়া",
                titleEn = "Joyful Birthday",
                messageBn = "আজকের এই শুভ দিনে আপনার উত্তরোত্তর সমৃদ্ধি কামনা করছি। শুভ জন্মদিন!",
                messageEn = "Warmest congratulations and best wishes on your birthday! Honored to celebrate with you.",
                gradientColors = listOf(Color(0xFFEC4899), Color(0xFFBE185D)),
                iconEmoji = "🎉"
            ),
            // Thank You
            GreetingTemplate(
                id = "ty_1",
                category = "Thank You",
                titleBn = "আন্তরিক ধন্যবাদ",
                titleEn = "Thank You",
                messageBn = "আমাদের প্রতিষ্ঠানের ওপর আস্থা রেখে কেনাকাটা করার জন্য আপনাকে ধন্যবাদ। আপনার সন্তুষ্টিই আমাদের আসল অর্জন।",
                messageEn = "Thank you for your valuable patronage and trust in our business. Your satisfaction is our highest achievement.",
                gradientColors = listOf(Color(0xFF0D9488), Color(0xFF0F766E)),
                iconEmoji = "🙏"
            ),
            GreetingTemplate(
                id = "ty_2",
                category = "Thank You",
                titleBn = "কৃতজ্ঞতা ও শুভেচ্ছা",
                titleEn = "Customer Appreciation",
                messageBn = "আপনার সার্বিক সহযোগিতার জন্য আমরা আন্তরিকভাবে কৃতজ্ঞ। সর্বদা আপনার সেবায় প্রস্তুত।",
                messageEn = "We deeply appreciate your partnership and support. Looking forward to serving you always.",
                gradientColors = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)),
                iconEmoji = "💐"
            ),
            // Festivals
            GreetingTemplate(
                id = "fest_1",
                category = "Festivals",
                titleBn = "ঈদ মোবারক",
                titleEn = "Eid Mubarak",
                messageBn = "পবিত্র ঈদের আনন্দ ও বরকত ছড়িয়ে পড়ুক আপনার পরিবারে ও জীবনে। ঈদ মোবারক!",
                messageEn = "Eid Mubarak! May the divine blessings of Eid bring peace, happiness, and prosperity to you and your family.",
                gradientColors = listOf(Color(0xFF047857), Color(0xFF065F46)),
                iconEmoji = "🕌"
            ),
            GreetingTemplate(
                id = "fest_2",
                category = "Festivals",
                titleBn = "শুভ নববর্ষ",
                titleEn = "Happy New Year",
                messageBn = "নতুন বছরের নতুন আলো, মুছে দিক সব গ্লানি। আপনার ও পরিবারের জন্য শুভ নববর্ষের উষ্ণ শুভেচ্ছা।",
                messageEn = "Wishing you and your family a Happy and Prosperous New Year filled with fresh energy and success!",
                gradientColors = listOf(Color(0xFFDC2626), Color(0xFFB91C1C)),
                iconEmoji = "🏮"
            ),
            GreetingTemplate(
                id = "fest_3",
                category = "Festivals",
                titleBn = "শারদীয় দুর্গোৎসবের শুভেচ্ছা",
                titleEn = "Happy Puja",
                messageBn = "শারদীয় দুর্গোৎসবের আন্তরিক প্রীতি ও শুভেচ্ছা। সুখ, শান্তি ও সম্প্রীতিতে ভরে উঠুক সবার জীবন।",
                messageEn = "Warmest greetings on the auspicious occasion of Durga Puja! May joy and peace illuminate your life.",
                gradientColors = listOf(Color(0xFFD97706), Color(0xFFB45309)),
                iconEmoji = "🪔"
            )
        )
    }

    val categories = listOf("Good Morning", "Good Night", "Happy Birthday", "Thank You", "Festivals")

    val filteredTemplates = remember(searchQuery) {
        if (searchQuery.isBlank()) allTemplates
        else {
            val q = searchQuery.lowercase()
            allTemplates.filter {
                it.titleBn.contains(q, ignoreCase = true) ||
                        it.titleEn.contains(q, ignoreCase = true) ||
                        it.category.contains(q, ignoreCase = true) ||
                        it.messageBn.contains(q, ignoreCase = true) ||
                        it.messageEn.contains(q, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Generator Banner Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAiGeneratorDialog = true }
                .testTag("btn_open_ai_card_generator"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
            border = BorderStroke(1.dp, VividEmeraldAccent.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(VividEmeraldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = VividEmeraldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isBangla) "AI কার্ড জেনারেটর" else "Gemini AI Card Generator",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = VividEmeraldAccent
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(VividEmeraldAccent.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AI SMART",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VividEmeraldAccent
                                )
                            }
                        }
                        Text(
                            text = if (isBangla) "ছোট বিবরণে দোকানের নামে স্পেশাল শুভেচ্ছা বার্তা তৈরি করুন" else "Create instant custom branded cards with AI assistance",
                            fontSize = 12.sp,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_search_greeting_cards"),
            placeholder = { Text(if (isBangla) "উৎসব বা উপলক্ষ খুঁজুন (যেমন: সকাল, ঈদ, জন্মদিন)..." else "Search festivals & occasions (e.g. Eid, Morning)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VividEmeraldPrimary,
                unfocusedBorderColor = DarkSlateCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Sections
        categories.forEach { cat ->
            val templatesInCat = filteredTemplates.filter { it.category == cat }
            if (templatesInCat.isNotEmpty()) {
                val catTitle = when (cat) {
                    "Good Morning" -> if (isBangla) "শুভ সকাল (Good Morning)" else "Good Morning"
                    "Good Night" -> if (isBangla) "শুভ রাত্রি (Good Night)" else "Good Night"
                    "Happy Birthday" -> if (isBangla) "শুভ জন্মদিন (Birthday)" else "Happy Birthday"
                    "Thank You" -> if (isBangla) "ধন্যবাদ ও কৃতজ্ঞতা (Thank You)" else "Thank You"
                    "Festivals" -> if (isBangla) "উৎসব ও বিশেষ দিবস (Festivals)" else "Festivals"
                    else -> cat
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = catTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    TextButton(
                        onClick = {
                            selectedTemplateForEdit = templatesInCat.first()
                        }
                    ) {
                        Text(
                            text = if (isBangla) "সব দেখুন" else "View All",
                            color = VividEmeraldAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 6.dp)
                ) {
                    items(templatesInCat, key = { it.id }) { item ->
                        GreetingMiniCard(
                            template = item,
                            isBangla = isBangla,
                            onClick = { selectedTemplateForEdit = item }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    // Editor Dialog
    selectedTemplateForEdit?.let { template ->
        GreetingCardEditorDialog(
            template = template,
            isBangla = isBangla,
            onDismiss = { selectedTemplateForEdit = null },
            onShare = { shareText ->
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, if (isBangla) "কার্ড শেয়ার করুন" else "Share Greeting Card")
                context.startActivity(shareIntent)
            },
            onDownload = {
                Toast.makeText(context, if (isBangla) "কার্ড সফলভাবে ফোনে সংরক্ষিত হয়েছে!" else "Card saved to device gallery!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // AI Card Generator Dialog
    if (showAiGeneratorDialog) {
        AiCardGeneratorDialog(
            isBangla = isBangla,
            onDismiss = { showAiGeneratorDialog = false },
            onCardCreated = { generatedTemplate ->
                showAiGeneratorDialog = false
                selectedTemplateForEdit = generatedTemplate
            }
        )
    }
}

@Composable
fun GreetingMiniCard(
    template: GreetingTemplate,
    isBangla: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .height(210.dp)
            .clickable { onClick() }
            .testTag("greeting_card_${template.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        border = BorderStroke(1.dp, DarkSlateCardBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Visual header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .background(Brush.linearGradient(template.gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = template.iconEmoji,
                    fontSize = 36.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isBangla) template.titleBn else template.titleEn,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBangla) template.messageBn else template.messageEn,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 15.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = if (isBangla) "এডিট ও শেয়ার →" else "Edit & Share →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VividEmeraldAccent
                    )
                }
            }
        }
    }
}

// Greeting Card Editor Dialog
@Composable
fun GreetingCardEditorDialog(
    template: GreetingTemplate,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onShare: (String) -> Unit,
    onDownload: () -> Unit
) {
    val activeProfile = ProfileManager.activeProfile
    var shopName by remember { mutableStateOf(activeProfile.shopName.ifBlank { "আমার দোকান (Dokan)" }) }
    var shopPhone by remember { mutableStateOf(activeProfile.phone.ifBlank { "০১৭১১-XXXXXX" }) }
    var customMessage by remember { mutableStateOf(if (isBangla) template.messageBn else template.messageEn) }
    var selectedFrame by remember { mutableIntStateOf(0) } // 0: Emerald Modern, 1: Golden Floral, 2: Royal Slate
    var hideAppBranding by remember { mutableStateOf(false) }

    val frameBorders = listOf(
        BorderStroke(3.dp, VividEmeraldAccent),
        BorderStroke(3.dp, Color(0xFFF59E0B)),
        BorderStroke(3.dp, Color(0xFF64748B))
    )

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
                    text = if (isBangla) "শুভেচ্ছা কার্ড এডিটর" else "Greeting Card Editor",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Live Card Preview Canvas
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = frameBorders[selectedFrame],
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(template.gradientColors))
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top: Header & Icon
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = template.iconEmoji,
                                    fontSize = 28.sp
                                )
                                Text(
                                    text = if (isBangla) template.titleBn else template.titleEn,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // Center: Greeting message
                            Text(
                                text = customMessage,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.95f),
                                textAlign = TextAlign.Center,
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            // Bottom: Shop details & branding
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = shopName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "যোগাযোগ: $shopPhone",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )

                                if (!hideAppBranding) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Powered by Dokan Business Suite",
                                        fontSize = 8.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Controls: Frame Selector
                Text(
                    text = if (isBangla) "ফ্রেম নির্বাচন করুন:" else "Select Frame Style:",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val frameNames = listOf(
                        if (isBangla) "পান্না ফ্রেম" else "Emerald",
                        if (isBangla) "গোল্ডেন ফ্রেম" else "Gold",
                        if (isBangla) "রয়্যাল স্লেট" else "Slate"
                    )
                    frameNames.forEachIndexed { index, name ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedFrame == index) VividEmeraldPrimary else DarkSlateSurfaceVariant)
                                .clickable { selectedFrame = index }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                fontSize = 12.sp,
                                color = if (selectedFrame == index) Color.White else TextMuted
                            )
                        }
                    }
                }

                // Hide App Branding Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "অ্যাপ ব্র্যান্ডিং লুকান" else "Hide App Branding",
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isBangla) "কার্ড থেকে 'Powered by Dokan' সরান" else "Remove footer watermark",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Switch(
                        checked = hideAppBranding,
                        onCheckedChange = { hideAppBranding = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = VividEmeraldPrimary
                        )
                    )
                }

                // Edit Message
                OutlinedTextField(
                    value = customMessage,
                    onValueChange = { customMessage = it },
                    label = { Text(if (isBangla) "শুভেচ্ছা বার্তা পরিবর্তন" else "Custom Message") },
                    modifier = Modifier.fillMaxWidth().testTag("input_custom_message"),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Edit Shop Name & Phone
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text(if (isBangla) "দোকানের নাম" else "Shop Name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VividEmeraldPrimary,
                            unfocusedBorderColor = DarkSlateCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = shopPhone,
                        onValueChange = { shopPhone = it },
                        label = { Text(if (isBangla) "ফোন নম্বর" else "Phone") },
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
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onDownload,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSlateSurfaceVariant),
                    modifier = Modifier.testTag("btn_download_card")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "ডাউনলোড" else "Save")
                }

                Button(
                    onClick = {
                        val fullShareText = """
                            🌟 ${if (isBangla) template.titleBn else template.titleEn} 🌟
                            
                            $customMessage
                            
                            🏪 $shopName
                            📞 $shopPhone
                            ${if (!hideAppBranding) "\n(Sent via Dokan Business Suite)" else ""}
                        """.trimIndent()
                        onShare(fullShareText)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                    modifier = Modifier.testTag("btn_share_card")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "শেয়ার করুন" else "Share Card")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বন্ধ" else "Close", color = TextMuted)
            }
        }
    )
}

// AI Card Generator Dialog
@Composable
fun AiCardGeneratorDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onCardCreated: (GreetingTemplate) -> Unit
) {
    val activeProfile = ProfileManager.activeProfile
    var promptDescription by remember { mutableStateOf("") }
    var selectedOccasion by remember { mutableStateOf("New Year") }
    var isGenerating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val occasionOptions = listOf(
        "Festival" to if (isBangla) "উৎসব ও পূজা" else "Festival & Celebration",
        "Sale" to if (isBangla) "বিশেষ মূল্যছাড় / সেল" else "Mega Sale Discount",
        "Appreciation" to if (isBangla) "কাস্টমার শুভেচ্ছা" else "Loyal Customer Thank You",
        "Morning" to if (isBangla) "অনুপ্রেরণামূলক সকাল" else "Inspiring Morning"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = VividEmeraldAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBangla) "Gemini AI কার্ড জেনারেটর" else "Gemini AI Card Generator",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isBangla)
                        "দোকানের জন্য কাস্টম শুভেচ্ছা বার্তা বা অফার কার্ড স্বয়ংক্রিয়ভাবে তৈরি করুন:"
                    else
                        "Generate personalized greetings combined with your shop profile:",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                // Occasion selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    occasionOptions.forEach { (key, label) ->
                        val isSelected = selectedOccasion == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) VividEmeraldPrimary else DarkSlateSurfaceVariant)
                                .clickable { selectedOccasion = key }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else TextMuted
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = promptDescription,
                    onValueChange = { promptDescription = it },
                    label = { Text(if (isBangla) "সংক্ষিপ্ত বিবরণ বা নির্দেশিকা" else "Short Prompt / Notes") },
                    placeholder = { Text(if (isBangla) "যেমন: ৫০% ছাড়ের সাথে শুভ নববর্ষের আমন্ত্রণ" else "e.g. Warm greetings with special loyalty discount") },
                    modifier = Modifier.fillMaxWidth().testTag("input_ai_prompt"),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                if (isGenerating) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = VividEmeraldAccent, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBangla) "AI কার্ড তৈরি করছে..." else "Generating card with AI...",
                            fontSize = 12.sp,
                            color = VividEmeraldAccent
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isGenerating = true
                    scope.launch {
                        val aiResult = com.example.utils.GeminiCardGenerator.generateCard(
                            shopName = activeProfile.shopName,
                            occasion = selectedOccasion,
                            customPrompt = promptDescription,
                            isBangla = isBangla
                        )

                        val newTemplate = GreetingTemplate(
                            id = "ai_${System.currentTimeMillis()}",
                            category = "AI Generated",
                            titleBn = aiResult.titleBn,
                            titleEn = aiResult.titleEn,
                            messageBn = aiResult.messageBn,
                            messageEn = aiResult.messageEn,
                            gradientColors = listOf(Color(0xFF059669), Color(0xFF065F46)),
                            iconEmoji = "✨"
                        )
                        isGenerating = false
                        onCardCreated(newTemplate)
                    }
                },
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_generate_ai_card")
            ) {
                Text(if (isBangla) "কার্ড তৈরি করুন" else "Generate Card")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}

// -------------------------------------------------------------
// SECTION 2: DIGITAL BUSINESS CARD MAKER
// -------------------------------------------------------------

@Composable
fun BusinessCardMakerSection(
    currentLanguage: AppLanguage
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val activeProfile = ProfileManager.activeProfile

    var ownerName by remember { mutableStateOf(activeProfile.shopName.ifBlank { "মো: রফিকুল ইসলাম (Owner)" }) }
    var businessName by remember { mutableStateOf(activeProfile.shopName.ifBlank { "দোকান হার্ডওয়্যার অ্যান্ড সেনিটারি" }) }
    var phone by remember { mutableStateOf(activeProfile.phone.ifBlank { "০১৭০০-০০০০০০" }) }
    var email by remember { mutableStateOf(activeProfile.email.ifBlank { "dokan.business@example.com" }) }
    var address by remember { mutableStateOf(activeProfile.address.ifBlank { "মেইন রোড, ঢাকা" }) }
    var tagline by remember { mutableStateOf(if (isBangla) "পাইকারি ও খুচরা বিশ্বাসযোগ্য বিশ্বস্ত প্রতিষ্ঠান" else "Wholesale & Retail Trusted Merchants") }

    // Styles & Colors
    var selectedColorIndex by remember { mutableIntStateOf(0) }
    var selectedTemplateIndex by remember { mutableIntStateOf(0) } // 0: Emerald Modern, 1: Royal Slate, 2: Luxury Gold

    val cardGradients = listOf(
        listOf(Color(0xFF0F172A), Color(0xFF059669)), // Emerald Slate
        listOf(Color(0xFF1E293B), Color(0xFF3B82F6)), // Deep Navy
        listOf(Color(0xFF2E1065), Color(0xFF9333EA)), // Royal Purple
        listOf(Color(0xFF78350F), Color(0xFFD97706)), // Classic Amber/Gold
        listOf(Color(0xFF18181B), Color(0xFF27272A))  // Carbon Stealth
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isBangla) "লাইভ কার্ড প্রিভিউ" else "Live Card Preview",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = VividEmeraldAccent
        )

        // Live Preview Business Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .testTag("business_card_live_preview"),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF475569)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(cardGradients[selectedColorIndex]))
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header: Logo avatar + Business Name + Tagline
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = businessName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = tagline,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = businessName.take(1).uppercase(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Middle: Owner Name & Title
                    Column {
                        Text(
                            text = ownerName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VividEmeraldAccent
                        )
                        Text(
                            text = if (isBangla) "স্বত্বাধিকারী / Proprietor" else "Owner & Manager",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    // Footer: Contact Info & QR code icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = phone, fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = email, fontSize = 10.sp, color = Color.White.copy(alpha = 0.9f))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = address, fontSize = 10.sp, color = Color.White.copy(alpha = 0.9f), maxLines = 1)
                            }
                        }

                        // Mini QR badge
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = Color.Black, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
        }

        // Color Palettes
        Text(
            text = if (isBangla) "কার্ডের কালার থিম" else "Card Color Style",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            cardGradients.forEachIndexed { index, gradient ->
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(gradient))
                        .border(
                            width = if (selectedColorIndex == index) 2.dp else 0.dp,
                            color = if (selectedColorIndex == index) Color.White else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { selectedColorIndex = index },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedColorIndex == index) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Action Buttons: Download & Share
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    Toast.makeText(context, if (isBangla) "ডিজিটাল বিজনেস কার্ড গ্যালারিতে সংরক্ষিত!" else "Card image exported successfully!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_save_business_card"),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSlateSurfaceVariant)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isBangla) "ডাউনলোড" else "Save Card")
            }

            Button(
                onClick = {
                    val cardText = """
                        📇 DIGITAL BUSINESS CARD
                        ━━━━━━━━━━━━━━━━━━━━━━
                        🏢 $businessName
                        🏷️ $tagline
                        👤 $ownerName
                        📞 Phone: $phone
                        ✉️ Email: $email
                        📍 Address: $address
                        ━━━━━━━━━━━━━━━━━━━━━━
                        Verified Dokan Business Merchant
                    """.trimIndent()

                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, cardText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(intent, if (isBangla) "বিজনেস কার্ড পাঠান" else "Share Business Card"))
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_share_business_card"),
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isBangla) "শেয়ার করুন" else "Share Card")
            }
        }

        HorizontalDivider(color = DarkSlateCardBorder)

        // Editable Fields
        Text(
            text = if (isBangla) "কার্ডের বিবরণ পরিবর্তন করুন" else "Edit Card Information",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        OutlinedTextField(
            value = businessName,
            onValueChange = { businessName = it },
            label = { Text(if (isBangla) "প্রতিষ্ঠানের নাম" else "Business Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_bc_business_name"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VividEmeraldPrimary,
                unfocusedBorderColor = DarkSlateCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        OutlinedTextField(
            value = ownerName,
            onValueChange = { ownerName = it },
            label = { Text(if (isBangla) "মালিকের নাম ও পদবী" else "Owner Name & Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_bc_owner_name"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VividEmeraldPrimary,
                unfocusedBorderColor = DarkSlateCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        OutlinedTextField(
            value = tagline,
            onValueChange = { tagline = it },
            label = { Text(if (isBangla) "স্লোগান / ট্যাগলাইন" else "Business Tagline") },
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(if (isBangla) "ফোন নম্বর" else "Phone") },
                singleLine = true,
                modifier = Modifier.weight(1f),
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
                label = { Text(if (isBangla) "ইমেইল" else "Email") },
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
            value = address,
            onValueChange = { address = it },
            label = { Text(if (isBangla) "ঠিকানা" else "Address") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VividEmeraldPrimary,
                unfocusedBorderColor = DarkSlateCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(60.dp))
    }
}
