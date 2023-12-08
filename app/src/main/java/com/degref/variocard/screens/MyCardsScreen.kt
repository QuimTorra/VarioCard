import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.degref.variocard.components.CardListItem
import com.degref.variocard.components.ListCards
import com.degref.variocard.components.SharedViewModel
import com.degref.variocard.data.Card

var myOwnCards: List<Card> by mutableStateOf(
    listOf()
)

@Composable
fun MyCardsScreen(navController: NavHostController, viewModel: SharedViewModel) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopBar(navController, viewModel)
            ListCards(myOwnCards, navController, viewModel)
            // Resto del contenido de tu pantalla debajo de la barra
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TopBar(navController: NavHostController, viewModel: SharedViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "MY OWN CONTACT CARDS",
            fontSize = 24.sp,
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )
        if (myOwnCards.size < 1) {
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

fun addCard(newCard: Card) {
    myOwnCards = listOf(newCard)
}

