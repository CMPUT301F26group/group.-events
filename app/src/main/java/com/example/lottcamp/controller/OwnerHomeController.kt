package com.example.lottcamp.controller

import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.domain.UserRole

/**
 * Controller for the owner home screen ("My campsites").
 *
 * Supplies the data the screen shows and handles its user actions.
 *
 * @param owner Shared owner controller that owns the data.
 * @param availableRoles Roles the user may switch to, shown in the role pill.
 * @param onRoleChange Called when the user picks a different role.
 * @param navigate Called with an [OwnerRoutes] route when the screen should change.
 */
class OwnerHomeController(
    private val owner: OwnerController,
    val availableRoles: List<UserRole>,
    private val onRoleChange: (UserRole) -> Unit,
    private val navigate: (String) -> Unit = {},
) {

    /** Role the user is acting as on this screen. */
    val role = UserRole.OWNER

    /** The owner's campsites. */
    val campsites: List<Campsite> get() = owner.campsites

    fun onRoleSelected(role: UserRole) {
        if (role != this.role) onRoleChange(role)
    }

    fun onRegisterClick() = navigate(OwnerRoutes.REGISTER_CAMPSITE)


    fun onCampsiteClick(campsite: Campsite) = navigate(OwnerRoutes.campsite(campsite.id))
}