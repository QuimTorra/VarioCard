package com.degref.variocard.components

import android.content.BroadcastReceiver
import android.net.Uri
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
import java.net.URI

class SharedViewModel(
    nfcManagerParam: NFCManager,
    wifiDirectManagerParam: WiFiDirectManager
) : ViewModel(), CoroutineScope by CoroutineScope(Dispatchers.Default)  {

    private val nfcManager: NFCManager = nfcManagerParam
    private val wifiDirectManager:  WiFiDirectManager = wifiDirectManagerParam

    val selectedCard = mutableStateOf<Card?>(null)
    val listDestination = mutableStateOf<String>("")
    lateinit var wifiDirectReceiver: BroadcastReceiver

    var listAllCards: MutableList<Card> = mutableListOf(
        /*Card(1, "Laura Chavarria Solé", "609007385", "laura.chavarria@estudiantat.upc.edu", "FIB", "", null),
        Card(2,"John Doe", "123456789", "john.doe@example.com", "Company ABC", "", null)*/
    )

    fun activateReader(){
        Log.d("MONDONGO", "reader initiated?")
        nfcManager.startReaderMode(wifiDirectManager)
        //wifiDirectManager.stopServer()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun activateSender(card: String) {
        nfcManager.stopReaderMode()
        launch {
            val deviceName = wifiDirectManager.getDeviceName()
            Log.d("MONDONGO", deviceName)
            // Now that we have the deviceName, update the UI or perform other operations
            nfcManager.sendNfcMessage(deviceName)
            Log.d("MONDONGO", card)
            wifiDirectManager.card = card
            Log.d("MONDONGO", "Setting: ${wifiDirectManager.card}")
            wifiDirectManager.openWiFiDirect()
        }
    }

    fun setValueImage(path: String){
        wifiDirectManager.imageCard = path
    }
}