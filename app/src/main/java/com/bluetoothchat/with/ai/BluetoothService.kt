package com.bluetoothchat.with.ai

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothService(private val context: Context) {
    
    companion object {
        private const val TAG = "BluetoothService"
        private val APP_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
        private const val SERVICE_NAME = "ChatBluetoothService"
    }
    
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var serverSocket: BluetoothServerSocket? = null
    private var clientSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var onMessageReceived: ((String) -> Unit)? = null
    private var onConnectionStatusChanged: ((Boolean) -> Unit)? = null
    
    fun setBluetoothAdapter(adapter: BluetoothAdapter) {
        bluetoothAdapter = adapter
    }
    
    fun setOnMessageReceived(callback: (String) -> Unit) {
        onMessageReceived = callback
    }
    
    fun setOnConnectionStatusChanged(callback: (Boolean) -> Unit) {
        onConnectionStatusChanged = callback
    }
    
    fun startServer() {
        serviceScope.launch {
            try {
                if (ContextCompat.checkSelfPermission(
                        context, 
                        Manifest.permission.BLUETOOTH_ADVERTISE
                    ) == PackageManager.PERMISSION_GRANTED) {
                    serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(SERVICE_NAME, APP_UUID)
                    Log.d(TAG, "Server socket created")
                    
                    while (true) {
                        val socket = serverSocket?.accept()
                        socket?.let {
                            Log.d(TAG, "Client connected")
                            setupConnection(it)
                            onConnectionStatusChanged?.invoke(true)
                        }
                    }
                } else {
                    Log.e(TAG, "BLUETOOTH_ADVERTISE permission not granted")
                    onConnectionStatusChanged?.invoke(false)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Server error: ${e.message}")
                onConnectionStatusChanged?.invoke(false)
            }
        }
    }
    
    fun connectToDevice(device: BluetoothDevice) {
        serviceScope.launch {
            try {
                if (ContextCompat.checkSelfPermission(
                        context, 
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED) {
                    clientSocket = device.createRfcommSocketToServiceRecord(APP_UUID)
                    clientSocket?.connect()
                    Log.d(TAG, "Connected to device: ${device.name}")
                    
                    setupConnection(clientSocket!!)
                    onConnectionStatusChanged?.invoke(true)
                } else {
                    Log.e(TAG, "BLUETOOTH_CONNECT permission not granted")
                    onConnectionStatusChanged?.invoke(false)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Connection failed: ${e.message}")
                onConnectionStatusChanged?.invoke(false)
            }
        }
    }
    
    private fun setupConnection(socket: BluetoothSocket) {
        try {
            inputStream = socket.inputStream
            outputStream = socket.outputStream
            
            // Start listening for messages
            startMessageListener()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to setup connection: ${e.message}")
        }
    }
    
    private fun startMessageListener() {
        serviceScope.launch {
            val buffer = ByteArray(1024)
            while (true) {
                try {
                    val bytes = inputStream?.read(buffer)
                    if (bytes != null && bytes > 0) {
                        val message = String(buffer, 0, bytes)
                        Log.d(TAG, "Received message: $message")
                        onMessageReceived?.invoke(message)
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Error reading message: ${e.message}")
                    break
                }
            }
        }
    }
    
    fun sendMessage(message: String) {
        serviceScope.launch {
            try {
                outputStream?.write(message.toByteArray())
                Log.d(TAG, "Message sent: $message")
            } catch (e: IOException) {
                Log.e(TAG, "Failed to send message: ${e.message}")
            }
        }
    }
    
    fun disconnect() {
        try {
            inputStream?.close()
            outputStream?.close()
            clientSocket?.close()
            serverSocket?.close()
            
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
        return clientSocket?.isConnected == true || serverSocket != null
    }
    
    fun cleanup() {
        disconnect()
        serviceScope.cancel()
    }
} 