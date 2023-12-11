package com.degref.variocard

import android.content.Context
import android.content.Intent
import android.location.GnssNavigationMessage
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.Toast
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.util.concurrent.CountDownLatch


class FileServerAsyncTask(
    private val context: Context,
    private val activity: MainActivity
) : Thread() {

    private var serverSocket: ServerSocket? = null
    private var isServerRunning = true
    private var messageToSend: String? = null

    @Deprecated("Deprecated in Java")
    //override fun doInBackground(vararg params: Void): String? {
    override fun run() {
        try {
            serverSocket = ServerSocket(8888)
            Log.d("MONDONGO", "Starting server")
            while (isServerRunning) {
                try {
                    val client = serverSocket!!.accept()
                    Log.d("MONDONGO", "Accepted")
                    if (serverSocket!!.isBound) Log.d("MONDONGO", "Server bound")
                    if (!client.isClosed) Log.d("MONDONGO", "Client not closed")
                    if (client.isConnected) Log.d("MONDONGO", "Client connected")
                    if (!activity.isSenderActive) {
                        val inputStream: InputStream = client.getInputStream()
                        val bufferedInputStream = BufferedInputStream(inputStream)
                        val buffer = ByteArray(4096)
                        var bytesRead: Int

                        val textData = StringBuilder()
                        val imageBuffer = ByteArrayOutputStream()

                        var latch = CountDownLatch(1)

                        try {
                            var asteriskFound = false

                            do {
                                bytesRead = bufferedInputStream.read(buffer)
                                if (bytesRead != -1) {
                                    if (asteriskFound) {
                                        imageBuffer.write(buffer, 0, bytesRead)
                                    } else {
                                        // Check for the asterisk to separate text and image data
                                        for (i in 0 until bytesRead) {
                                            if (i + 3 < buffer.size && buffer[i] == '\r'.toByte() && buffer[i + 1] == '\n'.toByte() &&
                                                buffer[i + 2] == '\r'.toByte() && buffer[i + 3] == '\n'.toByte()
                                            ) {
                                                asteriskFound = true
                                                Log.d("MONDONGO", "FOUND at $i")
                                                // Write the remaining bytes to the image buffer
                                                imageBuffer.write(
                                                    buffer,
                                                    i + 4,
                                                    bytesRead - (i + 4)
                                                )
                                                latch.countDown()
                                                break
                                            }
                                            textData.append(buffer[i].toChar())
                                        }

                                        if (!asteriskFound) {
                                            // If asterisk is not found, write all bytes to text data
                                            textData.append(String(buffer, 0, bytesRead))
                                        }
                                    }
                                } else {
                                    latch.countDown()
                                }
                            } while (bytesRead != -1)
                            Log.d("MONDONGO", "Start to wait")
                            latch.await()
                            Log.d("MONDONGO", "Waiting")
                        } catch (e: Exception) {
                            Log.d("MONDONGO", e.printStackTrace().toString())
                        } finally {
                            bufferedInputStream.close()
                        }

                        val message = textData.toString()
                        // Convert the image data to a byte array
                        val image = imageBuffer.toByteArray()
                        var imageFilePath: String = ""
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
                            Log.d("MONDONGO", "here2")
                            val fileOutputStream = FileOutputStream(file)

                            fileOutputStream.write(image)
                            fileOutputStream.close()
                            imageFilePath = file.absolutePath
                            Log.d("MONDONGO", "Image saved successfully to ${file.absolutePath}")
                        } catch (e: Exception) {
                            Log.e("MONDONGO", "Error saving image: ${e.message}")
                        }

                        if (message != null) {
                            if (image != null && imageFilePath != "") activity.tryToAddCard(
                                message,
                                imageFilePath
                            )
                            else activity.tryToAddCard(message, "")
                        }

                        messageToSend?.let { message ->
                            // Send the specified message
                            val outputStream: OutputStream = client.getOutputStream()
                            outputStream.write(message.toByteArray())
                            outputStream.write("END".toByteArray())
                            outputStream.write("This is a test".toByteArray())

                            activity.showToast("Message has been sent")

                            outputStream.close()
                        }

                        activity.showToast("Message has been sent")
                        Log.d("MONDONGO", "End image reading??")
                        client.close()
                    } else {
                        Log.d("MONDONGO", "Sending...")
                        var outputStream = client.getOutputStream()
                        val card = activity.viewModel.getCard()
                        val imageCard = activity.viewModel.getImageCard()
                        Log.d("MONDONGO", "Output: $card")
                        outputStream.write(card!!.toByteArray())
                        Log.d("MONDONGO", "not here")
                        outputStream.write("\r\n\r\n".toByteArray())
                        if (imageCard != null) {

                            Log.d("MONDONGO", "Image not null...")
                            val file = File(imageCard)
                            Log.d("MONDONGO", "File size: ${file.length()}")

                            // Create a FileInputStream for the file
                            val fileInputStream = FileInputStream(file)
                            val buffer = ByteArray(4096)
                            var bytesRead: Int

                            while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                                //Log.d("MONDONGO", "writing image....")
                                outputStream.write(buffer, 0, bytesRead)
                            }

                            Log.d("MONDONGO", "End of transmission")
                        }
                        Log.d("MONDONGO", "Card sent: $card")
                        Log.d("MONDONGO", "Setting card to null")
                        //manager.card = null
                    }
                } catch (e: Exception) {
                    //Log.e("FileServerAsyncTask", "Error in server setup: ${e.message}")
                } finally {
                    //Log.d("MONDONGO", "Reached end of transmission, data theoretically sent")
                    serverSocket?.takeIf { it.isBound }?.apply {
                        close()
                    }
                }
            }
            //return null
        } catch(e: Exception){
            Log.d("MONDONGO", "Error in transmission")
        }
    }

    private fun File.doesNotExist(): Boolean = !exists()

    fun sendMessage(message: String){
        messageToSend = message
    }
    fun stopServer() {
        isServerRunning = false
        serverSocket?.close()
    }

    fun writeToStorage(byteArray: ByteArray){
        val name = "${System.currentTimeMillis()}.jpg"
        val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyAppImages")

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, name)

        if (!file.exists()) {
            file.createNewFile()
        }
        try {
            // Create a FileOutputStream to write to the file
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(byteArray)
            fileOutputStream.close()
            Log.d("MONDONGO", "Image successfully sent")
            // File has been written successfully
        } catch (e: Exception) {
            Log.d("MONDONGO", "Exception")
            //Log.d("MONDONGO", e.printStackTrace().toString())
            // Handle the exception
        }
    }
}

