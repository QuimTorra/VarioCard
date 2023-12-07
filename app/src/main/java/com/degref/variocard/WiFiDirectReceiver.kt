package com.degref.variocard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.AsyncTask
import android.util.Log

class WiFiDirectReceiver(
    private val manager: WiFiDirectManager, private val activity: MainActivity
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                val wifiP2pInfo =
                    intent.getParcelableExtra<WifiP2pInfo>(WifiP2pManager.EXTRA_WIFI_P2P_INFO)
                if (wifiP2pInfo != null && wifiP2pInfo.groupFormed) {
                    manager.onConnectionInfoAvailable(wifiP2pInfo)
                    Log.d("MONDONGO", "Wi-Fi P2P connection success!")
                    if (wifiP2pInfo.isGroupOwner) {
                        val fs = FileServerAsyncTask(activity)
                        fs.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                    }
                    else {
                        manager.sendData()
                    }
                } else {
                    // Wi-Fi P2P connection is lost (failure)
                    Log.d("MONDONGO", "Wi-Fi P2P connection failure!")
                }
            }
        }
    }
}
