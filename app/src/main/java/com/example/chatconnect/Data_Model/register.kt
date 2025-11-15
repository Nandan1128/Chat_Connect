package com.example.chatconnect.Data_Model

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.chatconnect.R
import com.example.chatconnect.login
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class register : AppCompatActivity() {

    private lateinit var mAuth : FirebaseAuth
    private lateinit var et_name : EditText
    private lateinit var etphone : EditText
    private lateinit var etemailID : EditText
    private lateinit var edpassword : EditText
    private lateinit var edconfirmpassword : EditText
    private lateinit var btn_register : Button
    private lateinit var tv_login : TextView

    private lateinit var mDbRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()
        et_name = findViewById(R.id.et_name)
        etphone = findViewById(R.id.et_phone)
        etemailID = findViewById(R.id.et_email)
        edpassword = findViewById(R.id.et_password)
        btn_register = findViewById(R.id.btn_register)
        tv_login = findViewById(R.id.tv_login)
        edconfirmpassword = findViewById(R.id.et_confirm_password)

        tv_login.setOnClickListener {
            val intent = Intent(this, login::class.java)
            startActivity(intent)
        }



        btn_register.setOnClickListener {

            val name = et_name.text.toString()
            val phone = etphone.text.toString()
            val email = etemailID.text.toString()
            val password = edpassword.text.toString()
            val conformpassword = edconfirmpassword.text.toString()

            if(password != conformpassword){
                Toast.makeText(this@register, "Password does not match", Toast.LENGTH_SHORT).show()
            }
            else{
                register(name,email, password,phone)
            }


        }
    }


    private fun register(name: String, email: String, password: String,phone: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val user = mAuth.currentUser
                    // 🔹 Send verification email
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { verifyTask ->
                            if (verifyTask.isSuccessful) {

                                // 🔹 Add user to your database (optional)
                                addUserToDatabase(name, email,user.uid, phone)

                                Toast.makeText(
                                    this@register,
                                    "Verification email sent to ${user.email}. Please verify before login.",
                                    Toast.LENGTH_LONG
                                ).show()

                                // 🔹 Sign out the user until verification
                                mAuth.signOut()

                                // Go back to login screen
                                val intent = Intent(this@register, login::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()

                            } else {
                                Toast.makeText(
                                    this@register,
                                    "Failed to send verification email: ${verifyTask.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                } else {
                    Toast.makeText(
                        this@register,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun addUserToDatabase(name: String, email: String, uid: String?,phone: String?) {
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mDbRef.child("user").child(uid!!).setValue(User(name, email, uid, phone))
    }
}