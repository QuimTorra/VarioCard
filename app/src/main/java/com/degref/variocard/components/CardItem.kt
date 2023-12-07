package com.degref.variocard.components

import Card
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


@Composable
fun CardListItem(card: Card, navController: NavController) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Handle click on a card
                // You can navigate to another screen or perform other actions here
            },
        shape = MaterialTheme.shapes.medium, // Use a shape that doesn't clip the background
        color = Color.Gray,
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = "Name: ${card.name}")
            Text(text = "Phone: ${card.phone}")
            Text(text = "Email: ${card.email}")
        }
    }
}
