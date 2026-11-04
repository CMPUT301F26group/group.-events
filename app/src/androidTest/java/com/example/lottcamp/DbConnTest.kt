package com.example.lottcamp

import com.google.firebase.FirebaseApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class FirebaseSetupTest {
    @Test
    fun testFirebaseInitialization() {
        // Retrieve the default initialized FirebaseApp instance
        val firebaseApp = FirebaseApp.getInstance()

        // Assert that the app is initialized successfully
        assertNotNull("FirebaseApp failed to initialize", firebaseApp)

        // Assert that the connected project ID matches your active project
        val expectedProjectId = "group-events-52c1d"
        assertEquals(
            "Firebase is not connected to the expected Project ID",
            expectedProjectId,
            firebaseApp.options.projectId
        )
    }
}
