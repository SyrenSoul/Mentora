package com.example.mentora.data

import java.util.UUID

data class Murid(
    val id: String = UUID.randomUUID().toString(),
    val nama: String,
    var avatar: String = "",
    var hurufProgress: Int = 0,
    var angkaProgress: Int = 0,
    var warnaProgress: Int = 0,
    var lastPlayed: Long = 0L
)