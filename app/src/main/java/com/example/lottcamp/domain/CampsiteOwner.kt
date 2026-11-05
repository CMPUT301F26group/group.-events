package com.example.lottcamp.domain

/**
 * Domain model representing a Campsite Owner who creates and manages listings.
 */
data class CampsiteOwner(
    val id: String,
    val name: String,
    val email: String,
    val phoneNumber: String
)