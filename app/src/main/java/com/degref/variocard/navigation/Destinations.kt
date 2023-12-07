package com.degref.variocard.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destinations(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object ListCard: Destinations("ListCards", "List of cards", Icons.Default.List)
    object MyOwnCards: Destinations("MyCards", "My cards", Icons.Default.AccountBox)
}
