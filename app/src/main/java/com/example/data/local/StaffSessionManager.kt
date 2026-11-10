package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.firebase.FirebaseManager
import com.example.data.local.entity.StaffMember
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class ActiveStaffSession(
    val staffId: String = "",
    val storeId: String = "",
    val storeName: String = "",
    val staffName: String = "",
    val role: String = "Sales", // Partner, Manager, Accountant, Sales, Stock, Entry
    val permissionLevel: String = "Limited", // Full, Half, Limited, Selected
    val allowedFeatures: List<String> = listOf("sales", "stock"),
    val isActive: Boolean = true
)

object StaffSessionManager {
    private const val TAG = "StaffSessionManager"
    private const val PREFS_NAME = "dokan_staff_session_prefs"
    private const val KEY_IS_STAFF_MODE = "is_staff_mode"
    private const val KEY_STAFF_ID = "staff_id"
    private const val KEY_STORE_ID = "store_id"
    private const val KEY_STORE_NAME = "store_name"
    private const val KEY_STAFF_NAME = "staff_name"
    private const val KEY_ROLE = "role"
    private const val KEY_PERM_LEVEL = "permission_level"
    private const val KEY_ALLOWED_FEATURES = "allowed_features"
    private const val KEY_IS_ACTIVE = "is_active"

    var currentSession by mutableStateOf<ActiveStaffSession?>(null)
        private set

    val isStaffSession: Boolean
        get() = currentSession != null

