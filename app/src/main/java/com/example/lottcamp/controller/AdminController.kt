package com.example.lottcamp.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.domain.ImageType
import com.example.lottcamp.domain.MessageLog
import com.example.lottcamp.domain.NotificationLog
import com.example.lottcamp.domain.Review
import com.example.lottcamp.domain.UploadedImage
import com.example.lottcamp.domain.UserProfile
import com.example.lottcamp.domain.UserRole
import com.example.lottcamp.repository.AdminRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Controller shared by the admin screens.
 *
 * Loads everything admins browse from [data] with [refresh], then searches and filters
 * it in memory. Removals are sent to [data] first, then applied to the lists.
 * The lists are Compose state, so screens update automatically.
 *
 * @param data Where the data is read from and removed.
 * @param scope Runs database work in the background so the screen doesn't freeze.
 */
class AdminController(private val data: AdminRepository, private val scope: CoroutineScope) {

    /** Every user account. */
    val users = mutableStateListOf<UserProfile>()

    /** Every registered campsite. */
    val campsites = mutableStateListOf<Campsite>()

    /** Every campsite review. */
    val reviews = mutableStateListOf<Review>()

    /** Log of every notification sent, newest first. */
    val notifications = mutableStateListOf<NotificationLog>()

    /** Log of every message owners sent to entrants, newest first. */
    val messages = mutableStateListOf<MessageLog>()

    /** Message describing the last thing that failed, or null. Cleared with [clearError]. */
    var error by mutableStateOf<String?>(null)
        private set

    /** Every uploaded image: campsite photos and entrant profile photos. */
    val images: List<UploadedImage>
        get() {
            val result = mutableListOf<UploadedImage>()
            for (campsite in campsites) {
                if (campsite.imageUrl != null) {
                    result.add(
                        UploadedImage(
                            "campsite-${campsite.id}", campsite.id, campsite.name, campsite.ownerId,
                            ownerName(campsite), ImageType.CAMPSITE, campsite.imageUrl,
                        ),
                    )
                }
            }
            for (user in users) {
                if (user.photoUrl != null) {
                    result.add(
                        UploadedImage(
                            "profile-${user.id}", user.id, "Profile photo", user.id,
                            user.name, ImageType.PROFILE, user.photoUrl,
                        ),
                    )
                }
            }
            return result
        }

    /** Reloads all data from the database. */
    fun refresh() {
        runInBackground("Couldn't load data") {
            val newUsers = data.getUsers()
            val newCampsites = data.getCampsites()
            val newReviews = data.getReviews()
            val newNotifications = data.getNotifications()
            val newMessages = data.getMessages()

            users.clear()
            users.addAll(newUsers)
            campsites.clear()
            campsites.addAll(newCampsites)
            reviews.clear()
            reviews.addAll(newReviews)
            notifications.clear()
            notifications.addAll(newNotifications)
            messages.clear()
            messages.addAll(newMessages)
        }
    }

    /** Hides the current [error]. */
    fun clearError() {
        error = null
    }

    /** Users whose name, email or ID contains [query], limited to [role] when given. */
    fun searchUsers(query: String, role: UserRole? = null) =
        users.filter { (role == null || it.role == role) && matches(query, it.name, it.email, it.id) }

    /** Campsites whose name, location or ID contains [query]. */
    fun searchCampsites(query: String) =
        campsites.filter { matches(query, it.name, it.locationName, it.id) }

    /** Images whose uploader, title or ID contains [query], limited to [type] when given. */
    fun searchImages(query: String, type: ImageType? = null) =
        images.filter { (type == null || it.type == type) && matches(query, it.uploaderName, it.title, it.id) }

    /**
     * Notifications whose sender, recipient (name or user ID), title or message contains [query].
     *
     * @param fromSystem When non-null, only system (true) or owner (false) notifications are returned.
     */
    fun searchNotifications(query: String, fromSystem: Boolean? = null) =
        notifications.filter {
            (fromSystem == null || it.fromSystem == fromSystem) &&
                matches(query, it.sender, it.recipient, it.title, it.message, it.recipientId, it.senderId.orEmpty())
        }

    /** Messages whose sender, recipient or campsite contains [query]. */
    fun searchMessages(query: String) =
        messages.filter { matches(query, it.sender, it.recipient, it.campsiteName) }

