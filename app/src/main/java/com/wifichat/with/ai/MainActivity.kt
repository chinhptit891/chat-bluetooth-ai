package com.wifichat.with.ai

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.wifichat.with.ai.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Snackbar.make(binding.root, "All permissions granted", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(binding.root, "Some permissions are required for network communication", Snackbar.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        requestPermissions()
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.serverButton.setOnClickListener {
            if (isNetworkAvailable()) {
                startActivity(Intent(this, ServerActivity::class.java))
            } else {
                Snackbar.make(binding.root, "Please connect to WiFi first", Snackbar.LENGTH_LONG).show()
            }
        }

        binding.clientButton.setOnClickListener {
            if (isNetworkAvailable()) {
                startActivity(Intent(this, ClientActivity::class.java))
            } else {
                Snackbar.make(binding.root, "Please connect to WiFi first", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        )
        
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
               activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}