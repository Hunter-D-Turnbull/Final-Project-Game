package edu.gvsu.cis.finalprojectgame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import edu.gvsu.cis.finalprojectgame.ui.theme.CardBlack
import edu.gvsu.cis.finalprojectgame.ui.theme.MyBlue
import edu.gvsu.cis.finalprojectgame.ui.theme.MyGold
import edu.gvsu.cis.finalprojectgame.ui.theme.MyGreen
import edu.gvsu.cis.finalprojectgame.ui.theme.MyMaroon
import edu.gvsu.cis.finalprojectgame.ui.theme.MyRed

@Composable
fun ColorSelectorScreen(modifier: Modifier, viewModel: AppViewModel, onBack: () -> Unit) {
    val color by viewModel.currentBackgroundColor.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = color),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Select Background Color", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Button(onClick = {viewModel.updateBackground(MyGreen)},
            colors = ButtonDefaults.buttonColors(containerColor = MyGreen, contentColor = Color.White)){
            Text("Green")
        }
        Button(onClick = {viewModel.updateBackground(MyBlue)},
            colors = ButtonDefaults.buttonColors(containerColor = MyBlue, contentColor = Color.White)){
            Text("Blue")
        }
        Button(onClick = {viewModel.updateBackground(MyRed)},
            colors = ButtonDefaults.buttonColors(containerColor = MyRed, contentColor = Color.White)){
            Text("Red")
        }
        Button(onClick = {viewModel.updateBackground(MyMaroon)},
            colors = ButtonDefaults.buttonColors(containerColor = MyMaroon, contentColor = Color.White)){
            Text("Maroon")
        }
        Button(onClick = {viewModel.updateBackground(MyGold)},
            colors = ButtonDefaults.buttonColors(containerColor = MyGold, contentColor = Color.White)){
            Text("Gold")
        }
        Button(onClick = {onBack()}){
            Text("Back")
        }
    }
}