package com.degref.variocard.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.degref.variocard.components.SharedViewModel
import com.degref.variocard.ui.theme.Blue50
import com.degref.variocard.ui.theme.Blue900
import java.io.File

@Composable
fun CardDetailScreen(navController: NavHostController, viewModel: SharedViewModel) {
    val selectedCard = viewModel.selectedCard.value

    if (selectedCard != null) {
        Column (
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier
                    .clickable {
                        navController.navigateUp()
                    }
            )

            Spacer(modifier = Modifier.padding(8.dp))

            if (selectedCard.image != "") {
                loadBitmapFromFile(selectedCard.image)
                Spacer(modifier = Modifier.padding(16.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Blue50)
                    .border(2.dp, Blue900, shape = MaterialTheme.shapes.medium)
                    .padding(12.dp)
            ) {
                Column() {
                    Text("NAME", fontWeight = FontWeight.Bold)
                    Text(selectedCard.name)
                }
            }

            Spacer(modifier = Modifier.padding(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Blue50)
                    .border(2.dp, Blue900, shape = MaterialTheme.shapes.medium)
                    .padding(12.dp)
            ) {
                Column() {
                    Text("PHONE", fontWeight = FontWeight.Bold)
                    Text(selectedCard.phone)
                }
            }

            Spacer(modifier = Modifier.padding(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Blue50)
                    .border(2.dp, Blue900, shape = MaterialTheme.shapes.medium)
                    .padding(12.dp)
            ) {
                Column() {
                    Text("EMAIL", fontWeight = FontWeight.Bold)
                    Text(selectedCard.email)
                }
            }

            Spacer(modifier = Modifier.padding(8.dp))

            if (selectedCard.company.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Blue50)
                        .border(2.dp, Blue900, shape = MaterialTheme.shapes.medium)
                        .padding(12.dp)
                ) {
                    Column() {
                        Text("COMPANY", fontWeight = FontWeight.Bold)
                        Text(selectedCard.company)
                    }
                }
                Spacer(modifier = Modifier.padding(8.dp))
            }

            if (selectedCard.additionalInfo.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Blue50)
                        .border(2.dp, Blue900, shape = MaterialTheme.shapes.medium)
                        .padding(12.dp)
                ) {
                    Column() {
                        Text("ADDITIONAL INFORMATION", fontWeight = FontWeight.Bold)
                        Text(selectedCard.additionalInfo)
                    }
                }
                Spacer(modifier = Modifier.padding(8.dp))
            }

            if (viewModel.listDestination.value != "all") {
                Button(
                    onClick = {
                        Log.d("cardSelected", selectedCard.toString())
                        navController.navigate("addCard")
                    },
                    colors = ButtonDefaults.buttonColors(Blue900),
                    modifier = Modifier
                        .align(Alignment.End)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit")
                }
            }
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                )
            }
        }
    } else {
        Log.d("YOBAMA", "image file doesn't exists")
    }
}