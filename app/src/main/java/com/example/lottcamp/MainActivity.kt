package com.example.lottcamp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.lottcamp.ui.components.AppNavBar
import com.example.lottcamp.ui.components.AppNavItem
import com.example.lottcamp.ui.screens.admin.AdminHomeScreen
import com.example.lottcamp.ui.theme.LottCampTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LottCampTheme {
                var tab by rememberSaveable { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        AppNavBar {
                            AppNavItem(
                                selected = tab == 0,
                                onClick = { tab = 0 },
                                label = "Explore",
                                icon = Icons.Outlined.Home,
                            )
                            AppNavItem(
                                selected = tab == 1,
                                onClick = { tab = 1 },
                                label = "Bookings",
                                icon = Icons.Outlined.DateRange,
                            )
                            AppNavItem(
                                selected = tab == 2,
                                onClick = { tab = 2 },
                                label = "Messages",
                                icon = Icons.Outlined.Email,
                            )
                            AppNavItem(
                                selected = tab == 3,
                                onClick = { tab = 3 },
                                label = "Profile",
                                icon = Icons.Outlined.Person,
                            )
                        }
                    }
                ) { innerPadding ->
                    // Only the Explore tab has a screen so far
                    if (tab == 0) {
                        AdminHomeScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}
