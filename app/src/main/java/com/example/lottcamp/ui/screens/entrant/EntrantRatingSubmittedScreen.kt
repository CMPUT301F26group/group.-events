package com.example.lottcamp.ui.screens.entrant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lottcamp.controller.EntrantRoutes
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.repository.EntrantRepository
import com.example.lottcamp.repository.SampleEntrantRepository
import com.example.lottcamp.ui.components.AppButton
import com.example.lottcamp.ui.components.AppNavBar
import com.example.lottcamp.ui.components.AppNavItem
import com.example.lottcamp.ui.components.AppTopBar
import com.example.lottcamp.ui.theme.LottCampTheme

/**
 * Confirmation shown after an entrant submits a campsite rating.
 *
 * Loads the campsite name from Firestore and delegates the layout to
 * [RatingSubmittedContent]. The bottom navigation bar is supplied by the
 * parent scaffold in [LottoCampAppEntrant].
 *
 * @param navController Used for the back arrow and "Back to campsite".
 * @param modifier Applied to the whole screen.
 * @param campsiteId Campsite that was rated.
 * @param rating Star count the entrant submitted.
 * @param data Where the campsite comes from. Tests and previews pass
 *   [com.example.lottcamp.repository.SampleEntrantRepository].
 */
@Composable
fun EntrantRatingSubmittedScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    campsiteId: String,
    rating: Int,
    data: EntrantRepository = EntrantRepository(),
) {
    val repository = remember { data }
    var campsite by remember { mutableStateOf<Campsite?>(null) }

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

    RatingSubmittedContent(
        campsiteName = currentCampsite.name,
        rating = rating,
        modifier = modifier,
        onBack = { navController.popBackStack() },
        onAcknowledge = {
            if (repository is SampleEntrantRepository) {
                // Sample campsites are not stored in Firestore, so return to the
                // bookings list instead of opening a campsite details page.
                navController.popBackStack(EntrantRoutes.BOOKINGS, inclusive = false)
            } else {
                val detailsDestination = navController.graph
                    .findNode(EntrantRoutes.CAMPSITE_DETAILS)
                val returnedToDetails = detailsDestination != null &&
                        navController.popBackStack(detailsDestination.id, inclusive = false)

                if (!returnedToDetails) {
                    navController.navigate(EntrantRoutes.BOOKINGS) {
                        popUpTo(EntrantRoutes.RATING_SUBMITTED) {
                            inclusive = true
                        }
                    }
                }
            }
        },
    )
}

/**
 * Stateless confirmation layout: top bar and a card with a check mark,
 * confirmation text and a button back to the campsite.
 *
 * @param campsiteName Name shown in the confirmation message.
 * @param rating Star count the entrant submitted.
 * @param modifier Applied to the whole screen.
 * @param onBack Invoked when the back arrow is tapped.
 * @param onAcknowledge Invoked when "Back to campsite" is tapped.
 */
@Composable
fun RatingSubmittedContent(
    campsiteName: String,
    rating: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onAcknowledge: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AppTopBar("Thank you", onBack)

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp),
                    )

                    Text(
                        text = "Rating submitted",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        text = "You rated $campsiteName $rating out of 5.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    AppButton(
                        text = "Ok",
                        onClick = onAcknowledge,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun RatingSubmittedContentPreview() {
    LottCampTheme {
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
            RatingSubmittedContent(
                campsiteName = "Pine Ridge Camp",
                rating = 4,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}