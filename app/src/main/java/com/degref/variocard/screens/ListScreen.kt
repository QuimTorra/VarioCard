package com.degref.variocard.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.degref.variocard.components.ListCards
import com.degref.variocard.components.SharedViewModel
import com.degref.variocard.data.Card

var listAllCards: MutableList<Card> = mutableListOf(
        Card(1, "Laura Chavarria Solé", "609007385", "laura.chavarria@estudiantat.upc.edu", "FIB", "", null),
        Card(2,"John Doe", "123456789", "john.doe@example.com", "Company ABC", "", null)
    )

@Composable
fun ListScreen(navController: NavHostController, viewModel: SharedViewModel) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "SAVED CARDS",
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .clickable {
                            viewModel.activateReader()
                        }
                        .padding(16.dp)
                        .align(Alignment.CenterEnd)
                )
            }
            ListCards(listAllCards, navController, viewModel)
        }
    }
}

fun editCard(newCard: Card) {
    val existingCardIndex = listAllCards.indexOfFirst { it.id == newCard.id }

    if (existingCardIndex != -1) {
        listAllCards[existingCardIndex] = newCard
    } else {
        listAllCards.add(newCard)
    }
}