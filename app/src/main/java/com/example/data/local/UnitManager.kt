package com.example.data.local

import android.content.Context

object UnitManager {
    private val defaultUnits = listOf(
        "টি",
        "pcs",
        "কেজি",
        "kg",
        "গ্রাম",
        "gm",
        "লিটার",
        "ltr",
        "বস্তা",
        "ব্যাগ",
        "ফুট",
        "ft",
        "মিটার",
        "m",
        "ডজন",
        "জোড়া",
        "রোল",
        "সেট",
        "প্যাকেট",
        "বক্স",
        "কার্টুন",
        "ক্যান"
    )

    fun init(context: Context) {
        // Initialization if needed
    }

    fun getAllUnits(): List<String> {
        return defaultUnits
    }
}
