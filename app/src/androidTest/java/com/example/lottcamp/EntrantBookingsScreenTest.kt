package com.example.lottcamp

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lottcamp.controller.EntrantBookingsController
import com.example.lottcamp.controller.EntrantRoutes
import com.example.lottcamp.repository.SampleEntrantRepository
import com.example.lottcamp.ui.screens.entrant.EntrantBookingsScreen
import com.example.lottcamp.ui.screens.entrant.EntrantCampsiteRatingScreen
import com.example.lottcamp.ui.theme.LottCampTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for the entrant bookings screen, using sample data instead of Firestore. */
class EntrantBookingsScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val data = SampleEntrantRepository()

    @Before
    fun setUp() {
        rule.setContent {
            LottCampTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                val controller = remember(scope, data) {
                    EntrantBookingsController(
                        entrantId = "entrant1",
                        scope = scope,
                        data = data,
                        navigate = { route -> navController.navigate(route) },
                    )
                }

                LaunchedEffect(controller) {
                    controller.refresh()
                }

                NavHost(navController, startDestination = EntrantRoutes.BOOKINGS) {
                    composable(EntrantRoutes.BOOKINGS) {
                        EntrantBookingsScreen(controller = controller)
                    }
                    composable(
                        route = EntrantRoutes.RATE_CAMPSITE,
                        arguments = listOf(navArgument("campsiteId") { type = NavType.StringType }),
                    ) { entry ->
                        Scaffold { innerPadding ->
                            EntrantCampsiteRatingScreen(
                                navController = navController,
                                modifier = Modifier.padding(innerPadding),
                                campsiteId = entry.arguments?.getString("campsiteId") ?: "",
                                data = data,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun click(text: String) = rule.onAllNodesWithText(text).onFirst().performClick()

    private fun clickInDialog(text: String) = rule
        .onNode(hasText(text) and hasAnyAncestor(isDialog()))
        .performClick()

    private fun assertGone(text: String) = rule.onAllNodesWithText(text).assertCountEquals(0)

    @Test
    fun activeTab_showsConfirmedReservations() {
        rule.onNodeWithText("Bookings").assertIsDisplayed()
        rule.onNodeWithText("Pine Ridge Camp").assertIsDisplayed()
        rule.onNodeWithText("Confirmed · RES-2038").assertIsDisplayed()
        rule.onNodeWithText("Bow Valley Retreat").assertIsDisplayed()
        rule.onNodeWithText("Confirmed · RES-2051").assertIsDisplayed()
    }

    @Test
    fun cancelReservation_keepKeepsIt_andConfirmRemovesIt() {
        click("Cancel reservation")
        rule.onNodeWithText("Cancel booking?").assertIsDisplayed()
        clickInDialog("Keep it")
        rule.onNodeWithText("Pine Ridge Camp").assertIsDisplayed()

        click("Cancel reservation")
        clickInDialog("Cancel booking")
        assertGone("Pine Ridge Camp")
        rule.onNodeWithText("Bow Valley Retreat").assertIsDisplayed()
    }

    @Test
    fun pastTab_showsCompletedStaysWithRateButtons() {
        click("Past")
        rule.onNodeWithText("Moose Lake").assertIsDisplayed()
        rule.onNodeWithText("Elk Island").assertIsDisplayed()
        rule.onAllNodesWithText("Completed").assertCountEquals(2)
        rule.onAllNodesWithText("Rate campsite").assertCountEquals(2)
        assertGone("Pine Ridge Camp")
    }

    @Test
    fun waitingListsTab_showsLotteryDetails() {
        click("Waiting lists")
        rule.onNodeWithText("Bow Valley Retreat").assertIsDisplayed()
        rule.onNodeWithText("Lottery closes Apr 20, 2027").assertIsDisplayed()
        rule.onNodeWithText("On the waiting list · 18 entrants").assertIsDisplayed()
        rule.onNodeWithText("Lake Nordegg").assertIsDisplayed()
        rule.onNodeWithText("Lottery closes May 2, 2027").assertIsDisplayed()
        rule.onNodeWithText("On the waiting list · 9 entrants").assertIsDisplayed()
    }

    @Test
    fun leaveWaitingList_stayKeepsIt_andConfirmRemovesIt() {
        click("Waiting lists")
        click("Leave waiting list")
        rule.onNodeWithText("Leave waiting list?").assertIsDisplayed()
        clickInDialog("Stay")
        rule.onNodeWithText("Bow Valley Retreat").assertIsDisplayed()

        click("Leave waiting list")
        clickInDialog("Leave waiting list")
        assertGone("Bow Valley Retreat")
        rule.onNodeWithText("Lake Nordegg").assertIsDisplayed()
    }

    @Test
    fun rateCampsite_opensRatingScreenForThatCampsite() {
        click("Past")
        click("Rate campsite")
        rule.onNodeWithText("Rate a campsite").assertIsDisplayed()
        rule.onNodeWithText("Moose Lake").assertIsDisplayed()
        rule.onNodeWithText("Your rating").assertIsDisplayed()
    }
}