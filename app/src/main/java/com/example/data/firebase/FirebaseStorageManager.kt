package com.example.data.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

object FirebaseStorageManager {
    private const val TAG = "FirebaseStorageManager"

    suspend fun uploadProductPhoto(
        context: Context,
        storeId: String,
        localUriString: String
    ): String = withContext(Dispatchers.IO) {
        uploadFile(context, storeId, "products", localUriString)
    }

    suspend fun uploadBillPhoto(
        context: Context,
        storeId: String,
        localUriString: String
    ): String = withContext(Dispatchers.IO) {
        uploadFile(context, storeId, "bills", localUriString)
    }

    suspend fun uploadStoreLogo(
        context: Context,
        storeId: String,
        localUriString: String
    ): String = withContext(Dispatchers.IO) {
        uploadFile(context, storeId, "logos", localUriString)
    }

    private suspend fun uploadFile(
        context: Context,
        storeId: String,
        folder: String,
        localUriString: String
    ): String {
        if (localUriString.isBlank() || localUriString.startsWith("http://") || localUriString.startsWith("https://")) {
            return localUriString
        }

        return try {
            val storageRef = Firebase.storage.reference
            val fileName = "${folder}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
            val fileRef = storageRef.child("stores/$storeId/$folder/$fileName")

            val uri = Uri.parse(localUriString)
            val uploadTask = if (localUriString.startsWith("content://") || localUriString.startsWith("file://")) {
                fileRef.putFile(uri).await()
            } else {
                val file = File(localUriString)
                if (file.exists()) {
                    fileRef.putFile(Uri.fromFile(file)).await()
                } else {
                    return localUriString
                }
            }

            val downloadUrl = fileRef.downloadUrl.await().toString()
            Log.d(TAG, "Uploaded $folder image to Firebase Storage: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.w(TAG, "Storage upload skipped/failed (will retain local path): ${e.message}")
            localUriString
        }
    }
}
