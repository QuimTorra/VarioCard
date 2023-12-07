package com.degref.variocard.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsProperties.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun AddCardScreen(
    navController: NavHostController,
    name: String = "",
    phone: String = "",
    email: String = "",
    company: String = "",
    additionalInfo: String = ""
) {
    var name by remember { mutableStateOf(name) }
    var phone by remember { mutableStateOf(phone) }
    var email by remember { mutableStateOf(email) }
    var company by remember { mutableStateOf(company) }
    var additionalInfo by remember { mutableStateOf(additionalInfo) }

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

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
            },
            label = { Text("Name") },
            keyboardActions = KeyboardActions(
                onDone = {
                    // Acciones cuando se presiona "Done" en el teclado
                    // Puedes ocultar el teclado u realizar otras acciones aquí
                }
            ),
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
            keyboardActions = KeyboardActions(
                onDone = {
                    // Acciones cuando se presiona "Done" en el teclado
                    // Puedes ocultar el teclado u realizar otras acciones aquí
                }
            ),
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
            keyboardActions = KeyboardActions(
                onDone = {
                    // Acciones cuando se presiona "Done" en el teclado
                    // Puedes ocultar el teclado u realizar otras acciones aquí
                }
            ),
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
            keyboardActions = KeyboardActions(
                onDone = {
                    // Acciones cuando se presiona "Done" en el teclado
                    // Puedes ocultar el teclado u realizar otras acciones aquí
                }
            ),
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
            keyboardActions = KeyboardActions(
                onDone = {
                    // Acciones cuando se presiona "Done" en el teclado
                    // Puedes ocultar el teclado u realizar otras acciones aquí
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        Button(
            onClick = {
                  var formCompleted = formIsCompleted(name, phone, email)
                  formValidated = true

                  if (formCompleted) {
                    navController.navigateUp()
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