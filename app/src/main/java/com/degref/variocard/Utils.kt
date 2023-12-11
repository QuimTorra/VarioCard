package com.degref.variocard

import java.lang.reflect.Array
import java.nio.charset.StandardCharsets

object Utils {
    @JvmStatic
    fun bytesToHex(bytes: ByteArray): String {
        val result = StringBuffer()
        for (b in bytes) result.append(
            ((b.toInt() and 0xff) + 0x100).toString(16).substring(1)
        )
        return result.toString()
    }

    @JvmStatic
    fun parseTextrecordPayload(ndefPayload: ByteArray): String {
        val languageCodeLength = Array.getByte(ndefPayload, 0).toInt()
        val ndefPayloadLength = ndefPayload.size
        val languageCode = ByteArray(languageCodeLength)
        System.arraycopy(ndefPayload, 1, languageCode, 0, languageCodeLength)
        val message = ByteArray(ndefPayloadLength - 1 - languageCodeLength)
        System.arraycopy(
            ndefPayload,
            1 + languageCodeLength,
            message,
            0,
            ndefPayloadLength - 1 - languageCodeLength
        )
        return String(message, StandardCharsets.UTF_8)
    }
}