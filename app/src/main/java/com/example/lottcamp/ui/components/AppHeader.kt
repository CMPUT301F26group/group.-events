package com.example.lottcamp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lottcamp.domain.UserRole

/**
 * App logo and title shown at the top of the entrant, owner and admin home screens.
 * When [roles] is not empty, a chip on the right opens a menu for switching between them.
 *
 * @param role Role whose screens are currently shown.
 * @param roles Roles the user can switch to. Empty hides the chip.
 * @param onRoleSelected Called with the role picked from the menu.
 */
@Composable
fun AppHeader(
    role: UserRole,
    roles: List<UserRole> = emptyList(),
    onRoleSelected: (UserRole) -> Unit = {},
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary))
        Spacer(Modifier.width(12.dp))
        Text(
            text = "Campsite App",
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        if (roles.isNotEmpty()) {
            Box {
                AppChip(
                    text = "${role.label} ▾",
                    selected = false,
                    onClick = { menuOpen = true },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    variant = AppChipVariant.Secondary,
                )
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    roles.forEach {
                        DropdownMenuItem(
                            text = { Text(it.label) },
                            onClick = {
                                menuOpen = false
                                onRoleSelected(it)
                            },
                            trailingIcon = { if (it == role) Icon(Icons.Default.Check, contentDescription = "Selected") },
                        )
                    }
                }
            }
        }
    }
}
