package com.example.lottcamp.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.lottcamp.data.ReservationDoc
import com.example.lottcamp.domain.Reservation
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class ReservationRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("reservations")

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getReservationsForEntrant(entrantId: String): List<Reservation> {
        val snapshot = collection.whereEqualTo("entrantId", entrantId).get().await()
        return snapshot.documents.mapNotNull { it.toObject(ReservationDoc::class.java)?.toDomain() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getReservationsForCampsite(campsiteId: String): List<Reservation> {
        val snapshot = collection.whereEqualTo("campsiteId", campsiteId).get().await()
        return snapshot.documents.mapNotNull { it.toObject(ReservationDoc::class.java)?.toDomain() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveReservation(reservation: Reservation): String {
        val doc = reservation.toDoc()
        return if (doc.id.isEmpty()) {
            val ref = collection.add(doc).await()
            ref.id
        } else {
            collection.document(doc.id).set(doc).await()
            doc.id
        }
    }
    suspend fun deleteReservation(id: String) {
        collection.document(id).delete().await()
    }
    suspend fun updateReservationStatus(id: String, statusName: String) {
        collection.document(id).update("status", statusName).await()
    }
}