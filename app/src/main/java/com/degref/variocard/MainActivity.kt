package com.degref.variocard

import com.degref.variocard.screens.MyCardsScreen
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.degref.variocard.ui.theme.VarioCardTheme
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.degref.variocard.components.SharedViewModel
import com.degref.variocard.data.Serializer
import com.degref.variocard.screens.AddCardScreen
import com.degref.variocard.screens.CardDetailScreen
import com.degref.variocard.screens.ListScreen
import com.degref.variocard.screens.addCardToStorage
import com.degref.variocard.ui.theme.Blue900
import kotlinx.coroutines.GlobalScope
import java.util.UUID


class MainActivity : ComponentActivity() {
    // Pls work
    var isSenderActive by mutableStateOf(true)
    private lateinit var nfcManager: NFCManager
    private lateinit var wifiDirectManager: WiFiDirectManager
    private lateinit var wifiDirectReceiver: BroadcastReceiver
    private lateinit var intentFilter: IntentFilter
    lateinit var viewModel: SharedViewModel
    private lateinit var navController: NavHostController
    val context: Context = this


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wifiDirectManager = WiFiDirectManager(this, this@MainActivity)
        nfcManager = NFCManager(this, this@MainActivity)

        wifiDirectManager.arePermissionsOk()
        initializeWiFiDirectReceiver()
        viewModel = SharedViewModel(nfcManager, wifiDirectManager)
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter?.let {
            if (!it.isEnabled) {
                showToast("NFC is not enabled")
            } else {
                nfcManager.startReaderMode(wifiDirectManager)
            }
        }
        setContent {
            VarioCardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreenPreview()
                }
            }
        }
    }



    private fun initializeWiFiDirectReceiver() {
        wifiDirectReceiver = WiFiDirectReceiver(wifiDirectManager, this@MainActivity)
        intentFilter = IntentFilter()

        // Add necessary Wi-Fi Direct action filters
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        registerReceiver(wifiDirectReceiver, intentFilter)
    }

    fun tryToAddCard(card: String, image: String){
        try{
            val newCard = Serializer().jsonToCard(card)
            newCard.image = image
            newCard.id = UUID.randomUUID()
            Log.d("VarioCard", "Card, serialized: ${newCard.toString()}")
            addCardToStorage(newCard, context)
            viewModel.listAllCards.add(newCard)
        } catch (e: Exception){
            Log.d("VarioCard", e.toString())
            Log.d("VarioCard", "error serializing card, message: $card, image:$image")
        }
    }

    override fun onDestroy() {
        lifecycleScope.launch {
            wifiDirectManager.closeWifiDirectGroup()
        }
        wifiDirectManager.stopServer()
        unregisterReceiver(wifiDirectReceiver)
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Composable
    fun MainScreenPreview() {
        navController = rememberNavController()
        MainScreen(navController!!, viewModel)
    }

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Composable
    fun MainScreen(navController: NavHostController, viewModel: SharedViewModel) {
        Scaffold(
            bottomBar = {
                BottomNavigation(backgroundColor = Blue900, contentColor = Color.White) {
                    BottomNavigationItem(
                        icon = {
                            Icon(imageVector = Icons.Default.List, contentDescription = "List")
                        },
                        label = { Text("List") },
                        selected = true,
                        onClick = {
                            navController.navigate("list")
                        }
                    )
                    BottomNavigationItem(
                        icon = {
                            Icon(imageVector = Icons.Default.AccountBox, contentDescription = "My Cards")
                        },
                        label = { Text("My cards") },
                        selected = false,
                        onClick = { navController.navigate("myCards") }
                    )
                }
            },
            content = {
                NavHost(navController = navController, startDestination = "list") {
                    val resources = resources
                    composable("list") {
                        viewModel.listDestination.value = "all"
                        ListScreen(navController, viewModel, context)
                    }
                    composable("myCards") {
                        viewModel.listDestination.value = "myCards"
                        MyCardsScreen(navController, viewModel, context)
                    }
                    composable("addCard") {
                        AddCardScreen(navController, viewModel)
                    }
                    composable("cardDetail") {
                        CardDetailScreen(navController, viewModel)
                    }
                }
            }
        )
    }

    fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isSenderActive) {
            nfcManager.startReaderMode(wifiDirectManager)
        } else {
            nfcManager.stopReaderMode()
        }
    }

    override fun onPause() {
        super.onPause()
        NfcAdapter.getDefaultAdapter(this)?.disableForegroundDispatch(this)
        if (!isSenderActive) {
            nfcManager.stopReaderMode()
        }
        wifiDirectManager.stopServer()
    }
}

