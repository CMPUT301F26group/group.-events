package com.example.lottcamp.domain

/**
 * Domain model representing an Entrant.
 */
data class Entrant(
    val androidId: String,
    val name: String,
    val phoneNumber: String,
    val email: String,
    val profilePictureUrl: String? = null,
    val notificationsEnabled: Boolean = true
) {
    /** Checks if the entrant has completed their required profile details. */
    fun isProfileComplete(): Boolean {
        return name.isNotBlank() && phoneNumber.isNotBlank() && email.isNotBlank()
    }
}