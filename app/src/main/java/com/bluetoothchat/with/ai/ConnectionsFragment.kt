package com.bluetoothchat.with.ai

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluetoothchat.with.ai.databinding.BluetoothOffStateBinding
import com.bluetoothchat.with.ai.databinding.BluetoothOnStateBinding
import com.bluetoothchat.with.ai.databinding.FragmentConnectionsBinding
import com.google.android.material.snackbar.Snackbar

class ConnectionsFragment : Fragment() {
    
    private var _binding: FragmentConnectionsBinding? = null
    private val binding get() = _binding!!
    
    private var bluetoothOffBinding: BluetoothOffStateBinding? = null
    private var bluetoothOnBinding: BluetoothOnStateBinding? = null
    
    private lateinit var pairedDevicesAdapter: PairedDevicesAdapter
    private lateinit var availableDevicesAdapter: AvailableDevicesAdapter
    
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    when (state) {
                        BluetoothAdapter.STATE_ON -> showBluetoothOnState()
                        BluetoothAdapter.STATE_OFF -> showBluetoothOffState()
                    }
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    device?.let { availableDevicesAdapter.addDevice(it) }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    binding.scanButton.text = getString(R.string.scanning)
                    binding.scanButton.isEnabled = false
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    binding.scanButton.text = getString(R.string.scan_for_new_devices)
                    binding.scanButton.isEnabled = true
                }
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectionsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupAdapters()
        setupClickListeners()
        updateBluetoothState()
        registerBluetoothReceiver()
    }
    
    private fun setupToolbar() {
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    Snackbar.make(binding.root, "Settings clicked", Snackbar.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupAdapters() {
        pairedDevicesAdapter = PairedDevicesAdapter { device ->
            // Handle device connection
            val deviceName = if (ContextCompat.checkSelfPermission(
                    requireContext(), 
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED) {
                device.name ?: "Unknown Device"
            } else {
                "Unknown Device"
            }
            Snackbar.make(binding.root, "Connecting to $deviceName", Snackbar.LENGTH_SHORT).show()
        }
        
        availableDevicesAdapter = AvailableDevicesAdapter { device ->
            // Handle device pairing
            val deviceName = if (ContextCompat.checkSelfPermission(
                    requireContext(), 
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED) {
                device.name ?: "Unknown Device"
            } else {
                "Unknown Device"
            }
            Snackbar.make(binding.root, "Pairing with $deviceName", Snackbar.LENGTH_SHORT).show()
        }
        
        bluetoothOnBinding?.pairedDevicesRecyclerView?.adapter = pairedDevicesAdapter
        bluetoothOnBinding?.availableDevicesRecyclerView?.adapter = availableDevicesAdapter
    }
    
    private fun setupClickListeners() {
        binding.scanButton.setOnClickListener {
            startBluetoothDiscovery()
        }
    }
    
    private fun updateBluetoothState() {
        val mainActivity = activity as? MainActivity
        if (mainActivity?.isBluetoothEnabled() == true) {
            showBluetoothOnState()
        } else {
            showBluetoothOffState()
        }
    }
    
    private fun showBluetoothOffState() {
        if (bluetoothOffBinding == null) {
            bluetoothOffBinding = BluetoothOffStateBinding.inflate(layoutInflater, binding.bluetoothStateContainer, true)
            bluetoothOffBinding?.turnOnBluetoothButton?.setOnClickListener {
                (activity as? MainActivity)?.enableBluetooth()
            }
        }
        
        bluetoothOffBinding?.root?.visibility = View.VISIBLE
        bluetoothOnBinding?.root?.visibility = View.GONE
        binding.scanButton.visibility = View.GONE
    }
    
    private fun showBluetoothOnState() {
        if (bluetoothOnBinding == null) {
            bluetoothOnBinding = BluetoothOnStateBinding.inflate(layoutInflater, binding.bluetoothStateContainer, true)
            setupAdapters()
        }
        
        bluetoothOffBinding?.root?.visibility = View.GONE
        bluetoothOnBinding?.root?.visibility = View.VISIBLE
        binding.scanButton.visibility = View.VISIBLE
        
        loadPairedDevices()
    }
    
    private fun loadPairedDevices() {
        val mainActivity = activity as? MainActivity
        val bluetoothAdapter = mainActivity?.getBluetoothAdapter()
        
        if (ContextCompat.checkSelfPermission(
                requireContext(), 
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter?.bondedDevices?.let { devices ->
                if (devices.isNotEmpty()) {
                    pairedDevicesAdapter.updateDevices(devices.toList())
                } else {
                    // Show empty state
                    Snackbar.make(binding.root, getString(R.string.no_paired_devices), Snackbar.LENGTH_LONG).show()
                }
            }
        } else {
            Snackbar.make(binding.root, "Bluetooth permission required", Snackbar.LENGTH_LONG).show()
        }
    }
    
    private fun startBluetoothDiscovery() {
        val mainActivity = activity as? MainActivity
        val bluetoothAdapter = mainActivity?.getBluetoothAdapter()
        
        if (ContextCompat.checkSelfPermission(
                requireContext(), 
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED) {
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter.cancelDiscovery()
            }
            
            bluetoothAdapter?.startDiscovery()
        } else {
            Snackbar.make(binding.root, "Bluetooth scan permission required", Snackbar.LENGTH_LONG).show()
        }
    }
    
    private fun registerBluetoothReceiver() {
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        requireActivity().registerReceiver(bluetoothReceiver, filter)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        bluetoothOffBinding = null
        bluetoothOnBinding = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            requireActivity().unregisterReceiver(bluetoothReceiver)
        } catch (e: Exception) {
            // Receiver not registered
        }
    }
} 