package com.degref.variocard.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.degref.variocard.data.Card
import java.io.File

@Composable
fun CardListItem(card: Card, navController: NavController, viewModel: SharedViewModel) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                viewModel.selectedCard.value = card
                navController.navigate("cardDetail")
            }),
        shape = MaterialTheme.shapes.medium, // Use a shape that doesn't clip the background
        color = Color.Gray,
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()

        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            ) {
                if (card.image != "") {
                    loadBitmapFromFile(card.image)
                }
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${card.name}",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = "${card.phone}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = "${card.email}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .align(Alignment.CenterVertically)
            )

        }
    }
}

@Composable
private fun loadBitmapFromFile(filePath: String) {
    val file = File(filePath)
    if (file.exists()) {
        var bitmap: Bitmap? = null
        bitmap = BitmapFactory.decodeFile(file.absolutePath)
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
            )
        }
    } else {
        Log.d("YOBAMA", "image file doesn't exists")
    }
}
