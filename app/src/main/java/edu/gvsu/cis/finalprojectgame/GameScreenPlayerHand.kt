package edu.gvsu.cis.finalprojectgame

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GameScreenPlayerHand(hands: List<Hand>, currentIndex: Int) {
    val currentHand = hands.getOrNull(currentIndex)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Player", color = Color.White)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            currentHand?.hand?.forEach { CardView(it) }
        }
    }
}