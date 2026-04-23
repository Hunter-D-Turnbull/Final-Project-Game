package edu.gvsu.cis.finalprojectgame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(modifier: Modifier, viewModel: AppViewModel, onBack: () -> Unit, onLoginSuccess: () -> Unit, onGoToSignup: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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

            TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") }
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = {
                    viewModel.signIn(email, password) { success, _ ->
                        if (success) onLoginSuccess()
                    }
                }) {
                    Text("Sign In")
                }

                Button(onClick = { onGoToSignup() }) {
                    Text("Sign Up")
                }

                Button(onClick = { onBack() }) {
                    Text("Back")
                }
            }
        }
    }
}