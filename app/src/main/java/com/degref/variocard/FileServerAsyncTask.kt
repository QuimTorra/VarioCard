package com.degref.variocard

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
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
    override fun run() {
        try {
            Log.d("VarioCard", "Starting server")
            serverSocket = ServerSocket(8888)
            try {
                Log.d("VarioCard","TRYING")
                val client = serverSocket!!.accept()
                Log.d("VarioCard", "Accepted")
                if (serverSocket!!.isBound) Log.d("VarioCard", "Server bound")
                if (!client.isClosed) Log.d("VarioCard", "Client not closed")
                if (client.isConnected) {
                    Log.d("VarioCard", "Client connected")
                    if (!activity.isSenderActive) {
                        activity.showToast("Ready to receive")
                        val inputStream: InputStream = client.getInputStream()
                        val bufferedInputStream = BufferedInputStream(inputStream)
                        val buffer = ByteArray(4096)
                        var bytesRead: Int

                        val textData = StringBuilder()
                        val imageBuffer = ByteArrayOutputStream()

                        val latch = CountDownLatch(1)

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

                                        if (!asteriskFound) {
                                            // If asterisk is not found, write all bytes to text data
                                            textData.append(String(buffer, 0, bytesRead))
                                        }
                                    }
                                } else {
                                    latch.countDown()
                                }
                            } while (bytesRead != -1)
                            Log.d("VarioCard", "Start to wait")
                            latch.await()
                            Log.d("VarioCard", "Waiting")
                        } catch (e: Exception) {
                            Log.d("VarioCard", e.toString())
                        } finally {
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

                        if (message != "") {
                            activity.showToast("Adding card")
                            if (image != null && imageFilePath != "") activity.tryToAddCard(
                                message,
                                imageFilePath
                            )
                            else activity.tryToAddCard(message, "")
                        }

                        messageToSend?.let { msg ->
                            // Send the specified message
                            val outputStream: OutputStream = client.getOutputStream()
                            outputStream.write(msg.toByteArray())
                            outputStream.write("END".toByteArray())

                            activity.showToast("Message has been sent")

                            outputStream.close()
                        }

                        activity.showToast("Message has been sent")
                        Log.d("VarioCard", "End image reading??")
                        client.close()
                    } else {
                        activity.showToast("Ready to send")
                        Log.d("VarioCard", "Sending...")
                        val outputStream = client.getOutputStream()
                        val card = activity.viewModel.getCard()
                        val imageCard = activity.viewModel.getImageCard()
                        if(card != null){
                            Log.d("VarioCard", "Output: $card")
                            outputStream.write(card.toByteArray())
                            Log.d("VarioCard", "not here")
                            outputStream.write("\r\n\r\n".toByteArray())
                            if (imageCard != null) {

                                Log.d("VarioCard", "Image not null...")
                                val file = File(imageCard)
                                Log.d("VarioCard", "File size: ${file.length()}")

                                // Create a FileInputStream for the file
                                val fileInputStream = FileInputStream(file)
                                val buffer = ByteArray(4096)
                                var bytesRead: Int

                                while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                                    outputStream.write(buffer, 0, bytesRead)
                                }

                                Log.d("VarioCard", "End of transmission")
                            }
                            Log.d("VarioCard", "Card sent: $card")
                            Log.d("VarioCard", "Setting card to null")
                            activity.viewModel.setCard() //card to null
                        }
                        outputStream.close()
                    }
                }
            } catch (e: Exception) {
                Log.e("FileServerAsyncTask", "Error in server setup: ${e.message}")
            }
        } catch(e: Exception){
            Log.d("VarioCard", "Error in transmission: $e")
        }
    }

    fun sendMessage(message: String){
        messageToSend = message
    }
    fun stopServer() {
        isServerRunning = false
        serverSocket?.close()
    }

}

