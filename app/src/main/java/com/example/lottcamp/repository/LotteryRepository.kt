package com.example.lottcamp.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.lottcamp.data.CampsiteDoc
import com.example.lottcamp.data.LotteryDoc

import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.domain.Lottery
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class LotteryRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("lotteries")
    private val campsiteCollection = db.collection("campsites")

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getLotteryById(id: String): Lottery? {
        val snapshot = collection.document(id).get().await()
        val lotteryDoc = snapshot.toObject(LotteryDoc::class.java) ?: return null
        val campsites = fetchCampsitesByIds(lotteryDoc.campsiteIds)
        return lotteryDoc.toDomain(campsites)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getActiveLotteries(): List<Lottery> {
        val snapshot = collection.get().await()
        val docs = snapshot.documents.mapNotNull { it.toObject(LotteryDoc::class.java) }

        // Gather all unique campsite IDs across all lotteries
        val allCampsiteIds = docs.flatMap { it.campsiteIds }.distinct()
        val campsiteMap = fetchCampsitesByIds(allCampsiteIds).associateBy { it.id }

        return docs.map { doc ->
            val campsites = doc.campsiteIds.mapNotNull { campsiteMap[it] }
            doc.toDomain(campsites)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveLottery(lottery: Lottery): String {
        val doc = lottery.toDoc()
        return if (doc.id.isEmpty()) {
            val ref = collection.add(doc).await()
            ref.id
        } else {
            collection.document(doc.id).set(doc).await()
            doc.id
        }
    }

    suspend fun deleteLottery(id: String) {
        collection.document(id).delete().await()
    }

    private suspend fun fetchCampsitesByIds(ids: List<String>): List<Campsite> {
        if (ids.isEmpty()) return emptyList()

        // Chunk by 30 to comply with Firestore whereIn limits
        val chunks = ids.chunked(30)
        return coroutineScope {
            val deferreds = chunks.map { chunk ->
                async {
                    campsiteCollection.whereIn(FieldPath.documentId(), chunk)
                        .get()
                        .await()
                        .documents
                        .mapNotNull { it.toObject(CampsiteDoc::class.java)?.toDomain() }
                }
            }
            deferreds.awaitAll().flatten()
        }
    }
}