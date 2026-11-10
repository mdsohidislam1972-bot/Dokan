package com.example.data.local

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

data class BusinessProfile(
    val id: String,
    val name: String,
    val phone: String,
    val address: String,
    val businessType: String = "Retailer", // Retailer, Wholesaler, Distributor, Manufacturer, Service
    val category: String = "Hardware & Sanitary",
    val email: String = "",
    val ownerName: String = "",
    val division: String = "Dhaka",
    val district: String = "Dhaka",
    val streetAddress: String = "",
    val registrationNumber: String = "",
    val bankAccount: String = "",
    val logoUri: String? = null,
    val bannerUri: String? = null,
    val googleAccountLinked: Boolean = false,
    val googleEmail: String = "",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val shopName: String get() = name

    fun getCompletionPercentage(): Int {
        var score = 0
        if (name.isNotBlank() && name != "My Hardware Store") score += 20
        if (phone.isNotBlank()) score += 20
        if (address.isNotBlank() || streetAddress.isNotBlank()) score += 20
        if (registrationNumber.isNotBlank()) score += 20
        if (bankAccount.isNotBlank()) score += 20
        return score
    }

    fun getMissingFields(isBangla: Boolean): List<String> {
        val list = mutableListOf<String>()
        if (name.isBlank() || name == "My Hardware Store") list.add(if (isBangla) "দোকানের নাম" else "Store Name")
        if (phone.isBlank()) list.add(if (isBangla) "মোবাইল নম্বর" else "Mobile Number")
        if (address.isBlank() && streetAddress.isBlank()) list.add(if (isBangla) "ঠিকানা" else "Address")
        if (registrationNumber.isBlank()) list.add(if (isBangla) "ট্রেড লাইসেন্স / রেজিঃ" else "Trade License / Reg No")
        if (bankAccount.isBlank()) list.add(if (isBangla) "ব্যাংক বা বিকাশ নম্বর" else "Bank / bKash Account")
        return list
    }
}

object ProfileManager {
    private const val PREFS_NAME = "hardware_store_prefs"
    private const val KEY_PROFILES = "saved_business_profiles"
    private const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
    private const val KEY_FIRST_RUN_DONE = "first_run_setup_done"

    val defaultFallbackProfile = BusinessProfile(
        id = "profile_default",
        name = "Dokan Store",
        phone = "",
        address = "",
        businessType = "Retail Store",
        isDefault = true
    )

    var profiles by mutableStateOf<List<BusinessProfile>>(emptyList())
        private set

    var activeProfile by mutableStateOf<BusinessProfile>(defaultFallbackProfile)
        private set

    var isFirstRunSetupCompleted by mutableStateOf(false)
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isFirstRunSetupCompleted = prefs.getBoolean(KEY_FIRST_RUN_DONE, false)

        val rawProfiles = prefs.getString(KEY_PROFILES, null)
        val list = mutableListOf<BusinessProfile>()

        if (!rawProfiles.isNullOrBlank()) {
            try {
                val array = JSONArray(rawProfiles)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        BusinessProfile(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            phone = obj.optString("phone", ""),
                            address = obj.optString("address", ""),
                            businessType = obj.optString("businessType", "Retailer"),
                            category = obj.optString("category", "Hardware & Sanitary"),
                            email = obj.optString("email", ""),
                            ownerName = obj.optString("ownerName", ""),
                            division = obj.optString("division", "Dhaka"),
                            district = obj.optString("district", "Dhaka"),
                            streetAddress = obj.optString("streetAddress", ""),
                            registrationNumber = obj.optString("registrationNumber", ""),
                            bankAccount = obj.optString("bankAccount", ""),
                            googleAccountLinked = obj.optBoolean("googleAccountLinked", false),
                            googleEmail = obj.optString("googleEmail", ""),
                            isDefault = obj.optBoolean("isDefault", false),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            } catch (e: Exception) {
                // Ignore parse errors
            }
        }

        // If list is empty but first run was somehow marked done, seed a default profile
        if (list.isEmpty()) {
            val defaultProfile = BusinessProfile(
                id = "profile_default",
                name = "My Hardware Store",
                phone = "",
                address = "",
                businessType = "Hardware & Tools",
                isDefault = true
            )
            list.add(defaultProfile)
            saveProfiles(context, list)
        }

        profiles = list

        val activeId = prefs.getString(KEY_ACTIVE_PROFILE_ID, list.firstOrNull()?.id)
        activeProfile = list.find { it.id == activeId } ?: list.firstOrNull() ?: defaultFallbackProfile
    }

    fun completeFirstRunSetup(
        context: Context,
        storeName: String,
        phone: String,
        address: String,
        businessType: String
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val newProfile = BusinessProfile(
            id = "profile_1",
            name = storeName.trim().ifBlank { "My Hardware Store" },
            phone = phone.trim(),
            address = address.trim(),
            businessType = businessType.trim().ifBlank { "Hardware & Tools" },
            isDefault = true
        )
        val list = listOf(newProfile)
        profiles = list
        activeProfile = newProfile
        isFirstRunSetupCompleted = true

        prefs.edit()
            .putBoolean(KEY_FIRST_RUN_DONE, true)
            .putString(KEY_ACTIVE_PROFILE_ID, newProfile.id)
            .apply()

        saveProfiles(context, list)
    }

    fun switchProfile(context: Context, profileId: String) {
        val found = profiles.find { it.id == profileId } ?: return
        activeProfile = found
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ACTIVE_PROFILE_ID, profileId).apply()
    }

    fun createProfile(
        context: Context,
        name: String,
        phone: String,
        address: String,
        businessType: String
    ): BusinessProfile {
        val newProfile = BusinessProfile(
            id = "profile_${System.currentTimeMillis()}",
            name = name.trim().ifBlank { "New Business" },
            phone = phone.trim(),
            address = address.trim(),
            businessType = businessType.trim().ifBlank { "General Store" },
            isDefault = false
        )
        val updated = profiles.toMutableList().apply { add(newProfile) }
        profiles = updated
        activeProfile = newProfile

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ACTIVE_PROFILE_ID, newProfile.id).apply()
        saveProfiles(context, updated)
        return newProfile
    }

    fun updateProfile(context: Context, updatedProfile: BusinessProfile) {
        val updated = profiles.map { if (it.id == updatedProfile.id) updatedProfile else it }
        profiles = updated
        if (activeProfile.id == updatedProfile.id) {
            activeProfile = updatedProfile
        }
        saveProfiles(context, updated)
    }

    fun deleteProfile(context: Context, profileId: String): Boolean {
        if (profiles.size <= 1) return false // Cannot delete the only profile
        val updated = profiles.filterNot { it.id == profileId }
        profiles = updated
        if (activeProfile.id == profileId) {
            activeProfile = updated.firstOrNull() ?: defaultFallbackProfile
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_ACTIVE_PROFILE_ID, activeProfile.id).apply()
        }
        saveProfiles(context, updated)
        return true
    }

    private fun saveProfiles(context: Context, list: List<BusinessProfile>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()
        list.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("phone", p.phone)
                put("address", p.address)
                put("businessType", p.businessType)
                put("category", p.category)
                put("email", p.email)
                put("ownerName", p.ownerName)
                put("division", p.division)
                put("district", p.district)
                put("streetAddress", p.streetAddress)
                put("registrationNumber", p.registrationNumber)
                put("bankAccount", p.bankAccount)
                put("googleAccountLinked", p.googleAccountLinked)
                put("googleEmail", p.googleEmail)
                put("isDefault", p.isDefault)
                put("createdAt", p.createdAt)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_PROFILES, array.toString()).apply()
    }
}
