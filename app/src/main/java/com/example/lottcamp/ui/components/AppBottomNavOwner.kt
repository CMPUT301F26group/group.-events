package com.example.lottcamp.ui.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector


/** The four main destinations shown in the bottom navigation bar. */
enum class MainTab(val label: String, val icon: ImageVector) {
    Explore("Explore", Icons.Outlined.Home),
    Bookings("Bookings", Icons.Outlined.CalendarMonth),
    Messages("Messages", Icons.Outlined.ChatBubbleOutline),
    Profile("Profile", Icons.Outlined.Person),
}

/**
 * The app's bottom navigation bar with its four main destinations.
 *
 * Selection is controlled by the caller: the bar reports taps through [onSelect]
 * and renders itself from [selected].
 *
 * @param selected The current tab.
 * @param onSelect Called with the tab that was tapped.
 */
@Composable
fun AppBottomNavOwner(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
) {
    AppNavBar {
        MainTab.entries.forEach { tab ->
            AppNavItem(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                label = tab.label,
                icon = tab.icon,
            )
        }
    }
}