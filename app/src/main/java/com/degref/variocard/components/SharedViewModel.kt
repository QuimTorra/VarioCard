package com.degref.variocard.components

import android.content.BroadcastReceiver
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.degref.variocard.NFCManager
import com.degref.variocard.WiFiDirectManager
import com.degref.variocard.data.Card
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SharedViewModel(
    nfcManagerParam: NFCManager,
    wifiDirectManagerParam: WiFiDirectManager
) : ViewModel(), CoroutineScope by CoroutineScope(Dispatchers.Default)  {

    private val nfcManager: NFCManager = nfcManagerParam
    private val wifiDirectManager:  WiFiDirectManager = wifiDirectManagerParam

    val selectedCard = mutableStateOf<Card?>(null)
    val listDestination = mutableStateOf<String>("")
    lateinit var wifiDirectReceiver: BroadcastReceiver

    fun activateReader(){
        Log.d("MONDONGO", "reader initiated?")
        nfcManager.startReaderMode(wifiDirectManager)
        //wifiDirectManager.stopServer()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun activateSender() {
        nfcManager.stopReaderMode()
        launch {
            val deviceName = wifiDirectManager.getDeviceName()
            Log.d("MONDONGO", deviceName)
            // Now that we have the deviceName, update the UI or perform other operations
            nfcManager.sendNfcMessage(deviceName)
            wifiDirectManager.openWiFiDirect()
        }
    }
}