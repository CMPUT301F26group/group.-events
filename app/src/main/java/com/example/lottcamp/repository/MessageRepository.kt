package com.example.lottcamp.repository

import com.example.lottcamp.data.MessageDoc
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class MessageRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("messages")

    suspend fun send(doc: MessageDoc) {
        collection.add(doc).await()
    }

    /** Every message about the reservation with [reservationId], oldest first. */
    suspend fun getForReservation(reservationId: String): List<MessageDoc> {
        val snapshot = collection.whereEqualTo("reservationId", reservationId).get().await()
        return snapshot.documents
            .mapNotNull { it.toObject(MessageDoc::class.java) }
            .sortedBy { it.sentAt?.seconds ?: 0L }
    }
}