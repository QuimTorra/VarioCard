package com.degref.variocard

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import java.util.Arrays

class VarioCardApduService : HostApduService() {
    private lateinit var mNdefRecordFile: ByteArray
    private var mAppSelected = false
    private var mCcSelected = false
    private var mNdefSelected = false
    override fun onCreate() {
        super.onCreate()
        mAppSelected = false
        mCcSelected = false
        mNdefSelected = false


        val defaultMessage = "Default NFC message for VarioCard."
        val ndefDefaultMessage = getNdefMessage(defaultMessage)
        val nLen = ndefDefaultMessage!!.byteArrayLength
        mNdefRecordFile = ByteArray(nLen + 2)
        mNdefRecordFile[0] = ((nLen and 0xff00) / 256).toByte()
        mNdefRecordFile[1] = (nLen and 0xff).toByte()
        System.arraycopy(ndefDefaultMessage.toByteArray(), 0, mNdefRecordFile, 2, ndefDefaultMessage.byteArrayLength)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.hasExtra("ndefMessage")) {
            val ndefMessage = getNdefMessage(intent.getStringExtra("ndefMessage"))
            if (ndefMessage != null) {
                val nLen = ndefMessage.byteArrayLength
                mNdefRecordFile = ByteArray(nLen + 2)
                mNdefRecordFile[0] = ((nLen and 0xff00) / 256).toByte()
                mNdefRecordFile[1] = (nLen and 0xff).toByte()
                System.arraycopy(ndefMessage.toByteArray(), 0, mNdefRecordFile, 2, ndefMessage.byteArrayLength)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun getNdefMessage(ndefData: String?): NdefMessage? {
        if (ndefData!!.isEmpty()) {
            return null
        }
        val ndefRecord: NdefRecord = NdefRecord.createTextRecord("en", ndefData)
        return NdefMessage(ndefRecord)
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        Log.d(TAG, "commandApdu: " + Utils.bytesToHex(commandApdu))

        if (SELECT_APPLICATION.contentEquals(commandApdu)) {
            mAppSelected = true
            mCcSelected = false
            mNdefSelected = false
            Log.d(TAG, "responseApdu: " + Utils.bytesToHex(SUCCESS_SW))
            return SUCCESS_SW
        }

        else if (mAppSelected && SELECT_CAPABILITY_CONTAINER.contentEquals(commandApdu)) {
            mCcSelected = true
            mNdefSelected = false
            Log.d(TAG, "responseApdu: " + Utils.bytesToHex(SUCCESS_SW))
            return SUCCESS_SW
        }

        else if (mAppSelected && SELECT_NDEF_FILE.contentEquals(commandApdu)) {
            mCcSelected = false
            mNdefSelected = true
            Log.d(TAG, "responseApdu: " + Utils.bytesToHex(SUCCESS_SW))
            return SUCCESS_SW
        }
        else if (commandApdu[0] == 0x00.toByte() && commandApdu[1] == 0xb0.toByte()) {
            val offset = (0x00ff and commandApdu[2].toInt()) * 256 + (0x00ff and commandApdu[3].toInt())
            val le = 0x00ff and commandApdu[4].toInt()
            val responseApdu = ByteArray(le + SUCCESS_SW.size)
            if (mCcSelected && offset == 0 && le == CAPABILITY_CONTAINER_FILE.size) {
                System.arraycopy(CAPABILITY_CONTAINER_FILE, offset, responseApdu, 0, le)
                System.arraycopy(SUCCESS_SW, 0, responseApdu, le, SUCCESS_SW.size)
                Log.d(TAG, "responseApdu: " + Utils.bytesToHex(responseApdu))
                return responseApdu
            }
            else if (mNdefSelected) {
                if (offset + le <= mNdefRecordFile.size) {
                    System.arraycopy(mNdefRecordFile, offset, responseApdu, 0, le)
                    System.arraycopy(SUCCESS_SW, 0, responseApdu, le, SUCCESS_SW.size)
                    Log.d(TAG, "responseApdu: " + Utils.bytesToHex(responseApdu))
                    return responseApdu
                }
            }
        }
        Log.d(TAG, "responseApdu: " + Utils.bytesToHex(FAILURE_SW))
        return FAILURE_SW
    }
    override fun onDeactivated(reason: Int) {
        mAppSelected = false
        mCcSelected = false
        mNdefSelected = false
    }


    // AID: F0010203040506
    companion object {
        // source: https://github.com/TechBooster/C85-Android-4.4-Sample/blob/master/chapter08/NdefCard/src/com/example/ndefcard/NdefHostApduService.java
        private const val TAG = "VarioCardApduService"
        private val SELECT_APPLICATION = byteArrayOf(
            0x00.toByte(),
            0xA4.toByte(),
            0x04.toByte(),
            0x00.toByte(),
            0x07.toByte(),
            0xD2.toByte(),
            0x76.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x85.toByte(),
            0x01.toByte(),
            0x01.toByte(),
            0x00.toByte()
        )
        private val SELECT_CAPABILITY_CONTAINER = byteArrayOf(
            0x00.toByte(),
            0xa4.toByte(),
            0x00.toByte(),
            0x0c.toByte(),
            0x02.toByte(),
            0xe1.toByte(),
            0x03.toByte()
        )
        private val SELECT_NDEF_FILE = byteArrayOf(
            0x00.toByte(),
            0xa4.toByte(),
            0x00.toByte(),
            0x0c.toByte(),
            0x02.toByte(),
            0xE1.toByte(),
            0x04.toByte()
        )
        private val CAPABILITY_CONTAINER_FILE = byteArrayOf(
            0x00,
            0x0f,  // CCLEN
            0x20,  // Mapping Version
            0x00,
            0x3b,  // Maximum R-APDU data size
            0x00,
            0x34,  // Maximum C-APDU data size
            0x04,
            0x06,
            0xe1.toByte(),
            0x04,
            0x00.toByte(),
            0xff.toByte(),  // Maximum NDEF size, do NOT extend this value
            0x00,
            0xff.toByte()
        )

        private val SUCCESS_SW = byteArrayOf(0x90.toByte(), 0x00.toByte())
        private val FAILURE_SW = byteArrayOf(0x6a.toByte(), 0x82.toByte())
    }
}