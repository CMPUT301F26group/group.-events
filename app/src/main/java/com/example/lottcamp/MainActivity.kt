package com.example.lottcamp

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.lottcamp.domain.UserRole
import com.example.lottcamp.ui.screens.admin.AdminNavGraph
import com.example.lottcamp.ui.screens.entrant.EntrantNavGraph
import com.example.lottcamp.ui.screens.owner.OwnerNavGraph
import com.example.lottcamp.ui.theme.LottCampTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LottCampTheme {
                // Admins are the users whose Android ID is in the admins collection
                val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                var availableRoles by remember { mutableStateOf<List<UserRole>>(emptyList()) }
                var activeRole by rememberSaveable { mutableStateOf<UserRole?>(null) }

                LaunchedEffect(Unit) {
                    val isAdmin = true // AdminRepository().isAdmin(androidId)
                    availableRoles = if (isAdmin) UserRole.entries else listOf(UserRole.ENTRANT, UserRole.OWNER)
                    if (activeRole == null) {
                        activeRole = if (isAdmin) UserRole.ADMIN else UserRole.ENTRANT
                    }
                }

                when (activeRole) {
                    UserRole.ADMIN -> AdminNavGraph(roles = availableRoles, onRoleSelected = { activeRole = it })
                    UserRole.ENTRANT -> EntrantNavGraph(roles = availableRoles, onRoleSelected = { activeRole = it })
                    UserRole.OWNER -> OwnerNavGraph(
                        ownerId = androidId,
                        availableRoles = availableRoles,
                        onRoleChange = { activeRole = it },
                    )
                    null -> {}
                }
            }
        }
    }
}