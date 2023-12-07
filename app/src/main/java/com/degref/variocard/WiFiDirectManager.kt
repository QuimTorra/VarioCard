package com.degref.variocard

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.CountDownLatch


class WiFiDirectManager(private val context: Context, private val activity: MainActivity) {
    private val wifiP2pManager: WifiP2pManager = activity.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    private val channel = wifiP2pManager.initialize(context, activity.mainLooper, null)
    private var fs: FileServerAsyncTask = FileServerAsyncTask(context)
    private var isGroupOwner: Boolean? = null
    private var serverAddress: InetAddress? = null
    private var isGroupFormed: Boolean = false
    private var deviceName: String = ""

    //MN: Reduce duplicated code, check and request permissions
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun arePermissionsOk(): Boolean{
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
            return false
        }
        else return true
    }

    fun onConnectionInfoAvailable(info: WifiP2pInfo) {
        serverAddress = info.groupOwnerAddress
        isGroupOwner = info.isGroupOwner
        isGroupFormed = info.groupFormed
        // Use groupOwnerAddress and isGroupOwner as needed
        Log.d("MONDONGO","Group Owner - $isGroupOwner, Group Owner Address - $serverAddress")
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    fun openWiFiDirect() {
        if(!arePermissionsOk()) Log.d("MONDONGO", "4. (sender) Permission said nonono")
        wifiP2pManager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("MONDONGO", "4. (sender) Discovering devices")
//                if (isGroupOwner != null) {
//                    Log.d("MONDONGO", "Entering not being null for sender...")
//                   if (isGroupOwner!!) {
//                        fs = FileServerAsyncTask(activity)
//                        fs.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
//                    } else {
//                        sendData()
//                    }
//                }
//                else Log.d("MONDONGO", "Group is being null at this moment for sender")
            }

            override fun onFailure(reasonCode: Int) {
                Log.d("MONDONGO", "4. (sender) Cannot discover devices $reasonCode")
            }
        })
    }

    fun stopServer() {
        fs.stopServer()
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun getDeviceName(): String {
        if (!arePermissionsOk()) Log.d("MONDONGO", "permissions not okey")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var deviceName = "default deviceName"
            startWifiDirectGroup()
            val latch = CountDownLatch(1)
            wifiP2pManager.requestDeviceInfo(channel) { device ->
                if (device != null) {
                    deviceName = device.deviceName
                    Log.d("MONDONGO","Found device name $deviceName")
                    latch.countDown()

                }
                else {
                    Log.d("MONDONGO", "Not found device :(")
                    latch.countDown()
                }
            }
            withContext(Dispatchers.IO) {
                latch.await()
            }
            Log.d("MONDONGO", "isGroupFormed $isGroupFormed")
            if (isGroupFormed) closeWifiDirectGroup()
            return deviceName
        }
        else {
            Log.d("MONDONGO", "0. This device is old")
            return "HUAWEI Mate 9"
        }
    }

    private suspend fun closeWifiDirectGroup() {
        val latch = CountDownLatch(1)
        wifiP2pManager.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                isGroupFormed = false
                Log.d("MONDONGO","3. Group deleted :)")
                latch.countDown()
            }

            override fun onFailure(reason: Int) {
                Log.d("MONDONGO", "3. Didn't delete group $reason")
                latch.countDown()
            }
        })
        withContext(Dispatchers.IO) {
            latch.await()
        }


    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    private suspend fun startWifiDirectGroup() {
        Log.d("MONDONGO", "1. Group intent....")
        if (!arePermissionsOk()) {
            Log.d("MONDONGO", "Permissions not okay")
        }
        val latch = CountDownLatch(1)
        wifiP2pManager.createGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // Group creation successful
                isGroupFormed = true
                Log.d("MONDONGO", "2. Group created :)")
                latch.countDown()
            }

            override fun onFailure(reason: Int) {
                // Group creation failed
                Log.d("MONDONGO", "2. Cannot create group ${reason.toString()}")
                latch.countDown()
            }
        })
        withContext(Dispatchers.IO) {
            latch.await()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    fun connectWifiDirect(deviceName: String) {
        if(!arePermissionsOk()) Log.d("MONDONGO", "2. (reader) I no permission")
        Log.d("MONDONGO", "2. (reader) trying to connect to $deviceName")
        wifiP2pManager.requestPeers(channel) { peers ->
            Log.d("MONDONGO", "3. (reader) devicesList: ${peers.deviceList}")
            val deviceA = peers.deviceList.firstOrNull { it.deviceName == deviceName }
            if (deviceA != null) {
                Log.d("MONDONGO", "4. (reader) Found device ${deviceA.deviceName}")
            }
            else Log.d("MONDONGO", "4. (reader) Device is null :(")
            if (deviceA != null) {
                val config = WifiP2pConfig()
                config.deviceAddress = deviceA.deviceAddress
                wifiP2pManager.connect(channel, config, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.d("MONDONGO", "5. (reader) Found device and connected")
//                        if(isGroupOwner != null){
//                            Log.d("MONDONGO", "entering not null for receiver...")
//                            if(isGroupOwner!!) {
//                                fs = FileServerAsyncTask(activity)
//                                fs.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
//                            }
//                            else sendData()
//                        }
//                        else Log.d("MONDONGO","Group is being null at this moment for receiver")
                    }

                    override fun onFailure(reasonCode: Int) {
                        Log.d("MONDONGO", "5. (reader) Could not connect $reasonCode")
                    }
                })
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun sendData() {
        Log.d("MONDONGO", "Started sender")
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val socket = Socket()

                socket.bind(null)
                Log.d("MONDONGO", "Logs of sending data...")
                Log.d("MONDONGO", "address ${serverAddress}")
                Log.d("MONDONGO", "End Logs of sending data...")
                socket.connect(
                    (InetSocketAddress(serverAddress, 8888)),
                    500
                )
                if (socket.isConnected) Log.d("MONDONGO", "Socket is conneeeeeeected :)")
                Log.d("MyApp", "Server socket bound: ${socket.isBound}")
                Log.d("MONDONGO", "deviceAddress....")

                val inputStream: InputStream = socket.getInputStream()
                val buffer = ByteArray(1024)
                val bytesRead = inputStream.read(buffer)

                // Convert the received bytes to a String
                val receivedData = String(buffer, 0, bytesRead)

                // Process the received data as needed
                Log.d("MONDONGO", "Received data: $receivedData")

                inputStream.close()

                /**
                 * Clean up any open sockets when done
                 * transferring or if an exception occurred.
                 */
                socket.takeIf { it.isConnected }?.apply {
                    close()
                }
            } catch (e: Exception) {
                Log.d("MONDONGO", "exception raised: $e")
            }
        }
    }
}