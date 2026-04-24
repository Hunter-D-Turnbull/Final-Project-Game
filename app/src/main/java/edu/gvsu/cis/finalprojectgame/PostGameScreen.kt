package edu.gvsu.cis.finalprojectgame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun PostGameScreen(modifier: Modifier = Modifier, viewModel: AppViewModel, onMainMenu: () -> Unit) {
    val color by viewModel.currentBackgroundColor.collectAsState()
    val finalPoints by viewModel.currentPoints.collectAsState()
    val handsPlayed by viewModel.totalHandsPlayed.collectAsState()
    val actualHandsPlayed = handsPlayed-1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Game Over",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Final Points: $finalPoints",
            fontSize = 22.sp,
            color = Color.White
        )

        Text(
            text = "Hands Played: $actualHandsPlayed",
            fontSize = 22.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {onMainMenu()}) {
            Text("Main Menu")
        }
    }
}