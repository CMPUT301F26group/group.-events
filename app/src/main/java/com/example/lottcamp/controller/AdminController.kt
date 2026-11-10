package com.example.lottcamp.controller

import androidx.compose.runtime.mutableStateListOf
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.domain.ImageType
import com.example.lottcamp.domain.MessageLog
import com.example.lottcamp.domain.NotificationLog
import com.example.lottcamp.domain.Review
import com.example.lottcamp.domain.UploadedImage
import com.example.lottcamp.domain.UserProfile
import com.example.lottcamp.domain.UserRole

/**
 * Controller shared by the admin screens.
 *
 * Holds the data admins browse and handles searching, filtering and removal.
 * The lists are snapshot state, so screens update when something is removed.
 * Uses dummy data for now; later this will read from the repositories.
 */
class AdminController {

    /** Every user account. */
    val users = mutableStateListOf(
        UserProfile("USR-1042", "Alex Morgan", "alex.morgan@example.com", "780-555-0142", UserRole.ENTRANT, "October 12, 2026"),
        UserProfile("USR-0871", "Jamie Chen", "jamie.chen@example.com", "780-555-0188", UserRole.OWNER, "September 3, 2026"),
        UserProfile("USR-1107", "Sam Patel", "sam.patel@example.com", "780-555-0119", UserRole.ENTRANT, "October 20, 2026"),
        UserProfile("USR-0932", "Robin Lee", "robin.lee@example.com", "780-555-0175", UserRole.OWNER, "September 18, 2026"),
        UserProfile("USR-1210", "Taylor Brooks", "taylor.b@example.com", "780-555-0101", UserRole.ENTRANT, "November 1, 2026"),
        UserProfile("USR-0001", "Jordan Wilson", "jordan.w@example.com", "780-555-0100", UserRole.ADMIN, "August 30, 2026"),
    )

    /** Every registered campsite, including removed ones. */
    val campsites = mutableStateListOf(
        Campsite("CAMP-001", "USR-0871", "Pine Ridge Camp", "Hinton, AB", maxOccupancy = 6, pricePerNight = 85.0),
        Campsite("CAMP-002", "USR-0871", "Bow Valley Retreat", "Canmore, AB", maxOccupancy = 4, pricePerNight = 120.0),
        Campsite("CAMP-003", "USR-0932", "Meadow Ridge", "Banff, AB", maxOccupancy = 8, pricePerNight = 95.0),
    )

    /** IDs of campsites an admin has removed. */
    private val removedCampsiteIds = mutableStateListOf<String>()

    /** Every campsite review. */
    val reviews = mutableStateListOf(
        Review("REV-1", "CAMP-001", "Alex Morgan", 5, "Great spot by the river. The cookhouse was spotless."),
        Review("REV-2", "CAMP-001", "Sam Patel", 4, "Quiet and well kept. Gravel parking is a little tight."),
        Review("REV-3", "CAMP-001", "Robin Lee", 1, "This review contains language that breaks app policy."),
    )

    /** Every uploaded image. */
    val images = mutableStateListOf(
        UploadedImage("IMG-012", "Pine Ridge Camp", "USR-0871", "Jamie Chen", ImageType.CAMPSITE, "October 17, 2026"),
        UploadedImage("IMG-013", "Bow Valley Retreat", "USR-0871", "Jamie Chen", ImageType.CAMPSITE, "October 18, 2026"),
        UploadedImage("IMG-020", "Profile photo", "USR-1042", "Alex Morgan", ImageType.PROFILE, "October 12, 2026"),
        UploadedImage("IMG-021", "Profile photo", "USR-1107", "Sam Patel", ImageType.PROFILE, "October 20, 2026"),
    )

