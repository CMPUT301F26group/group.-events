package com.example.lottcamp.ui.screens.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lottcamp.controller.AdminController
import com.example.lottcamp.controller.AdminHomeController
import com.example.lottcamp.controller.AdminRoutes

/**
 * Navigation between all admin screens, starting at the admin home screen.
 *
 * @param modifier Applied to the navigation host.
 * @param controller Admin controller shared by every screen.
 * @param navController Controls the back stack; exposed for tests.
 */
@Composable
fun AdminNavGraph(
    modifier: Modifier = Modifier,
    controller: AdminController = remember { AdminController() },
    navController: NavHostController = rememberNavController(),
) {
    val back: () -> Unit = { navController.popBackStack() }
    val go: (String) -> Unit = { navController.navigate(it) }

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
