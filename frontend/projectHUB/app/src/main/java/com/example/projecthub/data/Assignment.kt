package com.example.projecthub.data

import com.google.firebase.Timestamp

data class Assignment(
    val id : String =" ",
    val title : String = "",
    val description : String = "",
    val subject : String = "",
    val deadline : String = "",
    val budget : Int = 0,
    val createdBy : String = "",
    val timestamp : Timestamp = Timestamp.now(),
)
