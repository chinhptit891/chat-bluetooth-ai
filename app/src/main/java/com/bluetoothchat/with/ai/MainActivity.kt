package com.bluetoothchat.with.ai

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bluetoothchat.with.ai.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter
    
    private val bluetoothEnableLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Snackbar.make(binding.root, "Bluetooth enabled", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(binding.root, "Bluetooth is required for this app", Snackbar.LENGTH_LONG).show()
        }
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Snackbar.make(binding.root, "All permissions granted", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(binding.root, "Some permissions are required", Snackbar.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupBluetooth()
        requestPermissions()
        setupNavigation()
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
    }
    
    private fun setupBluetooth() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }
    
    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }
    
    fun enableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothEnableLauncher.launch(enableBtIntent)
        }
    }
    
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter.isEnabled
    }
    
    fun getBluetoothAdapter(): BluetoothAdapter {
        return bluetoothAdapter
    }
}