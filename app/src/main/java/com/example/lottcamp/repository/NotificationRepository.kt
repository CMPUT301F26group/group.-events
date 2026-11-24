package com.example.lottcamp.repository

import com.example.lottcamp.data.NotificationDoc
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class NotificationRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("notifications")

    /** Writes every notification in [docs]. Batches are limited in size, so they are chunked. */
    suspend fun send(docs: List<NotificationDoc>) {
        docs.chunked(400).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { batch.set(collection.document(), it) }
            batch.commit().await()
        }
    }

    /** Every notification sent by the owner with [ownerId]. */
    suspend fun getSentBy(ownerId: String): List<NotificationDoc> {
        val snapshot = collection.whereEqualTo("senderId", ownerId).get().await()
        return snapshot.documents.mapNotNull { it.toObject(NotificationDoc::class.java) }
    }
}