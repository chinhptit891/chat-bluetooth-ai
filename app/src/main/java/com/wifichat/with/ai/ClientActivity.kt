package com.wifichat.with.ai

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.wifichat.with.ai.databinding.ActivityClientBinding
import java.io.*
import java.net.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class ClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientBinding
    private lateinit var chatAdapter: ChatAdapter
    private var socket: Socket? = null
    private var outputStream: PrintWriter? = null
    private var inputStream: BufferedReader? = null
    private var isConnected = false

    companion object {
        private const val TAG = "ClientActivity"
        private const val PORT = 5000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupChatAdapter()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.title = "TCP Client"
        binding.toolbar.subtitle = "Enter server IP and connect"
    }

    private fun setupChatAdapter() {
        chatAdapter = ChatAdapter()
        binding.chatRecyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@ClientActivity)
        }

        // Thêm tin nhắn hướng dẫn
        addSystemMessage("Enter server IP address and tap Connect")
    }

    private fun setupClickListeners() {
        binding.connectButton.setOnClickListener {
            val serverIp = binding.serverIpInput.text.toString().trim()
            if (serverIp.isNotEmpty()) {
                if (!isConnected) {
                    connectToServer(serverIp)
                } else {
                    disconnect()
                }
            } else {
                Toast.makeText(this, "Please enter server IP address", Toast.LENGTH_SHORT).show()
            }
        }

        binding.sendButton.setOnClickListener {
            sendMessage()
        }

        binding.messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
    }

    private fun connectToServer(serverIp: String) {
        binding.connectButton.isEnabled = false
        binding.serverIpInput.isEnabled = false

        thread {
            try {
                Log.d(TAG, "Connecting to server: $serverIp:$PORT")
                runOnUiThread {
                    addSystemMessage("Connecting to $serverIp:$PORT...")
                }

                socket = Socket()
                socket?.connect(InetSocketAddress(serverIp, PORT), 10000) // 10 second timeout

                Log.d(TAG, "Connected to server")

                runOnUiThread {
                    addSystemMessage("Connected to server!")
                    isConnected = true
                    binding.connectButton.text = "Disconnect"
                    binding.connectButton.isEnabled = true
                    binding.sendButton.isEnabled = true
                    binding.messageInput.isEnabled = true
                    binding.toolbar.subtitle = "Connected to $serverIp:$PORT"
                }

                // Thiết lập input/output streams
                outputStream = PrintWriter(socket?.getOutputStream()!!, true)
                inputStream = BufferedReader(InputStreamReader(socket?.getInputStream()!!))

                // Lắng nghe tin nhắn từ server
                listenForMessages()

            } catch (e: Exception) {
                Log.e(TAG, "Connection error: ${e.message}", e)
                runOnUiThread {
                    addSystemMessage("Connection failed: ${e.message}")
                    Toast.makeText(this@ClientActivity, "Connection failed: ${e.message}", Toast.LENGTH_LONG).show()
                    resetConnectionUI()
                }
            }
        }
    }

    private fun listenForMessages() {
        thread {
            try {
                while (isConnected && socket?.isConnected == true) {
                    val message = inputStream?.readLine()
                    if (message != null) {
                        Log.d(TAG, "Received message: $message")
                        runOnUiThread {
                            chatAdapter.addMessage(ChatMessage(message, false, "Server"))
                            scrollToBottom()
                        }
                    } else {
                        // Server đã ngắt kết nối
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading message: ${e.message}", e)
            } finally {
                runOnUiThread {
                    if (isConnected) {
                        addSystemMessage("Disconnected from server")
                        resetConnectionUI()
                    }
                }
            }
        }
    }

    private fun sendMessage() {
        val messageText = binding.messageInput.text.toString().trim()
        if (messageText.isNotEmpty() && outputStream != null && isConnected) {
            thread {
                try {
                    outputStream?.println(messageText)
                    Log.d(TAG, "Sent message: $messageText")

                    runOnUiThread {
                        chatAdapter.addMessage(ChatMessage(messageText, true, "Client"))
                        binding.messageInput.text?.clear()
                        scrollToBottom()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending message: ${e.message}", e)
                    runOnUiThread {
                        Toast.makeText(this@ClientActivity, "Failed to send message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun disconnect() {
        thread {
            cleanup()
            runOnUiThread {
                addSystemMessage("Disconnected from server")
                resetConnectionUI()
            }
        }
    }

    private fun resetConnectionUI() {
        isConnected = false
        binding.connectButton.text = "Connect"
        binding.connectButton.isEnabled = true
        binding.serverIpInput.isEnabled = true
        binding.sendButton.isEnabled = false
        binding.messageInput.isEnabled = false
        binding.toolbar.subtitle = "Enter server IP and connect"
    }

    private fun addSystemMessage(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        chatAdapter.addMessage(ChatMessage("[$timestamp] $message", false, "System"))
        scrollToBottom()
    }

    private fun scrollToBottom() {
        binding.chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanup()
    }

    private fun cleanup() {
        isConnected = false
        try {
            inputStream?.close()
            outputStream?.close()
            socket?.close()
            Log.d(TAG, "Client cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup: ${e.message}", e)
        }
    }
}
