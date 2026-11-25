package com.example.lottcamp.controller

/** Navigation routes for the entrant screens. */
object EntrantRoutes {
    const val SEARCH = "entrant/search"
    const val FILTER = "entrant/filter"
    const val BOOKINGS = "entrant/bookings"
    const val MESSAGES = "entrant/messages"
    const val PROFILE = "entrant/profile"
    const val CAMPSITE_DETAILS = "entrant/campsite/{campsiteId}"
    const val RATE_CAMPSITE = "entrant/campsite/{campsiteId}/rate"
    const val RATING_SUBMITTED = "entrant/campsite/{campsiteId}/rating-submitted/{rating}"

    /** Route to the details screen for the campsite with [campsiteId]. */
    fun campsiteDetails(campsiteId: String) =
        CAMPSITE_DETAILS.replace("{campsiteId}", campsiteId)

    /** Route to the rating screen for the campsite with [campsiteId]. */
    fun rateCampsite(campsiteId: String) =
        RATE_CAMPSITE.replace("{campsiteId}", campsiteId)

    /** Route to the rating confirmation screen for [campsiteId] and [rating]. */
    fun ratingSubmitted(campsiteId: String, rating: Int) =
        RATING_SUBMITTED
            .replace("{campsiteId}", campsiteId)
            .replace("{rating}", rating.toString())
}