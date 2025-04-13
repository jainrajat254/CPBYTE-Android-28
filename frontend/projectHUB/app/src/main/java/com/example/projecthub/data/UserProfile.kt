package com.example.projecthub.data

import com.example.projecthub.R

data class UserProfile(
    val name: String = "",
    val bio: String = "",
    val collegeName: String = "",
    val semester: String = "",
    val collegeLocation: String = "",
    val skills: List<String> = emptyList(),
    val profilePhotoId: Int = R.drawable.profilephoto1
)