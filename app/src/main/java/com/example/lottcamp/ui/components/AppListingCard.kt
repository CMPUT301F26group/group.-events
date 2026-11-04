package com.example.lottcamp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Horizontal campsite listing: square image on the left, details on the right.
 *
 * Shows a title, location, rating, and price. Passing an [onClick] makes the whole
 * card clickable, leaving it null keeps the card static.
 *
 * @param title Listing name.
 * @param location Secondary text below the title.
 * @param rating Average rating, shown as a number and a star row.
 * @param price Primary price text.
 * @param modifier Applied to the card, which fills the available width.
 * @param priceUnit Optional suffix appended to [price], such as `"/night"`,
 * rendered smaller and in the muted color.
 * @param onClick Optional handler for opening the listing.
 * @param image Content drawn in the square image slot. Empty leaves a plain placeholder.
 */
@Composable
fun AppListingCard(
    title: String,
    location: String,
    rating: Float,
    price: String,
    modifier: Modifier = Modifier,
    priceUnit: String = "",
    onClick: (() -> Unit)? = null,
    image: @Composable () -> Unit = {},
) {
    val shape = MaterialTheme.shapes.large

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .then(
                if (onClick == null) Modifier
                else Modifier.clickable(onClick = onClick)
            ),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                image()
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = location,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "%.1f".format(rating),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(6.dp))
                    AppRatingBar(
                        rating = rating,
                        readOnly = true,
                        starSize = 20.dp,
                    )
                }

                Spacer(Modifier.height(12.dp))

                val priceColor = MaterialTheme.colorScheme.primary
                val unitColor = MaterialTheme.colorScheme.onSurfaceVariant

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = priceColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                            )
                        ) {
                            append(price)
                        }
                        if (priceUnit.isNotEmpty()) {
                            withStyle(
                                SpanStyle(
                                    color = unitColor,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                )
                            ) {
                                append(priceUnit)
                            }
                        }
                    },
                )
            }
        }
    }
}