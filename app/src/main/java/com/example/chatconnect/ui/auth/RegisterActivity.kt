package com.example.chatconnect.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.chatconnect.R
import com.example.chatconnect.data.model.User
import com.example.chatconnect.databinding.ActivityRegisterBinding
import com.example.chatconnect.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val uid = binding.etUid.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val pseudonym = binding.etPseudonym.text.toString().trim()

            if (uid.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = User(uid = uid, password_hash = password, pseudonym = pseudonym)
            viewModel.register(user)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.isRegistered.collectLatest {
                if (it) Toast.makeText(this@RegisterActivity, "Registered!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}