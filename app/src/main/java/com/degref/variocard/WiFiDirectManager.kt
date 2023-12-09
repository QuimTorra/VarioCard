package com.degref.variocard

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.os.postDelayed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.CountDownLatch


class WiFiDirectManager(private val context: Context, private val activity: MainActivity) {
    private val wifiP2pManager: WifiP2pManager = activity.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    private val channel = wifiP2pManager.initialize(context, activity.mainLooper, null)
    var fs: FileServerAsyncTask? = null
    var cs: ClientMessager? = null
    private var isGroupOwner: Boolean? = null
    var serverAddress: InetAddress? = null
    private var isGroupFormed: Boolean = false
    private var deviceName: String? = null
    private var groupAlreadyCreated: CountDownLatch? = null
    private var groupNeverDeleted: CountDownLatch? = null
    var card: String? = null
    var imageCard: String? = null

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
            }

            override fun onFailure(reasonCode: Int) {
                Log.d("MONDONGO", "4. (sender) Cannot discover devices $reasonCode")
            }
        })
    }

    fun stopServer() {
        if(fs != null) fs!!.stopServer()
        if(cs != null) cs!!.stopServer()
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun getDeviceName(): String {
        if(deviceName != null){
            return deviceName!!
        }
        if (!arePermissionsOk()) Log.d("MONDONGO", "permissions not okey")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
                Log.d("MONDONGO","Before await on requestDeviceName")
                latch.await()
            }
            Log.d("MONDONGO", "isGroupFormed $isGroupFormed")
            groupNeverDeleted = CountDownLatch(1)
            GlobalScope.launch {
                delay(3000)
                closeWifiDirectGroup()
            }
            Log.d("MONDONGO","Before await on groupNeverDeleted")
            groupNeverDeleted!!.await()
            Log.d("MONDONGO", "sem release")
            return deviceName!!
        }
        else {
            Log.d("MONDONGO", "0. This device is old")
            return "HUAWEI Mate 9"
        }
    }

    suspend fun closeWifiDirectGroup() {
        val latch = CountDownLatch(1)
        var time = System.currentTimeMillis()
        Log.d("MONDONGO","Close group intent... $time")
        wifiP2pManager.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                isGroupFormed = false
                Log.d("MONDONGO","3. Group deleted :) $time")
                if(groupAlreadyCreated != null) groupAlreadyCreated!!.countDown()
                groupNeverDeleted!!.countDown()
                latch.countDown()
            }

            override fun onFailure(reason: Int) {
                if(reason.equals(WifiP2pManager.BUSY)){
                    Log.d("MONDONGO", "3. Didn't delete group -> BUSY $time")
                }
                else Log.d("MONDONGO", "3. Didn't delete group $reason")
                latch.countDown()
            }
        })
        withContext(Dispatchers.IO) {
            Log.d("MONDONGO", "before await on CloseWifiDirect $time")
            groupNeverDeleted!!.countDown()
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
                Log.d("MONDONGO", "$isGroupFormed")
                latch.countDown()
            }

            override fun onFailure(reason: Int) {
                // Group creation failed
                //If cannot create group, delete the one and create it...
                groupAlreadyCreated = CountDownLatch(1);
                GlobalScope.launch {
                    closeWifiDirectGroup()
                }
                Log.d("MONDONGO","Before await on retry")
                groupAlreadyCreated!!.await()
                Log.d("MONDONGO", "2. Cannot create group ${reason.toString()}")
                latch.countDown()
            }
        })
        withContext(Dispatchers.IO) {
            Log.d("MONDONGO","Before await on startWifiDirect")
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
                    }

                    override fun onFailure(reasonCode: Int) {
                        Log.d("MONDONGO", "5. (reader) Could not connect $reasonCode")
                    }
                })
            }
        }
    }

    fun sendMessage(message: String){
        if((fs != null) && isGroupFormed && isGroupOwner!!){
            Log.d("MONDONGO", "Server: Trying to deliver $message")
            //fs!!.start()
            fs!!.sendMessage(message)
            //fs.execute()
            //fs.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun sendClientMessage(message: String) {
        if(cs != null){
            Log.d("MONDONGO", "Client: Trying to deliver $message")
            cs!!.sendMessage(message)
        }
    }

    fun sendData() {
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

                val outputStream: OutputStream = socket.getOutputStream()
                Log.d("MONDONGO", "isSenderActive: ${activity.isSenderActive}")
                Log.d("MONDONGO", "card: $card")
                if(activity.isSenderActive && card != null) {
                    outputStream.write(card!!.toByteArray())
                    outputStream.write("*".toByteArray())
                    if(imageCard != null) {
                        val file = File(imageCard!!)

                        // Create a FileInputStream for the file
                        val fileInputStream = FileInputStream(file)
                        val buffer = ByteArray(4096)
                        var bytesRead: Int

                        while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                            Log.d("MONDONGO", "writing image....")
                            outputStream.write(buffer, 0, bytesRead)
                        }
                    }
                    outputStream.write("This is a test".toByteArray())
                    Log.d("MONDONGO", "Card sent: $card")
                    Log.d("MONDONGO", "Setting card to null")
                    card = null
                }
                else Log.d("MONDONGO", "Received null, may be receiver or error")

                val inputStream: InputStream = socket.getInputStream()
                val buffer = ByteArray(4096)
                var bytesRead: Int = -1

                val stringBuilder = StringBuilder()
                while (inputStream.available() > 0 || bytesRead != -1) {
                    bytesRead = inputStream.read(buffer)
                    if (bytesRead != -1) {
                        // Convert the received bytes to a String and append to the StringBuilder
                        stringBuilder.append(String(buffer, 0, bytesRead))
                    }
                }

                // The complete received data
                val receivedData = stringBuilder.toString()

                activity.showToast("Received message: $receivedData")
                // Process the received data as needed
                Log.d("MONDONGO", "Received data: $receivedData")

                inputStream.close()
                outputStream.close()

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