    /** Log of every notification sent. */
    val notifications = listOf(
        NotificationLog(
            "NTF-1", "Lottery selection", NotificationLog.SYSTEM_SENDER, null, "Alex Morgan", "USR-1042",
            "You were selected in the Pine Ridge lottery. Choose from the remaining dates to reserve your stay.",
            "Today · 10:30 AM",
        ),
        NotificationLog(
            "NTF-2", "Reservation confirmed", NotificationLog.SYSTEM_SENDER, null, "Sam Patel", "USR-1107",
            "Check-in opens at 3 PM. Find the gate instructions in your reservation details.",
            "Today · 9:15 AM",
        ),
        NotificationLog(
            "NTF-3", "Check-in instructions", "Jamie Chen", "USR-0871", "Alex Morgan", "USR-1042",
            "Check-in opens at 3 PM. Find the gate instructions in your reservation details.",
            "Yesterday · 4:00 PM",
        ),
    )

    /** Log of every message owners sent to entrants. */
    val messages = listOf(
        MessageLog(
            "MSG-1", "Jamie Chen", "Alex Morgan", "Pine Ridge Camp", "RES-2038",
            "Hi Alex, check-in starts at 3 PM. The cookhouse will be available throughout your stay.",
            "Today · 11:02 AM",
        ),
        MessageLog(
            "MSG-2", "Jamie Chen", "Sam Patel", "Bow Valley Retreat", "RES-2041",
            "Thanks for your reservation. Let me know if you have any questions before your stay.",
            "Yesterday · 2:20 PM",
        ),
        MessageLog(
            "MSG-3", "Robin Lee", "Jordan Wilson", "Meadow Ridge", "RES-2027",
            "The north gate is open. Please use the gravel parking area beside your campsite.",
            "October 16 · 8:45 AM",
        ),
    )

    /** Users whose name, email or ID contains [query], limited to [role] when given. */
    fun searchUsers(query: String, role: UserRole? = null) =
        users.filter { (role == null || it.role == role) && matches(query, it.name, it.email, it.id) }

    /**
     * Campsites whose name, location or ID contains [query].
     *
     * @param removed When non-null, only campsites with that removed state are returned.
     */
    fun searchCampsites(query: String, removed: Boolean? = null) =
        campsites.filter {
            (removed == null || isRemoved(it.id) == removed) &&
                matches(query, it.name, it.locationName, it.id)
        }

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
    fun averageRating(campsiteId: String) =
        reviewsFor(campsiteId).map { it.rating }.average().takeUnless { it.isNaN() } ?: 0.0

    /** Whether the campsite with [id] has been removed. */
    fun isRemoved(id: String) = id in removedCampsiteIds

    /** Name of the owner of [campsite], or "Unknown" if the owner no longer exists. */
    fun ownerName(campsite: Campsite) = user(campsite.ownerId)?.name ?: "Unknown"

    /**
     * Activity counts for [user] as label to value pairs.
     * TODO: other than the campsite count these are placeholders until the repositories are hooked up.
     */
    fun userActivity(user: UserProfile) = when (user.role) {
        UserRole.OWNER -> listOf(
            "Campsites" to campsites.count { it.ownerId == user.id }.toString(),
            "Active lotteries" to "2",
            "Active reservations" to "4",
        )
        else -> listOf(
            "Active reservations" to "2",
            "Past reservations" to "5",
            "Lottery waiting lists" to "3",
        )
    }

    /**
     * Activity counts for [campsite] as label to value pairs.
     * TODO: placeholders until the repositories are hooked up.
     */
    fun campsiteActivity(campsite: Campsite) = listOf(
        "Active reservations" to "2",
        "Active lotteries" to "1",
    )

    /** Removes the user with [id]. Removing an owner also removes their campsites. */
    fun removeUser(id: String) {
        campsites.filter { it.ownerId == id }.forEach { removeCampsite(it.id) }
        users.removeAll { it.id == id }
    }

    /** Marks the campsite with [id] as removed. It stays listed under the removed filter. */
    fun removeCampsite(id: String) {
        if (!isRemoved(id)) removedCampsiteIds.add(id)
    }

    /** Removes the review (and its rating) with [id]. */
    fun removeReview(id: String) {
        reviews.removeAll { it.id == id }
    }

    /** Removes the image with [id]. */
    fun removeImage(id: String) {
        images.removeAll { it.id == id }
    }

    /** True when [query] is blank or any of [fields] contains it, ignoring case. */
    private fun matches(query: String, vararg fields: String) =
        query.isBlank() || fields.any { it.contains(query.trim(), ignoreCase = true) }
}
