package com.example.lottcamp

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.example.lottcamp.ui.components.AppAvatar
import org.junit.Rule
import org.junit.Test

/** UI tests for [AppAvatar] choosing between a profile photo and initials. */
class AppAvatarTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun withoutPhoto_showsOnlyInitials() {
        rule.setContent { AppAvatar("Alex Morgan") }
        rule.onNodeWithText("AM").assertIsDisplayed()
        rule.onAllNodesWithContentDescription("Profile photo of Alex Morgan").assertCountEquals(0)
    }

    @Test
    fun withPhoto_showsPhotoOverInitials() {
        rule.setContent { AppAvatar("Alex Morgan", imageUrl = "https://photo.invalid/alex.png") }
        rule.onNodeWithContentDescription("Profile photo of Alex Morgan").assertIsDisplayed()
        rule.onNodeWithText("AM").assertIsDisplayed()
    }
}
