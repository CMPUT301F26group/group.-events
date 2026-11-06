package com.example.lottcamp.data

data class EntrantDoc(
    val androidId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val profilePictureUrl: String? = null,
    var isNotificationsEnabled: Boolean = true
)