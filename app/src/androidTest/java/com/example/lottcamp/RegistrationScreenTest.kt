package com.example.lottcamp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lottcamp.domain.UserRole
import com.example.lottcamp.ui.screens.RegistrationScreen
import com.example.lottcamp.ui.screens.RegistrationTestTags
import com.example.lottcamp.ui.theme.LottCampTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistrationScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun launchRegistration(
        initialName: String = "",
        initialPhone: String = "",
        initialEmail: String = "",
        initialOptOut: Boolean = false,
        initialProfilePictureUrl: String? = null,
        initialRole: UserRole = UserRole.ENTRANT,
        nameError: String? = null,
        submitError: String? = null,
        isSubmitting: Boolean = false,
        onCreateAccountClick: () -> Unit = {},
        onRoleSelected: (UserRole) -> Unit = {},
    ) {
        composeRule.setContent {
            var name by remember { mutableStateOf(initialName) }
            var phone by remember { mutableStateOf(initialPhone) }
            var email by remember { mutableStateOf(initialEmail) }
            var optOut by remember { mutableStateOf(initialOptOut) }
            var photoUrl by remember { mutableStateOf(initialProfilePictureUrl) }
            var role by remember { mutableStateOf(initialRole) }

            LottCampTheme {
                RegistrationScreen(
                    name = name,
                    onNameChange = { name = it },
                    phoneNumber = phone,
                    onPhoneNumberChange = { phone = it },
                    email = email,
                    onEmailChange = { email = it },
                    optOutOfNotifications = optOut,
                    onOptOutOfNotificationsChange = { optOut = it },
                    profilePictureUrl = photoUrl,
                    onProfilePhotoSelected = { photoUrl = it },
                    selectedRole = role,
                    onRoleSelect = {
                        role = it
                        onRoleSelected(it)
                    },
                    onCreateAccountClick = onCreateAccountClick,
                    nameError = nameError,
                    submitError = submitError,
                    isSubmitting = isSubmitting,
                )
            }
        }
    }

    private fun fieldInput(tag: String) =
        composeRule.onNode(
            androidx.compose.ui.test.hasAnyAncestor(
                androidx.compose.ui.test.hasTestTag(tag)
            ) and hasSetTextAction()
        )

    @Test
    fun displaysFormControlsAndDefaults() {
        launchRegistration()

        composeRule.onAllNodesWithText("Create Account").onFirst().assertIsDisplayed()
        composeRule.onNodeWithTag(RegistrationTestTags.PROFILE_PHOTO_PICKER).assertIsDisplayed()
        composeRule.onNodeWithTag(RegistrationTestTags.NAME_FIELD).assertIsDisplayed()
        composeRule.onNodeWithTag(RegistrationTestTags.PHONE_FIELD).assertIsDisplayed()
        composeRule.onNodeWithTag(RegistrationTestTags.EMAIL_FIELD).assertIsDisplayed()
        composeRule.onNodeWithText("Admin").assertIsDisplayed()
        composeRule.onNodeWithText("Entrant").assertIsDisplayed()
        composeRule.onNodeWithText("Owner").assertIsDisplayed()
        composeRule.onNodeWithTag(RegistrationTestTags.OPT_OUT_NOTIFICATIONS_SWITCH)
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsOff()
        composeRule.onNodeWithTag(RegistrationTestTags.CREATE_ACCOUNT_BUTTON)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun userCanFillFieldsSelectRoleToggleOptOutAndSubmit() {
        var createClicked = false
        var selectedRole: UserRole? = null

        launchRegistration(
            onRoleSelected = { selectedRole = it },
            onCreateAccountClick = { createClicked = true },
        )

        fieldInput(RegistrationTestTags.NAME_FIELD)
            .performTextInput("Alex Rivera")
        fieldInput(RegistrationTestTags.PHONE_FIELD)
            .performTextInput("780-555-0142")
        fieldInput(RegistrationTestTags.EMAIL_FIELD)
            .performTextInput("alex@example.com")

        composeRule.onNodeWithText("Owner").performClick()
        assertEquals(UserRole.OWNER, selectedRole)

        composeRule.onNodeWithTag(RegistrationTestTags.OPT_OUT_NOTIFICATIONS_SWITCH)
            .performScrollTo()
            .performClick()
            .assertIsOn()

        composeRule.onNodeWithTag(RegistrationTestTags.CREATE_ACCOUNT_BUTTON)
            .performScrollTo()
            .performClick()

        assertTrue(createClicked)
    }

    @Test
    fun showsNameErrorAndSubmitErrorWhenProvided() {
        launchRegistration(
            nameError = "Name is required.",
            submitError = "Could not create account: Network error",
        )

        composeRule.onNodeWithText("Name is required.").assertIsDisplayed()
        composeRule.onNodeWithText("Could not create account: Network error")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun disablesInputsAndShowsSubmittingLabelWhenSubmitting() {
        var createClicked = false
        launchRegistration(
            isSubmitting = true,
            onCreateAccountClick = { createClicked = true },
        )

        composeRule.onNodeWithText("Creating Account...")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag(RegistrationTestTags.CREATE_ACCOUNT_BUTTON)
            .assertIsNotEnabled()
            .performClick()

        assertTrue(!createClicked)
    }
}