package com.bluetoothchat.with.ai

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.*
import java.util.concurrent.ConcurrentHashMap

class NetworkService(private val context: Context) {
    
    companion object {
        private const val TAG = "NetworkService"
        private const val SERVER_PORT = 8888
        private const val BUFFER_SIZE = 1024
        private const val DISCOVERY_PORT = 8889
        private const val DISCOVERY_TIMEOUT = 5000L
    }
    
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val connectedClients = ConcurrentHashMap<String, Socket>()
    
    private var onMessageReceived: ((String, String) -> Unit)? = null
    private var onConnectionStatusChanged: ((Boolean) -> Unit)? = null
    private var onDeviceDiscovered: ((NetworkDevice) -> Unit)? = null
    
    private var isServerRunning = false
    private var isClientConnected = false
    
    data class NetworkDevice(
        val ipAddress: String,
        val deviceName: String,
        val isOnline: Boolean = true
    )
    
    fun setOnMessageReceived(callback: (String, String) -> Unit) {
        onMessageReceived = callback
    }
    
    fun setOnConnectionStatusChanged(callback: (Boolean) -> Unit) {
        onConnectionStatusChanged = callback
    }
    
    fun setOnDeviceDiscovered(callback: (NetworkDevice) -> Unit) {
        onDeviceDiscovered = callback
    }
    
