package com.example.lottcamp.repository

import com.example.lottcamp.data.CampsiteDoc
import com.example.lottcamp.data.ReviewDoc
import com.example.lottcamp.domain.Campsite
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class CampsiteRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("campsites")
    private val reviews = db.collection("reviews")

    suspend fun getCampsiteById(id: String): Campsite? {
        val snapshot = collection.document(id).get().await()
        val campsite = snapshot.toObject(CampsiteDoc::class.java)?.toDomain() ?: return null
        return campsite.withReviewRatings()
    }

    suspend fun getCampsitesByOwner(ownerId: String): List<Campsite> {
        val snapshot = collection.whereEqualTo("ownersAndroidId", ownerId).get().await()
        return snapshot.documents
            .mapNotNull { it.toObject(CampsiteDoc::class.java)?.toDomain() }
            .withReviewRatings()
    }

    suspend fun getAllCampsites(): List<Campsite> {
        val snapshot = collection.get().await()
        return snapshot.documents
            .mapNotNull { it.toObject(CampsiteDoc::class.java)?.toDomain() }
            .withReviewRatings()
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

    suspend fun searchCampsitesByName(query: String): List<Campsite> {
        val snapshot = collection
            .orderBy("name")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
            .await()
        return snapshot.documents
            .mapNotNull { it.toObject(CampsiteDoc::class.java)?.toDomain() }
            .withReviewRatings()
    }

    suspend fun searchCampsitesByLocation(location: String): List<Campsite> {
        val snapshot = collection
            .orderBy("locationName")
            .startAt(location)
            .endAt(location + "\uf8ff")
            .get()
            .await()
        return snapshot.documents
            .mapNotNull { it.toObject(CampsiteDoc::class.java)?.toDomain() }
            .withReviewRatings()
    }

    suspend fun searchCampsitesByTags(tags: List<String>): List<Campsite> {
        if (tags.isEmpty()) return getAllCampsites()

        val snapshot = collection
            .whereArrayContains("tags", tags.first())
            .get()
            .await()

        return snapshot.documents
            .mapNotNull { it.toObject(CampsiteDoc::class.java)?.toDomain() }
            .withReviewRatings()
            .filter { campsite -> campsite.tags.containsAll(tags) }
    }

    suspend fun searchCampsitesByOccupancy(guests: Int): List<Campsite> {
        val snapshot = collection
            .whereGreaterThanOrEqualTo("maxOccupancy", guests)
            .get()
            .await()
        return snapshot.documents
            .mapNotNull { it.toObject(CampsiteDoc::class.java)?.toDomain() }
            .withReviewRatings()
    }

    suspend fun searchCampsitesByMaxPrice(maxPricePerNight: Double): List<Campsite> {
        val snapshot = collection
            .whereLessThanOrEqualTo("pricePerNight", maxPricePerNight)
            .get()
            .await()
        return snapshot.documents
            .mapNotNull { it.toObject(CampsiteDoc::class.java)?.toDomain() }
            .withReviewRatings()
    }

    suspend fun getAllTags(): List<String> {
        return getAllCampsites().flatMap { it.tags }.distinct()
    }

    /**
     * Replaces the stored rating counters with values computed from the actual reviews.
     *
     * The counters on a campsite document can drift when a review is edited or removed, which
     * would hide the rating on the entrant and admin screens. Campsites without reviews keep the
     * counters they were saved with.
     */
    private suspend fun List<Campsite>.withReviewRatings(): List<Campsite> {
        if (isEmpty()) return this
        val docs = try {
            reviews.get().await().toObjects(ReviewDoc::class.java)
        } catch (e: Exception) {
            // The caller may not be allowed to read reviews; keep the stored counters.
            return this
        }
        val ratings = docs.groupBy { it.campsiteId }
            .mapValues { (_, list) -> list.sumOf { it.rating } to list.size }
        return map { campsite ->
            val (sum, count) = ratings[campsite.id] ?: return@map campsite
            campsite.copy(ratingSum = sum, ratingCount = count)
        }
    }

    private suspend fun Campsite.withReviewRatings(): Campsite {
        val docs = try {
            reviews.whereEqualTo("campsiteId", id).get().await()
                .toObjects(ReviewDoc::class.java)
        } catch (e: Exception) {
            // The caller may not be allowed to read reviews; keep the stored counters.
            return this
        }
        return if (docs.isEmpty()) this
        else copy(ratingSum = docs.sumOf { it.rating }, ratingCount = docs.size)
    }
}