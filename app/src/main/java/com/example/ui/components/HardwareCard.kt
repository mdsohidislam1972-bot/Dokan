package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.DarkSlateCardBorder
import com.example.ui.theme.DarkSlateSurface
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextPrimary
import java.util.Locale

/**
 * Standard compact Card for Hardware Manager matching the reference UI.
 */
@Composable
fun HardwareCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        border = BorderStroke(1.dp, DarkSlateCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

fun isCustomProductPhoto(photoKey: String): Boolean {
    if (photoKey.isBlank()) return false
    return photoKey.startsWith("content://") ||
            photoKey.startsWith("file://") ||
            photoKey.startsWith("http://") ||
            photoKey.startsWith("https://") ||
            photoKey.startsWith("/")
}

@Composable
fun HardwareThumbnailIcon(
    photoKey: String,
    category: String,
    modifier: Modifier = Modifier
) {
    if (isCustomProductPhoto(photoKey)) {
        AsyncImage(
            model = photoKey,
            contentDescription = category,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(RoundedCornerShape(8.dp))
        )
    } else {
        val icon = when (category.lowercase(Locale.ROOT)) {
            "paint" -> Icons.Default.FormatPaint
            "fasteners" -> Icons.Default.Construction
            "plumbing" -> Icons.Default.Plumbing
            "electrical" -> Icons.Default.Lightbulb
            "tools" -> Icons.Default.Build
            "pipes" -> Icons.Default.WaterDrop
            "cement" -> Icons.Default.Home
            else -> Icons.Default.Inventory2
        }
        Icon(
            imageVector = icon,
            contentDescription = category,
            modifier = modifier,
            tint = TextDark
        )
    }
}
