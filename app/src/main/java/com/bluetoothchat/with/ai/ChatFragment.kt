package com.bluetoothchat.with.ai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bluetoothchat.with.ai.databinding.FragmentChatBinding
import com.google.android.material.snackbar.Snackbar

class ChatFragment : Fragment() {
    
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var networkService: NetworkService
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Handle image selection
            Snackbar.make(binding.root, "Image selected: ${uri.lastPathSegment}", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupNetworkService()
        setupToolbar()
        setupChatAdapter()
        setupClickListeners()
        setupMessageInput()
        updateConnectionStatus()
    }
    
    private fun setupNetworkService() {
        val mainActivity = activity as? MainActivity
        networkService = mainActivity?.getNetworkService() ?: NetworkService(requireContext())
        
        networkService.setOnMessageReceived { message, senderIp ->
            requireActivity().runOnUiThread {
                val senderName = if (senderIp == "SERVER") "Server" else "Device $senderIp"
                chatAdapter.addMessage(ChatMessage(message, false, senderName))
            }
        }
        
        networkService.setOnConnectionStatusChanged { isConnected ->
            requireActivity().runOnUiThread {
                updateConnectionStatus()
                if (isConnected) {
                    Snackbar.make(binding.root, "Connected to chat network", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(binding.root, "Disconnected from chat network", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            // Navigate back to connections
            Snackbar.make(binding.root, "Back to connections", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    private fun setupChatAdapter() {
        chatAdapter = ChatAdapter()
        binding.chatRecyclerView.adapter = chatAdapter
        
        // Add welcome message
        chatAdapter.addMessage(ChatMessage("Welcome to LAN Chat! Connect to a device to start chatting.", false, "System"))
    }
    
    private fun setupClickListeners() {
        binding.attachButton.setOnClickListener {
            openImagePicker()
        }
        
        binding.sendButton.setOnClickListener {
            sendMessage()
        }
    }
    
    private fun setupMessageInput() {
        binding.messageInputEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                binding.sendButton.isEnabled = !s.isNullOrBlank() && networkService.isConnected()
            }
        })
    }
    
    private fun updateConnectionStatus() {
        val isConnected = networkService.isConnected()
        binding.sendButton.isEnabled = isConnected && !binding.messageInputEditText.text.isNullOrBlank()
        
        if (isConnected) {
            binding.connectionStatus.text = "Connected"
            binding.connectionStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
        } else {
            binding.connectionStatus.text = "Disconnected"
            binding.connectionStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
        }
    }
    
    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }
    
    private fun sendMessage() {
        val messageText = binding.messageInputEditText.text.toString().trim()
        if (messageText.isNotEmpty() && networkService.isConnected()) {
            // Add message to local chat
            chatAdapter.addMessage(ChatMessage(messageText, true, "You"))
            
            // Send message through network
            networkService.sendMessage(messageText)
            
            // Clear input
            binding.messageInputEditText.text?.clear()
        } else if (!networkService.isConnected()) {
            Snackbar.make(binding.root, "Not connected to any device", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
