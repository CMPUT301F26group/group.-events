package com.example.lottcamp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.example.lottcamp.repository.ImageRepository
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun AppStoredImage(
    imageId: String?,
    imageRepo: ImageRepository,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    var bitmap by remember(imageId) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(imageId) {
        bitmap = if (imageId == null) null else try {
            imageRepo.load(imageId)?.toImageBitmap()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
        )
    }
}