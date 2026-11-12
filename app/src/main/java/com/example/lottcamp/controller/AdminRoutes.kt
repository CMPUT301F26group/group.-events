package com.example.lottcamp.controller

/** Navigation routes for the admin screens. **/
object AdminRoutes {
    const val HOME = "admin/home"
    const val USERS = "admin/users"
    const val CAMPSITES = "admin/campsites"
    const val IMAGES = "admin/images"
    const val NOTIFICATIONS = "admin/notifications?query={query}"
    const val MESSAGES = "admin/messages"
    const val USER = "admin/user/{id}"
    const val CAMPSITE = "admin/campsite/{id}"
    const val REVIEWS = "admin/reviews/{id}"
    const val IMAGE = "admin/image/{id}"

    /** Route to the details of the user with [id]. */
    fun user(id: String) = USER.replace("{id}", id)

    /** Route to the details of the campsite with [id]. */
    fun campsite(id: String) = CAMPSITE.replace("{id}", id)

    /** Route to the reviews of the campsite with [id]. */
    fun reviews(id: String) = REVIEWS.replace("{id}", id)

    /** Route to the notification logs, with the search bar pre-filled with [query] */
    fun notifications(query: String = "") = NOTIFICATIONS.replace("{query}", query)

    /** Route to the details of the image with [id]. */
    fun image(id: String) = IMAGE.replace("{id}", id)
}
