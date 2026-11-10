package com.example.lottcamp

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.lottcamp.controller.AdminController
import com.example.lottcamp.ui.screens.admin.AdminNavGraph
import com.example.lottcamp.ui.theme.LottCampTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests that click through the admin screens. */
class AdminScreensTest {

    @get:Rule
    val rule = createComposeRule()

    private val controller = AdminController()

    @Before
    fun setUp() {
        rule.setContent { LottCampTheme { AdminNavGraph(controller = controller) } }
    }

    private fun click(text: String) = rule.onAllNodesWithText(text).onFirst().performClick()

    private fun assertGone(text: String) = rule.onAllNodesWithText(text).assertCountEquals(0)

    @Test
    fun home_showsCountsAndListings() {
        rule.onNodeWithText("Campsite App").assertIsDisplayed()
        rule.onNodeWithText("3 listings").assertIsDisplayed()
        rule.onNodeWithText("Pine Ridge Camp").assertIsDisplayed()
    }

    @Test
    fun home_roleMenuSwitchesRole() {
        click("Admin ▾")
        click("Owner")
        rule.onNodeWithText("Owner ▾").assertIsDisplayed()
    }

    @Test
    fun users_searchAndFilter() {
        click("Users")
        rule.onNodeWithText("Search users").assertIsDisplayed()
        click("Owners")
        assertGone("Alex Morgan")
        rule.onNodeWithText("Jamie Chen").assertIsDisplayed()
        click("All users")
        rule.onNodeWithText("Search name, email or user ID").performTextInput("sam")
        rule.onNodeWithText("Sam Patel").assertIsDisplayed()
        assertGone("Jamie Chen")
    }

    @Test
    fun userDetail_cancelKeepsProfile_removeDeletesIt() {
        click("Users")
        click("Alex Morgan")
        rule.onNodeWithText("User details").assertIsDisplayed()
        click("Remove profile")
        rule.onNodeWithText("Remove profile?").assertIsDisplayed()
        click("Cancel")
        rule.onNodeWithText("User details").assertIsDisplayed()

        click("Remove profile")
        click("Remove")
        rule.onNodeWithText("Search users").assertIsDisplayed()
        assertGone("Alex Morgan")
    }

    @Test
    fun userDetail_viewNotificationLogsFiltersByUser() {
        click("Users")
        click("Alex Morgan")
        click("View notification logs")
        rule.onNodeWithText("Notification logs").assertIsDisplayed()
        rule.onNodeWithText("USR-1042").assertIsDisplayed()
        rule.onNodeWithText("Lottery selection").assertIsDisplayed()
        assertGone("Reservation confirmed")
    }

    @Test
    fun ownerDetail_showsOwnerVariant() {
        click("Users")
        click("Jamie Chen")
        rule.onNodeWithText("Owner details").assertIsDisplayed()
        rule.onNodeWithText("Remove owner").assertIsDisplayed()
        click("View campsites")
        rule.onNodeWithText("Search campsites").assertIsDisplayed()
    }

    @Test
    fun campsite_removeMovesItToRemovedFilter() {
        click("View all")
        click("Meadow Ridge")
        rule.onNodeWithText("Campsite details").assertIsDisplayed()
        click("Remove campsite")
        click("Remove")
        click("Active")
        assertGone("Meadow Ridge")
        click("Removed")
        rule.onNodeWithText("Meadow Ridge").assertIsDisplayed()
    }

    @Test
    fun reviews_removeReview() {
        click("Pine Ridge Camp")
        click("View reviews & ratings")
        rule.onNodeWithText("Robin Lee").assertIsDisplayed()
        rule.onAllNodesWithText("Remove review")[2].performClick()
        assertGone("Robin Lee")
    }

    @Test
    fun image_removeImage() {
        click("Images")
        click("Profiles")
        assertGone("Pine Ridge Camp")
        click("Profile photo")
        rule.onNodeWithText("Image details").assertIsDisplayed()
        click("Remove image")
        click("Remove")
        rule.onNodeWithText("1 shown").assertIsDisplayed()
    }

    @Test
    fun logs_filterNotificationsAndSearchMessages() {
        click("Notifications")
        click("Owner")
        rule.onNodeWithText("Check-in instructions").assertIsDisplayed()
        assertGone("Lottery selection")
        rule.onNodeWithContentDescription("Back").performClick()

        click("Owner messages")
        rule.onNodeWithText("Search sender, recipient or campsite").performTextInput("meadow")
        rule.onNodeWithText("Robin Lee → Jordan Wilson").assertIsDisplayed()
        assertGone("Jamie Chen → Sam Patel")
    }
}
