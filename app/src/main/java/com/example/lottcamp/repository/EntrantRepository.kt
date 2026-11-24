package com.example.lottcamp.repository

import com.example.lottcamp.data.EntrantDoc
import com.example.lottcamp.domain.Entrant
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FieldPath

class EntrantRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("entrants")

    suspend fun getEntrantByAndroidId(androidId: String): Entrant? {
        val snapshot = collection.document(androidId).get().await()
        return snapshot.toObject(EntrantDoc::class.java)?.toDomain()
    }

    fun observeEntrant(androidId: String): Flow<Entrant?> = callbackFlow {
        val listener = collection.document(androidId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(EntrantDoc::class.java)?.toDomain())
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveEntrant(entrant: Entrant) {
        val doc = entrant.toDoc()
        collection.document(doc.androidId).set(doc).await()
    }

    suspend fun deleteEntrant(androidId: String) {
        collection.document(androidId).delete().await()
    }

    /** The entrants whose Android ID is in [ids]. */
    suspend fun getByIds(ids: List<String>): List<Entrant> {
        if (ids.isEmpty()) return emptyList()
        // whereIn accepts at most 30 values per query
        return ids.distinct().chunked(30).flatMap { chunk ->
            collection.whereIn(FieldPath.documentId(), chunk).get().await().documents
                .mapNotNull { it.toObject(EntrantDoc::class.java)?.toDomain() }
        }
    }
}