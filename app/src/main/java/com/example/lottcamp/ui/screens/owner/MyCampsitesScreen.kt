package com.example.lottcamp.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.domain.UserRole
import com.example.lottcamp.ui.components.AppButton
import com.example.lottcamp.ui.components.AppCampsiteThumbnail
import com.example.lottcamp.ui.components.AppChip
import com.example.lottcamp.ui.components.AppDivider
import com.example.lottcamp.ui.components.AppImagePlaceholder
import com.example.lottcamp.ui.components.AppListRow
import com.example.lottcamp.ui.components.AppMutedText
import com.example.lottcamp.ui.components.AppSectionTitle
import com.example.lottcamp.ui.theme.LottCampTheme

@Composable
fun MyCampsiteScreen(
    campsites: List<Campsite>,
    subtitle: (Campsite) -> String,
    image: @Composable (String) -> Unit,
    role: UserRole,
    availableRoles: List<UserRole>,
    onRoleSelected: (UserRole) -> Unit,
    onRegisterClick: () -> Unit,
    onCampsiteClick: (Campsite) -> Unit,
    modifier: Modifier = Modifier,
) {
    var roleMenuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Text(
                text = "Campsite App",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Box {
                AppChip(
                    text = "${role.label} ▾",
                    selected = false,
                    onClick = { roleMenuOpen = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                )
                DropdownMenu(
                    expanded = roleMenuOpen,
                    onDismissRequest = { roleMenuOpen = false }
                ) {
                    availableRoles.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                onRoleSelected(option)
                                roleMenuOpen = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppSectionTitle("My campsites")
            AppMutedText("${campsites.size} listings")
        }

        Spacer(Modifier.height(20.dp))

        AppButton(
            text = "Register campsite",
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(campsites, key = { it.id }) { campsite ->
                AppListRow(
                    title = campsite.name,
                    subtitle = subtitle(campsite),
                    leading = { AppCampsiteThumbnail(imageId = campsite.imageId, image = image) },
                    onClick = { onCampsiteClick(campsite) },
                )
                AppDivider(spacing = 0.dp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyCampsiteScreenPreview() {
    LottCampTheme {
        MyCampsiteScreen(
            campsites = listOf(
                Campsite("1", "owner1", "Pine Ridge Camp", "Hinton, AB", maxOccupancy = 6, pricePerNight = 50.0),
                Campsite("2", "owner1", "Bow Valley Retreat", "Canmore, AB", maxOccupancy = 6, pricePerNight = 50.0),
                Campsite("3", "owner1", "Meadow Ridge", "Banff, AB", maxOccupancy = 6, pricePerNight = 50.0),
            ),
            subtitle = { "${it.locationName} · Published" },
            image = {},
            role = UserRole.OWNER,
            availableRoles = UserRole.entries,
            onRoleSelected = {},
            onRegisterClick = {},
            onCampsiteClick = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
        )
    }
}