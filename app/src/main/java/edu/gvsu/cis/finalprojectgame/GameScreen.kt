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
fun GameScreen(modifier: Modifier, viewModel: AppViewModel, onGoToPostGame: () -> Unit) {
    val color by viewModel.currentBackgroundColor.collectAsState()
    val dealerHand by viewModel.dealerHand.collectAsState()
    val playerTurnOver by viewModel.playerTurnOver.collectAsState()
    val currentPoints by viewModel.currentPoints.collectAsState()
    val hands by viewModel.allHands.collectAsState()
    val currentIndex by viewModel.currentHandIndex.collectAsState()
    val currentBet by viewModel.currentBet.collectAsState()
    val hasPlayed by viewModel.hasPlayed.collectAsState()
    val betLocked by viewModel.betLocked.collectAsState()

    val currentHand = hands.getOrNull(currentIndex)

    val canSplit =
        betLocked &&
                currentHand?.hand?.size == 2 &&
                currentHand.hand[0]?.value == currentHand.hand[1]?.value &&
                currentPoints >= currentBet * 2 &&
                !hasPlayed

    val canDouble =
        betLocked &&
                currentPoints >= currentBet * 2 &&
                !hasPlayed

    val canIncrease25 = !betLocked && currentBet + 25 <= currentPoints
    val canIncrease100 = !betLocked && currentBet + 100 <= currentPoints
    val canDecrease25 = !betLocked && currentBet - 25 > 0
    val canDecrease100 = !betLocked && currentBet - 100 > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = color),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (currentPoints > 0) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Current Score: $currentPoints",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (playerTurnOver && currentIndex == hands.lastIndex) {
                        Button(onClick = { viewModel.playAgain() }) {
                            Text("Play Again")
                        }
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (!betLocked) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (canDecrease100) {
                            Button(onClick = { viewModel.minushundred() }) { Text("-100") }
                        }
                        if (canDecrease25) {
                            Button(onClick = { viewModel.minustwentyfive() }) { Text("-25") }
                        }
                        Text(
                            "$currentBet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (canIncrease25) {
                            Button(onClick = { viewModel.plustwentyfive() }) { Text("+25") }
                        }
                        if (canIncrease100) {
                            Button(onClick = { viewModel.plushundred() }) { Text("+100") }
                        }
                    }
                    Button(onClick = { viewModel.lockBetAndDeal() }) {
                        Text("Lock Bet")
                    }
                } else {
                    Text(
                        "Current Bet: $currentBet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            if (betLocked) {
                GameScreenDealerHand(dealerHand, reveal = playerTurnOver)
            }
            if (betLocked) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Player", color = Color.White)
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
            }
            if (betLocked) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { viewModel.hit() }) { Text("Hit") }
                    Button(onClick = { viewModel.stand() }) { Text("Stand") }
                    if (canDouble) {
                        Button(onClick = { viewModel.doubleDown() }) {
                            Text("Double")
                        }
                    }
                    if (canSplit) {
                        Button(onClick = { viewModel.split() }) {
                            Text("Split")
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    viewModel.endGame()
                    onGoToPostGame()
                }) {
                    Text("End Game")
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Game Over",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    viewModel.endGame()
                    onGoToPostGame()
                }) {
                    Text("End Game")
                }
            }
        }
    }
}