package com.example.lottcamp.domain

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * An image stored by the app, such as a campsite or profile photo.
 *
 * Equality is by [id] only, since stored images never change after they are saved.
 *
 * @property id Unique image ID.
 * @property bytes Compressed JPEG data.
 */
class AppImage(val id: String, val bytes: ByteArray) {

    /** Decodes [bytes] for display in Compose, off the main thread. Null if the data can't be decoded. */
    suspend fun toImageBitmap(): ImageBitmap? = withContext(Dispatchers.Default) {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }

    override fun equals(other: Any?) = other is AppImage && other.id == id
    override fun hashCode() = id.hashCode()

    companion object {
        /**
         * Downscales [bitmap] so its longest side is at most [maxSide] and JPEG-compresses it,
         * lowering the quality until the result is at most [maxBytes].
         */
        fun compress(bitmap: Bitmap, maxSide: Int = 800, maxBytes: Int = 150_000): ByteArray {
            val scale = maxSide.toFloat() / maxOf(bitmap.width, bitmap.height)
            val scaled = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true
                )
            } else bitmap

            var quality = 80
            var bytes: ByteArray
            do {
                val out = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
                bytes = out.toByteArray()
                quality -= 10
            } while (bytes.size > maxBytes && quality >= 20)
            return bytes
        }
    }
}