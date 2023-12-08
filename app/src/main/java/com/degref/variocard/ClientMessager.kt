package com.degref.variocard

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class ClientMessager (
    private val context: Context,
    private val activity: MainActivity,
    private val serverAddress: InetAddress
) : Thread() {

    private val socket = Socket()
    private var isClientRunning = true
    private var messageToSend: String? = null

    @Deprecated("Deprecated in Java")
    //override fun doInBackground(vararg params: Void): String? {
    override fun run(){
        try {
            GlobalScope.launch(Dispatchers.IO) {
                try {
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

                    if(messageToSend != null){
                        val outputStream: OutputStream = socket.getOutputStream()
                        outputStream.write(messageToSend!!.toByteArray())
                        outputStream.close()
                    }

                    val inputStream: InputStream = socket.getInputStream()
                    val buffer = ByteArray(1024)
                    val bytesRead = inputStream.read(buffer)

                    // Convert the received bytes to a String
                    val receivedData = String(buffer, 0, bytesRead)

                    activity.showToast("Received message: $receivedData")
                    // Process the received data as needed
                    Log.d("MONDONGO", "Received data: $receivedData")

                    inputStream.close()
                } catch (e: Exception) {
                    Log.d("MONDONGO", "exception raised: $e")
                }
            }
        } catch (e: Exception) {
            Log.e("FileServerAsyncTask", "Error in server setup: ${e.message}")
        } finally {
            Log.d("MONDONGO", "Reached end of transmission, data theoretically sent")
        }
        //return null
    }

    private fun File.doesNotExist(): Boolean = !exists()

    fun sendMessage(message: String){
        messageToSend = message
    }
    fun stopServer() {
        isClientRunning = false
        /**
         * Clean up any open sockets when done
         * transferring or if an exception occurred.
         */
        socket.takeIf { it.isConnected }?.apply {
            close()
        }
    }
}