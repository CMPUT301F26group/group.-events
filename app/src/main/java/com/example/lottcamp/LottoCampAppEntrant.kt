package com.example.lottcamp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lottcamp.ui.components.AppNavBar
import com.example.lottcamp.ui.components.AppNavItem
import com.example.lottcamp.ui.theme.LottCampTheme

@Composable
fun LottoCampAppEntrant() {
    val navController = rememberNavController()
    val searchViewModel: SearchViewModel = viewModel()

    LottCampTheme {
        var tab by rememberSaveable { mutableIntStateOf(0) }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                AppNavBar {
                    AppNavItem(
                        selected = tab == 0,
                        onClick = {
                            navController.navigate("entrant-campsite-search-screen")
                            tab = 0
                        },
                        label = "Explore",
                        icon = Icons.Outlined.Search,
                    )
                    AppNavItem(
                        selected = tab == 1,
                        onClick = {
                            navController.navigate("entrant-bookings-screen")
                            tab = 1
                        },
                        label = "Bookings",
                        icon = Icons.Outlined.DateRange,
                    )
                    AppNavItem(
                        selected = tab == 2,
                        onClick = {
                            navController.navigate("entrant-messages-screen")
                            tab = 2
                        },
                        label = "Messages",
                        icon = Icons.Outlined.Email,
                    )
                    AppNavItem(
                        selected = tab == 3,
                        onClick = {
                            navController.navigate("entrant-profile-screen")
                            tab = 3
                        },
                        label = "Profile",
                        icon = Icons.Outlined.Person,
                    )
                }
            }
        ) { innerPadding ->
            NavHost(navController = navController, startDestination = "entrant-campsite-search-screen") {
                composable("entrant-campsite-search-screen") { EntrantCampsiteSearchScreen(navController, Modifier.padding(innerPadding), searchViewModel) }
                composable("entrant-bookings-screen") { EntrantBookingsScreen(navController, Modifier.padding(innerPadding)) }
                composable("entrant-messages-screen") { EntrantMessagesScreen(navController, Modifier.padding(innerPadding)) }
                composable("entrant-profile-screen") { EntrantProfileScreen(navController, Modifier.padding(innerPadding)) }
                composable(
                    "entrant-campsite-details-screen/{campsiteId}",
                    arguments = listOf(
                        navArgument("campsiteId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val campsiteId = backStackEntry.arguments?.getString("campsiteId") ?: ""
                    EntrantCampsiteDetailsScreen(navController, Modifier.padding(innerPadding), campsiteId)
                }
                composable("entrant-campsite-filter-screen") { EntrantCampsiteFilterScreen(navController, Modifier.padding(innerPadding), searchViewModel) }
            }
        }
    }

}
