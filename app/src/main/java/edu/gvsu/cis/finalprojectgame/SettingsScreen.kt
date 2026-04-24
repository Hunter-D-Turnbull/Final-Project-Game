package edu.gvsu.cis.finalprojectgame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(modifier: Modifier, viewModel: AppViewModel, onGoToColorSelector: () -> Unit, onBack: () -> Unit, onGoToSignUp: () -> Unit, onGoToLogin: () -> Unit, onGoToAccount: () -> Unit, onGoToLocalHistory: () -> Unit, onGoToRemoteGameHistory: () -> Unit, onGoToLeaderboard: () -> Unit) {
    val color by viewModel.currentBackgroundColor.collectAsState()
    val gameInProgress by viewModel.gameInProgress.collectAsState()
    val isSignedIn by viewModel.isUserSignedIn.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = color),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Settings & Menu", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Button(onClick = {onGoToColorSelector()}) {Text("Color Selector")}
        if (!isSignedIn) {
            Button(onClick = { onGoToLogin() }) { Text("Login") }
            Button(onClick = { onGoToSignUp() }) { Text("Sign Up") }
        } else {
            Button(onClick = {onGoToAccount()}) {Text("User Account")}
            Button(onClick = {onGoToRemoteGameHistory()}) {Text("Remote History")}
        }
        Button(onClick = {onGoToLocalHistory()}) {Text("Local History")}
        Button(onClick = {onGoToLeaderboard()}) {Text("Leaderboard")}
        Button(onClick = {onBack()}) { Text("Back")}
    }
}