    /** The user with [id], or null if there is none. */
    fun user(id: String) = users.find { it.id == id }

    /** The campsite with [id], or null if there is none. */
    fun campsite(id: String) = campsites.find { it.id == id }

    /** The image with [id], or null if there is none. */
    fun image(id: String) = images.find { it.id == id }

    /** Reviews left on the campsite with [campsiteId]. */
    fun reviewsFor(campsiteId: String) = reviews.filter { it.campsiteId == campsiteId }

    /** Average star rating of the campsite with [campsiteId], or 0 if it has no reviews. */
    fun averageRating(campsiteId: String): Double {
        val campsiteReviews = reviewsFor(campsiteId)
        if (campsiteReviews.isEmpty()) return 0.0
        return campsiteReviews.map { it.rating }.average()
    }

    /** Name of the owner of [campsite], or "Unknown" if the owner no longer exists. */
    fun ownerName(campsite: Campsite) = user(campsite.ownerId)?.name ?: "Unknown"

    /** Activity counts for [user] as label to value pairs, or empty if they couldn't be loaded. */
    suspend fun userActivity(user: UserProfile): List<Pair<String, String>> {
        try {
            if (user.role == UserRole.OWNER) {
                val activity = data.getOwnerActivity(user.id)
                return listOf(
                    "Campsites" to activity.campsites.toString(),
                    "Active lotteries" to activity.activeLotteries.toString(),
                    "Active reservations" to activity.activeReservations.toString(),
                )
            }
            val activity = data.getEntrantActivity(user.id)
            return listOf(
                "Active reservations" to activity.activeReservations.toString(),
                "Past reservations" to activity.pastReservations.toString(),
                "Lottery waiting lists" to activity.waitingLists.toString(),
            )
        } catch (e: Exception) {
            error = "Couldn't load activity: ${e.message}"
            return emptyList()
        }
    }

    /** Activity counts for [campsite] as label to value pairs, or empty if they couldn't be loaded. */
    suspend fun campsiteActivity(campsite: Campsite): List<Pair<String, String>> {
        try {
            val activity = data.getCampsiteActivity(campsite.id)
            return listOf(
                "Active reservations" to activity.activeReservations.toString(),
                "Active lotteries" to activity.activeLotteries.toString(),
            )
        } catch (e: Exception) {
            error = "Couldn't load activity: ${e.message}"
            return emptyList()
        }
    }

    /** Removes [user]'s profile. Removing an owner also removes their campsites. */
    fun removeUser(user: UserProfile) {
        runInBackground("Couldn't remove ${user.name}") {
            data.removeUser(user)
            if (user.role == UserRole.OWNER) campsites.removeAll { it.ownerId == user.id }
            users.removeAll { it.id == user.id }
        }
    }

    /** Removes the campsite with [id]. */
    fun removeCampsite(id: String) {
        runInBackground("Couldn't remove the campsite") {
            data.removeCampsite(id)
            campsites.removeAll { it.id == id }
        }
    }

    /** Removes [review] and its rating. */
    fun removeReview(review: Review) {
        runInBackground("Couldn't remove the review") {
            data.removeReview(review)
            reviews.removeAll { it.id == review.id }
        }
    }

    /** Removes [image] from the campsite or profile that uses it. */
    fun removeImage(image: UploadedImage) {
        runInBackground("Couldn't remove the image") {
            data.removeImage(image)
            // Replace the campsite or user with a copy that has no photo
            if (image.type == ImageType.CAMPSITE) {
                val index = campsites.indexOfFirst { it.id == image.sourceId }
                if (index != -1) campsites[index] = campsites[index].copy(imageUrl = null)
            } else {
                val index = users.indexOfFirst { it.id == image.sourceId }
                if (index != -1) users[index] = users[index].copy(photoUrl = null)
            }
        }
    }

    /** Runs [work] in the background. If it fails, shows [errorMessage] through [error]. */
    private fun runInBackground(errorMessage: String, work: suspend () -> Unit) {
        scope.launch {
            try {
                work()
            } catch (e: Exception) {
                error = "$errorMessage: ${e.message}"
            }
        }
    }

    /** True when [query] is blank or any of [fields] contains it, ignoring case. */
    private fun matches(query: String, vararg fields: String) =
        query.isBlank() || fields.any { it.contains(query.trim(), ignoreCase = true) }
}
