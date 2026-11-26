package com.example.lottcamp

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lottcamp.controller.EntrantRoutes
import com.example.lottcamp.controller.EntrantSearchController
import com.example.lottcamp.ui.screens.entrant.EntrantCampsiteSearchScreen
import com.example.lottcamp.ui.screens.entrant.EntrantQrScannerTestTags
import com.example.lottcamp.ui.screens.entrant.EntrantSearchTestTags
import com.example.lottcamp.ui.theme.LottCampTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EntrantCampsiteSearchScreenTest {

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

    private fun waitForCampsiteText(text: String, timeoutMillis: Long = 10_000L) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule
                .onAllNodesWithText(text)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun launchSearchScreen(
        onNavigate: (String) -> Unit = {},
        onOpenFilterClick: () -> Unit = {},
        onControllerReady: (EntrantSearchController) -> Unit = {},
        waitForInitialCampsite: Boolean = false,
    ) {
        composeRule.setContent {
            val scope = rememberCoroutineScope()
            val controller = remember {
                EntrantSearchController(
                    scope = scope,
                    navigate = { route ->
                        if (route == "filter" || route.endsWith("/filter")) {
                            onOpenFilterClick()
                        } else {
                            onNavigate(route)
                        }
                    },
                    back = {},
                ).also(onControllerReady)
            }
            LaunchedEffect(controller) {
                controller.refreshResults()
            }
            LottCampTheme {
                EntrantCampsiteSearchScreen(controller = controller)
            }
        }
        waitForTag(EntrantSearchTestTags.QR_SCAN_BUTTON)
        if (waitForInitialCampsite) {
            waitForCampsiteText("Moraine Lake Shore Camp")
        }
    }

    @Test
    fun displaysSearchControlsAndResultsCount() {
        launchSearchScreen()

        composeRule.onNodeWithTag(EntrantSearchTestTags.SEARCH_BAR).assertIsDisplayed()
        composeRule.onNodeWithTag(EntrantSearchTestTags.FILTER_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(EntrantSearchTestTags.QR_SCAN_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(EntrantSearchTestTags.RESULTS_COUNT_TEXT).assertIsDisplayed()
    }

    @Test
    fun typingQueryFiltersMatchingCampsitesAndSupportsClearing() {
        launchSearchScreen(waitForInitialCampsite = true)

        composeRule.onNodeWithTag(EntrantSearchTestTags.SEARCH_BAR)
            .performTextInput("Moraine")
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Moraine Lake Shore Camp").assertIsDisplayed()
        composeRule.onNodeWithText("Elk Island Lakeside Camp").assertDoesNotExist()

        composeRule.onNodeWithTag(EntrantSearchTestTags.SEARCH_BAR)
            .performTextClearance()
        waitForCampsiteText("Elk Island Lakeside Camp")

        composeRule.onNodeWithText("Elk Island Lakeside Camp").assertIsDisplayed()
    }

    @Test
    fun clickingFilterButtonTriggersOpenFilter() {
        var filterOpened = false
        launchSearchScreen(onOpenFilterClick = { filterOpened = true })

        composeRule.onNodeWithTag(EntrantSearchTestTags.FILTER_BUTTON).performClick()
        composeRule.waitForIdle()

        assert(filterOpened)
    }

    @Test
    fun clickingCampsiteCardTriggersCampsiteNavigation() {
        var navigatedRoute: String? = null
        launchSearchScreen(
            onNavigate = { navigatedRoute = it },
            waitForInitialCampsite = true,
        )

        composeRule.onNodeWithTag(EntrantSearchTestTags.campsiteCard("moraine-lake"))
            .performClick()
        composeRule.waitForIdle()

        assertNotNull("Expected navigation route when clicking campsite card", navigatedRoute)
        assert(navigatedRoute!!.contains("moraine-lake")) {
            "Expected route containing 'moraine-lake', but got '$navigatedRoute'"
        }
    }

    @Test
    fun clickingQrScanButtonOpensAndClosesScanner() {
        var searchController: EntrantSearchController? = null
        launchSearchScreen(onControllerReady = { searchController = it })

        composeRule.onNodeWithTag(EntrantSearchTestTags.QR_SCAN_BUTTON).performClick()
        composeRule.waitForIdle()

        assertTrue(searchController?.isQrScannerOpen == true)
        composeRule.onNodeWithTag(EntrantQrScannerTestTags.INSTRUCTION_TEXT).assertIsDisplayed()
        composeRule.onNodeWithTag(EntrantQrScannerTestTags.BACK_BUTTON).performClick()
        composeRule.waitForIdle()

        assertFalse(searchController?.isQrScannerOpen == true)
        composeRule.onNodeWithTag(EntrantSearchTestTags.SEARCH_BAR).assertIsDisplayed()
    }

    @Test
    fun scanningValidCampsiteQrNavigatesToDetails() {
        var searchController: EntrantSearchController? = null
        var navigatedRoute: String? = null
        launchSearchScreen(
            onNavigate = { navigatedRoute = it },
            onControllerReady = { searchController = it },
        )

        composeRule.waitUntil(timeoutMillis = 10_000L) {
            searchController?.baseResults?.isNotEmpty() == true
        }

        val validId = searchController!!.baseResults.first().id

        composeRule.runOnUiThread {
            searchController?.onQrScanClick()
            searchController?.onQrCodeScanned("lottcamp://campsite/$validId")
        }

        composeRule.waitUntil(timeoutMillis = 10_000L) {
            navigatedRoute == EntrantRoutes.campsiteDetails(validId)
        }

        assertEquals(EntrantRoutes.campsiteDetails(validId), navigatedRoute)
        assertFalse(searchController?.isQrScannerOpen == true)
    }

    @Test
    fun scanningInvalidQrShowsNoCampsiteFoundDialog() {
        var searchController: EntrantSearchController? = null
        val navigatedRoutes = mutableListOf<String>()
        launchSearchScreen(
            onNavigate = { navigatedRoutes += it },
            onControllerReady = { searchController = it },
        )

        composeRule.runOnUiThread {
            searchController?.onQrScanClick()
            searchController?.onQrCodeScanned("nonexistent-campsite-999")
        }

        composeRule.waitUntil(timeoutMillis = 10_000L) {
            searchController?.qrCampsiteNotFound == true
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("No campsite found").assertIsDisplayed()
        assertTrue(navigatedRoutes.none { it.contains("moraine-lake") || it.contains("nonexistent") })

        composeRule.onNodeWithText("Scan again").performClick()
        composeRule.waitForIdle()

        assertFalse(searchController?.qrCampsiteNotFound == true)
    }
}