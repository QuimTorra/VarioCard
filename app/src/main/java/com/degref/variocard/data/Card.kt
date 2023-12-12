package com.degref.variocard.data

import java.util.UUID

data class Card(
    var id: UUID,
    var name: String,
    var phone: String,
    var email: String,
    var company: String,
    var additionalInfo: String,
    var image: String
)
