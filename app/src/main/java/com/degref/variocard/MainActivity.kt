package com.degref.variocard

import android.Manifest
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.degref.variocard.Utils.parseTextrecordPayload
import com.degref.variocard.ui.theme.VarioCardTheme
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.Arrays


class MainActivity : ComponentActivity() {
    // Pls work
    private var sendingMessage by mutableStateOf("No message sent")
    private var receivedMessage by mutableStateOf("No message received")
    var isSenderActive by mutableStateOf(true)
    private lateinit var nfcManager: NFCManager
    private lateinit var wifiDirectManager: WiFiDirectManager
    private lateinit var wifiP2pManager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var wifiDirectReceiver: BroadcastReceiver
    private lateinit var intentFilter: IntentFilter


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VarioCardTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
        wifiDirectManager = WiFiDirectManager(this, this@MainActivity)
        nfcManager = NFCManager(this, this@MainActivity)

        wifiDirectManager.arePermissionsOk()
        initializeWiFiDirectReceiver()

        // Set up NFC for HCE
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter?.let {
            if (!it.isEnabled) {
                showToast("NFC is not enabled")
            } else {
                //nfcManager.startReaderMode(wifiDirectManager)
            }
        }
    }



    private fun initializeWiFiDirectReceiver() {
        wifiDirectReceiver = WiFiDirectReceiver(wifiDirectManager)
        intentFilter = IntentFilter()

        // Add necessary Wi-Fi Direct action filters
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        registerReceiver(wifiDirectReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiDirectReceiver)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun MainScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Button to toggle between sender and reader modes
            Row(){
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
        }
    }

    private fun activateReader(){
        nfcManager.startReaderMode(wifiDirectManager)
        //wifiDirectManager.stopServer()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun activateSender() {
        nfcManager.stopReaderMode()
        val deviceName = wifiDirectManager.getDeviceName()
        Log.d("MONDONGO", deviceName)
        nfcManager.sendNfcMessage(deviceName)
        wifiDirectManager.openWiFiDirect()

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
    }
}

