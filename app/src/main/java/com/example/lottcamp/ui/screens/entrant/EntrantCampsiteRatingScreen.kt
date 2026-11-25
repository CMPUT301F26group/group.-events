package com.example.lottcamp.ui.screens.entrant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lottcamp.controller.EntrantRoutes
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.repository.EntrantRepository
import com.example.lottcamp.ui.components.AppButton
import com.example.lottcamp.ui.components.AppNavBar
import com.example.lottcamp.ui.components.AppNavItem
import com.example.lottcamp.ui.components.AppRatingBar
import com.example.lottcamp.ui.components.AppSectionTitle
import com.example.lottcamp.ui.components.AppTopBar
import com.example.lottcamp.ui.theme.LottCampTheme

/**
 * Lets an entrant rate a campsite and optionally leave a written review.
 *
 * The selected star count is held in [rating] and passed to the submit handler.
 * The bottom navigation bar is supplied by the parent scaffold in
 * [LottoCampAppEntrant], so it stays visible on this screen.
 *
 * @param navController Used for the back arrow and after submitting.
 * @param modifier Applied to the whole screen.
 * @param campsiteId Campsite being rated.
 * @param data Where the campsite comes from. Tests and previews pass
 *   [com.example.lottcamp.repository.SampleEntrantRepository].
 */
@Composable
fun EntrantCampsiteRatingScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    campsiteId: String,
    data: EntrantRepository = EntrantRepository(),
) {
    val repository = remember { data }
    var campsite by remember { mutableStateOf<Campsite?>(null) }
    var rating by rememberSaveable { mutableIntStateOf(0) }
    var review by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(campsiteId, repository) {
        campsite = repository.getCampsite(campsiteId)
    }

    val currentCampsite = campsite
    if (currentCampsite == null) {
        Box(
            modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    RateCampsiteContent(
        campsiteName = currentCampsite.name,
        campsiteLocation = currentCampsite.locationName,
        rating = rating,
        onRatingChange = { rating = it },
        review = review,
        onReviewChange = { review = it },
        modifier = modifier,
        onBack = { navController.popBackStack() },
        onSubmit = {
            // `rating` and `review` hold the current selection here.
            // TODO: Save the review and update the campsite's rating in Firestore.
            navController.navigate(EntrantRoutes.ratingSubmitted(campsiteId, rating)) {
                popUpTo(EntrantRoutes.RATE_CAMPSITE) { inclusive = true }
            }
        },
    )
}

/**
 * Stateless campsite rating layout: top bar, campsite card, star picker,
 * optional review field and a submit button pinned above the bottom nav bar.
 *
 * @param campsiteName Name shown in the campsite card.
 * @param campsiteLocation Location shown under the name.
 * @param rating Currently selected star count.
 * @param onRatingChange Called with the tapped star's 1-based index.
 * @param review Current review text.
 * @param onReviewChange Called with the new review text on every edit.
 * @param modifier Applied to the whole screen.
 * @param onBack Invoked when the back arrow is tapped.
 * @param onSubmit Invoked when the submit button is tapped.
 */
@Composable
fun RateCampsiteContent(
    campsiteName: String,
    campsiteLocation: String,
    rating: Int,
    onRatingChange: (Int) -> Unit,
    review: String,
    onReviewChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSubmit: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AppTopBar("Rate a campsite", onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = campsiteName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = campsiteLocation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppSectionTitle("Your rating")

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    AppRatingBar(
                        rating = rating.toFloat(),
                        readOnly = false,
                        starSize = 48.dp,
                        onRatingChange = onRatingChange,
                    )
                }

                Text(
                    text = "$rating out of 5 selected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            OutlinedTextField(
                value = review,
                onValueChange = onReviewChange,
                label = { Text("Review (optional)") },
                placeholder = { Text("Share your experience...") },
                minLines = 7,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AppButton(
            text = "Submit rating",
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = rating > 0,
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun RateCampsiteContentPreview() {
    LottCampTheme {
        var rating by remember { mutableIntStateOf(4) }
        var review by remember { mutableStateOf("") }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                AppNavBar {
                    AppNavItem(
                        selected = true,
                        onClick = {},
                        label = "Explore",
                        icon = Icons.Outlined.Search,
                    )
                    AppNavItem(
                        selected = false,
                        onClick = {},
                        label = "Bookings",
                        icon = Icons.Outlined.DateRange,
                    )
                    AppNavItem(
                        selected = false,
                        onClick = {},
                        label = "Messages",
                        icon = Icons.Outlined.Email,
                    )
                    AppNavItem(
                        selected = false,
                        onClick = {},
                        label = "Profile",
                        icon = Icons.Outlined.Person,
                    )
                }
            },
        ) { innerPadding ->
            RateCampsiteContent(
                campsiteName = "Pine Ridge Camp",
                campsiteLocation = "Hinton, AB",
                rating = rating,
                onRatingChange = { rating = it },
                review = review,
                onReviewChange = { review = it },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}