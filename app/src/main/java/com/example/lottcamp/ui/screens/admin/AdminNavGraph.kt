package com.example.lottcamp.ui.screens.admin

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lottcamp.controller.AdminController
import com.example.lottcamp.controller.AdminHomeController
import com.example.lottcamp.controller.AdminRoutes
import com.example.lottcamp.repository.AdminRepository
import com.example.lottcamp.ui.components.AppNavBar
import com.example.lottcamp.ui.components.AppNavItem

/**
 * Navigation between all admin screens, starting at the admin home screen, with a bottom nav bar.
 * Loads the admin data when first shown, and shows a dialog if loading or a removal fails.
 *
 * @param modifier Applied to the scaffold around the admin screens.
 * @param data Where the admin data comes from.
 */
@Composable
fun AdminNavGraph(
    modifier: Modifier = Modifier,
    data: AdminRepository = AdminRepository(),
) {
    val scope = rememberCoroutineScope()
    val controller = remember { AdminController(data, scope) }
    val navController = rememberNavController()
    val back: () -> Unit = { navController.popBackStack() }
    val go: (String) -> Unit = { navController.navigate(it) }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Load the data once, when the admin screens first open
    LaunchedEffect(Unit) { controller.refresh() }

    controller.error?.let { message ->
        AlertDialog(
            onDismissRequest = { controller.clearError() },
            confirmButton = { TextButton(onClick = { controller.clearError() }) { Text("OK") } },
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
                    selected = currentRoute == AdminRoutes.HOME,
                    onClick = {
                        navController.navigate(AdminRoutes.HOME) {
                            popUpTo(AdminRoutes.HOME)
                            launchSingleTop = true
                        }
                    },
                    label = "Explore",
                    icon = Icons.Outlined.Home,
                )
                // TODO: no admin bookings screen yet
                AppNavItem(
                    selected = false,
                    onClick = {},
                    label = "Bookings",
                    icon = Icons.Outlined.CalendarToday,
                )
                AppNavItem(
                    selected = currentRoute == AdminRoutes.MESSAGES,
                    onClick = {
                        navController.navigate(AdminRoutes.MESSAGES) { launchSingleTop = true }
                    },
                    label = "Messages",
                    icon = Icons.AutoMirrored.Outlined.Chat,
                )
                // TODO: no admin profile screen yet
                AppNavItem(
                    selected = false,
                    onClick = {},
                    label = "Profile",
                    icon = Icons.Outlined.Person,
                )
            }
        },
    ) { innerPadding ->
        NavHost(navController, startDestination = AdminRoutes.HOME, modifier = Modifier.padding(innerPadding)) {
            composable(AdminRoutes.HOME) {
                AdminHomeScreen(controller = remember { AdminHomeController(controller, go) })
            }
            composable(AdminRoutes.USERS) {
                AdminUsersScreen(controller, back) { go(AdminRoutes.user(it.id)) }
            }
            composable(AdminRoutes.CAMPSITES) {
                AdminCampsitesScreen(controller, back) { go(AdminRoutes.campsite(it.id)) }
            }
            composable(AdminRoutes.IMAGES) {
                AdminImagesScreen(controller, back) { go(AdminRoutes.image(it.id)) }
            }
            composable(AdminRoutes.NOTIFICATIONS, arguments = listOf(navArgument("query") { defaultValue = "" })) {
                AdminNotificationsScreen(controller, back, initialQuery = it.arguments?.getString("query") ?: "")
            }
            composable(AdminRoutes.MESSAGES) { AdminMessagesScreen(controller, back) }
            composable(AdminRoutes.USER) {
                AdminUserDetailScreen(
                    controller,
                    getId(it),
                    back,
                    onViewCampsites = { go(AdminRoutes.CAMPSITES) },
                    onViewNotifications = { go(AdminRoutes.notifications(getId(it))) },
                )
            }
            composable(AdminRoutes.CAMPSITE) { entry ->
                AdminCampsiteDetailScreen(controller, getId(entry), back) { go(AdminRoutes.reviews(getId(entry))) }
            }
            composable(AdminRoutes.REVIEWS) { AdminReviewsScreen(controller, getId(it), back) }
            composable(AdminRoutes.IMAGE) {
                AdminImageDetailScreen(controller, getId(it), back) { userId -> go(AdminRoutes.user(userId)) }
            }
        }
    }
}

/** The `{id}` argument of a detail route. */
private fun getId(entry: NavBackStackEntry): String {
    return entry.arguments?.getString("id") ?: ""
}
