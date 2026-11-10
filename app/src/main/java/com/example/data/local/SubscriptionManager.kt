package com.example.data.local

import android.content.Context

enum class PlanTier(val titleEn: String, val titleBn: String) {
    FREE("Unlocked", "আনলকড"),
    MOBILE_ONLY("Unlocked", "আনলকড"),
    MOBILE_PLUS_WEB("Unlocked", "আনলকড")
}

/**
 * All features in this application are fully unlocked for personal production use.
 * No subscriptions, locking, paywalls, or feature gates are enforced.
 */
object SubscriptionManager {
    val currentTier: PlanTier = PlanTier.MOBILE_PLUS_WEB
    const val expiryDateTimestamp: Long = Long.MAX_VALUE
    const val isTrialUsed: Boolean = true

    fun init(context: Context) {
        // No-op: Everything unlocked
    }

    /**
     * Always returns true: all features (barcode scanner, wholesale price, MRP,
     * low stock warning, hide app branding, bill images, staff report, app lock,
     * party balance on invoice, extra invoice styles, greeting cards, business card maker,
     * multiple business profiles, and unlimited staff accounts) are unrestricted.
     */
    fun isFeatureUnlocked(featureKey: String): Boolean = true
}
