package com.example.lottcamp.ui.screens.admin

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lottcamp.controller.AdminController
import com.example.lottcamp.controller.AdminHomeController
import com.example.lottcamp.controller.AdminRoutes
import com.example.lottcamp.repository.AdminRepository

/**
 * Navigation between all admin screens, starting at the admin home screen.
 * Loads the admin data when first shown, and shows a dialog if loading or a removal fails.
 *
 * @param modifier Applied to the navigation host.
 * @param data Where the admin data comes from. Tests pass [com.example.lottcamp.repository.SampleAdminRepository].
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

    NavHost(navController, startDestination = AdminRoutes.HOME, modifier = modifier) {
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

/** The `{id}` argument of a detail route. */
private fun getId(entry: NavBackStackEntry): String {
    return entry.arguments?.getString("id") ?: ""
}
