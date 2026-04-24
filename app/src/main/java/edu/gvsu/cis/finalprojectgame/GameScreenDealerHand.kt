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
fun GameScreenDealerHand(dealerHand: List<CardClass?>, reveal: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Dealer", color = Color.White)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            if (dealerHand.isEmpty()) return

            if (!reveal) {
                CardView(dealerHand[0])
                for (i in 1 until dealerHand.size) {
                    CardView(null)
                }
            } else {
                dealerHand.forEach { CardView(it) }
            }
        }
    }
}