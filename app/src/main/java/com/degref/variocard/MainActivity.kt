package com.degref.variocard

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.degref.variocard.Utils.parseTextrecordPayload
import com.degref.variocard.ui.theme.VarioCardTheme
import java.net.NetworkInterface
import java.util.Arrays
import java.util.Collections


class MainActivity : ComponentActivity() {
    // Pls work
    private var sendingMessage by mutableStateOf("No message sent")
    private var receivedMessage by mutableStateOf("No message received")
    private var isSenderActive by mutableStateOf(true)
    private lateinit var wifiP2pManager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var wifiDirectReceiver: BroadcastReceiver
    private lateinit var intentFilter: IntentFilter


    @OptIn(ExperimentalComposeUiApi::class)
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
        wifiP2pManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = wifiP2pManager.initialize(this, mainLooper, null)
        initializeWiFiDirectReceiver()

        // Set up NFC for HCE
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter?.let {
            if (!it.isEnabled) {
                showToast("NFC is not enabled")
            } else {
                startReaderMode()
            }
        }
    }

    private fun initializeWiFiDirectReceiver() {
        wifiDirectReceiver = WiFiDirectReceiver()
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
            Button(onClick = {
                toggleNfcMode()
            }) {
                Text(if (isSenderActive) "Sending" else "Reading")
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

    private fun startReaderMode() {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        val options = Bundle()
        // Work around for some broken Nfc firmware implementations that poll the card too fast
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 1000)
        nfcAdapter?.enableReaderMode(
            this,
            NfcCallback(),
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_NFC_BARCODE or
                    NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            options
        )
    }

    private fun stopReaderMode() {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter?.disableReaderMode(this)
        Log.d("MONDONGO", "desactivar reader mode")
    }

    private fun toggleNfcMode() {
        // Toggle between sender and reader modes
        isSenderActive = !isSenderActive
        if (isSenderActive) {
            stopReaderMode()
            Log.d("MONDONGO", Build.MODEL)
            sendNfcMessage(Build.MODEL)
        } else {
            startReaderMode()
        }
    }

    private fun sendNfcMessage(message: String) {
        val sendIntent = Intent(this, VarioCardApduService::class.java)
        sendIntent.putExtra("ndefMessage", message)
        startService(sendIntent)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("MONDONGO", "Permission said nonono")
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        wifiP2pManager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // Discovery initiation successful
                Log.d("MONDONGO", "Discovering devices")
            }

            override fun onFailure(reasonCode: Int) {
                // Discovery initiation failed
            }
        })
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isSenderActive) {
            startReaderMode()
        } else {
            stopReaderMode()
        }
    }

    override fun onPause() {
        super.onPause()
        NfcAdapter.getDefaultAdapter(this)?.disableForegroundDispatch(this)
        // Stop sender mode when the activity is paused
        if (!isSenderActive) {
            stopReaderMode()
        }
    }

    private fun connectWifiDirect(deviceName: String) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("MONDONGO", "I no permission")
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        wifiP2pManager.requestPeers(channel) { peers ->
            // Filter peers to find Device A based on some criteria (e.g., device name)
            val deviceA = peers.deviceList.firstOrNull { it.deviceName == "DeviceAName" }
            if (deviceA != null) {
                Log.d("MONDONGO", deviceA.deviceName)
            }
            else Log.d("MONDONGO", "Device is null :(")
            // If Device A is found, initiate a Wi-Fi Direct connection
            if (deviceA != null) {
                val config = WifiP2pConfig()
                config.deviceAddress = deviceA.deviceAddress

                wifiP2pManager.connect(channel, config, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        // Connection initiation successful
                    }

                    override fun onFailure(reasonCode: Int) {
                        // Connection initiation failed
                    }
                })
            }
        }
    }

    private inner class NfcCallback : NfcAdapter.ReaderCallback {
        override fun onTagDiscovered(tag: Tag?) {
            if (isSenderActive) {
                showToast("NFC tag discovered while sending")
                sendNfcMessage(sendingMessage)
            } else {
                showToast("Read a tag :)")
                val mNdef = Ndef.get(tag)
                if (mNdef != null) {
                    val mNdefMessage = mNdef.cachedNdefMessage
                    val record = mNdefMessage.records
                    val ndefRecordsCount = record.size
                    if (ndefRecordsCount > 0) {
                        var ndefText = ""
                        for (i in 0 until ndefRecordsCount) {
                            val ndefTnf = record[i].tnf
                            val ndefType = record[i].type
                            val ndefPayload = record[i].payload
                            if (ndefTnf == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefType, NdefRecord.RTD_TEXT)) {
                                ndefText = """$ndefText${parseTextrecordPayload(ndefPayload)}"""
                            }
                            val finalNdefText = ndefText
                            Log.d("MONDONGO", finalNdefText)
                            connectWifiDirect(finalNdefText)
                        }
                    }
                }
            }
        }
    }
}

