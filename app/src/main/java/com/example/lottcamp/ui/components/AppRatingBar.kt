package com.example.lottcamp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppRatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    readOnly: Boolean = true,
    starSize: Dp = 40.dp,
    onRatingChange: (Int) -> Unit = {},
) {
    Row(
        modifier = modifier.semantics {
            contentDescription = "%.1f out of %d stars".format(rating, maxRating)
        },
        horizontalArrangement = Arrangement.spacedBy(starSize * 0.1f),
    ) {
        for (index in 1..maxRating) {
            val fill = (rating - (index - 1)).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .size(starSize)
                    .then(
                        if (readOnly) Modifier
                        else Modifier
                            .minimumInteractiveComponentSize()
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = { onRatingChange(index) },
                                )
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.StarBorder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.matchParentSize(),
                )

                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .matchParentSize()
                        .drawWithContent {
                            clipRect(right = size.width * fill) {
                                this@drawWithContent.drawContent()
                            }
                        },
                )
            }
        }
    }
}