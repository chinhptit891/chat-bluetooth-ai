package com.bluetoothchat.with.ai

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluetoothchat.with.ai.databinding.ItemNetworkDeviceBinding

class NetworkDevicesAdapter(
    private val onDeviceClick: (NetworkService.NetworkDevice) -> Unit
) : RecyclerView.Adapter<NetworkDevicesAdapter.NetworkDeviceViewHolder>() {
    
    private val devices = mutableListOf<NetworkService.NetworkDevice>()
    
    fun updateDevices(newDevices: List<NetworkService.NetworkDevice>) {
        devices.clear()
        devices.addAll(newDevices)
        notifyDataSetChanged()
    }
    
    fun addDevice(device: NetworkService.NetworkDevice) {
        if (!devices.contains(device)) {
            devices.add(device)
            notifyItemInserted(devices.size - 1)
        }
    }
    
    fun clearDevices() {
        devices.clear()
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NetworkDeviceViewHolder {
        val binding = ItemNetworkDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NetworkDeviceViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: NetworkDeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }
    
    override fun getItemCount(): Int = devices.size
    
    inner class NetworkDeviceViewHolder(
        private val binding: ItemNetworkDeviceBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(device: NetworkService.NetworkDevice) {
            binding.deviceName.text = device.deviceName
            binding.deviceIp.text = device.ipAddress
            binding.deviceStatus.text = if (device.isOnline) "Online" else "Offline"
            
            binding.root.setOnClickListener {
                onDeviceClick(device)
            }
        }
    }
} 