package com.example.lottcamp.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lottcamp.ui.theme.LottCampTheme


@Composable
fun MyCampsiteScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(28.dp)
    ) {
        Text(
            text = "My Campsites",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }

}



@Preview(showBackground = true)
@Composable
fun MyCampsiteScreenPreview() {
    LottCampTheme {
        MyCampsiteScreen()
    }
}