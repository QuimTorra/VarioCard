package com.degref.variocard

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.nfc.Tag
import android.nfc.tech.Ndef
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
import androidx.compose.ui.unit.dp
import com.degref.variocard.Utils.parseTextrecordPayload
import com.degref.variocard.ui.theme.VarioCardTheme
import java.util.Arrays


class MainActivity : ComponentActivity() {
    // Pls work
    private var sendingMessage by mutableStateOf("No message sent")
    private var receivedMessage by mutableStateOf("No message received")
    private var isSenderActive by mutableStateOf(true)

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

    @OptIn(ExperimentalMaterial3Api::class)
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
            sendNfcMessage(sendingMessage)
        } else {
            startReaderMode()
        }
    }

    private fun sendNfcMessage(message: String) {
        val sendIntent = Intent(this, VarioCardApduService::class.java)
        sendIntent.putExtra("ndefMessage", message)
        startService(sendIntent)
        Log.d("MONDONGO", "sending")
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

//    override fun onNdefPushComplete(event: NfcEvent?) {
//        // Called when the NDEF push (send) operation is complete
//        showToast("NDEF push complete")
//        // Stop sender mode after sending a message
//        if (isSenderActive) {
//            stopReaderMode()
//        }
//    }

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

//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//        handleNfcIntent(intent)
//    }
//
//    private fun handleNfcIntent(intent: Intent) {
//        Log.d("aaaaaaaaaaaaaaa", "${intent.action}")
//        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
//            // Extract NDEF message from the intent
//            val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
//            if (!rawMessages.isNullOrEmpty()) {
//                val messages = rawMessages.map { it as NdefMessage }
//                val payload = String(messages[0].records[0].payload)
//                Log.d("aaaaaaaaaaaaaaa", "$payload")
//                sendNfcMessage(payload)
//            }
//        }
//    }

    private inner class NfcCallback : NfcAdapter.ReaderCallback {
        override fun onTagDiscovered(tag: Tag?) {
            if (isSenderActive) {
                showToast("NFC tag discovered while sending")
                Log.d("MONDONGO", "Mi tag: $tag")
                sendNfcMessage(sendingMessage)
            } else {
                showToast("Read a tag :)")
                val mNdef = Ndef.get(tag)
                Log.d("MONDONGO", "Atontag: $tag")
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
                                ndefText = """$ndefText
                                                rec: $i Well known Text payload
                                                ${String(ndefPayload)}""" + " \n"
                                ndefText = """$ndefText${parseTextrecordPayload(ndefPayload)}"""
                            }
                            val finalNdefText = ndefText
                            Log.d("MONDONGO", finalNdefText)
                        }
                    }
                }

            }
        }

    }

    private fun updateReceivedMessage(message: String) {
        showToast("HCE Message Received: $message")
        // Update the received message in the Compose UI
        runOnUiThread {
            receivedMessage = message
        }
    }
}