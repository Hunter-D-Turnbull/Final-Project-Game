package edu.gvsu.cis.finalprojectgame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
    //Needed data from the viewModel collected as state
    val color by viewModel.currentBackgroundColor.collectAsState()
    val dealerHand by viewModel.dealerHand.collectAsState()
    val playerTurnOver by viewModel.playerTurnOver.collectAsState()
    val currentPoints by viewModel.currentPoints.collectAsState()
    val hands by viewModel.allHands.collectAsState()
    val currentIndex by viewModel.currentHandIndex.collectAsState()
    //Column for the screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = color),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //Internal Column that hold the points value and button to play again after first game
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("$currentPoints", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (playerTurnOver) {
                    Button(onClick = { viewModel.startGame() }) { Text("Play Again") }
                }
            }
        }
        //Function for dealer hand composable
        GameScreenDealerHand(dealerHand, reveal = playerTurnOver)
        //Function for player hand composable
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Player", color = Color.White)
            val currentHand = hands.getOrNull(currentIndex)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                currentHand?.hand?.forEach { CardView(it) }
            }
            if (hands.size > 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (currentIndex > 0) {
                        Button(onClick = { viewModel.currentHandIndexMinusOne() }) {
                            Text("Prev")
                        }
                    }
                    if (currentIndex < hands.lastIndex) {
                        Button(onClick = { viewModel.currentHandIndexPlusOne() }) {
                            Text("Next")
                        }
                    }
                }
            }
        }
        //Row that holds buttons for playing the game
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {viewModel.hit()}) { Text("Hit") }
            Button(onClick = {viewModel.stand()}) { Text("Stand") }
            Button(onClick = {viewModel.doubleDown()}) { Text("Double") }
            Button(onClick = {viewModel.split()}) { Text("Split") }
        }
        //Row that holds buttons to navigate to different screens in the game
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {onGoToSettings()}) {Text("Settings")}
            Button(onClick = {onGoToPostGame()}) {Text("End Game")}
        }
    }
}
