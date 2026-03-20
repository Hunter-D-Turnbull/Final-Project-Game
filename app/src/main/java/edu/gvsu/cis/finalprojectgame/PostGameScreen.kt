package edu.gvsu.cis.finalprojectgame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PostGameScreen(modifier: Modifier, viewModel: AppViewModel, onGoToSettings: () -> Unit, onBack: () -> Unit) {
    val color by viewModel.currentBackgroundColor.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = color),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Game Results", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("Hand 1: Won 500 Points", color = Color.White)
        Text("Hand 2: Lost 1000 Points", color = Color.White)
        Text("Hand 3: Lost 500 Points", color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Total Points Finished With: 0", fontWeight = FontWeight.Bold)
        Row(modifier = Modifier
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { onGoToSettings() }) { Text("Settings") }
            Button(onClick = { onBack() }) { Text("Main Menu") }
        }
    }
}