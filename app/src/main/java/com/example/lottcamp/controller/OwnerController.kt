package com.example.lottcamp.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.repository.CampsiteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Controller shared by the owner screens.
 *
 * Loads and saves the campsites owned by [ownerId] through [data].
 * The list is Compose state, so screens update automatically.
 *
 * @param ownerId Android ID of the owner.
 * @param data Where the campsites are read from and saved to.
 * @param scope Runs database work in the background so the screen doesn't freeze.
 */
class OwnerController(
    private val ownerId: String,
    private val data: CampsiteRepository,
    private val scope: CoroutineScope,
) {

    /** The owner's campsites. */
    val campsites = mutableStateListOf<Campsite>()

    /** Message describing the last thing that failed, or null. Cleared with [clearError]. */
    var error by mutableStateOf<String?>(null)
        private set

    /** Reloads the owner's campsites from the database. */
    fun refresh() {
        runInBackground("Couldn't load your campsites") { load() }
    }
    fun campsite(id: String) = campsites.find { it.id == id }

    /** Saves [campsite], reloads the list, then calls [onSaved]. If saving fails, shows [error]. */
    fun saveCampsite(campsite: Campsite, onSaved: () -> Unit) {
        runInBackground("Couldn't save the campsite") {
            data.saveCampsite(campsite)
            load()
            onSaved()
        }
    }

    /** Hides the current [error]. */
    fun clearError() {
        error = null
    }

    private suspend fun load() {
        val newCampsites = data.getCampsitesByOwner(ownerId)
        campsites.clear()
        campsites.addAll(newCampsites)
    }

    /** Runs [work] in the background. If it fails, shows [errorMessage] through [error]. */
    private fun runInBackground(errorMessage: String, work: suspend () -> Unit) {
        scope.launch {
            try {
                work()
            } catch (e: Exception) {
                error = "$errorMessage: ${e.message}"
            }
        }
    }
}