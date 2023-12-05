package com.degref.variocard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log

class WiFiDirectReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {

              WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                  Log.d("MONDONGO", "Hwllo2?")
                val device =
                    intent.getParcelableExtra<WifiP2pDevice>(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
                if (device != null) {
                    val deviceName = device.deviceName
                    Log.d("MONDONGO", "reached here: $deviceName")
                }
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                Log.d("MONDONGO", "Hello?")
                val wifiP2pInfo =
                    intent.getParcelableExtra<WifiP2pInfo>(WifiP2pManager.EXTRA_WIFI_P2P_INFO)
                if (wifiP2pInfo != null && wifiP2pInfo.groupFormed) {
                    Log.d("MONDONGO", "Wi-Fi P2P connection success!")
                } else {
                    // Wi-Fi P2P connection is lost (failure)
                    Log.d("MONDONGO", "Wi-Fi P2P connection failure!")
                }
            }
        }
    }
}
