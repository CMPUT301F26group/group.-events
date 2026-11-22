package com.example.lottcamp.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Blob

/**
 * Image stored in the firestore's database. can be queried via ImageRepository
 */
data class ImageDoc(
    @DocumentId val id: String = "",
    val data: Blob? = null,
)