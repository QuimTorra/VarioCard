package com.degref.variocard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket


class FileServerAsyncTask(
    private val context: Context
) : AsyncTask<Void, Void, String?>() {

    private var serverSocket: ServerSocket? = null
    private var isServerRunning = true

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: Void): String? {
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
                    /*val f = File(
                        Environment.getExternalStorageDirectory().absolutePath +
                                "/${context.packageName}/wifip2pshared-${System.currentTimeMillis()}.txt"
                    )
                    val dirs = File(f.parent!!)

                    dirs.takeIf { it.doesNotExist() }?.apply {
                        mkdirs()
                    }
                    f.createNewFile()*/

                    val outputStream: OutputStream = client.getOutputStream()
                    val dataToSend = "Hello, this is the server!"
                    outputStream.write(dataToSend.toByteArray())

                    /*val inputStream = client.getInputStream()
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val receivedData = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        receivedData.append(line)
                    }*/

                    // Write the received text to a file
                    /*f.writeText(text)*/
                    Log.d("MONDONGO", "Written a file")
                    outputStream.close()
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

        return null
    }

    private fun File.doesNotExist(): Boolean = !exists()

    @Deprecated("Deprecated in Java")
    override fun onPostExecute(result: String?) {
        Log.d("MONDONGO", "Result is $result")
        result?.run {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse("file://$result"), "text/plain")
            }
            context.startActivity(intent)
        }
    }

    fun stopServer() {
        isServerRunning = false
        serverSocket?.close()
    }
}

