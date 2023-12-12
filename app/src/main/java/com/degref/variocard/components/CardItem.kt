package com.degref.variocard.components

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.degref.variocard.data.Card
import com.degref.variocard.data.Serializer
import com.degref.variocard.ui.theme.Blue50
import com.degref.variocard.ui.theme.Blue900
import java.io.File

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
        color = Blue50,
        border = BorderStroke(2.dp, Blue900)
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
                    LoadBitmapFromFile(card.image)
                }
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    Text(
                        text = card.name,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = card.phone, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = card.email, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .align(Alignment.CenterVertically)
                    .clickable {
                        Log.d("VarioCard", Serializer().cardToJson(card))
                        if(card.image != "") viewModel.setValueImage(card.image)
                        viewModel.activateSender(Serializer().cardToJson(card))
                    }
            )

        }
    }
}

@Composable
private fun LoadBitmapFromFile(filePath: String) {
    val file = File(filePath)
    if (file.exists()) {
        val bitmap: Bitmap? = BitmapFactory.decodeFile(file.absolutePath)
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
            )
        }
    } else {
        Log.d("Image not exists", "Image file doesn't exist")
    }
}
