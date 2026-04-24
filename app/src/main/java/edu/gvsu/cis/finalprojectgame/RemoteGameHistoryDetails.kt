package edu.gvsu.cis.finalprojectgame

import androidx.compose.foundation.background
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

@Composable
fun RemoteGameDetailScreen(modifier: Modifier,viewModel: AppViewModel, onBack: () -> Unit) {
    val hands by viewModel.remoteHands.collectAsState()
    val selectedGameId by viewModel.selectedRemoteGameId.collectAsState()
    val color by viewModel.currentBackgroundColor.collectAsState()

    LaunchedEffect(selectedGameId) {
        selectedGameId?.let {
            viewModel.fetchRemoteHands(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
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
                items(hands) { hand ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text("Hand ${hand.handNumber}")
                            Text("Result: ${hand.result}")
                            Text("Points: ${hand.pointsChange}")
                        }
                    }
                }
            }
        }
    }
}