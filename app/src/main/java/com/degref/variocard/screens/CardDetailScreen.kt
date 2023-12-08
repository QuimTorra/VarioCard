package com.degref.variocard.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
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

@Composable
fun CardDetailScreen(navController: NavHostController, viewModel: SharedViewModel) {
    val selectedCard = viewModel.selectedCard.value

    var bitmap = remember {
        mutableStateOf<Bitmap?>(null)
    }
    var context = LocalContext.current

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

            Spacer(modifier = Modifier.padding(16.dp))

            selectedCard.image?.let {
                if (Build.VERSION.SDK_INT < 28) {
                    bitmap.value = MediaStore.Images
                        .Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    bitmap.value = ImageDecoder.decodeBitmap(source)
                }

                bitmap.value?.let { btm ->
                    Image(
                        bitmap = btm.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }

            Spacer(modifier = Modifier.padding(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(8.dp)
            ) {
                Column() {
                    Text("Name", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(selectedCard.name)
                }
            }

            Spacer(modifier = Modifier.padding(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(8.dp)
            ) {
                Column() {
                    Text("Phone number", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(selectedCard.phone)
                }
            }

            Spacer(modifier = Modifier.padding(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(8.dp)
            ) {
                Column() {
                    Text("Email", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(selectedCard.email)
                }
            }

            Spacer(modifier = Modifier.padding(16.dp))

            if (selectedCard.company.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray)
                        .padding(8.dp)
                ) {
                    Column() {
                        Text("Company", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text(selectedCard.company)
                    }
                }
            }

            Spacer(modifier = Modifier.padding(16.dp))

            if (selectedCard.additionalInfo.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray)
                        .padding(8.dp)
                ) {
                    Column() {
                        Text("Additional information", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text(selectedCard.additionalInfo)
                    }
                }
            }

            Spacer(modifier = Modifier.padding(16.dp))

            Button(
                onClick = {
                    navController.navigate("addCard")
                },
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