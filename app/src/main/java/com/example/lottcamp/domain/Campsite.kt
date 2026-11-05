package com.example.lottcamp.domain

import com.google.firebase.firestore.DocumentId

/**
 * Domain model representing a Campsite registered by an owner.
 */
data class Campsite(
    val id: String,
    val ownerId: String,
    val name: String,
    val locationName: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val description: String = "",
    val maxOccupancy: Int,
    val pricePerNight: Double,
    val imageUrl: String? = null,
    val tags: List<String> = emptyList(),
    val ratingSum: Double = 0.0,
    val ratingCount: Int = 0,
    val rulesAndPolicy: String = ""
) {
    /** Average rating score out of 5 stars, or 0.0 if unrated. */
    val averageRating: Double
        get() = if (ratingCount > 0) ratingSum / ratingCount else 0.0

    /** Returns a new [Campsite] instance updated with a new rating. */
    fun addRating(rating: Double): Campsite {
        require(rating in 1.0..5.0) { "Rating must be between 1.0 and 5.0" }
        return copy(
            ratingSum = ratingSum + rating,
            ratingCount = ratingCount + 1
        )
    }
}