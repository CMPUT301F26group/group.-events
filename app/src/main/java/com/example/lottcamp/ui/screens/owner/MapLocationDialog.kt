package com.example.lottcamp.ui.screens.owner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.lottcamp.ui.components.AppMutedText
import com.example.lottcamp.ui.components.AppTextField
import androidx.compose.ui.Modifier

/** Dialog where the owner types the campsite's coordinates. */
@Composable
fun MapLocationDialog(
    latitude: String,
    onLatitudeChange: (String) -> Unit,
    longitude: String,
    onLongitudeChange: (String) -> Unit,
    error: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Map location") },
        text = {
            Column {
                AppMutedText("In Google Maps, long-press your campsite and copy the two numbers shown.")
                Spacer(Modifier.height(8.dp))
                AppTextField(
                    value = latitude,
                    onValueChange = onLatitudeChange,
                    label = "Latitude",
                    placeholder = "e.g. 53.4129",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                )
                Spacer(Modifier.height(8.dp))
                AppTextField(
                    value = longitude,
                    onValueChange = onLongitudeChange,
                    label = "Longitude",
                    placeholder = "e.g. -117.5895",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                )
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}