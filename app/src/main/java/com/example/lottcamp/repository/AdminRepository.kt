package com.example.lottcamp.repository

import com.example.lottcamp.domain.Admin
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class AdminRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("admins")

    suspend fun isAdmin(id: String): Boolean {
        val snapshot = collection.document(id).get().await()
        return snapshot.exists()
    }

    suspend fun saveAdmin(admin: Admin) {
        val doc = admin.toDoc()
        collection.document(doc.id).set(doc).await()
    }
}