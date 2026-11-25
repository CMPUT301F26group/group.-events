package com.example.lottcamp.controller

import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.repository.ImageRepository

/**
 * Controller for the admin home screen.
 *
 * Supplies the data the screen shows and handles its user actions.
 *
 * @param admin Shared admin controller that owns the data.
 * @param navigate Called with an [AdminRoutes] route when the screen should change.
 */
class AdminHomeController(
    private val admin: AdminController,
    private val navigate: (String) -> Unit = {},
) {
    val imageRepo: ImageRepository get() = admin.imageRepo

    val userCount get() = admin.users.size
    val imageCount get() = admin.images.size
    val campsiteCount get() = admin.campsites.size

    /** The first few campsites, shown under "Latest listings". */
    val latestCampsites: List<Campsite> get() = admin.campsites.take(2)

    // TODO: hook up once the global search is designed.
    fun onSearch(query: String) {}

    fun onBrowseCampsitesClick() = navigate(AdminRoutes.CAMPSITES)
    fun onUsersClick() = navigate(AdminRoutes.USERS)
    fun onImagesClick() = navigate(AdminRoutes.IMAGES)
    fun onViewAllListingsClick() = navigate(AdminRoutes.CAMPSITES)
    fun onCampsiteClick(campsite: Campsite) = navigate(AdminRoutes.campsite(campsite.id))
    fun onNotificationsClick() = navigate(AdminRoutes.notifications())
    fun onOwnerMessagesClick() = navigate(AdminRoutes.MESSAGES)
}
