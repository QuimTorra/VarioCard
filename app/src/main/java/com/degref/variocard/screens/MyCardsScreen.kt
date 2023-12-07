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
import androidx.navigation.NavHostController
import com.degref.variocard.components.CardListItem

data class Card(
    var name: String,
    var phone: String,
    var email: String,
    var company: String,
    var additionalInfo: String
)

var myOwnCards: List<Card> by mutableStateOf(
    listOf(
        Card("Laura Chavarria Solé", "609007385", "laura.chavarria@estudiantat.upc.edu", "FIB", ""),
        Card("John Doe", "123456789", "john.doe@example.com", "Company ABC", "")
    )
)
@Composable
fun MyCardsScreen(navController: NavHostController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopBar(navController)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(myOwnCards) { card ->
                    CardListItem(card = card, navController = navController)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            // Resto del contenido de tu pantalla debajo de la barra
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TopBar(navController: NavHostController) {
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
        Icon(
            imageVector = Icons.Default.AddCircle,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .clickable {
                    navController.navigate("addCard")
                }
                .padding(16.dp)
                .align(Alignment.CenterEnd)
        )
    }
}

fun addCard(newCard: Card) {
    myOwnCards = myOwnCards + listOf<Card>(newCard)
}
