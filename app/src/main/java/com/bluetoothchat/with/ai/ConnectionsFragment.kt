package com.bluetoothchat.with.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluetoothchat.with.ai.databinding.FragmentConnectionsBinding
import com.bluetoothchat.with.ai.databinding.NetworkOffStateBinding
import com.bluetoothchat.with.ai.databinding.NetworkOnStateBinding
import com.google.android.material.snackbar.Snackbar

class ConnectionsFragment : Fragment() {
    
    private var _binding: FragmentConnectionsBinding? = null
    private val binding get() = _binding!!
    
    private var networkOffBinding: NetworkOffStateBinding? = null
    private var networkOnBinding: NetworkOnStateBinding? = null
    
    private lateinit var networkDevicesAdapter: NetworkDevicesAdapter
    private lateinit var networkService: NetworkService
    
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
        
        setupNetworkService()
        setupToolbar()
        setupAdapters()
        setupClickListeners()
        updateNetworkState()
    }
    
    private fun setupNetworkService() {
        val mainActivity = activity as? MainActivity
        networkService = mainActivity?.getNetworkService() ?: NetworkService(requireContext())
        
        networkService.setOnDeviceDiscovered { device ->
            requireActivity().runOnUiThread {
                networkDevicesAdapter.addDevice(device)
            }
        }
        
        networkService.setOnConnectionStatusChanged { isConnected ->
            requireActivity().runOnUiThread {
                if (isConnected) {
                    Snackbar.make(binding.root, "Connected to network", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(binding.root, "Disconnected from network", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
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
        networkDevicesAdapter = NetworkDevicesAdapter { device ->
            // Handle device connection
            Snackbar.make(binding.root, "Connecting to ${device.deviceName}", Snackbar.LENGTH_SHORT).show()
            networkService.connectToDevice(device)
        }
        
        networkOnBinding?.networkDevicesRecyclerView?.adapter = networkDevicesAdapter
    }
    
    private fun setupClickListeners() {
        binding.scanButton.setOnClickListener {
            startNetworkDiscovery()
        }
    }
    
    private fun updateNetworkState() {
        val mainActivity = activity as? MainActivity
        if (mainActivity?.isNetworkAvailable() == true) {
            showNetworkOnState()
        } else {
            showNetworkOffState()
        }
    }
    
    private fun showNetworkOffState() {
        if (networkOffBinding == null) {
            networkOffBinding = NetworkOffStateBinding.inflate(layoutInflater, binding.networkStateContainer, true)
            networkOffBinding?.turnOnNetworkButton?.setOnClickListener {
                Snackbar.make(binding.root, "Please connect to WiFi network", Snackbar.LENGTH_LONG).show()
            }
        }
        
        networkOffBinding?.root?.visibility = View.VISIBLE
        networkOnBinding?.root?.visibility = View.GONE
        binding.scanButton.visibility = View.GONE
    }
    
    private fun showNetworkOnState() {
        if (networkOnBinding == null) {
            networkOnBinding = NetworkOnStateBinding.inflate(layoutInflater, binding.networkStateContainer, true)
            setupAdapters()
        }
        
        networkOffBinding?.root?.visibility = View.GONE
        networkOnBinding?.root?.visibility = View.VISIBLE
        binding.scanButton.visibility = View.VISIBLE
        
        // Show local IP address
        val mainActivity = activity as? MainActivity
        val localIp = mainActivity?.getLocalIpAddress() ?: "Unknown"
        networkOnBinding?.localIpText?.text = "Your IP: $localIp (Port: 8888)"
        
        // Start server to accept connections
        networkService.startServer()
        
        // Add debug info
        Snackbar.make(binding.root, "Server started on $localIp:8888", Snackbar.LENGTH_LONG).show()
    }
    
    private fun startNetworkDiscovery() {
        binding.scanButton.text = getString(R.string.scanning)
        binding.scanButton.isEnabled = false
        
        networkDevicesAdapter.clearDevices()
        networkService.discoverDevices()
        
        // Re-enable scan button after a delay
        binding.root.postDelayed({
            binding.scanButton.text = getString(R.string.scan_for_new_devices)
            binding.scanButton.isEnabled = true
        }, 10000) // 10 seconds timeout
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        networkOffBinding = null
        networkOnBinding = null
    }
} 