package com.example.lottcamp.repository

import com.example.lottcamp.data.EntrantDoc
import com.example.lottcamp.data.ReservationStatus
import com.example.lottcamp.data.ReviewDoc
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.domain.Entrant
import com.example.lottcamp.domain.Lottery
import com.example.lottcamp.domain.LotteryEntry
import com.example.lottcamp.domain.Reservation
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Everything the entrant screens read from and change in Firestore.
 *
 * The class and its functions are `open` so [SampleEntrantRepository] can replace them
 * with in-memory data for tests and previews. The database is only reached when a
 * function that needs it runs, so a sample repository never touches Firebase.
 *
 * @param dbProvider Gives the Firestore instance to use.
 */
open class EntrantRepository(
    dbProvider: () -> FirebaseFirestore = { Firebase.firestore },
) {
    /** Uses an existing Firestore instance, keeping the original call style. */
    constructor(db: FirebaseFirestore) : this({ db })

    private val db by lazy(dbProvider)
    private val entrants by lazy { db.collection("entrants") }
    private val reviews by lazy { db.collection("reviews") }
    private val campsiteRepo by lazy { CampsiteRepository(db) }
    private val reservationRepo by lazy { ReservationRepository(db) }
    private val lotteryEntryRepo by lazy { LotteryEntryRepository(db) }
    private val lotteryRepo by lazy { LotteryRepository(db) }

    // --- Entrant profile ---

    open suspend fun getEntrantByAndroidId(androidId: String): Entrant? {
        val snapshot = entrants.document(androidId).get().await()
        return snapshot.toObject(EntrantDoc::class.java)?.toDomain()
    }

    open fun observeEntrant(androidId: String): Flow<Entrant?> = callbackFlow {
        val listener = entrants.document(androidId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(EntrantDoc::class.java)?.toDomain())
        }
        awaitClose { listener.remove() }
    }

    open suspend fun saveEntrant(entrant: Entrant) {
        val doc = entrant.toDoc()
        entrants.document(doc.androidId).set(doc).await()
    }

    open suspend fun deleteEntrant(androidId: String) {
        entrants.document(androidId).delete().await()
    }

    open suspend fun getByIds(ids: List<String>): List<Entrant> {
        if (ids.isEmpty()) return emptyList()
        return ids.distinct().chunked(30).flatMap { chunk ->
            entrants.whereIn(FieldPath.documentId(), chunk).get().await().documents
                .mapNotNull { it.toObject(EntrantDoc::class.java)?.toDomain() }
        }
    }

    // --- Campsites ---

    /** Every campsite, used to show names and locations on booking cards. */
    open suspend fun getCampsites(): List<Campsite> = campsiteRepo.getAllCampsites()

    /** The campsite with [campsiteId], or null if it does not exist. */
    open suspend fun getCampsite(campsiteId: String): Campsite? =
        campsiteRepo.getCampsiteById(campsiteId)

    // --- Reviews ---

    /**
     * Stores [rating] and [comment] as a review of [campsiteId] by [entrantId], and adds the
     * rating to the campsite's average.
     *
     * The rating is saved as a float and the comment as a string. The review and the campsite's
     * updated `ratingSum` / `ratingCount` are written in one batch, so they stay in sync.
     */
    open suspend fun submitReview(
        campsiteId: String,
        entrantId: String,
        rating: Double,
        comment: String,
    ) {
        require(rating in 1.0..5.0) { "Rating must be between 1.0 and 5.0" }
        val entrant = getEntrantByAndroidId(entrantId)
        val review = ReviewDoc(
            campsiteId = campsiteId,
            authorId = entrantId,
            authorName = entrant?.name ?: "Anonymous",
            rating = rating,
            comment = comment.trim(),
            createdAt = Timestamp.now(),
        )

        val batch = db.batch()
        batch.set(reviews.document(), review)
        batch.update(
            db.collection("campsites").document(campsiteId),
            "ratingSum", FieldValue.increment(rating),
            "ratingCount", FieldValue.increment(1),
        )
        batch.commit().await()
    }

    // --- Reservations ---

    /** Every reservation belonging to [entrantId]. */
    open suspend fun getReservations(entrantId: String): List<Reservation> =
        reservationRepo.getReservationsForEntrant(entrantId)

    /** Marks the reservation with [reservationId] as cancelled. */
    open suspend fun cancelReservation(reservationId: String) {
        reservationRepo.updateReservationStatus(reservationId, ReservationStatus.CANCELLED.name)
    }

    // --- Waiting lists ---

    /** Every lottery entry belonging to [entrantId]. */
    open suspend fun getLotteryEntries(entrantId: String): List<LotteryEntry> =
        lotteryEntryRepo.getEntriesForEntrant(entrantId)

    /** The lottery with [lotteryId], or null if it does not exist. */
    open suspend fun getLottery(lotteryId: String): Lottery? =
        lotteryRepo.getLotteryById(lotteryId)

    /** Number of entrants waiting on the lottery with [lotteryId]. */
    open suspend fun getLotteryEntryCount(lotteryId: String): Int =
        lotteryEntryRepo.getEntriesForLottery(lotteryId).size

    /** Removes the entry with [entryId], taking the entrant off the waiting list. */
    open suspend fun leaveWaitingList(entryId: String) {
        lotteryEntryRepo.deleteEntry(entryId)
    }
}
