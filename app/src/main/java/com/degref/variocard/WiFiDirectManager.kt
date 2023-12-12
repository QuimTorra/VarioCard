package com.degref.variocard

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.CountDownLatch


class WiFiDirectManager(private val context: Context, private val activity: MainActivity) {
    private val wifiP2pManager: WifiP2pManager = activity.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    private val channel = wifiP2pManager.initialize(context, activity.mainLooper, null)
    var fs: FileServerAsyncTask? = null
    private var isGroupOwner: Boolean? = null
    private var serverAddress: InetAddress? = null
    private var isGroupFormed: Boolean = false
    private var deviceName: String? = null
    private var groupAlreadyCreated: CountDownLatch? = null
    private var groupNeverDeleted: CountDownLatch? = null
    var card: String? = null
    var imageCard: String? = null


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
        Log.d("VarioCard","Group Owner - $isGroupOwner, Group Owner Address - $serverAddress")
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    fun openWiFiDirect() {
        if(!arePermissionsOk()) Log.d("VarioCard", "4. (sender) Permission needed")
        wifiP2pManager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("VarioCard", "4. (sender) Discovering devices")
            }

            override fun onFailure(reasonCode: Int) {
                Log.d("VarioCard", "4. (sender) Cannot discover devices $reasonCode")
            }
        })
    }

    fun stopServer() {
        if(fs != null) fs!!.stopServer()
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun getDeviceName(): String {
        if(deviceName != null){
            return deviceName!!
        }
        if (!arePermissionsOk()) Log.d("VarioCard", "permissions not okey")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startWifiDirectGroup()
            val latch = CountDownLatch(1)
            wifiP2pManager.requestDeviceInfo(channel) { device ->
                if (device != null) {
                    deviceName = device.deviceName
                    Log.d("VarioCard","Found device name $deviceName")
                    latch.countDown()

                }
                else {
                    Log.d("VarioCard", "Device not found :(")
                    latch.countDown()
                }
            }
            withContext(Dispatchers.IO) {
                Log.d("VarioCard","Before await on requestDeviceName")
                latch.await()
            }
            Log.d("VarioCard", "isGroupFormed $isGroupFormed")
            groupNeverDeleted = CountDownLatch(1)
            GlobalScope.launch {
                delay(3000)
                closeWifiDirectGroup()
            }
            Log.d("VarioCard","Before await on groupNeverDeleted")
            groupNeverDeleted!!.await()
            Log.d("VarioCard", "sem release")
            return deviceName!!
        }
        else {
            Log.d("VarioCard", "0. This device is old")
            return "HUAWEI Mate 9"
        }
    }

    suspend fun closeWifiDirectGroup() {
        val latch = CountDownLatch(1)
        val time = System.currentTimeMillis()
        Log.d("VarioCard","Close group intent... $time")
        wifiP2pManager.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                isGroupFormed = false
                Log.d("VarioCard","3. Group deleted :) $time")
                if(groupAlreadyCreated != null) groupAlreadyCreated!!.countDown()
                groupNeverDeleted!!.countDown()
                latch.countDown()
            }

            override fun onFailure(reason: Int) {
                if(reason == WifiP2pManager.BUSY){
                    Log.d("VarioCard", "3. Didn't delete group -> BUSY $time")
                }
                else Log.d("VarioCard", "3. Didn't delete group $reason")
                latch.countDown()
            }
        })
        withContext(Dispatchers.IO) {
            Log.d("VarioCard", "before await on CloseWifiDirect $time")
            groupNeverDeleted!!.countDown()
            latch.await()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    private suspend fun startWifiDirectGroup() {
        Log.d("VarioCard", "1. Group intent....")
        if (!arePermissionsOk()) {
            Log.d("VarioCard", "Permissions not okay")
        }
        val latch = CountDownLatch(1)
        wifiP2pManager.createGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // Group creation successful
                isGroupFormed = true
                Log.d("VarioCard", "2. Group created :)")
                Log.d("VarioCard", "$isGroupFormed")
                latch.countDown()
            }

            override fun onFailure(reason: Int) {
                // Group creation failed
                //If cannot create group, delete the one and create it...
                groupAlreadyCreated = CountDownLatch(1)
                GlobalScope.launch {
                    closeWifiDirectGroup()
                }
                Log.d("VarioCard","Before await on retry")
                groupAlreadyCreated!!.await()
                Log.d("VarioCard", "2. Cannot create group $reason")
                latch.countDown()
            }
        })
        withContext(Dispatchers.IO) {
            Log.d("VarioCard","Before await on startWifiDirect")
            latch.await()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    fun connectWifiDirect(deviceName: String) {
        if(!arePermissionsOk()) Log.d("VarioCard", "2. (reader) I no permission")
        Log.d("VarioCard", "2. (reader) trying to connect to $deviceName")
        wifiP2pManager.requestPeers(channel) { peers ->
            Log.d("VarioCard", "3. (reader) devicesList: ${peers.deviceList}")
            val deviceA = peers.deviceList.firstOrNull { it.deviceName == deviceName }
            if (deviceA != null) {
                Log.d("VarioCard", "4. (reader) Found device ${deviceA.deviceName}")
            }
            else Log.d("VarioCard", "4. (reader) Device is null :(")
            if (deviceA != null) {
                val config = WifiP2pConfig()
                config.deviceAddress = deviceA.deviceAddress
                wifiP2pManager.connect(channel, config, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.d("VarioCard", "5. (reader) Found device and connected")
                    }

                    override fun onFailure(reasonCode: Int) {
                        Log.d("VarioCard", "5. (reader) Could not connect $reasonCode")
                    }
                })
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun sendData() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val socket = Socket()

                socket.bind(null)
                Log.d("VarioCard", "Logs of sending data...")
                Log.d("VarioCard", "address $serverAddress")
                Log.d("VarioCard", "End Logs of sending data...")
                socket.connect(
                    (InetSocketAddress(serverAddress, 8888)),
                    500
                )
                if (socket.isConnected) {
                    Log.d("VarioCard", "Socket is conneeeeeeected :)")
                }
                Log.d("MyApp", "Server socket bound: ${socket.isBound}")
                Log.d("VarioCard", "deviceAddress....")

                val outputStream: OutputStream = socket.getOutputStream()
                Log.d("VarioCard", "isSenderActive: ${activity.isSenderActive}")
                Log.d("VarioCard", "card: $card")
                if(activity.isSenderActive && card != null) {
                    activity.showToast("Ready to send")
                    outputStream.write(card!!.toByteArray())
                    outputStream.write("\r\n\r\n".toByteArray())
                    if(imageCard != null) {
                        val file = File(imageCard!!)
                        Log.d("VarioCard", "File size: ${file.length()}")

                        // Create a FileInputStream for the file
                        val fileInputStream = FileInputStream(file)
                        val buffer = ByteArray(4096)
                        var bytesRead: Int

                        while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                    }
                    Log.d("VarioCard", "Card sent: $card")
                    Log.d("VarioCard", "Setting card to null")
                    card = null
                }
                else if(!activity.isSenderActive) { //case that isReceiver or card null
                    activity.showToast("Ready to receive")
                    val inputStream: InputStream = socket.getInputStream()
                    val bufferedInputStream = BufferedInputStream(inputStream)
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    var bytesTotal = 0
                    val textData = StringBuilder()
                    val imageBuffer = ByteArrayOutputStream()

                    val latch = CountDownLatch(1)

                    try {
                        var asteriskFound = false

                        do {
                            Log.d("VarioCard","Trying to read...")
                            bytesRead = bufferedInputStream.read(buffer)
                            Log.d("VarioCard","TotalBytes: $bytesTotal and $bytesRead")
                            bytesTotal += bytesRead
                            if (bytesRead != -1) {
                                if (asteriskFound) {
                                    imageBuffer.write(buffer, 0, bytesRead)
                                } else {
                                    // Check for the asterisk to separate text and image data
                                    for (i in 0 until bytesRead) {
                                        Log.d("VarioCard", "Inside for")
                                        if (i + 3 < buffer.size && buffer[i] == '\r'.code.toByte() && buffer[i + 1] == '\n'.code.toByte() &&
                                            buffer[i + 2] == '\r'.code.toByte() && buffer[i + 3] == '\n'.code.toByte()
                                        ) {
                                            asteriskFound = true
                                            Log.d("VarioCard", "FOUND at $i")
                                            // Write the remaining bytes to the image buffer
                                            imageBuffer.write(
                                                buffer,
                                                i + 4,
                                                bytesRead - (i + 4)
                                            )
                                            latch.countDown()
                                            break
                                        }
                                        textData.append(buffer[i].toInt().toChar())
                                    }
                                }
                            } else {
                                Log.d("VarioCard","Here else")
                                latch.countDown()
                            }
                        } while (bytesRead != -1)
                        Log.d("VarioCard", "Start to wait")
                        latch.await()
                        Log.d("VarioCard", "Waiting")
                    } catch (e: Exception) {
                        Log.d("VarioCard", "error")
                        Log.d("VarioCard", e.toString())
                    } finally {
                        socket.takeIf { it.isConnected }?.apply {
                            close()
                        }
                        bufferedInputStream.close()
                    }

                    val message = textData.toString()
                    // Convert the image data to a byte array
                    val image = imageBuffer.toByteArray()
                    var imageFilePath = ""
                    try {
                        val directory = File(
                            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            "Cards"
                        )

                        if (!directory.exists()) {
                            directory.mkdirs()
                        }
                        val file = File(directory, "${System.currentTimeMillis()}.jpg")
                        if (!file.exists()) {
                            file.createNewFile()
                        }
                        Log.d("VarioCard", "here2")
                        val fileOutputStream = FileOutputStream(file)

                        fileOutputStream.write(image)
                        fileOutputStream.close()
                        imageFilePath = file.absolutePath
                        Log.d("VarioCard", "Image saved successfully to ${file.absolutePath}")
                    } catch (e: Exception) {
                        Log.e("VarioCard", "Error saving image: ${e.message}")
                    }
                    Log.d("VarioCard", "textData: $message ...")
                    if (message != "") {
                        activity.showToast("Adding card")
                        if (image != null && imageFilePath != "") activity.tryToAddCard(
                            message,
                            imageFilePath
                        )
                        else activity.tryToAddCard(message, "")
                    }

                    activity.showToast("Message has been sent")
                    Log.d("VarioCard", "End image reading??")
                }
                outputStream.close()

                /**
                 * Clean up any open sockets when done
                 * transferring or if an exception occurred.
                 */
            } catch (ie: IOException){
                Log.d("VarioCard", "IO exception raised here: $ie")
            } catch (s: SocketException){
                Log.d("VarioCard", "Socket exception raised here: $s")
            }
            catch (e: Exception) {
                Log.d("VarioCard", "exception raised here: $e")
            }
        }
    }
}