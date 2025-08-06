package com.wifichat.with.ai

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.wifichat.with.ai.databinding.ActivityServerBinding
import java.io.*
import java.net.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class ServerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServerBinding
    private lateinit var chatAdapter: ChatAdapter
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var outputStream: PrintWriter? = null
    private var inputStream: BufferedReader? = null
    private var isRunning = false

    companion object {
        private const val TAG = "ServerActivity"
        private const val PORT = 5000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupChatAdapter()
        setupClickListeners()
        startServer()
    }

    private fun setupToolbar() {
        binding.toolbar.title = "TCP Server"
        binding.toolbar.subtitle = "Listening on port $PORT"
    }

    private fun setupChatAdapter() {
        chatAdapter = ChatAdapter()
        binding.chatRecyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@ServerActivity)
        }

        // Thêm tin nhắn chào mừng
        addSystemMessage("Server started on port $PORT")
        addSystemMessage("Waiting for client connection...")
    }

    private fun setupClickListeners() {
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

    private fun startServer() {
        thread {
            try {
                Log.d(TAG, "Starting server on port $PORT")
                serverSocket = ServerSocket(PORT)
                isRunning = true

                runOnUiThread {
                    addSystemMessage("Server listening on ${getLocalIpAddress()}:$PORT")
                }

                // Chờ client kết nối
                clientSocket = serverSocket?.accept()

                Log.d(TAG, "Client connected: ${clientSocket?.remoteSocketAddress}")

                runOnUiThread {
                    addSystemMessage("Client connected: ${clientSocket?.remoteSocketAddress}")
                    binding.sendButton.isEnabled = true
                    binding.messageInput.isEnabled = true
                }

                // Thiết lập input/output streams
                outputStream = PrintWriter(clientSocket?.getOutputStream()!!, true)
                inputStream = BufferedReader(InputStreamReader(clientSocket?.getInputStream()!!))

                // Lắng nghe tin nhắn từ client
                listenForMessages()

            } catch (e: Exception) {
                Log.e(TAG, "Server error: ${e.message}", e)
                runOnUiThread {
                    addSystemMessage("Server error: ${e.message}")
                    Toast.makeText(this@ServerActivity, "Server error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun listenForMessages() {
        thread {
            try {
                while (isRunning && clientSocket?.isConnected == true) {
                    val message = inputStream?.readLine()
                    if (message != null) {
                        Log.d(TAG, "Received message: $message")
                        runOnUiThread {
                            chatAdapter.addMessage(ChatMessage(message, false, "Client"))
                            scrollToBottom()
                        }
                    } else {
                        // Client đã ngắt kết nối
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading message: ${e.message}", e)
            } finally {
                runOnUiThread {
                    addSystemMessage("Client disconnected")
                    binding.sendButton.isEnabled = false
                    binding.messageInput.isEnabled = false
                }
            }
        }
    }

    private fun sendMessage() {
        val messageText = binding.messageInput.text.toString().trim()
        if (messageText.isNotEmpty() && outputStream != null) {
            thread {
                try {
                    outputStream?.println(messageText)
                    Log.d(TAG, "Sent message: $messageText")

                    runOnUiThread {
                        chatAdapter.addMessage(ChatMessage(messageText, true, "Server"))
                        binding.messageInput.text?.clear()
                        scrollToBottom()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending message: ${e.message}", e)
                    runOnUiThread {
                        Toast.makeText(this@ServerActivity, "Failed to send message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun addSystemMessage(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        chatAdapter.addMessage(ChatMessage("[$timestamp] $message", false, "System"))
        scrollToBottom()
    }

    private fun scrollToBottom() {
        binding.chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
    }

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress ?: "Unknown"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IP address: ${e.message}", e)
        }
        return "Unknown"
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanup()
    }

    private fun cleanup() {
        isRunning = false
        try {
            inputStream?.close()
            outputStream?.close()
            clientSocket?.close()
            serverSocket?.close()
            Log.d(TAG, "Server cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup: ${e.message}", e)
        }
    }
}
