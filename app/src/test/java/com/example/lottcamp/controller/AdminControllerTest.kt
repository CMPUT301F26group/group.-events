package com.example.lottcamp.controller

import com.example.lottcamp.data.MessageDoc
import com.example.lottcamp.domain.ImageType
import com.example.lottcamp.domain.MessageLog
import com.example.lottcamp.domain.NotificationLog
import com.example.lottcamp.domain.UserProfile
import com.example.lottcamp.domain.UserRole
import com.example.lottcamp.repository.SampleAdminRepository
import com.example.lottcamp.repository.isOwnerToEntrantMessage
import com.example.lottcamp.ui.components.initials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the loading, searching, filtering and removal logic in [AdminController].
 * Uses [SampleAdminRepository] so no database is touched.
 */
class AdminControllerTest {

    private val data = SampleAdminRepository()
    private val controller = AdminController(data, CoroutineScope(Dispatchers.Unconfined))
    private class OfflineRepository : SampleAdminRepository() {
        override suspend fun getUsers(): List<UserProfile> = throw IllegalStateException("offline")
    }

    @Before
    fun setUp() {
        controller.refresh()
    }

    @Test
    fun refresh_loadsEverythingFromTheRepository() {
        assertEquals(6, controller.users.size)
        assertEquals(3, controller.campsites.size)
        assertEquals(3, controller.reviews.size)
        assertEquals(3, controller.notifications.size)
        assertEquals(3, controller.messages.size)
    }

    @Test
    fun refresh_failure_setsErrorAndClearErrorHidesIt() {
        val failed = AdminController(OfflineRepository(), CoroutineScope(Dispatchers.Unconfined))
        failed.refresh()
        assertEquals("Couldn't load data: offline", failed.error)
        assertTrue(failed.users.isEmpty())
        failed.clearError()
        assertNull(failed.error)
    }

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
    fun removeUser_removesProfileFromListAndRepository() {
        controller.removeUser(controller.user("USR-1042")!!)
        assertNull(controller.user("USR-1042"))
        assertTrue(data.users.none { it.id == "USR-1042" })
    }

    @Test
    fun removeOwner_alsoRemovesTheirCampsites() {
        controller.removeUser(controller.user("USR-0871")!!)
        assertEquals(listOf("CAMP-003"), controller.campsites.map { it.id })
        assertEquals("Robin Lee", controller.ownerName(controller.campsite("CAMP-003")!!))
    }

    @Test
    fun removeCampsite_removesFromListAndRepository() {
        controller.removeCampsite("CAMP-003")
        assertNull(controller.campsite("CAMP-003"))
        assertEquals(2, data.campsites.size)
    }

    @Test
    fun searchCampsites_matchesLocation() {
        assertEquals(listOf("Meadow Ridge"), controller.searchCampsites("banff").map { it.name })
    }

    @Test
    fun reviews_removeAndAverage() {
        assertEquals(10.0 / 3, controller.averageRating("CAMP-001"), 0.001)
        controller.removeReview(controller.reviews.first { it.id == "REV-3" })
        assertEquals(2, controller.reviewsFor("CAMP-001").size)
        assertEquals(4.5, controller.averageRating("CAMP-001"), 0.001)
        assertEquals(0.0, controller.averageRating("CAMP-002"), 0.001)
    }

    @Test
    fun images_comeFromCampsiteAndProfilePhotos() {
        assertEquals(2, controller.searchImages("", ImageType.CAMPSITE).size)
        assertEquals(2, controller.searchImages("", ImageType.PROFILE).size)
        assertEquals(2, controller.searchImages("jamie").size)
        assertEquals("Jamie Chen", controller.image("campsite-CAMP-001")!!.uploaderName)
    }

    @Test
    fun removeImage_clearsThePhotoItCameFrom() {
        controller.removeImage(controller.image("campsite-CAMP-001")!!)
        assertNull(controller.image("campsite-CAMP-001"))
        assertNull(controller.campsite("CAMP-001")!!.imageId)
        assertNotNull(controller.campsite("CAMP-001"))

        controller.removeImage(controller.image("profile-USR-1042")!!)
        assertNull(controller.user("USR-1042")!!.photoId)
        assertEquals(1, controller.searchImages("", ImageType.PROFILE).size)
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
        assertEquals(listOf("MSG-1"), controller.searchMessages("RES-2038").map { it.id })
        assertEquals(listOf("MSG-3"), controller.searchMessages("north gate").map { it.id })
    }

