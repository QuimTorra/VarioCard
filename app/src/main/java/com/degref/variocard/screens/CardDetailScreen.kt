package com.degref.variocard.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.degref.variocard.components.SharedViewModel

@Composable
fun CardDetailScreen(navController: NavHostController, viewModel: SharedViewModel) {
    // Obtener la tarjeta seleccionada del ViewModel
    val selectedCard = viewModel.selectedCard.value

    // Mostrar detalles de la tarjeta
    if (selectedCard != null) {
        Text(selectedCard.name)
    }
}