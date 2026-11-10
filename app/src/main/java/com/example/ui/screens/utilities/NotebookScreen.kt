package com.example.ui.screens.utilities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class QuickNote(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val colorTag: String = "#10B981" // emerald default
)

object QuickNoteManager {
    private const val PREFS_NAME = "dokan_quick_notes"
    private const val KEY_NOTES = "saved_notes"

    fun loadNotes(context: Context): List<QuickNote> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_NOTES, null) ?: return defaultNotes()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<QuickNote>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    QuickNote(
                        id = o.getString("id"),
                        title = o.getString("title"),
                        content = o.getString("content"),
                        timestamp = o.getLong("timestamp"),
                        isPinned = o.optBoolean("isPinned", false),
                        colorTag = o.optString("colorTag", "#10B981")
                    )
                )
            }
            list
        } catch (e: Exception) {
            defaultNotes()
        }
    }

    fun saveNotes(context: Context, notes: List<QuickNote>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray()
        notes.forEach { n ->
            val o = JSONObject().apply {
                put("id", n.id)
                put("title", n.title)
                put("content", n.content)
                put("timestamp", n.timestamp)
                put("isPinned", n.isPinned)
                put("colorTag", n.colorTag)
            }
            arr.put(o)
        }
        prefs.edit().putString(KEY_NOTES, arr.toString()).apply()
    }

    private fun defaultNotes(): List<QuickNote> {
        return listOf(
            QuickNote(
                id = "note_1",
                title = "মহাজন অর্ডার মেমো",
                content = "রহিম ব্রাদার্স থেকে আগামী রবিবার ১০ কার্টুন পিভিসি পাইপ ও ফিটিংস পাঠাতে হবে।",
                isPinned = true,
                colorTag = "#F59E0B"
            ),
            QuickNote(
                id = "note_2",
                title = "বিকাশ ও ব্যাংক ব্যালেন্স রিচেক",
                content = "মাসিক হিসাব ক্লোজিংয়ের আগে ডাচ-বাংলা ব্যাংক ও বিকাশ মার্চেন্ট ব্যালেন্স চেক করতে হবে।",
                isPinned = false,
                colorTag = "#10B981"
            )
        )
    }
}

@Composable
fun NotebookScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA

    var notes by remember { mutableStateOf(QuickNoteManager.loadNotes(context)) }
    var searchQuery by remember { mutableStateOf("") }
    var noteToEdit by remember { mutableStateOf<QuickNote?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredNotes = notes
        .filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.content.contains(searchQuery, ignoreCase = true)
        }
        .sortedWith(compareByDescending<QuickNote> { it.isPinned }.thenByDescending { it.timestamp })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isBangla) "নোট খুঁজুন..." else "Search notes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_notes"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VividEmeraldPrimary,
                    unfocusedBorderColor = DarkSlateCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Note, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isBangla) "কোনো নোট পাওয়া যায়নি" else "No notes found",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            isBangla = isBangla,
                            onPinToggle = {
                                val updated = notes.map { if (it.id == note.id) it.copy(isPinned = !it.isPinned) else it }
                                notes = updated
                                QuickNoteManager.saveNotes(context, updated)
                            },
                            onEdit = {
                                noteToEdit = note
                                showCreateDialog = true
                            },
                            onDelete = {
                                val updated = notes.filterNot { it.id == note.id }
                                notes = updated
                                QuickNoteManager.saveNotes(context, updated)
                                Toast.makeText(context, if (isBangla) "নোট ডিলিট হয়েছে" else "Note deleted", Toast.LENGTH_SHORT).show()
                            },
                            onCopy = {
                                val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cb.setPrimaryClip(ClipData.newPlainText(note.title, "${note.title}\n${note.content}"))
                                Toast.makeText(context, if (isBangla) "কপি করা হয়েছে!" else "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        // Add Note FAB
        FloatingActionButton(
            onClick = {
                noteToEdit = null
                showCreateDialog = true
            },
            containerColor = VividEmeraldPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_note")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Note")
        }
    }

    if (showCreateDialog) {
        NoteEditorDialog(
            isBangla = isBangla,
            initialNote = noteToEdit,
            onDismiss = { showCreateDialog = false },
            onSave = { title, content, isPinned, colorTag ->
                val updated = if (noteToEdit != null) {
                    notes.map {
                        if (it.id == noteToEdit!!.id) {
                            it.copy(title = title, content = content, isPinned = isPinned, colorTag = colorTag, timestamp = System.currentTimeMillis())
                        } else it
                    }
                } else {
                    listOf(
                        QuickNote(
                            id = "note_${System.currentTimeMillis()}",
                            title = title,
                            content = content,
                            isPinned = isPinned,
                            colorTag = colorTag
                        )
                    ) + notes
                }
                notes = updated
                QuickNoteManager.saveNotes(context, updated)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun NoteCard(
    note: QuickNote,
    isBangla: Boolean,
    onPinToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(note.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("card_note_${note.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        border = BorderStroke(1.dp, if (note.isPinned) VividEmeraldAccent else DarkSlateCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(try { Color(android.graphics.Color.parseColor(note.colorTag)) } catch (e: Exception) { VividEmeraldAccent })
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = note.title.ifBlank { "Untitled Note" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPinToggle, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pin",
                            tint = if (note.isPinned) VividEmeraldAccent else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LowStockRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Text(
                text = note.content,
                fontSize = 13.sp,
                color = TextPrimary.copy(alpha = 0.9f),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = dateStr,
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun NoteEditorDialog(
    isBangla: Boolean,
    initialNote: QuickNote?,
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean, String) -> Unit
) {
    var title by remember { mutableStateOf(initialNote?.title ?: "") }
    var content by remember { mutableStateOf(initialNote?.content ?: "") }
    var isPinned by remember { mutableStateOf(initialNote?.isPinned ?: false) }
    var selectedColor by remember { mutableStateOf(initialNote?.colorTag ?: "#10B981") }

    val colorOptions = listOf("#10B981", "#3B82F6", "#F59E0B", "#EF4444", "#8B5CF6")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSlateSurface,
        title = {
            Text(
                text = if (initialNote != null) (if (isBangla) "নোট সম্পাদন" else "Edit Note") else (if (isBangla) "নতুন নোট তৈরি" else "New Quick Note"),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isBangla) "শিরোনাম" else "Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_note_title"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(if (isBangla) "বিস্তারিত নোট..." else "Note details...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp).testTag("input_note_content"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VividEmeraldPrimary,
                        unfocusedBorderColor = DarkSlateCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Color picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isBangla) "কালার ট্যাগ:" else "Tag:", fontSize = 12.sp, color = TextMuted)
                    colorOptions.forEach { hex ->
                        val isSelected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .clickable { selectedColor = hex }
                                .then(
                                    if (isSelected) Modifier.padding(2.dp) else Modifier
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() || content.isNotBlank()) {
                        onSave(title.trim(), content.trim(), isPinned, selectedColor)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary),
                modifier = Modifier.testTag("btn_save_note")
            ) {
                Text(if (isBangla) "সংরক্ষণ" else "Save Note")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
            }
        }
    )
}
