package com.example.chatconnect.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatconnect.databinding.ActivityLoginBinding
import com.example.chatconnect.ui.chat.UserListActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val uid = binding.etUniqueId.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (uid.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: Add Supabase login authentication here
            Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, UserListActivity::class.java))
            finish()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}