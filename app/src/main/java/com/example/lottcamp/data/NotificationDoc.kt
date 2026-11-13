package com.example.lottcamp.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Firestore document logging one notification sent to a user.
 *
 * @property id Document ID, filled in by Firestore.
 * @property title Notification heading.
 * @property senderId Android ID of the owner who sent it, or null when the app sent it automatically.
 * @property senderName Name of the sender, or "System" for automatic notifications.
 * @property recipientId Android ID of the user who received it.
 * @property recipientName Name of the user who received it.
 * @property message Notification body.
 * @property status Delivery status, e.g. "Delivered".
 * @property sentAt When it was sent.
 */
data class NotificationDoc(
    @DocumentId val id: String = "",
    val title: String = "",
    val senderId: String? = null,
    val senderName: String = "System",
    val recipientId: String = "",
    val recipientName: String = "",
    val message: String = "",
    val status: String = "Delivered",
    val sentAt: Timestamp? = null,
)
