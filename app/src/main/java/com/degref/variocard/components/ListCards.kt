package com.degref.variocard.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.degref.variocard.data.Card

@Composable
fun ListCards(cards: List<Card>, navController: NavHostController, viewModel: SharedViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        itemsIndexed(cards) { index, card ->
            CardListItem(card = card, navController = navController, viewModel = viewModel)
            Spacer(modifier = Modifier.height(8.dp))

            if (index < cards.size - 1) {
                Spacer(modifier = Modifier.height(56.dp))
            }
        }
    }
}