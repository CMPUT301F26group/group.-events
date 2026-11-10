package com.example.lottcamp.domain

/**
 * Role a user account holds in the app.
 *
 * @property label Human readable name shown in the UI.
 */
enum class UserRole(val label: String) {
    ADMIN("Admin"),
    ENTRANT("Entrant"),
    OWNER("Owner"),
}

/**
 * Summary of any user account (entrant, owner or admin) as seen by an admin.
 *
 * @property id Unique user ID.
 * @property name Full name of the user.
 * @property email Contact email.
 * @property phone Contact phone number.
 * @property role Role the account holds.
 * @property joined Date the account was created, already formatted for display.
 * @property photoUrl Location of the profile photo, or null if the user has none.
 */
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val joined: String,
    val photoUrl: String? = null,
)

/**
 * A rating and written review left on a campsite.
 *
 * @property id Unique review ID.
 * @property campsiteId Campsite the review belongs to.
 * @property authorName Name of the user who wrote the review.
 * @property rating Star rating from 1 to 5.
 * @property text Written review.
 */
data class Review(
    val id: String,
    val campsiteId: String,
    val authorName: String,
    val rating: Int,
    val text: String,
)

/**
 * Kind of image a user uploaded.
 *
 * @property label Human readable name shown in the UI.
 */
enum class ImageType(val label: String) {
    CAMPSITE("Campsite"),
    PROFILE("Profile"),
}

/**
 * An image uploaded to the app.
 *
 * @property id Unique image ID.
 * @property title Campsite name for campsite images, or "Profile photo".
 * @property uploaderId ID of the user who uploaded the image.
 * @property uploaderName Name of the user who uploaded the image.
 * @property type Whether this is a campsite or profile image.
 * @property uploaded Upload date, already formatted for display.
 * @property url Location of the image file, null until storage is set up.
 */
data class UploadedImage(
    val id: String,
    val title: String,
    val uploaderId: String,
    val uploaderName: String,
    val type: ImageType,
    val uploaded: String,
    val url: String? = null,
)

/**
 * Log entry for a notification sent to a user.
 *
 * @property id Unique log ID.
 * @property title Notification heading.
 * @property sender Name of the sender, or [SYSTEM_SENDER] for automatic notifications.
 * @property senderId User ID of the sender, or null for automatic notifications.
 * @property recipient Name of the user who received it.
 * @property recipientId User ID of the user who received it.
 * @property message Notification body.
 * @property time When it was sent, already formatted for display.
 * @property status Delivery status.
 */
data class NotificationLog(
    val id: String,
    val title: String,
    val sender: String,
    val senderId: String?,
    val recipient: String,
    val recipientId: String,
    val message: String,
    val time: String,
    val status: String = "Delivered",
) {
    /** True when the app sent this notification rather than a campsite owner. */
    val fromSystem: Boolean
        get() = sender == SYSTEM_SENDER

    companion object {
        /** Sender name used for notifications the app sends automatically. */
        const val SYSTEM_SENDER = "System"
    }
}

/**
 * Log entry for a message a campsite owner sent to an entrant with a reservation.
 *
 * @property id Unique log ID.
 * @property sender Name of the owner who sent the message.
 * @property recipient Name of the entrant who received it.
 * @property campsiteName Campsite the reservation is for.
 * @property reservationId Reservation the message is about.
 * @property message Message body.
 * @property time When it was sent, already formatted for display.
 */
data class MessageLog(
    val id: String,
    val sender: String,
    val recipient: String,
    val campsiteName: String,
    val reservationId: String,
    val message: String,
    val time: String,
)
