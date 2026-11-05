package com.example.lottcamp.data
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ReservationDoc(
    @DocumentId val id: String = "",
    val entrantId: String = "",
    val campsiteId: String = "",
    val lotteryId: String = "",
    val checkIn: Timestamp? = null,
    val checkOut: Timestamp? = null,
    val status: String = ReservationStatus.ACTIVE.name,
    val createdAt: Timestamp = Timestamp.now()
)