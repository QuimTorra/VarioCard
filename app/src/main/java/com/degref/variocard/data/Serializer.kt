package com.degref.variocard.data

import com.google.gson.Gson

class Serializer {
    private val serializer: Gson = Gson()

    fun cardToJson(card: Card): String {
        return serializer.toJson(card)
    }

    fun jsonToCard(card: String): Card{
        return serializer.fromJson(card, Card::class.java)
    }
}