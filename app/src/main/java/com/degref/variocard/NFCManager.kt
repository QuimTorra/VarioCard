package com.degref.variocard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.util.Arrays

class NFCManager(
    private val context: Context,
    private val activity: MainActivity
    ) {
    private lateinit var wiFiDirectManager: WiFiDirectManager
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun sendNfcMessage(deviceName: String) {
        val sendIntent = Intent(context, VarioCardApduService::class.java)
        Log.d("MONDONGO", "4. (sender) gotDeviceName $deviceName")
        sendIntent.putExtra("ndefMessage", deviceName)
        activity.startService(sendIntent)
    }

    fun startReaderMode(wiFiDirectManager: WiFiDirectManager) {
        activity.isSenderActive = false
        wiFiDirectManager.stopServer()
        this.wiFiDirectManager = wiFiDirectManager
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        val options = Bundle()
        // Work around for some broken Nfc firmware implementations that poll the card too fast
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 1000)
        nfcAdapter?.enableReaderMode(
            activity,
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

    fun stopReaderMode() {
        activity.isSenderActive = true
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        nfcAdapter?.disableReaderMode(activity)
    }

    private inner class NfcCallback : NfcAdapter.ReaderCallback {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onTagDiscovered(tag: Tag?) {
            if (!activity.isSenderActive) {
                Log.d("MONDONGO", "HEYYYYYYYYYYYYYYYYYY")
                activity.showToast("Read a tag :)")
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
                                ndefText = Utils.parseTextrecordPayload(ndefPayload)
                            }
                            val finalNdefText = ndefText
                            Log.d("MONDONGO", "1. (reader) received: $finalNdefText")
                            wiFiDirectManager.openWiFiDirect()
                            wiFiDirectManager.connectWifiDirect(finalNdefText)
                        }
                    }
                }
            }
        }
    }
}