package com.example.lottcamp.repository

import com.example.lottcamp.data.CampsiteOwnerDoc
import com.example.lottcamp.data.EntrantDoc
import com.example.lottcamp.data.MessageDoc
import com.example.lottcamp.data.NotificationDoc
import com.example.lottcamp.data.ReviewDoc
import com.example.lottcamp.domain.Admin
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.domain.CampsiteActivity
import com.example.lottcamp.domain.EntrantActivity
import com.example.lottcamp.domain.ImageType
import com.example.lottcamp.domain.MessageLog
import com.example.lottcamp.domain.NotificationLog
import com.example.lottcamp.domain.OwnerActivity
import com.example.lottcamp.domain.Review
import com.example.lottcamp.domain.UploadedImage
import com.example.lottcamp.domain.UserProfile
import com.example.lottcamp.domain.UserRole
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
 * Everything the admin screens read from and remove in Firestore.
 *
 * The class and its functions are `open` so [SampleAdminRepository] can replace them with
 * in-memory data for tests and previews. The database is only reached when a function that
 * needs it runs, so a sample repository never touches Firebase.
 *
 * @param dbProvider Gives the Firestore instance to use.
 */
open class AdminRepository(
    dbProvider: () -> FirebaseFirestore = { Firebase.firestore },
) {
    private val db by lazy(dbProvider)
    private val admins by lazy { db.collection("admins") }
    private val entrants by lazy { db.collection("entrants") }
    private val owners by lazy { db.collection("campsite_owners") }
    private val campsites by lazy { db.collection("campsites") }
    private val lotteries by lazy { db.collection("lotteries") }
    private val lotteryEntries by lazy { db.collection("lottery_entries") }
    private val reservations by lazy { db.collection("reservations") }
    private val reviews by lazy { db.collection("reviews") }
    private val notifications by lazy { db.collection("notifications") }
    private val messages by lazy { db.collection("messages") }
    private val campsiteRepo by lazy { CampsiteRepository(db) }

    open suspend fun isAdmin(id: String): Boolean {
        val snapshot = admins.document(id).get().await()
        return snapshot.exists()
    }

    open suspend fun saveAdmin(admin: Admin) {
        val doc = admin.toDoc()
        admins.document(doc.id).set(doc).await()
    }

    /** Every owner and entrant. Entrants whose ID is in `admins` are admins. */
    open suspend fun getUsers(): List<UserProfile> {
        val adminIds = admins.get().await().documents.map { it.id }
        val ownerUsers = owners.get().await().toObjects(CampsiteOwnerDoc::class.java).map {
            UserProfile(it.androidId, it.name, it.email, it.phoneNumber, UserRole.OWNER)
        }
        val entrantUsers = entrants.get().await().toObjects(EntrantDoc::class.java).map {
            val role = if (it.androidId in adminIds) UserRole.ADMIN else UserRole.ENTRANT
            UserProfile(it.androidId, it.name, it.email, it.phoneNumber, role, it.profilePictureUrl)
        }
        // Someone who is both an owner and an entrant is listed once, as an owner
        return (ownerUsers + entrantUsers).distinctBy { it.id }
    }

    /** Every campsite. */
    open suspend fun getCampsites(): List<Campsite> = campsiteRepo.getAllCampsites()

    /** Every campsite review. */
    open suspend fun getReviews(): List<Review> {
        val docs = reviews.get().await().toObjects(ReviewDoc::class.java)
        return docs.map { Review(it.id, it.campsiteId, it.authorName, it.rating, it.text) }
    }

    /** Every logged notification, newest first. */
    open suspend fun getNotifications(): List<NotificationLog> {
        val docs = notifications.get().await().toObjects(NotificationDoc::class.java)
        val logs = docs.map {
            NotificationLog(
                it.id, it.title, it.senderName, it.senderId, it.recipientName, it.recipientId,
                it.message, toMillis(it.sentAt), it.status,
            )
        }
        return logs.sortedByDescending { it.sentAt }
    }

    /** Every logged owner to entrant message, newest first. */
    open suspend fun getMessages(): List<MessageLog> {
        val docs = messages.get().await().toObjects(MessageDoc::class.java)
        val logs = docs.map {
            MessageLog(it.id, it.senderName, it.recipientName, it.campsiteName, it.reservationId, it.message, toMillis(it.sentAt))
        }
        return logs.sortedByDescending { it.sentAt }
    }

    /** Activity counts for the entrant with [userId]. */
    open suspend fun getEntrantActivity(userId: String): EntrantActivity {
        val myReservations = reservations.whereEqualTo("entrantId", userId)
        val active = myReservations.whereEqualTo("status", "ACTIVE").get().await().size()
        val past = myReservations.whereEqualTo("status", "COMPLETED").get().await().size()
        val waiting = lotteryEntries.whereEqualTo("entrantId", userId)
            .whereEqualTo("status", "WAITING").get().await().size()
        return EntrantActivity(active, past, waiting)
    }

    /** Activity counts for the campsite owner with [ownerId]. */
    open suspend fun getOwnerActivity(ownerId: String): OwnerActivity {
        val campsiteIds = campsiteRepo.getCampsitesByOwner(ownerId).map { it.id }
        val ownerLotteries = lotteries.whereEqualTo("ownerId", ownerId).get().await().documents
        val activeLotteries = ownerLotteries.count { isActive(it) }

        var activeReservations = 0
        for (campsiteId in campsiteIds) {
            activeReservations += countActiveReservations(campsiteId)
        }
        return OwnerActivity(campsiteIds.size, activeLotteries, activeReservations)
    }

    /** Activity counts for the campsite with [campsiteId]. */
    open suspend fun getCampsiteActivity(campsiteId: String): CampsiteActivity {
        val campsiteLotteries = lotteries.whereArrayContains("campsiteIds", campsiteId).get().await().documents
        return CampsiteActivity(countActiveReservations(campsiteId), campsiteLotteries.count { isActive(it) })
    }

    /** Deletes [user]'s profile. Deleting an owner also deletes their campsites. */
    open suspend fun removeUser(user: UserProfile) {
        if (user.role == UserRole.OWNER) {
            for (campsite in campsiteRepo.getCampsitesByOwner(user.id)) {
                campsiteRepo.deleteCampsite(campsite.id)
            }
            owners.document(user.id).delete().await()
        } else {
            entrants.document(user.id).delete().await()
        }
    }

    /** Deletes the campsite with [campsiteId]. */
    open suspend fun removeCampsite(campsiteId: String) {
        campsiteRepo.deleteCampsite(campsiteId)
    }

    /** Deletes [review] and takes its rating out of the campsite's average. */
    open suspend fun removeReview(review: Review) {
        reviews.document(review.id).delete().await()
        campsites.document(review.campsiteId).update(
            "ratingSum", FieldValue.increment(-review.rating.toDouble()),
            "ratingCount", FieldValue.increment(-1),
        ).await()
    }

    /** Clears [image] from the campsite or profile that uses it. */
    open suspend fun removeImage(image: UploadedImage) {
        if (image.type == ImageType.CAMPSITE) {
            campsites.document(image.sourceId).update("imageUrl", null).await()
        } else {
            entrants.document(image.sourceId).update("profilePictureUrl", null).await()
        }
    }

    /** Number of active reservations for the campsite with [campsiteId]. */
    private suspend fun countActiveReservations(campsiteId: String): Int =
        reservations.whereEqualTo("campsiteId", campsiteId)
            .whereEqualTo("status", "ACTIVE").get().await().size()

    /** A lottery is active until its booking window ends. */
    private fun isActive(lottery: DocumentSnapshot): Boolean {
        val bookingEnd = lottery.getTimestamp("bookingEnd") ?: return false
        return bookingEnd > Timestamp.now()
    }

    /** [time] in milliseconds since 1970, or 0 if it is missing. */
    private fun toMillis(time: Timestamp?): Long = time?.toDate()?.time ?: 0L
}
