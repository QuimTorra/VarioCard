package com.degref.variocard.components

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.degref.variocard.data.Card

class SharedViewModel : ViewModel() {
    val selectedCard = mutableStateOf<Card?>(null)
}