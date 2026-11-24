package com.example.lottcamp.controller

/** Navigation routes for the owner screens. **/
object OwnerRoutes {
    const val MY_CAMPSITES = "owner/my-campsites"
    const val REGISTER_CAMPSITE = "owner/register-campsite"
    const val CAMPSITE = "owner/campsite/{id}"
    const val EDIT_CAMPSITE = "owner/campsite/{id}/edit"
    const val CREATE_LOTTERY = "owner/campsite/{id}/create-lottery"
    const val WAITING_LIST = "owner/lottery/{id}"
    const val EDIT_LOTTERY = "owner/lottery/{id}/edit"
    const val NOTIFICATIONS = "owner/lottery/{id}/notifications"

    /** Route to the details of the campsite with [id]. */
    fun campsite(id: String) = CAMPSITE.replace("{id}", id)

    /** Route to the edit form of the campsite with [id]. */
    fun editCampsite(id: String) = EDIT_CAMPSITE.replace("{id}", id)

    /** Route to the create lottery form, starting with the campsite [id] ticked. */
    fun createLottery(id: String) = CREATE_LOTTERY.replace("{id}", id)

    /** Route to the waiting list of the lottery with [id]. */
    fun waitingList(id: String) = WAITING_LIST.replace("{id}", id)

    /** Route to the edit form of the lottery with [id]. */
    fun editLottery(id: String) = EDIT_LOTTERY.replace("{id}", id)

    /** Route to the notifications of the lottery with [id]. */
    fun notifications(id: String) = NOTIFICATIONS.replace("{id}", id)

    const val RESERVATIONS = "owner/reservations"
    const val CHAT = "owner/reservations/{id}/chat"

    /** Route to the message thread of the reservation with [id]. */
    fun chat(id: String) = CHAT.replace("{id}", id)
}