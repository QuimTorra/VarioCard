package com.degref.variocard

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat


class WiFiDirectManager(private val context: Context, private val activity: MainActivity) {
    private val wifiP2pManager: WifiP2pManager = activity.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    private val channel = wifiP2pManager.initialize(context, activity.mainLooper, null)
    private var fs: FileServerAsyncTask? = null
    private var isGroupOwner: Boolean? = null

    private var deviceName: String = "elDegro"


    fun onConnectionInfoAvailable(info: WifiP2pInfo) {
        val serverAddress = info.groupOwnerAddress
        isGroupOwner = info.isGroupOwner

        // Use groupOwnerAddress and isGroupOwner as needed
        Log.d("MONDONGO","Group Owner - $isGroupOwner, Group Owner Address - $serverAddress")
    }

    fun openWiFiDirect() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("MONDONGO", "4. (sender) Permission said nonono")
            return
        }
        wifiP2pManager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("MONDONGO", "4. (sender) Discovering devices")
                if (isGroupOwner != null) {
                    if (isGroupOwner!!) {
                        fs = FileServerAsyncTask(activity)
                        fs!!.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                    } else {
                        // sendData()
                    }
                }
            }

            override fun onFailure(reasonCode: Int) {
                Log.d("MONDONGO", "4. (sender) Cannot discover devices $reasonCode")
            }
        })
    }

    fun stopServer() {
        fs!!.stopServer()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getDeviceName() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
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
                }
                else Log.d("MONDONGO", "0 Not found device :(")
                closeWifiDirectGroup()
            }
        }
        else {
            Log.d("MONDONGO", "0. This device is old")
            deviceName = "HUAWEI Mate 9"
        }
    }

    private fun closeWifiDirectGroup() {
        wifiP2pManager.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("MONDONGO","0.1 Group deleted :)")
            }

            override fun onFailure(reason: Int) {
                Log.d("MONDONGO", "0.1 Didn't delete group $reason")
            }
        })
    }

    private fun startWifiDirectGroup() {
        Log.d("MONDONGO", "1. Group intent....")
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        wifiP2pManager.createGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // Group creation successful
                Log.d("MONDONGO","2. Group created :)")
            }

            override fun onFailure(reason: Int) {
                // Group creation failed
                Log.d("MONDONGO", "2. Cannot create group ${reason.toString()}")
            }
        })
    }
}