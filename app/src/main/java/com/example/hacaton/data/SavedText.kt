package com.example.hacaton.data

data class SavedText(
    val originalText: String,
    val timestamp: Long = System.currentTimeMillis()
)
