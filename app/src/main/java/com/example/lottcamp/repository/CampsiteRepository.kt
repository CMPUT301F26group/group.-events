package com.example.lottcamp.repository

import com.example.lottcamp.data.CampsiteDoc
import com.example.lottcamp.domain.Campsite
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class CampsiteRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("campsites")

    suspend fun getCampsiteById(id: String): Campsite? {
        val snapshot = collection.document(id).get().await()
        return snapshot.toObject(CampsiteDoc::class.java)?.toDomain()
    }

    suspend fun getCampsitesByOwner(ownerId: String): List<Campsite> {
        val snapshot = collection.whereEqualTo("ownersAndroidId", ownerId).get().await()
        return snapshot.documents.mapNotNull { it.toObject(CampsiteDoc::class.java)?.toDomain() }
    }

    suspend fun getAllCampsites(): List<Campsite> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(CampsiteDoc::class.java)?.toDomain() }
    }

    suspend fun saveCampsite(campsite: Campsite): String {
        val doc = campsite.toDoc()
        return if (doc.id.isEmpty()) {
            val ref = collection.add(doc).await()
            ref.id
        } else {
            collection.document(doc.id).set(doc).await()
            doc.id
        }
    }

    suspend fun deleteCampsite(id: String) {
        collection.document(id).delete().await()
    }
}