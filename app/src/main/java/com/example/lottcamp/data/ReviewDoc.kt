package com.example.lottcamp.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Firestore document for a campsite review
 *
 * @property id Document ID, filled in by Firestore.
 * @property campsiteId Campsite the review is for.
 * @property authorId Android ID of the entrant who wrote it.
 * @property authorName Name of the entrant who wrote it.
 * @property rating Star rating from 1.0 to 5.0, stored as a float. Also counted in the campsite's
 *   `ratingSum` / `ratingCount`.
 * @property comment Written review.
 * @property createdAt When the review was posted.
 */
data class ReviewDoc(
    @DocumentId val id: String = "",
    val campsiteId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val rating: Double = 0.0,
    val comment: String = "",
    val createdAt: Timestamp? = null,
)
