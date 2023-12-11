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
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.degref.variocard.ui.theme.VarioCardTheme
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.degref.variocard.components.SharedViewModel
import com.degref.variocard.data.Card
import com.degref.variocard.data.Serializer
import com.degref.variocard.screens.AddCardScreen
import com.degref.variocard.screens.CardDetailScreen
import com.degref.variocard.screens.ListScreen
import com.degref.variocard.ui.theme.Blue800
import com.degref.variocard.ui.theme.Blue900
import com.degref.variocard.ui.theme.BlueA700
import com.degref.variocard.ui.theme.DarkBlue
import com.degref.variocard.ui.theme.Purple40
import com.degref.variocard.ui.theme.Purple80


class MainActivity : ComponentActivity() {
    // Pls work
    private var sendingMessage by mutableStateOf("No message sent")
    private var receivedMessage by mutableStateOf("No message received")
    var isSenderActive by mutableStateOf(true)
    private lateinit var nfcManager: NFCManager
    private lateinit var wifiDirectManager: WiFiDirectManager
    private lateinit var wifiDirectReceiver: BroadcastReceiver
    private lateinit var intentFilter: IntentFilter
    lateinit var viewModel: SharedViewModel
    var navController: NavHostController? = null
    val context: Context = this


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wifiDirectManager = WiFiDirectManager(this, this@MainActivity)
        nfcManager = NFCManager(this, this@MainActivity)

        wifiDirectManager.arePermissionsOk()
        initializeWiFiDirectReceiver()
        if(nfcManager == null) Log.d("MONDONGO", "null initialized")
        viewModel = SharedViewModel(nfcManager, wifiDirectManager)
        // Set up NFC for HCE
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
            var c = Serializer().jsonToCard(card)
            c.image = image
            viewModel.listAllCards.add(c)
            navController!!.navigate("list")
        } catch (e: Exception){
            Log.d("MONDONGO", "error serializing card, message: $card, image:$image")
        }
        Log.d("MONDONGO", "WTF??")
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
    // @OptIn(ExperimentalMaterial3Api::class)
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
                    var resources = resources
                    composable("list") {
                        viewModel.listDestination.value = "all"
                        ListScreen(navController, viewModel, resources, context)
                    }
                    composable("myCards") {
                        viewModel.listDestination.value = "myCards"
                        MyCardsScreen(navController, viewModel, context)
                    }
                    composable("addCard") {
                        AddCardScreen(navController, viewModel, context)
                    }
                    composable("cardDetail") {
                        CardDetailScreen(navController, viewModel)
                    }
                }
            }
        )
        /* Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Button to toggle between sender and reader modes
            Row {
                Button(
                    onClick = {
                        activateSender()
                    },
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text("Sender")
                }
                Button(
                    onClick = {
                        activateReader()
                    },
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text("Reader")
                }
            }
            // Input field for entering the message to send
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = sendingMessage,
                onValueChange = {
                    sendingMessage = it
                },
                label = { Text("Enter your message") },
                modifier = Modifier
                    .fillMaxWidth()
            )

            // Button to send and receive the message
            Spacer(modifier = Modifier.height(16.dp))

            // Display the received message
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Received Message:")
            Text(text = receivedMessage, style = MaterialTheme.typography.bodyMedium)
        }*/
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
        lifecycleScope.launch {
            //wifiDirectManager.closeWifiDirectGroup()
        }
        wifiDirectManager.stopServer()
    }
}

