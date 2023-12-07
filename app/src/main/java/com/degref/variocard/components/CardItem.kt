package com.degref.variocard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.degref.variocard.data.Card


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
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(0.9f)
            ) {
                Text(text = "${card.name}", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "${card.phone}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "${card.email}", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}