    @Test
    fun refreshLogs_fillsBlankNamesAndRefreshesIndependently() {
        var extraNotifications = listOf(
            NotificationLog("NTF-BLANK", "Lottery result", "", null, "", "USR-1042", "You won!", 1_794_500_000_000),
            NotificationLog("NTF-OWNER", "Update", "", "USR-0871", "", "USR-UNKNOWN", "Gate open", 1_794_499_000_000),
        )
        var extraMessages = listOf(
            MessageLog("MSG-BLANK", "", "", "Pine Ridge Camp", "abcdef1234", "See you at 3", 1_794_500_000_000, "USR-0871", "USR-1042"),
        )
        val repo = object : SampleAdminRepository() {
            override suspend fun getNotifications() = extraNotifications
            override suspend fun getMessages() = extraMessages
        }
        val fresh = AdminController(repo, CoroutineScope(Dispatchers.Unconfined))
        fresh.refresh()

        assertEquals(NotificationLog.SYSTEM_SENDER, fresh.notifications[0].sender)
        assertTrue(fresh.notifications[0].fromSystem)
        assertEquals("Alex Morgan", fresh.notifications[0].recipient)
        assertEquals("Jamie Chen", fresh.notifications[1].sender)
        assertFalse(fresh.notifications[1].fromSystem)
        assertEquals("USR-UNKNOWN", fresh.notifications[1].recipient)

        assertEquals("Jamie Chen", fresh.messages[0].sender)
        assertEquals("Alex Morgan", fresh.messages[0].recipient)
        assertEquals(listOf("MSG-BLANK"), fresh.searchMessages("RES-1234").map { it.id })
        assertEquals(listOf("MSG-BLANK"), fresh.searchMessages("USR-1042").map { it.id })

        extraNotifications = extraNotifications.take(1)
        extraMessages = emptyList()
        fresh.refreshNotifications()
        fresh.refreshMessages()
        assertEquals(1, fresh.notifications.size)
        assertTrue(fresh.messages.isEmpty())
    }

    @Test
    fun isOwnerToEntrantMessage_filtersEntrantRepliesOnly() {
        val ownerToEntrant = MessageDoc(senderId = "owner-1", recipientId = "entrant-1", reservationId = "res-1")
        val entrantToOwner = MessageDoc(senderId = "entrant-1", recipientId = "owner-1", reservationId = "res-1")
        val sameDevice = MessageDoc(senderId = "dev-1", recipientId = "dev-1", reservationId = "res-2")

        assertTrue(isOwnerToEntrantMessage(ownerToEntrant, reservationEntrantId = "entrant-1"))
        assertFalse(isOwnerToEntrantMessage(entrantToOwner, reservationEntrantId = "entrant-1"))
        assertTrue(isOwnerToEntrantMessage(sameDevice, reservationEntrantId = "dev-1"))
        assertTrue(isOwnerToEntrantMessage(entrantToOwner, reservationEntrantId = null))
    }

    @Test
    fun activity_isLabelledByRole() = runBlocking {
        val owner = controller.userActivity(controller.user("USR-0871")!!)
        assertEquals(listOf("Campsites" to "2", "Active lotteries" to "2", "Active reservations" to "4"), owner)
        val entrant = controller.userActivity(controller.user("USR-1042")!!)
        assertEquals(listOf("Active reservations" to "2", "Past reservations" to "5", "Lottery waiting lists" to "3"), entrant)
        val campsite = controller.campsiteActivity(controller.campsite("CAMP-001")!!)
        assertEquals(listOf("Active reservations" to "2", "Active lotteries" to "1"), campsite)
    }

    @Test
    fun initials_takesFirstTwoLetters() {
        assertEquals("AM", initials("Alex Morgan"))
        assertEquals("JM", initials("Jo Mary Smith"))
        assertEquals("C", initials("Cher"))
    }
}
