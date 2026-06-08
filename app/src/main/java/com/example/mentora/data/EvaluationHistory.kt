package com.example.mentora.data

data class EvaluationHistory(
    val materi: String,
    val score: Int,
    val bintang: Int,
    val durasi: Int,
    val salah: Int,
    val tanggal: Long,
    val wrongItems: String
)