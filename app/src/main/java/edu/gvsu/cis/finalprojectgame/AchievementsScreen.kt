package edu.gvsu.cis.finalprojectgame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AchievementsScreen(modifier: Modifier, viewModel: AppViewModel, onBack: () -> Unit) {
    val achievements = achievementsList
    val unlocked by viewModel.unlockedAchievements.collectAsState()
    val color by viewModel.currentBackgroundColor.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchUnlockedAchievements()
        viewModel.fetchStats()
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(achievements) { achievement ->

                val isUnlocked = achievement.id in unlocked

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            text = achievement.title,
                            fontWeight = FontWeight.Bold
                        )

                        Text(achievement.description)

                        Text(
                            text = if (isUnlocked) "Unlocked" else "Locked"
                        )
                    }
                }
            }
        }
    }
}