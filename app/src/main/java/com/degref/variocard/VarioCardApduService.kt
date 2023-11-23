package com.degref.variocard

import android.app.Service
import android.content.Intent
import android.os.IBinder

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

class VarioCardApduService : HostApduService() {
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        // Process the incoming APDU command
        Log.d("APDU cum", "Received Command: $commandApdu")
        return "Hello, World!".toByteArray()
    }

    override fun onDeactivated(reason: Int) {
        // Called when the NFC interface is no longer active
    }
}