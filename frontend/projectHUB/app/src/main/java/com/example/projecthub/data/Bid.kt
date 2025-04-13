package com.example.projecthub.data

import com.google.firebase.Timestamp

data class Bid(
    val id : String = "",
    val assignmentId: String = "",
    val bidderId : String = "",
    val bidderName: String = "",
    val bidAmount: Int = 0,
    val status: String = "pending",
    val enterCompletionDate : String = " ",
    val timestamp: Timestamp = Timestamp.now()
)
