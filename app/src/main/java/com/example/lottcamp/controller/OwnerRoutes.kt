package com.example.lottcamp.controller

/** Navigation routes for the owner screens. **/
object OwnerRoutes {
    const val MY_CAMPSITES = "owner/my-campsites"
    const val REGISTER_CAMPSITE = "owner/register-campsite"
    const val CAMPSITE = "owner/campsite/{id}"
    const val EDIT_CAMPSITE = "owner/campsite/{id}/edit"

    /** Route to the details of the campsite with [id]. */
    fun campsite(id: String) = CAMPSITE.replace("{id}", id)

    /** Route to the edit form of the campsite with [id]. */
    fun editCampsite(id: String) = EDIT_CAMPSITE.replace("{id}", id)
}