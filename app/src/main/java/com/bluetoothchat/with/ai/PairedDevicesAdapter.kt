package com.bluetoothchat.with.ai

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bluetoothchat.with.ai.databinding.ItemPairedDeviceBinding

class PairedDevicesAdapter(
    private val onDeviceClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<PairedDevicesAdapter.DeviceViewHolder>() {
    
    private var devices: List<BluetoothDevice> = emptyList()
    
    fun updateDevices(newDevices: List<BluetoothDevice>) {
        devices = newDevices
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemPairedDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeviceViewHolder(binding, parent.context)
    }
    
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }
    
    override fun getItemCount(): Int = devices.size
    
    inner class DeviceViewHolder(
        private val binding: ItemPairedDeviceBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(device: BluetoothDevice) {
            val deviceName = if (ContextCompat.checkSelfPermission(
                    context, 
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED) {
                device.name ?: "Unknown Device"
            } else {
                "Unknown Device"
            }
            
            binding.deviceName.text = deviceName
            binding.connectionStatus.text = "Disconnected"
            binding.connectButton.text = "Connect"
            
            binding.connectButton.setOnClickListener {
                onDeviceClick(device)
            }
        }
    }
} 