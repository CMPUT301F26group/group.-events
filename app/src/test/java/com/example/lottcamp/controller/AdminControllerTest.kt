package com.example.lottcamp.controller

import com.example.lottcamp.domain.ImageType
import com.example.lottcamp.domain.UserRole
import com.example.lottcamp.ui.components.initials
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for the searching, filtering and removal logic in [AdminController]. */
class AdminControllerTest {

    private val controller = AdminController()

    @Test
    fun searchUsers_matchesNameEmailAndIdIgnoringCase() {
        assertEquals(listOf("Sam Patel"), controller.searchUsers("sam").map { it.name })
        assertEquals(listOf("Taylor Brooks"), controller.searchUsers("TAYLOR.B@").map { it.name })
        assertEquals(listOf("Jamie Chen"), controller.searchUsers("usr-0871").map { it.name })
        assertEquals(controller.users.size, controller.searchUsers("  ").size)
    }

    @Test
    fun searchUsers_filtersByRole() {
        val owners = controller.searchUsers("", UserRole.OWNER)
        assertEquals(2, owners.size)
        assertTrue(owners.all { it.role == UserRole.OWNER })
        assertTrue(controller.searchUsers("alex", UserRole.OWNER).isEmpty())
    }

    @Test
    fun removeUser_removesProfile() {
        controller.removeUser("USR-1042")
        assertNull(controller.user("USR-1042"))
    }

    @Test
    fun removeOwner_alsoRemovesTheirCampsites() {
        controller.removeUser("USR-0871")
        assertTrue(controller.isRemoved("CAMP-001"))
        assertTrue(controller.isRemoved("CAMP-002"))
        assertFalse(controller.isRemoved("CAMP-003"))
        assertEquals("Unknown", controller.ownerName(controller.campsite("CAMP-001")!!))
    }

    @Test
    fun removeCampsite_movesItFromActiveToRemovedFilter() {
        controller.removeCampsite("CAMP-003")
        controller.removeCampsite("CAMP-003")
        assertEquals(listOf("CAMP-003"), controller.searchCampsites("", removed = true).map { it.id })
        assertEquals(2, controller.searchCampsites("", removed = false).size)
        assertEquals(3, controller.searchCampsites("").size)
    }

    @Test
    fun searchCampsites_matchesLocation() {
        assertEquals(listOf("Meadow Ridge"), controller.searchCampsites("banff").map { it.name })
    }

    @Test
    fun reviews_removeAndAverage() {
        assertEquals(10.0 / 3, controller.averageRating("CAMP-001"), 0.001)
        controller.removeReview("REV-3")
        assertEquals(2, controller.reviewsFor("CAMP-001").size)
        assertEquals(4.5, controller.averageRating("CAMP-001"), 0.001)
        assertEquals(0.0, controller.averageRating("CAMP-002"), 0.001)
    }

    @Test
    fun images_filterAndRemove() {
        assertEquals(2, controller.searchImages("", ImageType.PROFILE).size)
        assertEquals(2, controller.searchImages("jamie").size)
        controller.removeImage("IMG-012")
        assertNull(controller.image("IMG-012"))
        assertEquals(1, controller.searchImages("", ImageType.CAMPSITE).size)
    }

    @Test
    fun searchNotifications_filtersBySender() {
        assertEquals(2, controller.searchNotifications("", fromSystem = true).size)
        assertEquals(listOf("Check-in instructions"), controller.searchNotifications("", fromSystem = false).map { it.title })
        assertEquals(1, controller.searchNotifications("lottery").size)
    }

    @Test
    fun searchNotifications_matchesRecipientAndSenderIds() {
        assertEquals(listOf("NTF-1", "NTF-3"), controller.searchNotifications("USR-1042").map { it.id })
        assertEquals(listOf("NTF-3"), controller.searchNotifications("USR-0871").map { it.id })
    }

    @Test
    fun searchMessages_matchesSenderRecipientAndCampsite() {
        assertEquals(2, controller.searchMessages("jamie").size)
        assertEquals(1, controller.searchMessages("meadow").size)
        assertEquals(1, controller.searchMessages("sam patel").size)
    }

    @Test
    fun userActivity_ownerCountsTheirCampsites() {
        val owner = controller.user("USR-0871")!!
        assertEquals("Campsites" to "2", controller.userActivity(owner).first())
        val entrant = controller.user("USR-1042")!!
        assertEquals("Active reservations", controller.userActivity(entrant).first().first)
    }

    @Test
    fun initials_takesFirstTwoLetters() {
        assertEquals("AM", initials("Alex Morgan"))
        assertEquals("JM", initials("Jo Mary Smith"))
        assertEquals("C", initials("Cher"))
    }
}
