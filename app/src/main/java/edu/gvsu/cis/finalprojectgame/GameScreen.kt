package edu.gvsu.cis.finalprojectgame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun GameScreen(modifier: Modifier, viewModel: AppViewModel, onGoToSettings: () -> Unit, onGoToPostGame: () -> Unit) {
    val color by viewModel.currentBackgroundColor.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = color),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Blackjack", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Dealer", color = Color.White)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(80.dp, 120.dp)
                        .background(Color.White, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("?", fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .size(80.dp, 120.dp)
                        .background(Color.White, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("?", fontWeight = FontWeight.Bold)
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Player", color = Color.White)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(80.dp, 120.dp)
                        .background(Color.White, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("K of S", fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .size(80.dp, 120.dp)
                        .background(Color.White, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("4 of H", fontWeight = FontWeight.Bold)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {}) { Text("Hit") }
            Button(onClick = {}) { Text("Stand") }
            Button(onClick = {}) { Text("Double") }
            Button(onClick = {}) { Text("Split") }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {onGoToSettings()}) {Text("Settings")}
            Button(onClick = {onGoToPostGame()}) {Text("End Game")}
        }
    }
}
