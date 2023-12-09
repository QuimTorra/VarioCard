package com.degref.variocard.data

import android.net.Uri

data class Card(
    var id: Int,
    var name: String,
    var phone: String,
    var email: String,
    var company: String,
    var additionalInfo: String,
    var image: String
)
