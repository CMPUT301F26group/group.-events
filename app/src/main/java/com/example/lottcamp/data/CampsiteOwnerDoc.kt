package com.example.lottcamp.data

import com.google.firebase.firestore.DocumentId

data class CampsiteOwnerDoc(
    val androidId: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = ""
)