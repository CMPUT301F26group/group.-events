package com.example.lottcamp.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Firestore document for a message a campsite owner sent to an entrant
 *
 * @property id Document ID, filled in by Firestore.
 * @property senderId Android ID of the owner who sent it.
 * @property senderName Name of the owner who sent it.
 * @property recipientId Android ID of the entrant who received it.
 * @property recipientName Name of the entrant who received it.
 * @property campsiteName Campsite the reservation is for.
 * @property reservationId Reservation the message is about.
 * @property message Message body.
 * @property sentAt When it was sent.
 */
data class MessageDoc(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val recipientId: String = "",
    val recipientName: String = "",
    val campsiteName: String = "",
    val reservationId: String = "",
    val message: String = "",
    val sentAt: Timestamp? = null,
)
