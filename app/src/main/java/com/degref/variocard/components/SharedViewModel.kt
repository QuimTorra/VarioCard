package com.degref.variocard.components

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
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
    val listDestination = mutableStateOf("")

    var listAllCards: MutableList<Card> = mutableListOf()

    fun activateReader(){
        Log.d("VarioCard", "reader initiated?")
        nfcManager.startReaderMode(wifiDirectManager)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun activateSender(card: String) {
        nfcManager.stopReaderMode()
        launch {
            val deviceName = wifiDirectManager.getDeviceName()
            Log.d("VarioCard", deviceName)
            // Now that we have the deviceName, update the UI or perform other operations
            nfcManager.sendNfcMessage(deviceName)
            Log.d("VarioCard", card)
            wifiDirectManager.card = card
            Log.d("VarioCard", "Setting: ${wifiDirectManager.card}")
            wifiDirectManager.openWiFiDirect()
        }
    }

    fun setCard(){
        Log.d("VarioCard", "Setting card to null in viewmodel")
        wifiDirectManager.card = null
    }

    fun setValueImage(path: String){
        wifiDirectManager.imageCard = path
    }

    fun getCard(): String? {
        return wifiDirectManager.card
    }

    fun getImageCard(): String? {
        return wifiDirectManager.imageCard
    }
}