    fun getLocalIpAddress(): String {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress
        
        // Fallback for emulator
        if (ipAddress == 0) {
            return "10.0.2.15" // Default emulator IP
        }
        
        return String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff
        )
    }
    
    fun startServer() {
        serviceScope.launch {
            try {
                val localIp = getLocalIpAddress()
                Log.d(TAG, "Starting server on $localIp:$SERVER_PORT")
                
                // Bind to specific address for better compatibility
                serverSocket = ServerSocket()
                serverSocket?.reuseAddress = true
                serverSocket?.bind(InetSocketAddress(localIp, SERVER_PORT))
                
                isServerRunning = true
                Log.d(TAG, "Server started successfully on port $SERVER_PORT")
                onConnectionStatusChanged?.invoke(true)
                
                while (isServerRunning) {
                    try {
                        val clientSocket = serverSocket?.accept()
                        clientSocket?.let { socket ->
                            val clientIp = socket.inetAddress.hostAddress
                            Log.d(TAG, "Client connected from: $clientIp")
                            connectedClients[clientIp] = socket
                            
                            // Start listening for messages from this client
                            startClientMessageListener(socket, clientIp)
                        }
                    } catch (e: IOException) {
                        if (isServerRunning) {
                            Log.e(TAG, "Error accepting client: ${e.message}")
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Server error: ${e.message}")
                onConnectionStatusChanged?.invoke(false)
            }
        }
    }
    
    fun connectToDevice(device: NetworkDevice) {
        serviceScope.launch {
            try {
                Log.d(TAG, "Attempting to connect to ${device.deviceName} at ${device.ipAddress}")
                
                clientSocket = Socket()
                clientSocket?.connect(InetSocketAddress(device.ipAddress, SERVER_PORT), 5000)
                isClientConnected = true
                
                Log.d(TAG, "Connected to device: ${device.deviceName} at ${device.ipAddress}")
                
                setupClientConnection(clientSocket!!)
                onConnectionStatusChanged?.invoke(true)
            } catch (e: IOException) {
                Log.e(TAG, "Connection failed: ${e.message}")
                onConnectionStatusChanged?.invoke(false)
            }
        }
    }
    
    private fun setupClientConnection(socket: Socket) {
        try {
            inputStream = socket.inputStream
            outputStream = socket.outputStream
            
            // Start listening for messages from server
            startServerMessageListener()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to setup client connection: ${e.message}")
        }
    }
    
    private fun startClientMessageListener(socket: Socket, clientIp: String) {
        serviceScope.launch {
            val buffer = ByteArray(BUFFER_SIZE)
            try {
                val inputStream = socket.inputStream
                while (isServerRunning && !socket.isClosed) {
                    val bytes = inputStream.read(buffer)
                    if (bytes > 0) {
                        val message = String(buffer, 0, bytes)
                        Log.d(TAG, "Received message from $clientIp: $message")
                        onMessageReceived?.invoke(message, clientIp)
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error reading from client $clientIp: ${e.message}")
            } finally {
                connectedClients.remove(clientIp)
                try {
                    socket.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Error closing client socket: ${e.message}")
                }
            }
        }
    }
    
    private fun startServerMessageListener() {
        serviceScope.launch {
            val buffer = ByteArray(BUFFER_SIZE)
            try {
                while (isClientConnected && clientSocket?.isConnected == true) {
                    val bytes = inputStream?.read(buffer)
                    if (bytes != null && bytes > 0) {
                        val message = String(buffer, 0, bytes)
                        Log.d(TAG, "Received message from server: $message")
                        onMessageReceived?.invoke(message, "SERVER")
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error reading from server: ${e.message}")
                onConnectionStatusChanged?.invoke(false)
            }
        }
    }
    
    fun sendMessage(message: String) {
        serviceScope.launch {
            try {
                if (isClientConnected && clientSocket?.isConnected == true) {
                    // Send as client to server
                    outputStream?.write(message.toByteArray())
                    outputStream?.flush()
                    Log.d(TAG, "Message sent to server: $message")
                } else if (isServerRunning) {
                    // Send as server to all connected clients
                    val messageBytes = message.toByteArray()
                    val clientsToRemove = mutableListOf<String>()
                    
                    connectedClients.forEach { (clientIp, socket) ->
                        try {
                            socket.getOutputStream().write(messageBytes)
                            socket.getOutputStream().flush()
                        } catch (e: IOException) {
                            Log.e(TAG, "Failed to send to client $clientIp: ${e.message}")
                            clientsToRemove.add(clientIp)
                        }
                    }
                    
                    // Remove disconnected clients
                    clientsToRemove.forEach { connectedClients.remove(it) }
                    
                    Log.d(TAG, "Message broadcasted to ${connectedClients.size} clients: $message")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Failed to send message: ${e.message}")
            }
        }
    }
    
    fun discoverDevices() {
        serviceScope.launch {
            val localIp = getLocalIpAddress()
            Log.d(TAG, "Local IP: $localIp")
            
            // For emulator, use common emulator IPs
            val emulatorIps = listOf(
                "10.0.2.2",  // Host machine
                "10.0.2.15", // Emulator 1
                "10.0.2.16", // Emulator 2
                "10.0.2.17", // Emulator 3
                "10.0.2.18", // Emulator 4
                "10.0.2.19", // Emulator 5
                "10.0.2.20"  // Emulator 6
            )
            
            // Also scan local network
            val networkPrefix = if (localIp.startsWith("10.0.2.")) {
                "10.0.2."
            } else {
                localIp.substring(0, localIp.lastIndexOf(".") + 1)
            }
            
            Log.d(TAG, "Starting device discovery on network: $networkPrefix")
            
            // First check emulator IPs
            emulatorIps.forEach { ip ->
                if (ip != localIp) {
                    launch {
                        if (isDeviceReachable(ip)) {
                            val device = NetworkDevice(ip, "Emulator $ip")
                            onDeviceDiscovered?.invoke(device)
                        }
                    }
                }
            }
            
            // Then scan common IP range (1-254)
            for (i in 1..254) {
                val targetIp = networkPrefix + i
                if (targetIp != localIp && !emulatorIps.contains(targetIp)) {
                    launch {
                        if (isDeviceReachable(targetIp)) {
                            val device = NetworkDevice(targetIp, "Device $targetIp")
                            onDeviceDiscovered?.invoke(device)
                        }
                    }
                }
            }
        }
    }
    
    private suspend fun isDeviceReachable(ipAddress: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(ipAddress, SERVER_PORT), 1000)
                socket.close()
                Log.d(TAG, "Device found at $ipAddress")
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    fun disconnect() {
        try {
            isServerRunning = false
            isClientConnected = false
            
            inputStream?.close()
            outputStream?.close()
            clientSocket?.close()
            serverSocket?.close()
            
            connectedClients.values.forEach { it.close() }
            connectedClients.clear()
            
            inputStream = null
            outputStream = null
            clientSocket = null
            serverSocket = null
            
            onConnectionStatusChanged?.invoke(false)
            Log.d(TAG, "Disconnected")
        } catch (e: IOException) {
            Log.e(TAG, "Error during disconnect: ${e.message}")
        }
    }
    
    fun isConnected(): Boolean {
        return isClientConnected || isServerRunning
    }
    
    fun isServerRunning(): Boolean {
        return isServerRunning
    }
    
    fun isClientConnected(): Boolean {
        return isClientConnected
    }
    
    fun cleanup() {
        disconnect()
        serviceScope.cancel()
    }
} 