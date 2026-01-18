package com.example.chatconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import com.example.chatconnect.Data_Model.register

import com.google.firebase.auth.FirebaseAuth
import kotlin.jvm.java

class login : AppCompatActivity() {

    private lateinit var mAuth : FirebaseAuth
    private lateinit var edemailID : EditText
    private lateinit var edpassword : EditText
    private lateinit var btn_login : Button
    private lateinit var tv_register : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()
        mAuth = FirebaseAuth.getInstance()
        edemailID = findViewById(R.id.et_emailId)
        edpassword = findViewById(R.id.et_password)
        btn_login = findViewById(R.id.btn_login)
        tv_register = findViewById(R.id.tv_register)

        tv_register.setOnClickListener {
            val intent = Intent(this, register::class.java)
            startActivity(intent)
        }

        btn_login.setOnClickListener {
            val email = edemailID.text.toString()
            val password = edpassword.text.toString()

            login(email, password)
        }

    }
    private fun login(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    if (user != null && user.isEmailVerified) {
                        val intent = Intent(this@login, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_SHORT).show()
                        mAuth.signOut()
                    }
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


}