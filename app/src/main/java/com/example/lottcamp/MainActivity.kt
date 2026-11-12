package com.example.lottcamp

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.lottcamp.repository.AdminRepository
import com.example.lottcamp.ui.screens.admin.AdminNavGraph
import com.example.lottcamp.ui.screens.entrant.LottoCampAppEntrant
import com.example.lottcamp.ui.theme.LottCampTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LottCampTheme {
                // Admins are the users whose Android ID is in the admins collection
                val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                var isAdmin by remember { mutableStateOf<Boolean?>(null) }
                LaunchedEffect(Unit) {
                    isAdmin = runCatching { AdminRepository().isAdmin(androidId) }.getOrDefault(false)
                }

                when (isAdmin) {
                    true -> Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
                        AdminNavGraph(Modifier.padding(innerPadding))
                    }
                    false -> LottoCampAppEntrant()
                    null -> {}
                }
            }
        }
    }
}