package com.degref.variocard.screens

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.degref.variocard.components.ListCards
import com.degref.variocard.components.SharedViewModel
import com.degref.variocard.data.Card
import com.degref.variocard.ui.theme.Blue900
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream

var listAllCards: MutableList<Card> = mutableListOf()

@Composable
fun ListScreen(navController: NavHostController, viewModel: SharedViewModel, resources: Resources, context: Context) {
    listAllCards = getListCardsStorage(context)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Blue900),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "SAVED CARDS",
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .clickable {
                            viewModel.activateReader()
                        }
                        .padding(16.dp)
                        .align(Alignment.CenterEnd)
                )
            }
            ListCards(viewModel.listAllCards, navController, viewModel)
        }
    }
}

fun getListCardsStorage(context: Context): MutableList<Card> {
    val file = File(context.filesDir, "list_cards")

    if (!file.exists()) {
        try {
            file.createNewFile()
        } catch (e: Exception) {
            return mutableListOf()
        }
    }

    try {
        val content = file.readText()

        if (content.isNotBlank()) {
            val gson = Gson()
            val listCardsType = object : TypeToken<MutableList<Card>>() {}.type
            return gson.fromJson(content, listCardsType)
        }
        return mutableListOf()

    } catch (e: Exception) {
        return mutableListOf()
    }
}

fun addCardToStorage(card: Card, context: Context) {
    val currentCards = getListCardsStorage(context)

    currentCards.add(card)

    val gson = Gson()
    val updatedJson = gson.toJson(currentCards)

    val file = File(context.filesDir, "list_cards")

    FileOutputStream(file).use { it.write(updatedJson.toByteArray()) }
}

fun deleteCard(card: Card, context: Context) {
    val currentCards = getListCardsStorage(context)

    val existingCardIndex = listAllCards.indexOfFirst { it.id == card.id }

    if (existingCardIndex != -1) {
        currentCards.removeAt(existingCardIndex)

        val gson = Gson()
        val updatedJson = gson.toJson(currentCards)

        val file = File(context.filesDir, "list_cards")

        FileOutputStream(file).use { it.write(updatedJson.toByteArray()) }
    }
}