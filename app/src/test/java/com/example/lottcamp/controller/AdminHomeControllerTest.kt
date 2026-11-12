package com.example.lottcamp.controller

import com.example.lottcamp.domain.UserRole
import com.example.lottcamp.repository.SampleAdminRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** Unit tests for [AdminHomeController]'s counts and navigation. */
class AdminHomeControllerTest {

    private val admin = AdminController(SampleAdminRepository(), CoroutineScope(Dispatchers.Unconfined))
    private val routes = mutableListOf<String>()
    private val controller = AdminHomeController(admin) { routes.add(it) }

    @Before
    fun setUp() {
        admin.refresh()
    }

    @Test
    fun counts_reflectAdminData() {
        assertEquals(admin.users.size, controller.userCount)
        assertEquals(admin.images.size, controller.imageCount)
        assertEquals(3, controller.campsiteCount)
        admin.removeCampsite("CAMP-001")
        assertEquals(2, controller.campsiteCount)
        assertEquals("CAMP-002", controller.latestCampsites.first().id)
    }

    @Test
    fun clicks_navigateToTheRightScreens() {
        controller.onUsersClick()
        controller.onImagesClick()
        controller.onBrowseCampsitesClick()
        controller.onNotificationsClick()
        controller.onOwnerMessagesClick()
        controller.onCampsiteClick(admin.campsites.first())
        assertEquals(
            listOf(
                AdminRoutes.USERS,
                AdminRoutes.IMAGES,
                AdminRoutes.CAMPSITES,
                AdminRoutes.notifications(),
                AdminRoutes.MESSAGES,
                "admin/campsite/CAMP-001",
            ),
            routes,
        )
    }

    @Test
    fun onRoleSelected_updatesRole() {
        assertEquals(UserRole.ADMIN, controller.role)
        controller.onRoleSelected(UserRole.OWNER)
        assertEquals(UserRole.OWNER, controller.role)
    }
}
