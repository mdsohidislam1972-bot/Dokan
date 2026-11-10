package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.localization.AppLanguage

enum class NavTab(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    SELL(
        selectedIcon = Icons.Filled.PointOfSale,
        unselectedIcon = Icons.Outlined.PointOfSale
    ),
    STOCK(
        selectedIcon = Icons.Filled.Inventory2,
        unselectedIcon = Icons.Outlined.Inventory2
    ),
    HISTORY(
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    ),
    BUSINESS(
        selectedIcon = Icons.Filled.Assessment,
        unselectedIcon = Icons.Outlined.Assessment
    ),
    MORE(
        selectedIcon = Icons.Filled.MoreHoriz,
        unselectedIcon = Icons.Outlined.MoreHoriz
    );

    fun title(language: AppLanguage): String = when (this) {
        HOME -> if (language == AppLanguage.BANGLA) "হোম" else "Home"
        SELL -> if (language == AppLanguage.BANGLA) "বিক্রি" else "Sell"
        STOCK -> if (language == AppLanguage.BANGLA) "স্টক" else "Stock"
        HISTORY -> if (language == AppLanguage.BANGLA) "ইতিহাস" else "History"
        BUSINESS -> if (language == AppLanguage.BANGLA) "ব্যবসা" else "Business"
        MORE -> if (language == AppLanguage.BANGLA) "আরও" else "More"
    }
}
