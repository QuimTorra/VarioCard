package com.degref.variocard

import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import android.widget.TextView
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.ServerSocket


class FileServerAsyncTask(
    private val context: Context
) : AsyncTask<Void, Void, String?>() {

    override fun doInBackground(vararg params: Void): String? {
        val serverSocket = ServerSocket(8888)
        return serverSocket.use {
            val client = serverSocket.accept()
            val f = File(
                Environment.getExternalStorageDirectory().absolutePath +
                    "/${context.packageName}/wifip2pshared-${System.currentTimeMillis()}.txt")
            val dirs = File(f.parent)

            dirs.takeIf { it.doesNotExist() }?.apply {
                mkdirs()
            }
            f.createNewFile()

            val inputStream = client.getInputStream()
            val reader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(reader)

            val text = bufferedReader.readLine()

            serverSocket.close()

            // Write the received text to a file
            f.writeText(text)

            f.absolutePath
        }
    }

    private fun File.doesNotExist(): Boolean = !exists()

    override fun onPostExecute(result: String?) {
        Log.d("MONDONGO", "Result is $result")
        result?.run {
            //statusText.text = "Text received and saved to file - $result"
        }
    }
}