    private var activeStaffListener: ListenerRegistration? = null

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isStaff = prefs.getBoolean(KEY_IS_STAFF_MODE, false)
        if (isStaff) {
            val storeId = prefs.getString(KEY_STORE_ID, "") ?: ""
            val staffId = prefs.getString(KEY_STAFF_ID, "") ?: ""
            if (storeId.isNotBlank() && staffId.isNotBlank()) {
                val featsStr = prefs.getString(KEY_ALLOWED_FEATURES, "sales,stock") ?: "sales,stock"
                val feats = featsStr.split(",").map { it.trim().lowercase() }.filter { it.isNotBlank() }
                currentSession = ActiveStaffSession(
                    staffId = staffId,
                    storeId = storeId,
                    storeName = prefs.getString(KEY_STORE_NAME, "Dokan") ?: "Dokan",
                    staffName = prefs.getString(KEY_STAFF_NAME, "Staff") ?: "Staff",
                    role = prefs.getString(KEY_ROLE, "Sales") ?: "Sales",
                    permissionLevel = prefs.getString(KEY_PERM_LEVEL, "Limited") ?: "Limited",
                    allowedFeatures = feats,
                    isActive = prefs.getBoolean(KEY_IS_ACTIVE, true)
                )
                startStaffRealtimeListener(context, storeId, staffId)
            }
        }
    }

    /**
     * Checks whether the current staff member has access to a specific feature.
     * Owners have access to everything.
     * Staff accounts are NEVER admin even if Full permission.
     */
    fun hasPermission(feature: String): Boolean {
        val session = currentSession ?: return true // Owner has full access
        if (!session.isActive) return false // Paused or removed
        val normalized = feature.trim().lowercase()
        // Administrative features are NEVER accessible to staff
        if (normalized == "admin" || normalized == "delete_store" || normalized == "manage_staff" || normalized == "app_lock") {
            return false
        }
        if (session.permissionLevel.equals("Full", ignoreCase = true)) {
            // Full staff gets business operations, but never owner admin settings
            return true
        }
        return session.allowedFeatures.contains(normalized)
    }

    /**
     * Start listening to Firestore /stores/{storeId}/staff/{staffId} in real-time.
     * If the owner pauses or deletes the staff, it takes effect IMMEDIATELY on the staff's phone.
     */
    fun startStaffRealtimeListener(context: Context, storeId: String, staffId: String) {
        activeStaffListener?.remove()
        try {
            val db = FirebaseManager.getFirestore(context)
            activeStaffListener = db.collection("stores").document(storeId)
                .collection("staff").document(staffId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Staff listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot == null || !snapshot.exists()) {
                        // Staff removed by owner!
                        Log.i(TAG, "Staff document deleted by owner! Revoking access immediately.")
                        onStaffRevoked(context)
                        return@addSnapshotListener
                    }
                    val isActive = snapshot.getBoolean("isActive") ?: false
                    val role = snapshot.getString("role") ?: "Sales"
                    val permissionLevel = snapshot.getString("permissionLevel") ?: "Limited"
                    val allowedFeatsList = snapshot.get("permissions") as? List<*>
                    val feats = allowedFeatsList?.mapNotNull { it?.toString()?.lowercase() }
                        ?: listOf("sales", "stock")

                    val updatedSession = currentSession?.copy(
                        role = role,
                        permissionLevel = permissionLevel,
                        allowedFeatures = feats,
                        isActive = isActive
                    ) ?: ActiveStaffSession(
                        staffId = staffId,
                        storeId = storeId,
                        role = role,
                        permissionLevel = permissionLevel,
                        allowedFeatures = feats,
                        isActive = isActive
                    )

                    currentSession = updatedSession
                    saveSessionToPrefs(context, updatedSession)
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start staff listener: ${e.message}")
        }
    }

    private fun onStaffRevoked(context: Context) {
        activeStaffListener?.remove()
        activeStaffListener = null
        currentSession = currentSession?.copy(isActive = false)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_ACTIVE, false).apply()
    }

    /**
     * Join store as staff using a scanned or entered one-time sync token / QR payload.
     * Payload format:
     * DOKAN_STAFF_INVITE:storeId:storeName:staffId:staffName:role:permissionLevel:expiresAt
     */
    suspend fun joinStoreWithInvite(
        context: Context,
        invitePayload: String
    ): Result<ActiveStaffSession> = withContext(Dispatchers.IO) {
        try {
            val trimmed = invitePayload.trim()
            if (!trimmed.startsWith("DOKAN_STAFF_INVITE:")) {
                return@withContext Result.failure(IllegalArgumentException("Invalid QR Code or Invitation Token"))
            }
            val parts = trimmed.split(":")
            if (parts.size < 8) {
                return@withContext Result.failure(IllegalArgumentException("Malformed invitation token format"))
            }

            val storeId = parts[1]
            val storeName = parts[2]
            val staffDocId = parts[3]
            val staffName = parts[4]
            val role = parts[5]
            val permLevel = parts[6]
            val expiresAt = parts[7].toLongOrNull() ?: 0L

            val now = System.currentTimeMillis()
            if (now > expiresAt) {
                return@withContext Result.failure(IllegalStateException("QR Code has expired! Please ask the owner to generate a fresh 5-minute QR code."))
            }

            val currentUser = Firebase.auth.currentUser
            val currentUid = currentUser?.uid ?: ""

            // Link to Firestore
            val db = FirebaseManager.getFirestore(context)
            val staffRef = db.collection("stores").document(storeId).collection("staff").document(staffDocId)
            val snapshot = staffRef.get().await()

            if (!snapshot.exists()) {
                return@withContext Result.failure(IllegalStateException("Staff profile not found on store. Please contact the store owner."))
            }

            val isActive = snapshot.getBoolean("isActive") ?: true
            if (!isActive) {
                return@withContext Result.failure(IllegalStateException("This staff account is currently paused or inactive."))
            }

            // Bind current signed-in Google auth UID if available
            if (currentUid.isNotBlank()) {
                staffRef.set(
                    mapOf(
                        "authUid" to currentUid,
                        "lastJoinedAt" to now,
                        "deviceLinked" to true
                    ),
                    SetOptions.merge()
                ).await()
            }

            val permissionsList = (snapshot.get("permissions") as? List<*>)?.mapNotNull { it?.toString()?.lowercase() }
                ?: listOf("sales", "stock")

            val session = ActiveStaffSession(
                staffId = staffDocId,
                storeId = storeId,
                storeName = storeName,
                staffName = staffName,
                role = role,
                permissionLevel = permLevel,
                allowedFeatures = permissionsList,
                isActive = true
            )

            withContext(Dispatchers.Main) {
                currentSession = session
                saveSessionToPrefs(context, session)
                startStaffRealtimeListener(context, storeId, staffDocId)
            }

            Result.success(session)
        } catch (e: Exception) {
            Log.e(TAG, "joinStoreWithInvite failed", e)
            Result.failure(e)
        }
    }

    /**
     * Generates a 5-minute expiring QR invite token for a specific staff member.
     */
    fun generateExpiringQrPayload(
        storeId: String,
        storeName: String,
        staff: StaffMember
    ): String {
        val fiveMinutesFromNow = System.currentTimeMillis() + (5 * 60 * 1000L)
        val cleanStoreName = storeName.replace(":", " ").trim()
        val cleanStaffName = staff.name.replace(":", " ").trim()
        return "DOKAN_STAFF_INVITE:$storeId:$cleanStoreName:${staff.id}:$cleanStaffName:${staff.role}:${staff.permissionLevel}:$fiveMinutesFromNow"
    }

    fun leaveStaffSession(context: Context) {
        activeStaffListener?.remove()
        activeStaffListener = null
        currentSession = null
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    private fun saveSessionToPrefs(context: Context, session: ActiveStaffSession) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_IS_STAFF_MODE, true)
            .putString(KEY_STAFF_ID, session.staffId)
            .putString(KEY_STORE_ID, session.storeId)
            .putString(KEY_STORE_NAME, session.storeName)
            .putString(KEY_STAFF_NAME, session.staffName)
            .putString(KEY_ROLE, session.role)
            .putString(KEY_PERM_LEVEL, session.permissionLevel)
            .putString(KEY_ALLOWED_FEATURES, session.allowedFeatures.joinToString(","))
            .putBoolean(KEY_IS_ACTIVE, session.isActive)
            .apply()
    }
}
