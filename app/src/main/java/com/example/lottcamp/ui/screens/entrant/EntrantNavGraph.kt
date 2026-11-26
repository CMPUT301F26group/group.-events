package com.example.lottcamp.ui.screens.entrant

import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lottcamp.controller.EntrantBookingsController
import com.example.lottcamp.controller.EntrantCampsiteDetailsController
import com.example.lottcamp.controller.EntrantRoutes
import com.example.lottcamp.controller.EntrantSearchController
import com.example.lottcamp.domain.UserRole
import com.example.lottcamp.repository.EntrantRepository
import com.example.lottcamp.ui.components.AppNavBar
import com.example.lottcamp.ui.components.AppNavItem
import com.example.lottcamp.ui.screens.ProfileRoutes
import com.example.lottcamp.ui.screens.profileRoutes

/**
 * Navigation graph for all entrant screens, starting at [EntrantRoutes.SEARCH], with a bottom
 * navigation bar.
 *
 * Resolves the entrant identifier once at the top level, instantiates controllers and navigation
 * helpers, and surfaces errors via an [AlertDialog].
 *
 * @param modifier Modifier applied to the outer scaffold.
 * @param data Repository providing entrant reservations, campsites, and lottery entries.
 * @param testEntrantId Optional override for testing; when null, the device's
 *   [Settings.Secure.ANDROID_ID] is used.
 * @param roles Roles the user can switch to from the search screen header. Empty hides the switcher.
 * @param onRoleSelected Called with the role picked in the header.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EntrantNavGraph(
    modifier: Modifier = Modifier,
    data: EntrantRepository = EntrantRepository(),
    testEntrantId: String? = "entrant1",
    roles: List<UserRole> = emptyList(),
    onRoleSelected: (UserRole) -> Unit = {},
) {
    val context = LocalContext.current
    val entrantId = remember(testEntrantId) {
        testEntrantId
            ?: Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: ""
    }

    val repository = remember { data }
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val back: () -> Unit = { navController.popBackStack() }
    val go: (String) -> Unit = { navController.navigate(it) }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val searchController = remember(scope, repository) {
        EntrantSearchController(
            scope = scope,
            navigate = go,
            back = back,
        )
    }

    val bookingsController = remember(entrantId, scope, repository) {
        EntrantBookingsController(
            entrantId = entrantId,
            scope = scope,
            data = repository,
            navigate = go,
        )
    }

    // Reload the campsites whenever the search screen is shown again so ratings left on the
    // details screen appear on the listing cards right away.
    LaunchedEffect(currentRoute) {
        if (currentRoute == EntrantRoutes.SEARCH) searchController.refreshResults()
    }

    val activeError = searchController.error ?: bookingsController.error
    activeError?.let { message ->
        AlertDialog(
            onDismissRequest = {
                searchController.clearError()
                bookingsController.clearError()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        searchController.clearError()
                        bookingsController.clearError()
                    }
                ) {
                    Text("OK")
                }
            },
            title = { Text("Something went wrong") },
            text = { Text(message) },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AppNavBar {
                AppNavItem(
                    selected = currentRoute == EntrantRoutes.SEARCH,
                    onClick = {
                        navController.navigate(EntrantRoutes.SEARCH) {
                            popUpTo(EntrantRoutes.SEARCH)
                            launchSingleTop = true
                        }
                    },
                    label = "Explore",
                    icon = Icons.Outlined.Home,
                )
                AppNavItem(
                    selected = currentRoute == EntrantRoutes.BOOKINGS,
                    onClick = {
                        navController.navigate(EntrantRoutes.BOOKINGS) {
                            launchSingleTop = true
                        }
                    },
                    label = "Bookings",
                    icon = Icons.Outlined.CalendarToday,
                )
                AppNavItem(
                    selected = currentRoute == EntrantRoutes.MESSAGES,
                    onClick = {
                        navController.navigate(EntrantRoutes.MESSAGES) {
                            launchSingleTop = true
                        }
                    },
                    label = "Messages",
                    icon = Icons.AutoMirrored.Outlined.Chat,
                )
                AppNavItem(
                    selected = ProfileRoutes.isProfileRoute(currentRoute),
                    onClick = {
                        navController.navigate(ProfileRoutes.PROFILE) {
                            launchSingleTop = true
                        }
                    },
                    label = "Profile",
                    icon = Icons.Outlined.Person,
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = EntrantRoutes.SEARCH,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(EntrantRoutes.SEARCH) {
                EntrantCampsiteSearchScreen(
                    controller = searchController,
                    roles = roles,
                    onRoleSelected = onRoleSelected,
                )
            }
            composable(EntrantRoutes.FILTER) {
                EntrantCampsiteFilterScreen(controller = searchController)
            }
            composable(EntrantRoutes.BOOKINGS) {
                LaunchedEffect(bookingsController) {
                    bookingsController.refresh()
                }
                EntrantBookingsScreen(controller = bookingsController)
            }
            composable(EntrantRoutes.MESSAGES) {
                EntrantMessagesScreen(navController, Modifier)
            }
            profileRoutes(navController, userId = entrantId, entrantRepo = repository)
            composable(
                route = EntrantRoutes.CAMPSITE_DETAILS,
                arguments = listOf(navArgument("campsiteId") { type = NavType.StringType }),
            ) { entry ->
                val campsiteId = getArg(entry, "campsiteId")
                val detailsController = remember(campsiteId, entrantId, scope, repository) {
                    EntrantCampsiteDetailsController(
                        campsiteId = campsiteId,
                        entrantId = entrantId,
                        scope = scope,
                        navigate = go,
                        back = back,
                    )
                }
                LaunchedEffect(detailsController) {
                    detailsController.refresh()
                }
                EntrantCampsiteDetailsScreen(controller = detailsController)
            }
            composable(
                route = EntrantRoutes.RATE_CAMPSITE,
                arguments = listOf(navArgument("campsiteId") { type = NavType.StringType }),
            ) { entry ->
                val campsiteId = getArg(entry, "campsiteId")
                EntrantCampsiteRatingScreen(navController, Modifier, campsiteId, entrantId, repository)
            }
            composable(
                route = EntrantRoutes.RATING_SUBMITTED,
                arguments = listOf(
                    navArgument("campsiteId") { type = NavType.StringType },
                    navArgument("rating") { type = NavType.IntType },
                ),
            ) { entry ->
                val campsiteId = getArg(entry, "campsiteId")
                val rating = entry.arguments?.getInt("rating") ?: 0
                EntrantRatingSubmittedScreen(navController, Modifier, campsiteId, rating, repository)
            }
        }
    }
}

private fun getArg(entry: NavBackStackEntry, key: String): String =
    entry.arguments?.getString(key) ?: ""