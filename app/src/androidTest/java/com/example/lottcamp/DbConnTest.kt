package com.example.lottcamp

import com.example.lottcamp.domain.Admin
import com.example.lottcamp.repository.AdminRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.FirebaseApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking

class TestCampsiteOwner(
    var name: String = "",
    var campsiteName: String = ""
)

class FirebaseSetupTest {
    @Test
    fun testFirebaseInitialization() {
        val firebaseApp = FirebaseApp.getInstance()
        assertNotNull("FirebaseApp failed to initialize", firebaseApp)

        val expectedProjectId = "group-events-52c1d"
        assertEquals(
            "Firebase is not connected to the expected Project ID",
            expectedProjectId,
            firebaseApp.options.projectId
        )
    }

    @Test
    fun write() {
        val gerald = TestCampsiteOwner("Gerald", "Camp St. Louis")
        val db = Firebase.firestore
        val campsiteOwnersRef = db.collection("test-campsite-owners")

        // Capture the asynchronous Task returned by set()
        val writeTask = campsiteOwnersRef.document(gerald.name).set(gerald)

        try {
            // Synchronously block the test thread and wait up to 10 seconds for the write to finish
            Tasks.await(writeTask, 10, TimeUnit.SECONDS)
            println("Document successfully written!")
        } catch (e: Exception) {
            println("Failed to write document: ${e.message}")
            throw e
        }
    }
}
