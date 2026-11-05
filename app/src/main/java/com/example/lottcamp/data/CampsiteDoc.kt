package com.example.lottcamp.data

import com.google.firebase.firestore.DocumentId

data class CampsiteDoc(
    @DocumentId val id: String = "",
    val ownersAndroidId: String = "",
    val name: String = "",
    val locationName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val description: String = "",
    val maxOccupancy: Int = 0,
    val pricePerNight: Double = 0.0,
    val imageUrl: String? = null,
    val tags: List<String> = emptyList(),
    val ratingSum: Double = 0.0,
    val ratingCount: Int = 0,
    val rulesAndPolicy: String = ""
)