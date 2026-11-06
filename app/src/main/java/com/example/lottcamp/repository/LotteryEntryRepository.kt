package com.example.lottcamp.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.lottcamp.data.LotteryEntryDoc
import com.example.lottcamp.domain.LotteryEntry
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class LotteryEntryRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("lottery_entries")

    suspend fun getEntriesForLottery(lotteryId: String): List<LotteryEntry> {
        val snapshot = collection.whereEqualTo("lotteryId", lotteryId).get().await()
        return snapshot.documents.mapNotNull { it.toObject(LotteryEntryDoc::class.java)?.toDomain() }
    }

    suspend fun getEntriesForEntrant(entrantId: String): List<LotteryEntry> {
        val snapshot = collection.whereEqualTo("entrantId", entrantId).get().await()
        return snapshot.documents.mapNotNull { it.toObject(LotteryEntryDoc::class.java)?.toDomain() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveEntry(entry: LotteryEntry): String {
        val doc = entry.toDoc()
        return if (doc.id.isEmpty()) {
            val ref = collection.add(doc).await()
            ref.id
        } else {
            collection.document(doc.id).set(doc).await()
            doc.id
        }
    }

    suspend fun deleteEntry(id: String) {
        collection.document(id).delete().await()
    }
}
