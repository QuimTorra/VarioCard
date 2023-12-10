package com.degref.variocard.screens

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

var myOwnCards: MutableList<Card> = mutableListOf()

@Composable
fun MyCardsScreen(navController: NavHostController, viewModel: SharedViewModel, context: Context) {
    myOwnCards = getMyCardsStorage(context)
    Log.d("myOwnCards-list", myOwnCards.toString())
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopBar(navController, viewModel)
            ListCards(myOwnCards, navController, viewModel)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TopBar(navController: NavHostController, viewModel: SharedViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Blue900),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "MY OWN CONTACT CARDS",
            fontSize = 24.sp,
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )
        Log.d("myOwnCards", "abans icono +")
        if (myOwnCards.size < 1) {
            Log.d("myOwnCards", "if icono +")
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .clickable {
                        viewModel.selectedCard.value = null
                        navController.navigate("addCard")
                    }
                    .padding(16.dp)
                    .align(Alignment.CenterEnd)
            )
        }
    }
}

fun getMyCardsStorage(context: Context): MutableList<Card> {
    val file = File(context.filesDir, "my_cards")

    if (!file.exists()) {
        try {
            file.createNewFile()
        } catch (e: Exception) {
            return mutableListOf()
        }
    }

    try {
        val content = file.readText()
        Log.d("myOwnCards-content", content)

        if (content.isNotBlank()) {
            val gson = Gson()
            val listCardsType = object : TypeToken<MutableList<Card>>() {}.type
            return gson.fromJson(content, listCardsType)
        }
        return mutableListOf()

    } catch (e: Exception) {
        Log.d("myOwnCards-error", "error")
        return mutableListOf()
    }

}

fun addMyCardToStorage(card: Card, context: Context) {
    val currentCards: MutableList<Card> = mutableListOf()
    currentCards.add(card)

    val gson = Gson()
    val updatedJson = gson.toJson(currentCards)
    Log.d("myOwnCards-add", updatedJson)

    val file = File(context.filesDir, "my_cards")
    FileOutputStream(file).use { it.write(updatedJson.toByteArray()) }
    Log.d("myOwnCards-addend", "end")
}

