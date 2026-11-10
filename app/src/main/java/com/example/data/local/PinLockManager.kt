package com.example.data.local

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object PinLockManager {
    private const val PREFS_NAME = "hardware_store_prefs"
    private const val KEY_PIN_CODE = "security_pin_code"
    private const val KEY_PIN_ENABLED = "security_pin_enabled"

    var isPinSet by mutableStateOf(false)
        private set

    private var currentPinCode: String? = null
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentPinCode = prefs.getString(KEY_PIN_CODE, null)
        isPinSet = !currentPinCode.isNullOrBlank() && prefs.getBoolean(KEY_PIN_ENABLED, false)
    }

    fun verifyPin(enteredPin: String): Boolean {
        if (!isPinSet) return true
        return currentPinCode == enteredPin.trim()
    }

    fun setPin(context: Context, newPin: String) {
        val trimmed = newPin.trim()
        if (trimmed.length in 4..6) {
            currentPinCode = trimmed
            isPinSet = true
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_PIN_CODE, trimmed)
                .putBoolean(KEY_PIN_ENABLED, true)
                .apply()
        }
    }

    fun setPin(newPin: String) {
        appContext?.let { setPin(it, newPin) }
    }

    fun removePin(context: Context) {
        currentPinCode = null
        isPinSet = false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_PIN_CODE)
            .putBoolean(KEY_PIN_ENABLED, false)
            .apply()
    }

    fun removePin() {
        appContext?.let { removePin(it) }
    }
}
