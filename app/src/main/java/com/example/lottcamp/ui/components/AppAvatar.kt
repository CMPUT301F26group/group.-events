package com.example.lottcamp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/** Initials from [name], e.g. "John Smith" -> "JS" */
fun initials(name: String): String {
    var result = ""
    for (word in name.split(" ")) {
        if (word.isNotEmpty() && result.length < 2) {
            result += word[0]
        }
    }
    return result
}

/**
 * Circular profile picture. Shows the photo at [imageUrl] when there is one,
 * otherwise the initials of [name].
 *
 * The initials are drawn underneath the photo, so they also show while the photo
 * is loading or if it fails to load.
 *
 * @param name Full name to take the initials from.
 * @param size Diameter of the circle.
 * @param imageUrl Location of the profile photo, or null if the user has none.
 */
@Composable
fun AppAvatar(name: String, size: Dp = 40.dp, imageUrl: String? = null) {
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(MaterialTheme.colorScheme.secondary),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials(name), style = MaterialTheme.typography.labelMedium)
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Profile photo of $name",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
