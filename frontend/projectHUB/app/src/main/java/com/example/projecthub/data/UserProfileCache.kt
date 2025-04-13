// Create a singleton UserProfileCache object in a new file named UserProfileCache.kt
package com.example.projecthub.data

import androidx.compose.runtime.mutableStateMapOf
import com.example.projecthub.R
import com.google.firebase.firestore.FirebaseFirestore

object UserProfileCache {
    // Maps userId to profile data
    private val profileCache = mutableStateMapOf<String, UserProfileData>()
    private var isInitialized = false

    data class UserProfileData(
        val name: String,
        val profilePhotoId: Int
    )

    fun getUserName(userId: String): String {
        return profileCache[userId]?.name ?: "Unknown User"
    }

    fun getProfilePhotoId(userId: String): Int {
        return profileCache[userId]?.profilePhotoId ?: R.drawable.profilephoto1
    }

    fun preloadUserProfiles(onComplete: () -> Unit = {}) {
        if (isInitialized) {
            onComplete()
            return
        }

        FirebaseFirestore.getInstance().collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result.documents) {
                    val userId = document.id
                    val name = document.getString("name") ?: "Unknown User"
                    val photoId = document.getLong("profilePhotoId")?.toInt() ?: R.drawable.profilephoto1

                    profileCache[userId] = UserProfileData(name, photoId)
                }
                isInitialized = true
                onComplete()
            }
    }
}