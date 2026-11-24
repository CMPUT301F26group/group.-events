package com.example.lottcamp.controller

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.domain.UserRole

@RequiresApi(Build.VERSION_CODES.O)
class OwnerHomeController(
    private val owner: OwnerController,
    val availableRoles: List<UserRole>,
    private val onRoleChange: (UserRole) -> Unit,
    private val navigate: (String) -> Unit = {},
) {
    val role = UserRole.OWNER

    val campsites: List<Campsite> get() = owner.campsites

    /** "Hinton, AB · Lottery open" */
    fun subtitle(campsite: Campsite) = "${campsite.locationName} · ${owner.status(campsite)}"

    fun onRoleSelected(role: UserRole) {
        if (role != this.role) onRoleChange(role)
    }

    fun onRegisterClick() = navigate(OwnerRoutes.REGISTER_CAMPSITE)

    fun onCampsiteClick(campsite: Campsite) = navigate(OwnerRoutes.campsite(campsite.id))
}