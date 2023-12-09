package com.degref.variocard

import android.content.Context
import android.content.Intent
import android.location.GnssNavigationMessage
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket


class FileServerAsyncTask(
    private val context: Context,
    private val activity: MainActivity
) : Thread() {

    private var serverSocket: ServerSocket? = null
    private var isServerRunning = true
    private var messageToSend: String? = null

    @Deprecated("Deprecated in Java")
    //override fun doInBackground(vararg params: Void): String? {
    override fun run(){
        try {
            serverSocket = ServerSocket(8888)
            Log.d("MONDONGO", "Starting server")
            while (isServerRunning) {
                try {
                    val client = serverSocket!!.accept()
                    Log.d("MONDONGO", "Accepted")
                    if (serverSocket!!.isBound) Log.d("MONDONGO", "Server bound")
                    if(!client.isClosed) Log.d("MONDONGO", "Client not closed")
                    if(client.isConnected) Log.d("MONDONGO", "Client connected")

                    // Read data from the client's input stream
                    val inputStream: InputStream = client.getInputStream()
                    val buffer = ByteArray(4096)
                    var bytesRead: Int = -1

                    val receivedData = StringBuilder()
                    while (inputStream.available() > 0 || bytesRead != -1) {
                        bytesRead = inputStream.read(buffer)
                        if (bytesRead != -1) {
                            // Convert the received bytes to a String and append to the StringBuilder
                            receivedData.append(String(buffer, 0, bytesRead))
                            Log.d("MONDONGO", "Byte $bytesRead: ${String(buffer, 0, bytesRead)}")
                        }
                    }
                    val message = receivedData.split('*')[0]
                    val image = receivedData.split('*')[1]
                    Log.d("MONDONGO", "part 1: ${receivedData.split('*')[0]}")
                    Log.d("MONDONGO", "part 2: ${receivedData.split('*')[1]}")
                    if(message != null){
                        activity.tryToAddCard(message, image.toByteArray())
                    }
                    activity.showToast("Received message from client: $receivedData")

                    messageToSend?.let { message ->
                        // Send the specified message
                        val outputStream: OutputStream = client.getOutputStream()
                        outputStream.write(message.toByteArray())
                        outputStream.write("*".toByteArray())
                        outputStream.write("This is a test".toByteArray())

                        activity.showToast("Message has been sent")

                        outputStream.close()
                    }

                    activity.showToast("Message has been sent")

                    client.close()
                } catch (e: Exception) {
                    Log.e("FileServerAsyncTask", "Error in server: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("FileServerAsyncTask", "Error in server setup: ${e.message}")
        } finally {
            Log.d("MONDONGO", "Reached end of transmission, data theoretically sent")
            serverSocket?.close()
        }
        //return null
    }

    private fun File.doesNotExist(): Boolean = !exists()

   /* @Deprecated("Deprecated in Java")
    override fun onPostExecute(result: String?) {
        Log.d("MONDONGO", "Result is $result")
        result?.run {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse("file://$result"), "text/plain")
            }
            context.startActivity(intent)
        }
    }*/

    fun sendMessage(message: String){
        messageToSend = message
    }
    fun stopServer() {
        isServerRunning = false
        serverSocket?.close()
    }
}

