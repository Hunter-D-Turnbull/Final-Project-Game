package edu.gvsu.cis.finalprojectgame

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Date

@Composable
fun PostGameScreen(modifier: Modifier = Modifier, viewModel: AppViewModel, onGoToSettings: () -> Unit, onBack: () -> Unit, onGoToHandDetails: () -> Unit) {
    val games by viewModel.games.collectAsState(initial = emptyList())
    val color by viewModel.currentBackgroundColor.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = color),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.padding(top = 48.dp)
        )
        {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Main Menu Button
                Button(
                    onClick = { onBack() }
                ) {
                    Text("Main Menu")
                }
                // Settings button
                Button(
                    onClick = { onGoToSettings() }
                ) {
                    Text("Settings")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(games) { game ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectGame(game.gameId)
                                onGoToHandDetails()
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Text("Game ID: ${game.gameId}")
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val date = Date(game.timestamp)
                                Text("Date: $date")
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Text("Final Points: ${game.finalPoints}")
                                Text("Hands Played: ${game.totalHands}")
                            }
                        }
                    }
                }
            }
        }
    }
}