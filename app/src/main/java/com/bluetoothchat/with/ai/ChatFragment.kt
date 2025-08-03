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
        
        setupToolbar()
        setupChatAdapter()
        setupClickListeners()
        setupMessageInput()
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
        
        // Add some sample messages for demonstration
        chatAdapter.addMessage(ChatMessage("Hello!", false))
        chatAdapter.addMessage(ChatMessage("Hi there! How are you?", true))
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
                binding.sendButton.isEnabled = !s.isNullOrBlank()
            }
        })
    }
    
    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }
    
    private fun sendMessage() {
        val messageText = binding.messageInputEditText.text.toString().trim()
        if (messageText.isNotEmpty()) {
            chatAdapter.addMessage(ChatMessage(messageText, true))
            binding.messageInputEditText.text?.clear()
            
            // Simulate received message after a delay
            binding.root.postDelayed({
                chatAdapter.addMessage(ChatMessage("Message received!", false))
            }, 1000)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class ChatMessage(
    val text: String,
    val isSent: Boolean
) 