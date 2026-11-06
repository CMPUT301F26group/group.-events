package com.example.lottcamp.repository

import com.example.lottcamp.data.CampsiteOwnerDoc
import com.example.lottcamp.domain.CampsiteOwner
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class CampsiteOwnerRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("campsite_owners")

    suspend fun getOwnerById(id: String): CampsiteOwner? {
        val snapshot = collection.document(id).get().await()
        return snapshot.toObject(CampsiteOwnerDoc::class.java)?.toDomain()
    }

    suspend fun saveOwner(owner: CampsiteOwner) {
        val doc = owner.toDoc()
        collection.document(doc.androidId).set(doc).await()
    }

    suspend fun deleteOwner(androidId: String) {
        collection.document(androidId).delete().await()
    }
}