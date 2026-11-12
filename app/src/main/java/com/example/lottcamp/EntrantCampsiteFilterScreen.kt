package com.example.lottcamp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lottcamp.repository.CampsiteRepository
import com.example.lottcamp.ui.components.AppBackButton
import com.example.lottcamp.ui.components.AppButton

@Composable
fun EntrantCampsiteFilterScreen(navController: NavController, modifier: Modifier = Modifier) {
    var location by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var guests by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    val campsiteRepo = CampsiteRepository()
    var allTags by remember { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(Unit) {
        allTags = campsiteRepo.getAllTags()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppBackButton("Back to Search", onClick = { navController.navigate("entrant-campsite-search-screen") })

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text("Start date") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text("End date") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = guests,
                onValueChange = { guests = it.filter(Char::isDigit) },
                label = { Text("Number of guests") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Tags", style = MaterialTheme.typography.titleSmall)

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allTags.forEach { tag ->
                    FilterChip(
                        selected = tag in selectedTags,
                        onClick = {
                            selectedTags = if (tag in selectedTags)
                                selectedTags - tag
                            else
                                selectedTags + tag
                        },
                        label = { Text(tag) }
                    )
                }
            }
        }

        AppButton(
            "Show matching campsites",
            onClick = { navController.navigate("entrant-campsite-search-screen") },
            modifier.fillMaxWidth()
        )

        TextButton(
            onClick = {
                location = ""; startDate = ""; endDate = ""
                guests = ""; selectedTags = emptySet()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear filters")
        }
    }
}