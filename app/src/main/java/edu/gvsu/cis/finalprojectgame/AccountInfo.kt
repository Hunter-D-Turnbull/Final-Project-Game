package edu.gvsu.cis.finalprojectgame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun AccountInfoScreen(modifier: Modifier, viewModel: AppViewModel, onBack: () -> Unit, onToChangePassword: () -> Unit, onToAchievements: () -> Unit, onToAccountDeletion: () -> Unit) {
    val color by viewModel.currentBackgroundColor.collectAsState()
    val user = viewModel.currentUser

    var name by remember { mutableStateOf("Loading...") }
    var email by remember { mutableStateOf(user?.email ?: "No email") }

    LaunchedEffect(user) {
        val uid = user?.uid ?: return@LaunchedEffect

        try {
            val doc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .await()

            name = doc.getString("name") ?: "No name"
        } catch (e: Exception) {
            name = "Error loading name"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = color)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Account Info",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White)

        Text(text = "Name: $name",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White)

        Text(text = "Email: $email",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White)

        Button(onClick = { onBack() }) {
            Text("Back")
        }
        Button(onClick = {onToAchievements()}) {
            Text("Achievements")
        }
        Button(onClick = {onToChangePassword()}) {
            Text("Change Password")
        }
        Button(onClick = {viewModel.signOut()
            onBack()
        }) {
            Text("Sign Out")
        }
        Button(onClick = {onToAccountDeletion()}) {
            Text("Delete Account")
        }
    }
}