package com.degref.variocard.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.degref.variocard.components.SharedViewModel
import com.degref.variocard.data.Card
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

//var context: Context = TODO()
var bitmap: Bitmap? = null

@Composable
fun AddCardScreen(
    navController: NavHostController, viewModel: SharedViewModel, context: Context
) {
    val context = LocalContext.current
    var card = viewModel.selectedCard.value
    card?.let { Log.d("cardSelected", it.image) }

    var id by remember { mutableStateOf(card?.id ?: 1) }
    var name by remember { mutableStateOf(card?.name ?: "") }
    var phone by remember { mutableStateOf(card?.phone ?: "") }
    var email by remember { mutableStateOf(card?.email ?: "") }
    var company by remember { mutableStateOf(card?.company ?: "") }
    var additionalInfo by remember { mutableStateOf(card?.additionalInfo ?: "") }
    var image by remember { mutableStateOf(card?.image ?: "") }

    var formValidated by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier
                .clickable {
                    navController.navigateUp()
                }
                .padding(16.dp)
        )

        if (image == "") image = PickImageFromGallery(null, context)
        else {
            loadBitmapFromFile(image)
        }

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
            },
            label = { Text("Name") },
            isError = formValidated and name.isBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        OutlinedTextField(
            value = phone,
            onValueChange = {
                phone = it
            },
            label = { Text("Phone number") },
            isError = formValidated and phone.isBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = { Text("Email") },
            isError = formValidated and email.isBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        OutlinedTextField(
            value = company,
            onValueChange = {
                company = it
            },
            label = { Text("Company") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        OutlinedTextField(
            value = additionalInfo,
            onValueChange = {
                additionalInfo = it
            },
            label = { Text("Additional information") },
            maxLines = 5,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        Button(
            onClick = {
                  var formCompleted = formIsCompleted(name, phone, email)
                  formValidated = true

                  if (formCompleted) {
                      if (viewModel.listDestination.value != "all") {
                          var filePath = bitmap?.let { saveImageToStorage(context, it, image) }
                          if (filePath != null) {
                              Log.d("YOBAMA", filePath)
                              addMyCardToStorage(Card(id, name, phone, email, company, additionalInfo, filePath), context)
                          }
                          else {
                              Log.d("YOBAMA", "FILEpATH IS null")
                              addMyCardToStorage(Card(id, name, phone, email, company, additionalInfo, ""), context)
                          }
                          navController.navigate("myCards")
                      }

                  }
            },
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.End)
        ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Send")
        }
    }
}

fun formIsCompleted(name: String, phone: String, email: String): Boolean {
    return name.isNotBlank() and phone.isNotBlank() and email.isNotBlank()
}

@Composable
fun PickImageFromGallery(image: Uri?, context: Context): String {
    var imageUri by remember {
        mutableStateOf<Uri?>(image)
    }

    var launcher =  rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        uri: Uri? -> imageUri = uri
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        imageUri?.let { uri ->
            bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }

            bitmap?.let { btm ->
                Image(
                    bitmap = btm.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(170.dp)
                        .padding(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { launcher.launch("image/*") }) {
            Text(text = "Pick Image")
        }
    }

    return imageUri?.toString() ?: ""
}

fun saveImageToStorage(context: Context, bitmap: Bitmap, fileName: String): String? {
    val name = "${System.currentTimeMillis()}.jpg"
    val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyAppImages")

    if (!directory.exists()) {
        directory.mkdirs()
    }

    val file = File(directory, name)

    if (!file.exists()) {
        file.createNewFile()
    }

    try {
        val fileOutputStream = FileOutputStream(file)

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)

        fileOutputStream.flush()
        fileOutputStream.close()

        return file.absolutePath
    } catch (e: IOException) {
        e.printStackTrace()
        Log.d("errorsaveimage", "error")
        return null
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