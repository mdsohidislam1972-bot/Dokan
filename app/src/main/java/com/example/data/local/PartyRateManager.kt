package com.example.data.local

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf

/**
 * Manages customer-specific product rates (Party-wise item rates).
 * E.g., Customer A gets Product B for 120৳ instead of standard 140৳.
 */
object PartyRateManager {
    private const val PREFS_NAME = "hardware_store_party_rates"

    // In-memory cache for fast lookup during checkout
    private val ratesCache = mutableStateMapOf<String, Double>()
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        ratesCache.clear()
        prefs.all.forEach { (key, value) ->
            if (value is String) {
                value.toDoubleOrNull()?.let { ratesCache[key] = it }
            } else if (value is Float) {
                ratesCache[key] = value.toDouble()
            }
        }
        isInitialized = true
    }

    private fun makeKey(customerId: Long, productId: Long): String = "rate_${customerId}_$productId"

    fun getPartyRate(customerId: Long?, productId: Long): Double? {
        if (customerId == null || customerId <= 0) return null
        return ratesCache[makeKey(customerId, productId)]
    }

    fun setPartyRate(context: Context, customerId: Long, productId: Long, customRate: Double) {
        val key = makeKey(customerId, productId)
        ratesCache[key] = customRate
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(key, customRate.toString()).apply()
    }

    fun removePartyRate(context: Context, customerId: Long, productId: Long) {
        val key = makeKey(customerId, productId)
        ratesCache.remove(key)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(key).apply()
    }

    fun getAllRatesForCustomer(customerId: Long): Map<Long, Double> {
        val prefix = "rate_${customerId}_"
        val result = mutableMapOf<Long, Double>()
        ratesCache.forEach { (k, v) ->
            if (k.startsWith(prefix)) {
                val prodIdStr = k.removePrefix(prefix)
                prodIdStr.toLongOrNull()?.let { result[it] = v }
            }
        }
        return result
    }
}
