package com.example.lottcamp.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import com.example.lottcamp.data.ImageDoc
import com.example.lottcamp.domain.AppImage
import com.google.firebase.Firebase
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ImageRepository(
    db: FirebaseFirestore = Firebase.firestore
) {
    private val collection = db.collection("images")
    private val cache = LruCache<String, AppImage>(20)

    /** Saves [bytes] as a new image and returns its ID. */
    suspend fun save(bytes: ByteArray): String {
        require(bytes.size <= 900_000) { "Image too large for a Firestore document" }
        val ref = collection.add(ImageDoc(data = Blob.fromBytes(bytes))).await()
        cache.put(ref.id, AppImage(ref.id, bytes))
        return ref.id
    }

    /** Loads the image with [id], or null if it doesn't exist. Recently loaded images are cached in memory. */
    suspend fun load(id: String): AppImage? {
        cache.get(id)?.let { return it }
        val bytes = collection.document(id).get().await().getBlob("data")?.toBytes() ?: return null
        return AppImage(id, bytes).also { cache.put(id, it) }
    }

    suspend fun delete(id: String) {
        collection.document(id).delete().await()
        cache.remove(id)
    }

    suspend fun saveFromUri(
        context: Context,
        uri: Uri,
        maxSide: Int = 800,
        maxBytes: Int = 150_000,
    ): String {
        val bitmap = context.decodeBitmap(uri) ?: error("Couldn't read the image")
        val bytes = withContext(Dispatchers.Default) { AppImage.compress(bitmap, maxSide, maxBytes) }
        return save(bytes)
    }
}

suspend fun Context.decodeBitmap(uri: Uri, maxSide: Int = 1600): Bitmap? =
    withContext(Dispatchers.IO) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide) sample *= 2

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }