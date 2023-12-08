package com.degref.variocard

import MyCardsScreen
import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pConfig.GROUP_OWNER_INTENT_MAX
import android.net.wifi.p2p.WifiP2pConfig.GROUP_OWNER_INTENT_MIN
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.degref.variocard.Utils.parseTextrecordPayload
import com.degref.variocard.ui.theme.VarioCardTheme
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.Arrays
import androidx.navigation.compose.rememberNavController
import com.degref.variocard.components.SharedViewModel
import com.degref.variocard.screens.AddCardScreen
import com.degref.variocard.screens.CardDetailScreen
import com.degref.variocard.screens.ListScreen


class MainActivity : ComponentActivity() {
    // Pls work
    private var sendingMessage by mutableStateOf("No message sent")
    private var receivedMessage by mutableStateOf("No message received")
    private var isSenderActive by mutableStateOf(true)
    private var isGroupOwner: Boolean? = null
    private lateinit var wifiP2pManager: WifiP2pManager
    private lateinit var wifiInfoListener: WifiP2pManager.ChannelListener
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var wifiDirectReceiver: BroadcastReceiver
    private lateinit var intentFilter: IntentFilter
    //private var fs: FileServerAsyncTask = FileServerAsyncTask(this)
    private var fs: FileServerAsyncTask? = null
    private lateinit var serverAddress: InetAddress
    private var deviceName: String = ""

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VarioCardTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreenPreview()
                }
            }
        }
        wifiP2pManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = wifiP2pManager.initialize(this, mainLooper, null)
        initializeWiFiDirectReceiver()
        getDeviceName()

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

    fun onConnectionInfoAvailable(info: WifiP2pInfo) {
        serverAddress = info.groupOwnerAddress
        isGroupOwner = info.isGroupOwner

        // Use groupOwnerAddress and isGroupOwner as needed
        Log.d("MONDONGO","Group Owner - $isGroupOwner, Group Owner Address - $serverAddress")
    }

    private fun initializeWiFiDirectReceiver() {
        wifiDirectReceiver = WiFiDirectReceiver(wifiP2pManager, channel, this)
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
    @Composable
    fun MainScreenPreview() {
        val navController = rememberNavController()
        val viewModel: SharedViewModel = viewModel()
        MainScreen(navController, viewModel)
    }

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    // @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(navController: NavHostController, viewModel: SharedViewModel) {
        Scaffold(
            Modifier.background(Color.DarkGray),
            bottomBar = {
                BottomNavigation {
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
            }
        ) {
            NavHost(navController = navController, startDestination = "list") {
                composable("list") {
                    ListScreen(navController, viewModel)
                }
                composable("myCards") {
                    MyCardsScreen(navController, viewModel)
                }
                composable("addCard") {
                    AddCardScreen(navController)
                }
                composable("cardDetail") {
                    CardDetailScreen(navController, viewModel)
                }
            }
        }
        /* Column(
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
        }*/
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
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun toggleNfcMode() {
        // Toggle between sender and reader modes
        isSenderActive = !isSenderActive
        if (isSenderActive) {
            stopReaderMode()
            sendNfcMessage()
        } else {
            if(fs != null) fs!!.stopServer()
            startReaderMode()
        }
    }

    private fun startWifiDirectGroup() {
        Log.d("MONDONGO", "1. Group intent....")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        wifiP2pManager.createGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("MONDONGO","2. Group created :)")
                // Group creation successful
            }

            override fun onFailure(reason: Int) {
                // Group creation failed
                Log.d("MONDONGO", "2. Cannot create group ${reason.toString()}")
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun sendNfcMessage() {
        Log.d("MONDONGO", "* DEVICE: $deviceName")
        val sendIntent = Intent(this, VarioCardApduService::class.java)
        Log.d("MONDONGO", "3. (sender) gotDeviceName $deviceName")
        sendIntent.putExtra("ndefMessage", deviceName)
        startService(sendIntent)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("MONDONGO", "4. (sender) Permission said nonono")
            return
        }
        wifiP2pManager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                if(isGroupOwner != null){
                    if(isGroupOwner!!){
                        fs = FileServerAsyncTask(this@MainActivity,)
                        fs!!.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                    }
                    else sendData()
                }

                Log.d("MONDONGO", "4. (sender) Discovering devices")
            }

            override fun onFailure(reasonCode: Int) {
                Log.d("MONDONGO", "4. (sender) Cannot discover devices $reasonCode")
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
        if (!isSenderActive) {
            stopReaderMode()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun connectWifiDirect(deviceName: String) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("MONDONGO", "2. (reader) I no permission")
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return
        }
        Log.d("MONDONGO", "2. (reader) trying to connect to $deviceName")
        wifiP2pManager.requestPeers(channel) { peers ->
            Log.d("MONDONGO", "3. (reader) devicesList: ${peers.deviceList}")
            val deviceA = peers.deviceList.firstOrNull { it.deviceName == deviceName }
            if (deviceA != null) {
                Log.d("MONDONGO", "4. (reader) Found device ${deviceA.deviceName}")
            }
            else Log.d("MONDONGO", "4. (reader) Device is null :(")
            if (deviceA != null) {
                val config = WifiP2pConfig()
                config.deviceAddress = deviceA.deviceAddress
                config.groupOwnerIntent = GROUP_OWNER_INTENT_MAX
                wifiP2pManager.connect(channel, config, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.d("MONDONGO", "5. (reader) Found device and connected")
                        if(isGroupOwner != null){
                            if(isGroupOwner!!) {
                                fs = FileServerAsyncTask(this@MainActivity,)
                                fs!!.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                            }
                            else sendData()
                        }
                    }

                    override fun onFailure(reasonCode: Int) {
                        Log.d("MONDONGO", "5. (reader) Could not connect $reasonCode")
                    }
                })
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun sendData() {
        Log.d("MONDONGO", "Started sender")
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val socket = Socket()
//                val buf = ByteArray(1024)

                /**
                 * Create a client socket with the host,
                 * port, and timeout information.
                 */
                socket.bind(null)
                Log.d("MONDONGO", "Logs of sending data...")
                Log.d("MONDONGO", "address ${serverAddress}")
                Log.d("MONDONGO", "End Logs of sending data...")
                socket.connect(
                    (InetSocketAddress(serverAddress, 8888)),
                    500
                )
                if(socket.isConnected) Log.d("MONDONGO", "Socket is conneeeeeeected :)")
                Log.d("MyApp", "Server socket bound: ${socket.isBound}")
                Log.d("MONDONGO", "deviceAddress....")

                /**
                 * Create a byte stream from a message and pipe it to the output stream
                 * of the socket. This data is retrieved by the server device.
                 */
                /*val outputStream = socket.getOutputStream()
                val message = "Hello, WifiDirect working!"
                outputStream.write(message.toByteArray())*/
                val inputStream: InputStream = socket.getInputStream()
                val buffer = ByteArray(1024)
                val bytesRead = inputStream.read(buffer)

                // Convert the received bytes to a String
                val receivedData = String(buffer, 0, bytesRead)

                // Process the received data as needed
                Log.d("MONDONGO","Received data: $receivedData")

                // If you have an InputStream, you can uncomment the following lines
                // val cr = applicationContext.contentResolver
                // val inputStream: InputStream? = cr.openInputStream(Uri.parse("path/to/picture.jpg"))
                // while (inputStream?.read(buf).also { len = it } != -1) {
                //     outputStream.write(buf, 0, len)
                // }
                /*outputStream.close()*/
                inputStream?.close()

                /**
                 * Clean up any open sockets when done
                 * transferring or if an exception occurred.
                 */
                socket.takeIf { it.isConnected }?.apply {
                    close()
                }
            } catch (e: Exception) {
                Log.d("MONDONGO", "exception raised: $e")
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getDeviceName() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ),
                123
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startWifiDirectGroup()
            wifiP2pManager.requestDeviceInfo(channel) { device ->
                if (device != null) {
                    deviceName = device.deviceName
                    Log.d("MONDONGO","0. Found device name $deviceName")
                    wifiP2pManager.removeGroup(channel, object : WifiP2pManager.ActionListener {
                        override fun onSuccess() {
                            Log.d("MONDONGO","0.1 Group deleted :)")
                        }

                        override fun onFailure(reason: Int) {
                            Log.d("MONDONGO", "0.1 Didn't delete group $reason")
                        }
                    })
                }
                else Log.d("MONDONGO", "0 Not found device :(")
            }
        }
        else {
            Log.d("MONDONGO", "0. This device is old")
            deviceName = "Galaxy S7"
        }
    }

    private inner class NfcCallback : NfcAdapter.ReaderCallback {
        @RequiresApi(Build.VERSION_CODES.R)
        override fun onTagDiscovered(tag: Tag?) {
            if (!isSenderActive) {
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
                                ndefText = parseTextrecordPayload(ndefPayload)
                            }
                            val finalNdefText = ndefText
                            Log.d("MONDONGO", "1. (reader) received: $finalNdefText")
                            connectWifiDirect(finalNdefText)
                        }
                    }
                }
            }
        }
    }
}

