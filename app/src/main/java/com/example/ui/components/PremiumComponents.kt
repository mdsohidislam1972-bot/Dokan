package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

/**
 * Standard feature row component. Features are fully unlocked and accessible.
 */
@Composable
fun PremiumFeatureRow(
    title: String,
    subtitle: String? = null,
    isLocked: Boolean = false,
    onLockedClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = isLocked) { onLockedClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Column {
                Text(text = title, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                if (!subtitle.isNullOrBlank()) {
                    Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
                }
            }
        }
        content()
    }
}
