package com.example.lottcamp

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lottcamp.controller.EntrantCampsiteDetailsController
import com.example.lottcamp.ui.screens.entrant.EntrantCampsiteDetailsScreen
import com.example.lottcamp.ui.screens.entrant.EntrantDetailsTestTags
import com.example.lottcamp.ui.theme.LottCampTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EntrantCampsiteDetailsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun waitForTag(tag: String, timeoutMillis: Long = 10_000L) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule
                .onAllNodesWithTag(tag)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun launchDetailsScreen(
        campsiteId: String = "moraine-lake",
        entrantId: String = "entrant3",
        onBack: () -> Unit = {},
        onControllerReady: (EntrantCampsiteDetailsController) -> Unit = {},
    ) {
        composeRule.setContent {
            val scope = rememberCoroutineScope()
            val controller = remember(campsiteId, entrantId, scope) {
                EntrantCampsiteDetailsController(
                    campsiteId = campsiteId,
                    entrantId = entrantId,
                    scope = scope,
                    navigate = {},
                    back = onBack,
                ).also(onControllerReady)
            }
            LaunchedEffect(controller) {
                controller.refresh()
            }
            LottCampTheme {
                EntrantCampsiteDetailsScreen(controller = controller)
            }
        }
        waitForTag(EntrantDetailsTestTags.TITLE_TEXT)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Test
    fun displaysCampsiteDetailsWithoutReviewButton() {
        launchDetailsScreen(campsiteId = "moraine-lake")

        composeRule.onNodeWithTag(EntrantDetailsTestTags.TITLE_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText("Moraine Lake Shore Camp").assertIsDisplayed()
        composeRule.onNodeWithTag(EntrantDetailsTestTags.CALENDAR_SECTION)
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("Review campsite").assertDoesNotExist()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Test
    fun backButtonInvokesBackCallback() {
        var wentBack = false
        launchDetailsScreen(onBack = { wentBack = true })

        composeRule.onNodeWithTag(EntrantDetailsTestTags.BACK_BUTTON).performClick()
        composeRule.waitForIdle()

        assertTrue(wentBack)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Test
    fun enrollingInAvailableLotteryPeriodUpdatesWaitlistState() {
        var detailsController: EntrantCampsiteDetailsController? = null
        launchDetailsScreen(
            campsiteId = "elk-island",
            entrantId = "entrant3",
            onControllerReady = { detailsController = it },
        )

        composeRule.onNodeWithTag(EntrantDetailsTestTags.JOIN_WAITLIST_BUTTON)
            .performScrollTo()

        if (detailsController?.canJoin == true) {
            composeRule.onNodeWithTag(EntrantDetailsTestTags.JOIN_WAITLIST_BUTTON)
                .assertIsEnabled()
                .performClick()

            composeRule.waitUntil(timeoutMillis = 10_000L) {
                detailsController?.alreadyJoinedSelected == true ||
                        detailsController?.canJoin == false
            }

            composeRule.onNodeWithTag(EntrantDetailsTestTags.JOIN_WAITLIST_BUTTON)
                .assertIsNotEnabled()
        } else {
            composeRule.onNodeWithTag(EntrantDetailsTestTags.JOIN_WAITLIST_BUTTON)
                .assertIsDisplayed()
                .assertIsNotEnabled()
        }
    }
}