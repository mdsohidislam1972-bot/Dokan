package com.example.ui.screens.reminders

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AppReminder
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun RemindersScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val scope = rememberCoroutineScope()
    val repository = remember { AppDatabase.getRepository(context) }

    val allReminders by repository.allReminders.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Upcoming, 1: Completed
    var isGridView by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    val upcomingReminders = remember(allReminders) { allReminders.filter { !it.isCompleted } }
    val completedReminders = remember(allReminders) { allReminders.filter { it.isCompleted } }

    val activeList = if (selectedTab == 0) upcomingReminders else completedReminders

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top control bar: Tab row & List/Grid toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSlateSurface)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = VividEmeraldAccent,
                    modifier = Modifier.weight(1f),
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
                                text = if (isBangla) "আসন্ন (${upcomingReminders.size})" else "Upcoming (${upcomingReminders.size})",
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
                                text = if (isBangla) "সম্পন্ন (${completedReminders.size})" else "Completed (${completedReminders.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) VividEmeraldAccent else TextMuted
                            )
                        }
                    )
                }

                // Grid / List View Toggle button
                IconButton(
                    onClick = { isGridView = !isGridView },
                    modifier = Modifier.testTag("btn_toggle_view_mode")
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "Toggle Grid or List View",
                        tint = TextPrimary
                    )
                }
            }

            // Reminders View
            if (activeList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(DarkSlateSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = VividEmeraldAccent,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (selectedTab == 0)
                                (if (isBangla) "কোনো আসন্ন রিমাইন্ডার নেই" else "No upcoming reminders")
                            else
                                (if (isBangla) "কোনো সম্পন্ন রিমাইন্ডার নেই" else "No completed reminders"),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBangla) "টাস্ক, ইভেন্ট বা পেমেন্ট তাগাদার জন্য নতুন রিমাইন্ডার তৈরি করুন।" else "Tap the '+' button below to schedule tasks or payment alerts.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(activeList, key = { it.id }) { reminder ->
                            ReminderGridCard(
                                reminder = reminder,
                                isBangla = isBangla,
                                onToggleStatus = {
                                    scope.launch {
                                        repository.setReminderCompleted(reminder.id, !reminder.isCompleted)
                                    }
                                },
                                onDelete = {
                                    scope.launch {
                                        repository.deleteReminder(reminder.id)
                                    }
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(activeList, key = { it.id }) { reminder ->
                            ReminderListCard(
                                reminder = reminder,
                                isBangla = isBangla,
                                onToggleStatus = {
                                    scope.launch {
                                        repository.setReminderCompleted(reminder.id, !reminder.isCompleted)
                                    }
                                },
                                onDelete = {
                                    scope.launch {
                                        repository.deleteReminder(reminder.id)
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
        }

        // Add Reminder FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = VividEmeraldPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_reminder")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBangla) "নতুন রিমাইন্ডার" else "Add Reminder",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showAddDialog) {
        AddNewReminderDialog(
            isBangla = isBangla,
            onDismiss = { showAddDialog = false },
            onSave = { title, desc, dateMs, timeStr, type ->
                scope.launch {
                    repository.insertReminder(
                        AppReminder(
                            title = title,
                            description = desc,
                            reminderDate = dateMs,
                            reminderTime = timeStr,
                            type = type,
                            isCompleted = false
                        )
                    )
                    showAddDialog = false
                    Toast.makeText(
                        context,
                        if (isBangla) "রিমাইন্ডার সংরক্ষিত হয়েছে! সময়মতো নোটিফিকেশন পাঠানো হবে।" else "Reminder saved! Notification scheduled.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}

@Composable
fun ReminderListCard(
    reminder: AppReminder,
    isBangla: Boolean,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(reminder.reminderDate) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(reminder.reminderDate))
    }

    val typeColor = when (reminder.type) {
        "Payment" -> LowStockRed
        "Event" -> Color(0xFF3B82F6)
        else -> VividEmeraldAccent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reminder_card_${reminder.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        border = BorderStroke(1.dp, DarkSlateCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleStatus, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (reminder.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Status",
                        tint = if (reminder.isCompleted) VividEmeraldAccent else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = reminder.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (reminder.isCompleted) TextMuted else TextPrimary,
                            textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(typeColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when (reminder.type) {
                                    "Payment" -> if (isBangla) "পেমেন্ট" else "Payment"
                                    "Event" -> if (isBangla) "ইভেন্ট" else "Event"
                                    else -> if (isBangla) "টাস্ক" else "Task"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = typeColor
                            )
                        }
                    }

                    if (reminder.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = reminder.description,
                            fontSize = 12.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "$dateStr • ${reminder.reminderTime}", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun ReminderGridCard(
    reminder: AppReminder,
    isBangla: Boolean,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(reminder.reminderDate) {
        SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(reminder.reminderDate))
    }

    val typeColor = when (reminder.type) {
        "Payment" -> LowStockRed
        "Event" -> Color(0xFF3B82F6)
        else -> VividEmeraldAccent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        border = BorderStroke(1.dp, DarkSlateCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(typeColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when (reminder.type) {
                            "Payment" -> if (isBangla) "পেমেন্ট" else "Payment"
                            "Event" -> if (isBangla) "ইভেন্ট" else "Event"
                            else -> if (isBangla) "টাস্ক" else "Task"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            Column {
                Text(
                    text = reminder.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (reminder.isCompleted) TextMuted else TextPrimary,
                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (reminder.description.isNotBlank()) {
                    Text(
                        text = reminder.description,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = TextMuted, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "$dateStr, ${reminder.reminderTime}", fontSize = 10.sp, color = TextMuted)
                }

                IconButton(onClick = onToggleStatus, modifier = Modifier.size(26.dp)) {
                    Icon(
                        imageVector = if (reminder.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Complete",
                        tint = if (reminder.isCompleted) VividEmeraldAccent else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// Add New Reminder Dialog
@Composable
fun AddNewReminderDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, dateMs: Long, timeStr: String, type: String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Task") } // Task, Event, Payment
    var reminderDateMs by remember { mutableStateOf(System.currentTimeMillis() + 86400000L) }
    var reminderTimeStr by remember { mutableStateOf("10:00 AM") }

    val dateFormatted = remember(reminderDateMs) {
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(reminderDateMs))
    }

    val typeOptions = listOf(
        "Task" to if (isBangla) "টাস্ক (Task)" else "Task",
        "Event" to if (isBangla) "ইভেন্ট (Event)" else "Event",
        "Payment" to if (isBangla) "পেমেন্ট তাগাদা" else "Payment Due"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (isBangla) "নতুন রিমাইন্ডার যোগ করুন" else "Add New Reminder",
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
                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    typeOptions.forEach { (typeKey, label) ->
                        val isSelected = selectedType == typeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) VividEmeraldPrimary else DarkSlateSurfaceVariant)
                                .clickable { selectedType = typeKey }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextMuted
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isBangla) "রিমাইন্ডারের শিরোনাম *" else "Title *") },
                    placeholder = { Text(if (isBangla) "যেমন: রহিম ট্রেডার্সকে তাগাদা ফোন" else "e.g. Call supplier for stock arrival") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_reminder_title"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(if (isBangla) "বিস্তারিত বিবরণ (ঐচ্ছিক)" else "Description (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Date Picker trigger
                OutlinedTextField(
                    value = dateFormatted,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (isBangla) "তারিখ *" else "Date *") },
                    trailingIcon = {
                        IconButton(onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = reminderDateMs }
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newCal = Calendar.getInstance()
                                    newCal.set(year, month, dayOfMonth)
                                    reminderDateMs = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = VividEmeraldAccent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Time Picker trigger
                OutlinedTextField(
                    value = reminderTimeStr,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (isBangla) "সময় *" else "Time *") },
                    trailingIcon = {
                        IconButton(onClick = {
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    val amPm = if (hourOfDay >= 12) "PM" else "AM"
                                    val h12 = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                                    reminderTimeStr = String.format(Locale.getDefault(), "%02d:%02d %s", h12, minute, amPm)
                                },
                                10,
                                0,
                                false
                            ).show()
                        }) {
                            Icon(Icons.Default.AccessTime, contentDescription = "Pick Time", tint = VividEmeraldAccent)
                        }
                    },
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
                    if (title.isNotBlank()) {
                        onSave(title.trim(), desc.trim(), reminderDateMs, reminderTimeStr, selectedType)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_save_reminder")
            ) {
                Text(if (isBangla) "রিমাইন্ডার সংরক্ষণ করুন" else "Save Reminder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}
