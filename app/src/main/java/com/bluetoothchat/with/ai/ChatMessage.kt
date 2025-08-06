package com.bluetoothchat.with.ai

import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val text: String,
    val isSent: Boolean,
    val sender: String,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
)
