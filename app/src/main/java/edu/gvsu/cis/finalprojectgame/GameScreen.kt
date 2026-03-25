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
    /*
    Needed data from the viewModel collected as state
     */
    val color by viewModel.currentBackgroundColor.collectAsState()
    val dealerHand by viewModel.dealerHand.collectAsState()
    val playerHand by viewModel.playerHand.collectAsState()
    val playerTurnOver by viewModel.playerTurnOver.collectAsState()
    val currentPoints by viewModel.currentPoints.collectAsState()
    val numHands by viewModel.numHands.collectAsState()
    var currentHandIndex = 1
    val multiHands by viewModel.allHands.collectAsState()
    /*
    Column for the screen
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = color),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /*
        Internal Column that hold the points value and button to play again after first game
         */
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("$currentPoints", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (playerTurnOver) {
                    Button(onClick = { viewModel.startGame() }) { Text("Play Again") }
                }
            }
        }
        /*
        Internal Column for dealer hand
         */
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Dealer", color = Color.White)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!playerTurnOver) { // Shows ? for dealer cards until the player turn is over
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
                } else { // Lists all cards in the dealers hand, updating each time the dealer receives a new card, only when player is finished
                    dealerHand.forEach { card ->
                        Box(
                            modifier = Modifier
                                .size(80.dp, 120.dp)
                                .background(Color.White, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${card?.face} ${card?.suit}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        /*
        Internal column for player hand
         */
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Player", color = Color.White)
            if (numHands == 1) { // If/ Else If/ Else for when the player splits hands, showing buttons to move between, or only showing base hand if no split
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    playerHand.forEach { card ->
                        Box(
                            modifier = Modifier
                                .size(80.dp, 120.dp)
                                .background(Color.White, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${card?.face} ${card?.suit}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    multiHands[currentHandIndex-1]!!.hand.forEach { card ->
                        Box(
                            modifier = Modifier
                                .size(80.dp, 120.dp)
                                .background(Color.White, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${card?.face} ${card?.suit}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                /*
                Row that holds buttons for navigating between hands
                 */
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (currentHandIndex > 1 && currentHandIndex < multiHands.size) {
                        Button(onClick = {currentHandIndex -= 1}) {Text("Prev Hand")}
                        Button(onClick = {currentHandIndex += 1}) {Text("Next Hand")}
                    } else if (currentHandIndex > 1 && currentHandIndex == multiHands.size) {
                        Button(onClick = {currentHandIndex -= 1}) {Text("Prev Hand")}
                    } else {
                        Button(onClick = {currentHandIndex += 1}) {Text("Next Hand")}
                    }
                }
            }
        }
        /*
        Row that holds buttons for playing the game
         */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {viewModel.hit()}) { Text("Hit") }
            Button(onClick = {viewModel.stand()}) { Text("Stand") }
            Button(onClick = {viewModel.doubleDown()}) { Text("Double") }
            Button(onClick = {}) { Text("Split") }
        }
        /*
        Row that holds buttons to navigate to different screens in the game
         */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {onGoToSettings()}) {Text("Settings")}
            Button(onClick = {onGoToPostGame()}) {Text("End Game")}
        }
    }
}
