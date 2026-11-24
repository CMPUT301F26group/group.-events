package com.example.lottcamp

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lottcamp.controller.EntrantSearchController
import com.example.lottcamp.ui.screens.entrant.EntrantCampsiteFilterScreen
import com.example.lottcamp.ui.screens.entrant.EntrantFilterTestTags
import com.example.lottcamp.ui.theme.LottCampTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EntrantCampsiteFilterScreenTest {

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

    private fun launchFilterScreen(
        onBack: () -> Unit = {},
        onControllerReady: (EntrantSearchController) -> Unit = {},
    ) {
        composeRule.setContent {
            val scope = rememberCoroutineScope()
            val controller = remember {
                EntrantSearchController(
                    scope = scope,
                    navigate = {},
                    back = onBack,
                ).also(onControllerReady)
            }
            LaunchedEffect(controller) {
                controller.refreshResults()
            }
            LottCampTheme {
                EntrantCampsiteFilterScreen(controller = controller)
            }
        }
        waitForTag(EntrantFilterTestTags.APPLY_BUTTON)
    }

    @Test
    fun displaysFilterFieldsAndActionButtons() {
        launchFilterScreen()

        composeRule.onNodeWithTag(EntrantFilterTestTags.BACK_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(EntrantFilterTestTags.TITLE_TEXT).assertIsDisplayed()
        composeRule.onNodeWithTag(EntrantFilterTestTags.LOCATION_INPUT).assertIsDisplayed()
        composeRule.onNodeWithTag(EntrantFilterTestTags.MIN_GUESTS_INPUT).assertIsDisplayed()
        composeRule.onNodeWithTag(EntrantFilterTestTags.MAX_PRICE_INPUT).assertIsDisplayed()
        composeRule.onNodeWithTag(EntrantFilterTestTags.CLEAR_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(EntrantFilterTestTags.APPLY_BUTTON).assertIsDisplayed()
    }

    @Test
    fun applyingFiltersUpdatesControllerAndNavigatesBack() {
        var navigatedBack = false
        var searchController: EntrantSearchController? = null

        launchFilterScreen(
            onBack = { navigatedBack = true },
            onControllerReady = { searchController = it },
        )

        composeRule.onNodeWithTag(EntrantFilterTestTags.LOCATION_INPUT)
            .performTextInput("Banff")
        composeRule.onNodeWithTag(EntrantFilterTestTags.MIN_GUESTS_INPUT)
            .performTextInput("4")
        composeRule.onNodeWithTag(EntrantFilterTestTags.MAX_PRICE_INPUT)
            .performTextInput("75")
        composeRule.onNodeWithTag(EntrantFilterTestTags.APPLY_BUTTON)
            .performClick()
        composeRule.waitForIdle()

        assertEquals("Banff", searchController?.filter?.locationName)
        assertEquals(4, searchController?.filter?.minOccupancy)
        assertEquals(75.0, searchController?.filter?.maxPricePerNight)
        assertTrue(navigatedBack)
    }

    @Test
    fun clearingFiltersResetsValuesAndNavigatesBack() {
        var navigatedBack = false
        var searchController: EntrantSearchController? = null

        launchFilterScreen(
            onBack = { navigatedBack = true },
            onControllerReady = { searchController = it },
        )

        composeRule.onNodeWithTag(EntrantFilterTestTags.LOCATION_INPUT)
            .performTextInput("Jasper")
        composeRule.onNodeWithTag(EntrantFilterTestTags.APPLY_BUTTON)
            .performClick()
        composeRule.waitForIdle()

        assertEquals("Jasper", searchController?.filter?.locationName)

        navigatedBack = false
        composeRule.onNodeWithTag(EntrantFilterTestTags.CLEAR_BUTTON)
            .performClick()
        composeRule.waitForIdle()

        assertNull(searchController?.filter?.locationName)
        assertNull(searchController?.filter?.minOccupancy)
        assertNull(searchController?.filter?.maxPricePerNight)
        assertTrue(navigatedBack)
    }
}