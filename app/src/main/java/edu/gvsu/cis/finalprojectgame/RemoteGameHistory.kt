package edu.gvsu.cis.finalprojectgame

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Date

@Composable
fun RemoteGameHistoryScreen(modifier: Modifier,viewModel: AppViewModel, onBack: () -> Unit, onGoToHandDetails: () -> Unit){
    val games by viewModel.remoteGames.collectAsState()
    val color by viewModel.currentBackgroundColor.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchRemoteGames()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.padding(top = 48.dp)) {

            Button(onClick = onBack) {
                Text("Back")
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
                                viewModel.selectRemoteGame(game.id)
                                onGoToHandDetails()
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            Text("Game ID: ${game.id}")

                            val date = Date(game.timestamp)
                            Text("Date: $date")

                            Text("Final Points: ${game.finalPoints}")
                            Text("Hands Played: ${game.totalHands}")
                        }
                    }
                }
            }
        }
    }
}