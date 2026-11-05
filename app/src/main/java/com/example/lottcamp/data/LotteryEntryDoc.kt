package com.example.lottcamp.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId


data class LotteryEntryDoc(
    @DocumentId val id: String = "",
    val lotteryId: String = "",
    val entrantId: String = "",
    val status: String = EntryStatus.WAITING.name,
    val joinedAt: Timestamp = Timestamp.now()
)
