package com.example.lottcamp.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class LotteryDoc(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val campsiteIds: List<String> = emptyList(),
    val registrationStart: Timestamp? = null,
    val registrationEnd: Timestamp? = null,
    val bookingStart: Timestamp? = null,
    val bookingEnd: Timestamp? = null,
    val winnerCount: Int = 0,
    val waitlistCapacity: Int? = null
)