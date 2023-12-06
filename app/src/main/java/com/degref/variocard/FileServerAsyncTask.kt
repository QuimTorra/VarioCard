package com.degref.variocard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import android.widget.TextView
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.SocketTimeoutException


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
                    val f = File(
                        Environment.getExternalStorageDirectory().absolutePath +
                                "/${context.packageName}/wifip2pshared-${System.currentTimeMillis()}.txt"
                    )
                    val dirs = File(f.parent!!)

                    dirs.takeIf { it.doesNotExist() }?.apply {
                        mkdirs()
                    }
                    f.createNewFile()

                    val inputStream = client.getInputStream()
                    val reader = InputStreamReader(inputStream)
                    val bufferedReader = BufferedReader(reader)

                    val text = bufferedReader.readLine()

                    // Write the received text to a file
                    f.writeText(text)
                    Log.d("MONDONGO", "Written a file")
                    client.close()
                } catch (e: Exception) {
                        Log.e("FileServerAsyncTask", "Error in server: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("FileServerAsyncTask", "Error in server setup: ${e.message}")
        } finally {
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

